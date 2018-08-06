package pl.edu.tirex.vwstack.pattern;

import org.bukkit.inventory.ItemStack;

public class AmountPatternType implements PatternType
{
    @Override
    public String replace(ItemStack itemStack)
    {
        return Integer.toString(itemStack.getAmount());
    }

    @Override
    public String toString()
    {
        return "{AMOUNT}";
    }
}
