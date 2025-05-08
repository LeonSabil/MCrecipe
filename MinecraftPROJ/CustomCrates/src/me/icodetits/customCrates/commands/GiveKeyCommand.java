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

@CrateCommandInfo(aliases = { "give", "givekey", "gk" }, description = "Give player a key.", permission = "give", usage = "(playerName) (crateName) (amount)")
public class GiveKeyCommand extends CrateCommand {

	@Override
	public void onCommand(final CommandSender p, String[] args) {
		if (args.length == 0 || args.length == 1 || args.length == 2) {
			Message.sendMessage(p, "INVALID-USAGE");
			return;
		}
		
		Player target = Bukkit.getPlayer(args[0]);
		if (target == null) {
			Message.sendMessage(p, "INVALID-PLAYER");
			return;
		}

		String name = args[1];
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
			amount = Integer.valueOf(args[2]);
		} catch (NumberFormatException ignore) {
			Message.sendMessage(p, "INVALID-AMOUNT");
			return;
		}
		
		PlayerData data = PlayerManager.getInstance().getByPlayer(target);
		if (data == null) {
			Message.sendMessage(p, "INVALID-PLAYER");
			return;
		}
		
		data.giveKeys(name, amount);
		
		Message.sendMessage(target, Message.generate("SUCCESS-KEY").replace("%amount%", Integer.toString(amount)).replace("%crate%", name));
		
		if (p != target) {
			Message.sendMessage(p, Message.generate("SUCCESS-KEY-OTHER").replace("%player%", target.getName()).replace("%amount%", Integer.toString(amount)).replace("%crate%", name));
		}
	}
}
