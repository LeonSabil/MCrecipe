package me.icodetits.customCrates.commands;

import org.bukkit.command.CommandSender;

import me.icodetits.customCrates.commands.manager.CrateCommand;
import me.icodetits.customCrates.commands.manager.CrateCommandInfo;
import me.icodetits.customCrates.commands.manager.Message;
import me.icodetits.customCrates.data.DropperManager;
import me.icodetits.customCrates.data.ParkourManager;

@CrateCommandInfo(aliases = { "removeloc" }, description = "Remove location for crate types.", permission = "removelocation", usage = "(dropper|parkour) (name)")
public class RemoveLocation extends CrateCommand {

	@Override
	public void onCommand(CommandSender p, String[] args) {
		if (args.length == 0 || args.length == 1) {
			Message.sendMessage(p, "INVALID-USAGE");
			return;
		}
		
		if (args[0].equalsIgnoreCase("dropper")) {
			String name = args[1];
			if (DropperManager.getInstance().getByName(name) == null) {			
				Message.sendMessage(p, Message.generate("NOT-FOUND-DROPPER").replace("%name%", name));
				return;
			}

			DropperManager.getInstance().removeDropper(name);
			Message.sendMessage(p, Message.generate("REMOVED-DROPPER").replace("%name%", name));
		} else if (args[0].equalsIgnoreCase("parkour")) {
			String name = args[1];
			if (ParkourManager.getInstance().getByName(name) == null) {			
				Message.sendMessage(p, Message.generate("NOT-FOUND-DROPPER").replace("Dropper", "Parkour Map").replace("dropper", "parkour map").replace("%name%", name));
				return;
			}

			ParkourManager.getInstance().removeParkourMap(name);
			Message.sendMessage(p, Message.generate("REMOVED-DROPPER").replace("Dropper", "Parkour Map").replace("dropper", "parkour map").replace("%name%", name));	
		}
	}
}
