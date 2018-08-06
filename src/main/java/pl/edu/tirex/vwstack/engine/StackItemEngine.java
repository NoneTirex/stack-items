package pl.edu.tirex.vwstack.engine;

import org.bukkit.ChatColor;
import org.bukkit.entity.Item;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import pl.edu.tirex.vwstack.MainStack;
import pl.edu.tirex.vwstack.configuration.WorldConfiguration;
import pl.edu.tirex.vwstack.pattern.PatternType;

import java.util.HashMap;
import java.util.List;

public class StackItemEngine
{
    private static final String separator = ChatColor.RESET.toString();

    private final MainStack plugin;

    public StackItemEngine(MainStack plugin)
    {
        this.plugin = plugin;
    }

    public boolean pickup(Item item, Inventory inventory)
    {
        int amount = this.getAmountFromItem(item);
        if (amount <= 0)
        {
            return false;
        }
        ItemStack itemStack = item.getItemStack();
        itemStack.setAmount(amount);
        int maxStackSize = itemStack.getMaxStackSize();
        int stacks = amount / maxStackSize + 1;
        ItemStack[] itemStacks = new ItemStack[stacks];
        for (int i = 0; i < itemStacks.length; i++)
        {
            ItemStack childItemStack = itemStack.clone();
            childItemStack.setAmount(amount < maxStackSize ? amount : maxStackSize);
            itemStacks[i] = childItemStack;
            amount -= childItemStack.getAmount();
        }

        HashMap<Integer, ItemStack> map = inventory.addItem(itemStacks);
        for (ItemStack stack : map.values())
        {
            amount += stack.getAmount();
        }
        itemStack.setAmount(amount);
        if (amount <= 0)
        {
            item.remove();
            return true;
        }
        this.updateItem(item);
        return true;
    }

    public int getAmountFromItem(Item item)
    {
        MetadataValue amountMetadata = this.getMetadata(this.plugin, item.getMetadata("amount"));
        if (amountMetadata == null)
        {
            return this.parseAmount(item.getCustomName(), item.getItemStack());
//            return 0;
        }
        return amountMetadata.asInt();
    }

    public int getTicksLived(Item item)
    {
        MetadataValue amountMetadata = this.getMetadata(this.plugin, item.getMetadata("ticks-lived"));
        if (amountMetadata == null)
        {
            return 0;
        }
        return amountMetadata.asInt();
    }

    public void addTicksLived(Item item)
    {
        int ticksLived = this.getTicksLived(item);
        if (ticksLived > 0)
        {
            ticksLived -= 2;
        }
        ticksLived += item.getTicksLived();
        ticksLived = Math.max(ticksLived, 1);
        item.setMetadata("ticks-lived", new FixedMetadataValue(this.plugin, ticksLived));
    }

    public void setTicksLived(Item item)
    {
        int ticksLived = item.getTicksLived() - 2;
        ticksLived = Math.max(ticksLived, 1);
        item.setMetadata("ticks-lived", new FixedMetadataValue(this.plugin, ticksLived));
    }

    public void updateItem(Item item)
    {
        ItemStack itemStack = item.getItemStack();
        item.setCustomName(this.generateFullName(itemStack));
        item.setCustomNameVisible(true);
        item.setMetadata("amount", new FixedMetadataValue(this.plugin, itemStack.getAmount()));
        this.addTicksLived(item);
        item.setTicksLived(3);
        itemStack.setAmount(1);
    }

    private MetadataValue getMetadata(Plugin plugin, List<MetadataValue> metadataValues)
    {
        if (metadataValues.size() < 1)
        {
            return null;
        }
        for (MetadataValue metadataValue : metadataValues)
        {
            if (metadataValue.getOwningPlugin().equals(plugin))
            {
                return metadataValue;
            }
        }
        return null;
    }

    private int parseAmount(String name, ItemStack itemStack)
    {
        if (name == null || name.isEmpty())
        {
            return 0;
        }
        int startIndex = name.indexOf(separator);
        if (startIndex != 0)
        {
            return 0;
        }
        int endIndex = name.indexOf(separator, startIndex + 1);
        if (endIndex <= startIndex)
        {
            return 0;
        }
        name = name.substring(startIndex + separator.length(), endIndex);
        name = name.replace(Character.toString(ChatColor.COLOR_CHAR), "");
        if (name.length() < 1)
        {
            return 0;
        }
        try
        {
            return Integer.parseInt(name);
        }
        catch (NumberFormatException ignored)
        {
        }
        return 0;
    }

    private String generateFullName(ItemStack itemStack)
    {
        PatternType pattern = this.plugin.getConfiguration().getMaterialPattern(itemStack.getType());
        StringBuilder sb = new StringBuilder();
        sb.append(ChatColor.RESET);
        char[] chars = Integer.toString(itemStack.getAmount()).toCharArray();
        for (char c : chars)
        {
            sb.append(ChatColor.getByChar(c));
        }
        sb.append(ChatColor.RESET);
        sb.append(pattern.replace(itemStack));
        return sb.toString();
    }

    public boolean disallow(Item item)
    {
        WorldConfiguration configuration = this.plugin.getConfiguration().getWorldConfigurationMap().get(item.getWorld().getName().toLowerCase());
        return configuration != null && configuration.getBlockedMaterials().contains(item.getItemStack().getType());
    }
}
