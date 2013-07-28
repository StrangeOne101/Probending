package com.etriacraft.probending;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import tools.BendingType;
import tools.Tools;

public class PlayerListener implements Listener {

	Probending plugin;

	public PlayerListener(Probending plugin) {
		this.plugin = plugin;
	}

	public static String RemovedFromTeamBecauseDifferentElement;
	public static String SetTeamColor;
	
	// Match Stuff
	public static String CantEnterField;

	@EventHandler
	public static void onPlayerMove(PlayerMoveEvent e) {
		Player player = e.getPlayer();
		Location locTo = e.getTo();
		Location locFrom = e.getFrom();
		if (Methods.WGSupportEnabled) {
			if (Methods.getWorldGuard() != null) {
				ApplicableRegionSet set = WGBukkit.getRegionManager(locTo.getWorld()).getApplicableRegions(locTo);
				for (ProtectedRegion region: set) {
					if (region != null) {
						if (region.getId().equalsIgnoreCase(Methods.ProbendingField)) {
							if (Methods.matchStarted) {
								String teamName = Methods.getPlayerTeam(player.getName());
								if (teamName != null) {
									if (!Methods.playingTeams.contains(teamName.toLowerCase())) {
										player.sendMessage(Commands.Prefix + CantEnterField);
										player.teleport(locFrom);
										e.setCancelled(true);
									}
								}
								if (teamName == null) {
									player.sendMessage(Commands.Prefix + CantEnterField);
									player.teleport(locFrom);
									e.setCancelled(true);
								}
							}
						}
					}
				}
			}
		}
	}
	@EventHandler
	public static void onPlayerChat(AsyncPlayerChatEvent e) {
		if (Commands.pbChat.contains(e.getPlayer())) {
			e.getRecipients().clear();
			for (Player player: Bukkit.getOnlinePlayers()) {
				if (Commands.pbChat.contains(player)) {
					e.getRecipients().add(player);
				}
			}
			e.setFormat(Commands.Prefix + e.getFormat());
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		Player player = e.getPlayer();
		Block block = e.getClickedBlock();

		if (e.getAction() != Action.LEFT_CLICK_BLOCK && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

		if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (block.getState() instanceof Sign) {
				Sign s = (Sign) block.getState();

				String line1 = s.getLine(0);
				String teamColor = s.getLine(1);

				if (line1.equalsIgnoreCase("[probending]")) {
					if (!player.hasPermission("probending.team.sign.use")) {
						player.sendMessage(Commands.Prefix + Commands.noPermission);
						return;
					}

					if (!SignListener.colors.contains(teamColor)) {
						player.sendMessage(Commands.Prefix + SignListener.InvalidSign);
						return;
					}

					ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
					ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
					ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
					ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);

					player.getInventory().setHelmet(Methods.createColorArmor(helmet, Methods.getColorFromString(teamColor)));
					player.getInventory().setChestplate(Methods.createColorArmor(chestplate, Methods.getColorFromString(teamColor)));
					player.getInventory().setLeggings(Methods.createColorArmor(leggings, Methods.getColorFromString(teamColor)));
					player.getInventory().setBoots(Methods.createColorArmor(boots, Methods.getColorFromString(teamColor)));
					e.setUseItemInHand(Result.DENY);
					e.setUseInteractedBlock(Result.DENY);
					player.updateInventory();
					player.sendMessage(Commands.Prefix + SetTeamColor.replace("%color", teamColor));
					return;
				}

			}
		}
	}
	@EventHandler
	public static void onPlayerJoin(PlayerJoinEvent e) {
		Player player = e.getPlayer();
		if (!(Tools.getBendingTypes(player).size() > 1)) {
			String team = Methods.getPlayerTeam(player.getName());
			if (team != null) {
				String playerElement = null;
				if (Tools.isBender(player.getName(), BendingType.Air)) {
					playerElement = "Air";
				}
				if (Tools.isBender(player.getName(), BendingType.Water)) {
					playerElement = "Water";
				}
				if (Tools.isBender(player.getName(), BendingType.Earth)) {
					playerElement = "Earth";
				}
				if (Tools.isBender(player.getName(), BendingType.Fire)) {
					playerElement = "Fire";
				}
				if (Tools.isBender(player.getName(), BendingType.ChiBlocker)) {
					playerElement = "Chi";
				}
				String playerElementInTeam = Methods.getPlayerElementInTeam(player.getName(), team);
				if (playerElementInTeam != null) {
					if (!playerElementInTeam.equals(playerElement)) {
						player.sendMessage(Commands.Prefix + RemovedFromTeamBecauseDifferentElement);
						Methods.removePlayerFromTeam(team, player.getName(), playerElementInTeam);
						Set<String> teamElements = Methods.getTeamElements(team);
						if (teamElements.contains("Air")) {
							String airbender = Methods.getTeamAirbender(team);
							Methods.setOwner(airbender, team);
							return;
						}
						if (teamElements.contains("Water")) {
							String bender = Methods.getTeamWaterbender(team);
							Methods.setOwner(bender, team);
							return;
						}
						if (teamElements.contains("Earth")) {
							String bender = Methods.getTeamEarthbender(team);
							Methods.setOwner(bender, team);
							return;
						}
						if (teamElements.contains("Fire")) {
							String bender = Methods.getTeamFirebender(team);
							Methods.setOwner(bender, team);
							return;
						}
						if (teamElements.contains("Chi")) {
							String bender = Methods.getTeamChiblocker(team);
							Methods.setOwner(bender, team);
							return;
						} else {
							Methods.deleteTeam(team);
						}
					}
				}
			}
		}
	}
}