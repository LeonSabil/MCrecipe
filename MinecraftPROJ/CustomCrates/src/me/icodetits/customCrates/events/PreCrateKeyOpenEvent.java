package me.icodetits.customCrates.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import lombok.Getter;
import lombok.Setter;
import me.icodetits.customCrates.task.TaskType;

public class PreCrateKeyOpenEvent extends PlayerEvent implements Cancellable {
	private static final HandlerList handlers = new HandlerList();
	
	@Getter @Setter private String key;
	@Getter private final TaskType taskType;
	@Getter @Setter private boolean cancelled;

	public PreCrateKeyOpenEvent(Player who, String key, TaskType taskType) {
		super(who);
		this.key = key;
		this.taskType = taskType;
		this.cancelled = false;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
