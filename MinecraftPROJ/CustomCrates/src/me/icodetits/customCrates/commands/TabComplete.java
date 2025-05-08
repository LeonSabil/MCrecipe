package me.icodetits.customCrates.commands;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import com.google.common.collect.Lists;

import me.icodetits.customCrates.configutils.ConfigUtils;

public class TabComplete implements TabCompleter {

	private final List<String> commands;
	private final List<String> amounts;
	
	public TabComplete() {
		this.commands = Arrays.asList("giveall", "give", "givecrate", "reload");
		this.amounts = Arrays.asList("1", "8", "16", "32", "64");
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("crate")) {
			if (args.length == 1) {
				if (!(args[0].isEmpty())) {
					String search = args[0].toLowerCase();
					List<String> found = Lists.newArrayList();
					
					for (String comp : this.commands) {
						if (comp.toLowerCase().startsWith(search)) {
							found.add(comp);
						}
					}
					
					Collections.sort(found);
					return found;
				} else {
					List<String> found = Lists.newArrayList(this.commands);
					Collections.sort(found);
					return found;
				}
			} else if (args.length == 2) {
				if (args[0].equalsIgnoreCase("giveall") || args[0].equalsIgnoreCase("ga")) {
					List<String> crates = ConfigUtils.getInstance().getCrates();

					if (!(args[1].isEmpty())) {
						List<String> found = Lists.newArrayList();
						
						for (String crate : crates) {
							String search = args[1].toLowerCase();
							if (crate.toLowerCase().startsWith(search)) {
								found.add(crate);
							}
						}
						
						return found;
					} else {
						List<String> found = Lists.newArrayList();
						
						for (String crate : crates) {
							found.add(crate);
						}
						
						Collections.sort(found);
						return found;
					}
				}
			} else if (args.length == 3) {
				if (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("givekey") || args[0].equalsIgnoreCase("gk")) {
					List<String> crates = ConfigUtils.getInstance().getCrates();
					
					if (!(args[2].isEmpty())) {
						List<String> found = Lists.newArrayList();
						
						for (String crate : crates) {
							String search = args[2].toLowerCase();
							if (crate.toLowerCase().startsWith(search)) {
								found.add(crate);
							}
						}
						
						return found;
					} else {
						List<String> found = Lists.newArrayList();
						
						for (String crate : crates) {
							found.add(crate);
						}
						
						Collections.sort(found);
						return found;
					}
				}
				if (args[0].equalsIgnoreCase("giveall") || args[0].equalsIgnoreCase("ga")) {
					if (args[2].isEmpty()) {
						return this.amounts;
					}
				}
			} else if (args.length == 4) {
				if (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("givekey") || args[0].equalsIgnoreCase("gk")) {
					if (args[3].isEmpty()) {
						return this.amounts;
					}
				}
			}
		}
		return null;
	}
}
