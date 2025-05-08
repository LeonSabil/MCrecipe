package me.icodetits.customCrates.commands;

import me.icodetits.customCrates.commands.manager.CrateCommand;
import me.icodetits.customCrates.commands.manager.CrateCommandInfo;
import me.icodetits.customCrates.commands.manager.Message;
import me.icodetits.customCrates.configutils.ConfigUtils;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

@CrateCommandInfo(aliases = { "givecrate", "gc" }, description = "Give player a crate.", permission = "givecrate", usage = "(playerName)")
public class GiveCrateCommand extends CrateCommand {

	@Override
	public void onCommand(CommandSender p, String[] args) {
		if (args.length == 0) {
			Message.sendMessage(p, "INVALID-USAGE");
			return;
		}

		ItemStack casse = ConfigUtils.getInstance().getBlockType();
		ItemMeta meta = casse.getItemMeta();

		meta.setDisplayName("§bCrate §7(Place Down to Register)");
		casse.setItemMeta(meta);

		Player target = Bukkit.getPlayer(args[0]);
		if (target == null) {
			Message.sendMessage(p, "INVALID-PLAYER");
			return;
		}
		target.getInventory().addItem(casse);
		Message.sendMessage(target, "SUCCESS-CRATE");
	}
}
