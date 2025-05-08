package me.icodetits.customCrates.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.icodetits.customCrates.commands.manager.CrateCommand;
import me.icodetits.customCrates.commands.manager.CrateCommandInfo;
import me.icodetits.customCrates.commands.manager.Message;
import me.icodetits.customCrates.configutils.ConfigUtils;
import me.icodetits.customCrates.data.PlayerData;
import me.icodetits.customCrates.data.PlayerManager;

@CrateCommandInfo(aliases = { "giveall", "ga" }, description = "Give everyone online a key.", permission = "giveall", usage = "(crateName) (amount)")
public class GiveKeyAllCommand extends CrateCommand {

	@Override
	public void onCommand(CommandSender p, String[] args) {
		if (args.length == 0 || args.length == 1) {
			Message.sendMessage(p, "INVALID-USAGE");
			return;
		}
		
		String name = args[0];
		if (name.equalsIgnoreCase("NewStorm") || name.equalsIgnoreCase("Storm")) {
			name = "Air";
		} else if (name.equalsIgnoreCase("Cloud")) {
			name = "Earth";
		} else if (name.equalsIgnoreCase("Sunset")) {
			name = "Earth";
		}
		
		if (!(ConfigUtils.getInstance().getCrates().contains(name))) {
			Message.sendMessage(p, "NOT-A-CRATE");
			return;
		}

		int amount = 0;
		try {
			amount = Integer.valueOf(args[1]);
		} catch (NumberFormatException ignore) {
			Message.sendMessage(p, "INVALID-AMOUNT");
			return;
		}

		for (Player online : Bukkit.getOnlinePlayers()) {
			PlayerData data = PlayerManager.getInstance().getByPlayer(online);
			if (data == null) {
				continue;
			}
			
			data.giveKeys(name, amount);
			Message.sendMessage(online, Message.generate("SUCCESS-KEY").replace("%amount%", Integer.toString(amount)).replace("%crate%", name));
		}
	}
}
