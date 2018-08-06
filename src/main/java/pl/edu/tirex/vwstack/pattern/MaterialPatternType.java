package pl.edu.tirex.vwstack.pattern;

import org.bukkit.inventory.ItemStack;

public class MaterialPatternType implements PatternType
{
    public static final MaterialPatternType INSTANCE = new MaterialPatternType();

    @Override
    public String replace(ItemStack itemStack)
    {
        return itemStack.getType().name().replace('_', ' ');
    }

    @Override
    public String toString()
    {
        return "{MATERIAL}";
    }
}
