package me.icodetits.customCrates.commands;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.icodetits.customCrates.commands.manager.CrateCommand;
import me.icodetits.customCrates.commands.manager.CrateCommandInfo;
import me.icodetits.customCrates.commands.manager.Message;
import me.icodetits.customCrates.data.DropperData;
import me.icodetits.customCrates.data.DropperManager;
import me.icodetits.customCrates.data.ParkourData;
import me.icodetits.customCrates.data.ParkourManager;

@CrateCommandInfo(aliases = { "addloc" }, description = "Add location for a crate types.", permission = "addlocation", usage = "(dropper|parkour) (name)")
public class AddLocation extends CrateCommand {

	@Override
	public void onCommand(CommandSender p, String[] args) {
		if (args.length == 0 || args.length == 1) {
			Message.sendMessage(p, "INVALID-USAGE");
			return;
		}
		
		if (args[0].equalsIgnoreCase("dropper")) {
			String name = args[1];
			if (DropperManager.getInstance().getByName(name) != null) {
				DropperData data = DropperManager.getInstance().getByName(name);
				
				Message.sendMessage(p, Message.generate("ALREADY-EXISTS-DROPPER").replace("%name%", data.getName()));
				return;
			}

			Player pl = (Player) p;
			Location location = pl.getLocation();
			
			DropperManager.getInstance().registerDropper(name, location);
			Message.sendMessage(pl, Message.generate("ADDED-DROPPER").replace("%name%", name));
		} else if (args[0].equalsIgnoreCase("parkour")) {
			String name = args[1];
			if (ParkourManager.getInstance().getByName(name) != null) {
				ParkourData data = ParkourManager.getInstance().getByName(name);
				
				Message.sendMessage(p, Message.generate("ALREADY-EXISTS-DROPPER").replace("Dropper", "Parkour Map").replace("dropper", "parkour map").replace("%name%", data.getName()));
				return;
			}

			Player pl = (Player) p;
			Location location = pl.getLocation();
			
			ParkourManager.getInstance().registerParkourMap(name, location);
			Message.sendMessage(pl, Message.generate("ADDED-DROPPER").replace("Dropper", "Parkour Map").replace("dropper", "parkour map").replace("%name%", name));
		}
	}
}
