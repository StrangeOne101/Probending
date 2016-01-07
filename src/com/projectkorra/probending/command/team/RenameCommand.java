package com.projectkorra.probending.command.team;

import com.projectkorra.probending.PBMethods;
import com.projectkorra.probending.Probending;
import com.projectkorra.probending.command.Commands;
import com.projectkorra.probending.command.PBCommand;
import com.projectkorra.probending.objects.Team;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class RenameCommand extends PBCommand {
	
	public RenameCommand() {
		super ("team-rename", "/pb team rename <Name>", "Rename your team.", new String[] {"rename", "name"}, true, Commands.teamaliases);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void execute(CommandSender sender, List<String> args) {
		if (!isPlayer(sender) || !hasTeamPermission(sender) || !correctLength(sender, args.size(), 2, 2)) {
			return;
		}
		
		UUID uuid = ((Player) sender).getUniqueId();

		String teamName = PBMethods.getPlayerTeam(uuid);
		Team team = PBMethods.getTeam(teamName);
		if (!PBMethods.playerInTeam(uuid)) {
			sender.sendMessage(PBMethods.Prefix + PBMethods.PlayerNotInTeam);
			return;
		}
		if (!PBMethods.isPlayerOwner(uuid, teamName)) {
			sender.sendMessage(PBMethods.Prefix + PBMethods.NotOwnerOfTeam);
			return;
		}
		boolean econEnabled = Probending.plugin.getConfig().getBoolean("Economy.Enabled");

		String newName = args.get(1);
		if (newName.length() > 15) {
			sender.sendMessage(PBMethods.Prefix + PBMethods.NameTooLong);
			return;
		}
		if (newName.equalsIgnoreCase(teamName)) {
			sender.sendMessage(PBMethods.Prefix + PBMethods.TeamAlreadyNamedThat.replace("%newname", teamName));
			return;
		}
		if (econEnabled) {
			Double playerBalance = Probending.econ.getBalance((Player) sender);
			Double renameFee = Probending.plugin.getConfig().getDouble("Economy.TeamRenameFee");
			String serverAccount = Probending.plugin.getConfig().getString("Economy.ServerAccount");
			String currency = Probending.econ.currencyNamePlural();
			if (playerBalance < renameFee) {
				sender.sendMessage(PBMethods.Prefix + PBMethods.NotEnoughMoney.replace("%amount", renameFee.toString()).replace("%currency", currency));
				return;
			}
			Probending.econ.withdrawPlayer((Player) sender, renameFee);
			Probending.econ.depositPlayer(serverAccount, renameFee);
			sender.sendMessage(PBMethods.Prefix + PBMethods.MoneyWithdrawn.replace("%amount", renameFee.toString()).replace("%currency", currency));
		}

		int Wins = team.getWins();
		int Losses = team.getLosses();

		PBMethods.createTeam(newName, uuid);

		OfflinePlayer airbender = null;
		OfflinePlayer waterbender = null;
		OfflinePlayer earthbender = null;
		OfflinePlayer firebender = null;
		OfflinePlayer chiblocker = null;

		if (PBMethods.getAirbender(team) != null) {
			airbender = PBMethods.getAirbender(team);
		}

		if (PBMethods.getWaterbender(team) != null) {
			waterbender = PBMethods.getWaterbender(team);
		}

		if (PBMethods.getEarthbender(team) != null) {
			earthbender = PBMethods.getEarthbender(team);
		}

		if (PBMethods.getFirebender(team) != null) {
			firebender = PBMethods.getFirebender(team);
		}

		if (PBMethods.getChiblocker(team) != null) {
			chiblocker = PBMethods.getChiblocker(team);
		}

		if (airbender != null) {
			PBMethods.removePlayerFromTeam(team, airbender.getUniqueId(), "Air");
			PBMethods.addPlayerToTeam(newName, airbender.getUniqueId(), "Air");
		}
		if (waterbender != null) {
			PBMethods.removePlayerFromTeam(team, waterbender.getUniqueId(), "Water");
			PBMethods.addPlayerToTeam(newName, waterbender.getUniqueId(), "Water");
		}
		if (earthbender != null) {
			PBMethods.removePlayerFromTeam(team, earthbender.getUniqueId(), "Earth");
			PBMethods.addPlayerToTeam(newName, earthbender.getUniqueId(), "Earth");
		}
		if (firebender != null) {
			PBMethods.removePlayerFromTeam(team, firebender.getUniqueId(), "Fire");
			PBMethods.addPlayerToTeam(newName, firebender.getUniqueId(), "Fire");
		}
		if (chiblocker != null) {
			PBMethods.removePlayerFromTeam(team, chiblocker.getUniqueId(), "Chi");
			PBMethods.addPlayerToTeam(newName, chiblocker.getUniqueId(), "Chi");
		}
		
		team.setLosses(Losses);
		team.setWins(Wins);

		sender.sendMessage(PBMethods.Prefix + PBMethods.TeamRenamed.replace("%newname", newName));
		PBMethods.setOwner(uuid, newName);
		PBMethods.deleteTeam(teamName);
		Probending.plugin.saveConfig();
		return;
	}
}