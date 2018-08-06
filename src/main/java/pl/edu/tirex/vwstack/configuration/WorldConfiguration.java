package pl.edu.tirex.vwstack.configuration;

import org.bukkit.Material;

import java.util.EnumSet;

public class WorldConfiguration
{
    private final EnumSet<Material> blockedMaterials;

    public WorldConfiguration(EnumSet<Material> blockedMaterials)
    {
        this.blockedMaterials = blockedMaterials;
    }

    public EnumSet<Material> getBlockedMaterials()
    {
        return blockedMaterials;
    }
}
