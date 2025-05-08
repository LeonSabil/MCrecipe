package me.icodetits.customCrates.commands.manager;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.google.common.collect.Lists;

import me.icodetits.customCrates.Main;
import me.icodetits.customCrates.commands.AddLocation;
import me.icodetits.customCrates.commands.GiveCrateCommand;
import me.icodetits.customCrates.commands.GiveKeyAllCommand;
import me.icodetits.customCrates.commands.GiveKeyCommand;
import me.icodetits.customCrates.commands.ReloadCommand;
import me.icodetits.customCrates.commands.RemoveLocation;

public class CrateCommandManager implements CommandExecutor {

	private List<CrateCommand> cmds;

	public CrateCommandManager() {
		this.cmds = Lists.newArrayList();
		this.cmds.add(new GiveCrateCommand());
		this.cmds.add(new GiveKeyCommand());
		this.cmds.add(new GiveKeyAllCommand());
		this.cmds.add(new ReloadCommand());
		this.cmds.add(new AddLocation());
		this.cmds.add(new RemoveLocation());
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		CommandSender p = sender;

		if (cmd.getName().equalsIgnoreCase("crate")) {
			if (args.length == 0) {
				int index = 0;
				
				for (CrateCommand cm : this.cmds) {
					CrateCommandInfo info = cm.getClass().getAnnotation(CrateCommandInfo.class);
					if (!(p.hasPermission("crates." + info.permission()))) {
						continue;
					}
					
					index++;
					p.sendMessage(Main.getInstance().getPrefix() + "/" + label + (info.aliases().length > 0 ? " " + StringUtils.join(info.aliases(), " | ") : "") + (info.usage().isEmpty() ? "" : " " + info.usage()) + " - " + info.description());
					
					if (index != this.cmds.size()) {
						p.sendMessage(" ");
					}
				}
				
				if (index == 0) {
					Message.sendMessage(p, Message.generate("NO-PERMISSION"));
					return true;
				}
				return true;
			}

			CrateCommand command = null;

			for (CrateCommand cm : this.cmds) {
				CrateCommandInfo info = cm.getClass().getAnnotation(CrateCommandInfo.class);
				for (String aliases : info.aliases()) {
					if (aliases.equals(args[0])) {
						command = cm;
						break;
					}
				}
			}

			if (command == null) {
				Message.sendMessage(p, "INVALID-CMD");
				return true;
			}

			if (!p.hasPermission("crates." + command.getClass().getAnnotation(CrateCommandInfo.class).permission())) {
				Message.sendMessage(p, "NO-PERMISSION");
				return true;
			}

			Vector<String> a = new Vector<String>(Arrays.asList(args));
			a.remove(0);
			args = a.toArray(new String[a.size()]);

			command.onCommand(p, args);
		}
		return false;
	}

}
