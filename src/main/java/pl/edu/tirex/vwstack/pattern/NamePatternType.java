package pl.edu.tirex.vwstack.pattern;

import org.bukkit.inventory.ItemStack;
import pl.edu.tirex.vwstack.configuration.Configuration;

public class NamePatternType implements PatternType
{
    private final Configuration configuration;

    public NamePatternType(Configuration configuration)
    {
        this.configuration = configuration;
    }

    @Override
    public String replace(ItemStack itemStack)
    {
        PatternType pattern = this.configuration.getMaterialNamePattern(itemStack.getType());
        if (pattern == null)
        {
            pattern = this.configuration.getMaterialPattern(itemStack.getType());
        }
        return pattern.replace(itemStack);
    }

    @Override
    public String toString()
    {
        return "{NAME}";
    }
}
