package me.axilirate.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerGhostEvent extends PlayerEvent implements Cancellable {

    private boolean cancelled;

    private static final HandlerList HANDLER_LIST = new HandlerList();

    public PlayerGhostEvent(Player player) {
        super(player);
        this.cancelled = false;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
