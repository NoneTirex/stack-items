package pl.edu.tirex.vwstack;

import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.plugin.java.JavaPlugin;
import pl.edu.tirex.vwstack.configuration.Configuration;
import pl.edu.tirex.vwstack.engine.StackItemEngine;
import pl.edu.tirex.vwstack.listeners.EntityPickupListener;
import pl.edu.tirex.vwstack.listeners.ItemStackListener;
import pl.edu.tirex.vwstack.listeners.PlayerPickupListener;
import pl.edu.tirex.vwstack.pattern.AmountPatternType;
import pl.edu.tirex.vwstack.pattern.DisplayNamePatternType;
import pl.edu.tirex.vwstack.pattern.MaterialPatternType;
import pl.edu.tirex.vwstack.pattern.NamePattern;
import pl.edu.tirex.vwstack.pattern.NamePatternType;

import java.io.File;
import java.util.EnumSet;
import java.util.logging.Level;

public class MainStack
        extends JavaPlugin
{
    private StackItemEngine stackItemEngine;
    private Configuration   configuration;

    @Override
    public void onEnable()
    {
        this.configuration = new Configuration("items", this.getConfig());

        File file = new File(this.getDataFolder(), "config.yml");
        if (!file.exists())
        {
            this.saveDefaultConfig();
        }
        NamePattern.registerPattern("name", new NamePatternType(this.configuration));
        NamePattern.registerPattern("display_name", new DisplayNamePatternType(this.configuration));
        NamePattern.registerPattern("material", MaterialPatternType.INSTANCE);
        NamePattern.registerPattern("amount", new AmountPatternType());

        try
        {
            this.configuration.loadConfiguration(file);
        }
        catch (InvalidConfigurationException e)
        {
            this.getLogger().log(Level.WARNING, "Problem with load configuration", e);
            return;
        }

        this.stackItemEngine = new StackItemEngine(this);
        this.getServer().getPluginManager().registerEvents(new ItemStackListener(this.stackItemEngine), this);
        try
        {
            Class.forName(EntityPickupItemEvent.class.getName());
            this.getServer().getPluginManager().registerEvents(new EntityPickupListener(this.stackItemEngine), this);
        }
        catch (Throwable e)
        {
            this.getServer().getPluginManager().registerEvents(new PlayerPickupListener(this.stackItemEngine), this);
        }

        if (this.getConfig().getBoolean("debug", false))
        {
            this.getLogger().info("Debug is already enabled!");
            this.getLogger().info("Name Pattern: " + this.configuration.getNamePattern());
            this.getLogger().info("World Configuration:");
            this.configuration.getWorldConfigurationMap().forEach((worldName, configuration) ->
            {
                this.getLogger().info("  " + worldName + ":");
                EnumSet<Material> enumSet = configuration.getBlockedMaterials();
                boolean reverse = false;
                if (enumSet.size() > Material.values().length / 2)
                {
                    EnumSet<Material> newEnumSet = EnumSet.allOf(Material.class);
                    newEnumSet.removeAll(enumSet);
                    enumSet = newEnumSet;
                    reverse = true;
                }
                else if (enumSet.size() <= 0)
                {
                    reverse = true;
                }
                StringBuilder sb = new StringBuilder();
                sb.append("      Stacking ");
                if (reverse)
                {
                    sb.append("enabled");
                }
                else
                {
                    sb.append("disabled");
                }
                sb.append(" for:");
                if (enumSet.size() <= 0 || enumSet.size() >= Material.values().length)
                {
                    sb.append(" all");
                }
                this.getLogger().info(sb.toString());
                for (Material material : enumSet)
                {
                    this.getLogger().info("        " + material.name());
                }
            });
            this.getLogger().info("Name Configuration:");
            this.getLogger().info("  Name Pattern:");
            this.configuration.getNamePatternMap().forEach((material, name) ->
            {
                this.getLogger().info("    " + material + ": " + name);
            });
            if (this.configuration.getNamePatternWildcard() != null)
            {
                this.getLogger().info("    Other material: " + this.configuration.getNamePatternWildcard());
            }
            this.getLogger().info("  Pattern:");
            this.configuration.getPatternMap().forEach((material, name) ->
            {
                this.getLogger().info("    " + material + ": " + name);
            });
            if (this.configuration.getPatternWildcard() != null)
            {
                this.getLogger().info("    Other material: " + this.configuration.getPatternWildcard());
            }
        }
    }

    public Configuration getConfiguration()
    {
        return configuration;
    }

    public StackItemEngine getStackItemEngine()
    {
        return stackItemEngine;
    }
}
