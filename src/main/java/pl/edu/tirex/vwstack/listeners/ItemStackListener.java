package pl.edu.tirex.vwstack.listeners;

import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import pl.edu.tirex.vwstack.engine.StackItemEngine;

import java.util.Collection;

public class ItemStackListener
        implements Listener
{
    private final StackItemEngine engine;

    public ItemStackListener(StackItemEngine engine)
    {
        this.engine = engine;
    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event)
    {
        Item item = event.getEntity();
        ItemStack itemStack = item.getItemStack();
        World world = item.getWorld();

        if (this.engine.disallow(item))
        {
            return;
        }

        Collection<Entity> nearbyEntities = world.getNearbyEntities(item.getLocation(), 5, 5, 5);
        for (Entity entity : nearbyEntities)
        {
            if (!(entity instanceof Item))
            {
                continue;
            }
            Item itemOnGround = (Item) entity;
            if (this.engine.disallow(itemOnGround))
            {
                continue;
            }
            ItemStack itemStackOnGround = itemOnGround.getItemStack();
            if (!itemStack.isSimilar(itemOnGround.getItemStack()))
            {
                continue;
            }
            int newAmount = this.engine.getAmountFromItem(item);
            if (newAmount <= 0)
            {
                newAmount = itemStack.getAmount();
            }
            int addAmount = this.engine.getAmountFromItem(itemOnGround);
            if (addAmount <= 0)
            {
                addAmount = itemStackOnGround.getAmount();
            }
            newAmount += addAmount;
            itemStackOnGround.setAmount(newAmount);
            this.engine.updateItem(itemOnGround);
            event.setCancelled(true);
            return;
        }
        this.engine.updateItem(item);
    }

    @EventHandler
    public void onItemDespawn(ItemDespawnEvent event)
    {
        Item item = event.getEntity();
        if (this.engine.disallow(item))
        {
            return;
        }
        if (item.getPickupDelay() == 32767)
        {
            return;
        }
        if (item.getTicksLived() < this.engine.getTicksLived(item))
        {
            item.setTicksLived(3);
            this.engine.setTicksLived(item);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onMergeItem(ItemMergeEvent event)
    {
        Item item = event.getEntity();
        Item itemTarget = event.getTarget();
        if (this.engine.disallow(item) || this.engine.disallow(itemTarget))
        {
            return;
        }
        event.setCancelled(true);
        if (item.getFallDistance() != 0 || itemTarget.getFallDistance() != 0)
        {
            return;
        }
        int itemTicks = this.engine.getTicksLived(item);
        int itemTargetTicks = this.engine.getTicksLived(itemTarget);
        boolean reverse = false;
        if (itemTicks < itemTargetTicks)
        {
            reverse = true;
        }
        else if (itemTicks == itemTargetTicks && ((item.getTicksLived() < itemTarget.getTicksLived()) || (itemTarget.isOnGround() && !item.isOnGround())))
        {
            reverse = true;
        }
        if (reverse)
        {
            reverse = itemTarget.isOnGround() || !item.isOnGround();
        }
        if (reverse)
        {
            reverse = item.getPickupDelay() < itemTarget.getPickupDelay();
        }
        if (reverse)
        {
            Item cacheItem = item;
            item = itemTarget;
            itemTarget = cacheItem;
        }
        int amount = this.engine.getAmountFromItem(item);
        int amountTarget = this.engine.getAmountFromItem(itemTarget);
        if (amount <= 0)
        {
            amount = item.getItemStack().getAmount();
        }
        if (amountTarget <= 0)
        {
            amountTarget = itemTarget.getItemStack().getAmount();
        }
        itemTarget.remove();
        ItemStack itemStack = item.getItemStack();
        itemStack.setAmount(amount + amountTarget);
        this.engine.updateItem(item);
    }

    @EventHandler
    public void onPickupItem(InventoryPickupItemEvent event)
    {
        if (this.engine.disallow(event.getItem()))
        {
            return;
        }
        event.setCancelled(this.engine.pickup(event.getItem(), event.getInventory()));
    }
}
