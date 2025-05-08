package me.icodetits.customCrates.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CratesCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("crates")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("§c§l[!] §cYou must be in-game to execute this command...");
				return true;
			}
			
			Player p = (Player) sender;
			me.icodetits.customCrates.listeners.KeyListener.openKeyMenu(p, p.getLocation());
		}
		return false;
	}
}
