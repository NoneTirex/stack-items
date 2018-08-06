package pl.edu.tirex.vwstack.listeners;

import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import pl.edu.tirex.vwstack.engine.StackItemEngine;

public class PlayerPickupListener
        implements Listener
{
    private final StackItemEngine engine;

    public PlayerPickupListener(StackItemEngine engine)
    {
        this.engine = engine;
    }

    @EventHandler
    @SuppressWarnings("deprecation")
    public void onPickupItem(PlayerPickupItemEvent event)
    {
        Item item = event.getItem();
        if (this.engine.disallow(item))
        {
            return;
        }
        event.setCancelled(this.engine.pickup(item, event.getPlayer().getInventory()));
    }
}
