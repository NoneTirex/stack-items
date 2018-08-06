package pl.edu.tirex.vwstack.pattern;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.edu.tirex.vwstack.configuration.Configuration;

public class DisplayNamePatternType
        implements PatternType
{
    private final Configuration configuration;

    public DisplayNamePatternType(Configuration configuration)
    {
        this.configuration = configuration;
    }

    @Override
    public String replace(ItemStack itemStack)
    {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null && itemMeta.getDisplayName() != null && !itemMeta.getDisplayName().isEmpty())
        {
            return itemMeta.getDisplayName();
        }
        return this.configuration.getMaterialNamePattern(itemStack.getType()).replace(itemStack);
    }

    @Override
    public String toString()
    {
        return "{DISPLAY_NAME}";
    }
}
