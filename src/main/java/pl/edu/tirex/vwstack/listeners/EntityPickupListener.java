package pl.edu.tirex.vwstack.listeners;

import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import pl.edu.tirex.vwstack.engine.StackItemEngine;

public class EntityPickupListener implements Listener
{
    private final StackItemEngine engine;

    public EntityPickupListener(StackItemEngine engine)
    {
        this.engine = engine;
    }

    @EventHandler
    public void onPickupItem(EntityPickupItemEvent event)
    {
        Item item = event.getItem();
        if (this.engine.disallow(item))
        {
            return;
        }
        EntityEquipment equipment = event.getEntity().getEquipment();
        if (equipment instanceof Inventory)
        {
            event.setCancelled(this.engine.pickup(item, (Inventory) equipment));
        }
    }
}
