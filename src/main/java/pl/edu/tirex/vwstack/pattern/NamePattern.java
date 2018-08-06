package pl.edu.tirex.vwstack.pattern;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NamePattern
        implements PatternType
{
    private static final Map<String, PatternType> patterns             = new HashMap<>();

    private List<Object> elements;

    public NamePattern()
    {
        this.elements = new ArrayList<>();
    }

    public NamePattern(Object... elements)
    {
        this.elements = Arrays.asList(elements);
    }

    @Override
    public String replace(ItemStack item)
    {
        StringBuilder sb = new StringBuilder();
        for (Object element : this.elements)
        {
            if (element instanceof String)
            {
                sb.append(element);
                continue;
            }
            if (element instanceof PatternType)
            {
                sb.append(((PatternType) element).replace(item));
                continue;
            }
        }
        return sb.toString();
    }

    public static NamePattern parsePattern(String pattern)
    {
        if (pattern == null || pattern.isEmpty())
        {
            return null;
        }
        pattern = ChatColor.translateAlternateColorCodes('&', pattern);
        NamePattern namePattern = new NamePattern();
        boolean openBracket = false;
        StringBuilder sb = null;
        char[] chars = pattern.toCharArray();
        for (char c : chars)
        {
            if (c == '{')
            {
                if (sb != null)
                {
                    namePattern.elements.add((openBracket ? '{' : "") + sb.toString());
                }
                sb = new StringBuilder();
                openBracket = true;
                continue;
            }
            else if (c == '}' && openBracket)
            {
                openBracket = false;
                String name = sb.toString();
                PatternType patternType = patterns.get(name.toLowerCase());
                if (patternType != null)
                {
                    namePattern.elements.add(patternType);
                }
                else
                {
                    namePattern.elements.add('{' + name + '}');
                }
                sb = null;
                continue;
            }
            if (sb == null)
            {
                sb = new StringBuilder();
            }
            sb.append(c);
        }
        if (sb != null)
        {
            namePattern.elements.add((openBracket ? '{' : "") + sb.toString());
        }
        return namePattern;
    }

    public static void registerPattern(String name, PatternType patternType)
    {
        patterns.put(name.toLowerCase(), patternType);
    }

    @Override
    public String toString()
    {
        return String.valueOf(this.elements);
    }
}
