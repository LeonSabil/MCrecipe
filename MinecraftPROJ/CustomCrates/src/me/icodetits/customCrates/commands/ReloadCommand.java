package me.icodetits.customCrates.commands;

import java.text.DecimalFormat;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import me.icodetits.customCrates.Main;
import me.icodetits.customCrates.commands.manager.CrateCommand;
import me.icodetits.customCrates.commands.manager.CrateCommandInfo;
import me.icodetits.customCrates.commands.manager.Message;
import me.icodetits.customCrates.configutils.ConfigUtils;
import me.icodetits.customCrates.crateplayer.CPManager;

@CrateCommandInfo(aliases = { "reload", "rl" }, description = "Reload the config.", permission = "reload", usage = "")
public class ReloadCommand extends CrateCommand {

	@Override
	public void onCommand(final CommandSender p, String[] args) {
		long l1 = System.currentTimeMillis();
		Message.sendMessage(p, "RELOADING");
		Bukkit.getScheduler().cancelTasks(Main.getInstance());
		Main.getInstance().reloadConfig();
		Main.getInstance().saveConfig();
		Main.getInstance().reloadMessages();
		ConfigUtils.getInstance().setupParticles();
		CPManager.clearCratePlayers();

		long l2 = System.currentTimeMillis() - l1;
		DecimalFormat formatter = new DecimalFormat("###.#");
		Message.sendMessage(p, Message.generate("RELOAD-COMPLETE").replace("%ms%", formatter.format(l2)));
		return;
	}
}
