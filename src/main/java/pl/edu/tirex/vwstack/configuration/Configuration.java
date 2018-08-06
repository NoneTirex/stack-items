package pl.edu.tirex.vwstack.configuration;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import pl.edu.tirex.vwstack.LimitException;
import pl.edu.tirex.vwstack.pattern.MaterialPatternType;
import pl.edu.tirex.vwstack.pattern.NamePattern;
import pl.edu.tirex.vwstack.pattern.PatternType;

import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class Configuration
{
    private final Map<Material, NamePattern>      patternMap            = new EnumMap<>(Material.class);
    private final Map<Material, NamePattern>      namePatternMap        = new EnumMap<>(Material.class);
    private final Map<String, WorldConfiguration> worldConfigurationMap = new HashMap<>();
    private final String prefix;
    private final FileConfiguration config;
    private       NamePattern       namePattern;
    private       NamePattern       namePatternWildcard;
    private       NamePattern       patternWildcard;

    public Configuration(String prefix, FileConfiguration config)
    {
        this.prefix = prefix;
        this.config = config;
    }

    public void loadConfiguration(File file) throws InvalidConfigurationException
    {
        try
        {
            this.config.load(file);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return;
        }
        this.namePattern = NamePattern.parsePattern(this.config.getString(this.prefix + ".name-pattern", "&e{count}x {name}"));
        ConfigurationSection section = this.config.getConfigurationSection(this.prefix + ".limit");
        Set<String> keys = section.getKeys(false);
        EnumSet<Material> wildcardLimit = null;
        if (keys.contains("*"))
        {
            keys.remove("*");
            ConfigurationSection worldSection = section.getConfigurationSection("*");
            wildcardLimit = this.getLimit(worldSection, null);
        }
        for (String key : keys)
        {
            ConfigurationSection worldSection = section.getConfigurationSection(key);
            EnumSet<Material> limit = this.getLimit(worldSection, wildcardLimit);
            this.worldConfigurationMap.put(worldSection.getName().toLowerCase(), new WorldConfiguration(limit));
        }
        section = this.config.getConfigurationSection(this.prefix + ".name");
        keys = section.getKeys(false);
        EnumSet<Material> materials = EnumSet.noneOf(Material.class);
        for (String key : keys)
        {
            Object object = section.get(key);
            String name = null;
            String patternString = null;
            if ("".equals(object))
            {
                materials.addAll(this.getMaterials(key));
                continue;
            }
            else if (object instanceof ConfigurationSection)
            {
                name = ((ConfigurationSection) object).getString("name");
                patternString = ((ConfigurationSection) object).getString("pattern");
            }
            else
            {
                name = section.getString(key);
            }
            NamePattern namePattern = NamePattern.parsePattern(name);
            NamePattern pattern = NamePattern.parsePattern(patternString);
            if (key.equals("*"))
            {
                this.patternWildcard = pattern;
                this.namePatternWildcard = namePattern;
            }
            else
            {
                materials.addAll(this.getMaterials(key));
            }
            for (Material material : materials)
            {
                if (pattern != null)
                {
                    this.patternMap.putIfAbsent(material, pattern);
                }
                if (namePattern != null)
                {
                    this.namePatternMap.putIfAbsent(material, namePattern);
                }
            }
            materials = EnumSet.noneOf(Material.class);
        }
    }

    public PatternType getMaterialNamePattern(Material material)
    {
        NamePattern name = this.namePatternMap.get(material);
        if (name == null && this.namePatternWildcard != null)
        {
            return this.namePatternWildcard;
        }
        else if (name == null)
        {
            return MaterialPatternType.INSTANCE;
        }
        return name;
    }

    public PatternType getMaterialPattern(Material material)
    {
        NamePattern name = this.patternMap.get(material);
        if (name == null && this.namePattern != null)
        {
            return this.namePattern;
        }
        else if (name == null)
        {
            return MaterialPatternType.INSTANCE;
        }
        return name;
    }

    public Map<Material, NamePattern> getPatternMap()
    {
        return patternMap;
    }

    public NamePattern getPatternWildcard()
    {
        return patternWildcard;
    }

    public NamePattern getNamePattern()
    {
        return namePattern;
    }

    public NamePattern getNamePatternWildcard()
    {
        return namePatternWildcard;
    }

    public Map<Material, NamePattern> getNamePatternMap()
    {
        return namePatternMap;
    }

    public Map<String, WorldConfiguration> getWorldConfigurationMap()
    {
        return worldConfigurationMap;
    }

    public EnumSet<Material> getLimit(ConfigurationSection worldSection, EnumSet<Material> parent)
    {
        try
        {
            return this.parseLimit(worldSection, parent);
        }
        catch (LimitException e)
        {
            System.out.println(e.getMessage());
        }
        return null;
    }

    private EnumSet<Material> parseLimit(ConfigurationSection worldSection, EnumSet<Material> parent)
    {
        EnumSet<Material> blockedMaterials = parent != null ? EnumSet.copyOf(parent) : null;
        Object blacklist = worldSection.get("blacklist");
        Object whitelist = worldSection.get("whitelist");
        if (blacklist != null && blacklist.equals(whitelist))
        {
            throw new LimitException("Whitelist can not match blacklist.");
        }
        if (blacklist != null && blacklist.equals("*"))
        {
            blockedMaterials = EnumSet.allOf(Material.class);
        }
        else if (whitelist != null && whitelist.equals("*"))
        {
            blockedMaterials = EnumSet.noneOf(Material.class);
        }
        if (blockedMaterials == null)
        {
            blockedMaterials = EnumSet.noneOf(Material.class);
        }
        if (blacklist instanceof List)
        {
            for (String name : (List<String>) blacklist)
            {
                blockedMaterials.addAll(this.getMaterials(name));
            }
        }
        else if (blacklist instanceof String && !blacklist.equals("*"))
        {
            blockedMaterials.addAll(this.getMaterials((String) blacklist));
        }
        if (blockedMaterials.isEmpty())
        {
            return EnumSet.noneOf(Material.class);
        }
        if (whitelist instanceof List)
        {
            for (String name : (List<String>) whitelist)
            {
                blockedMaterials.removeAll(this.getMaterials(name));
            }
        }
        else if (whitelist instanceof String && !whitelist.equals("*"))
        {
            blockedMaterials.removeAll(this.getMaterials((String) whitelist));
        }
        return blockedMaterials;
    }

    private EnumSet<Material> getMaterials(String name)
    {
        int index = name.indexOf('-');
        if (index < 0)
        {
            return this.matchMaterials(name);
        }
        if (index + 1 >= name.length())
        {
            throw new LimitException("Failed range of '" + name + "'");
        }
        Material from = Material.matchMaterial(name.substring(0, index));
        Material to = Material.matchMaterial(name.substring(index + 1, name.length()));
        return EnumSet.range(from, to);
    }

    private EnumSet<Material> matchMaterials(String name)
    {
        int index = name.indexOf('*');
        if (index < 0)
        {
            return EnumSet.of(Material.matchMaterial(name));
        }
        name = name.toUpperCase(Locale.ENGLISH);
        name = name.replace(".*", "*");
        name = name.replace("*", ".*");
        name = name.replaceAll("\\s+", "_");
        Pattern pattern = Pattern.compile(name);
        EnumSet<Material> materials = EnumSet.noneOf(Material.class);
        for (Material material : Material.values())
        {
            if (pattern.matcher(material.name()).matches())
            {
                materials.add(material);
            }
        }
        return materials;
    }
}
