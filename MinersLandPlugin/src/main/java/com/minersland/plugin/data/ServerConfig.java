package com.minersland.plugin.data;

import com.minersland.plugin.MinersLandPlugin;
import com.minersland.plugin.PluginException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ServerConfig
{
    private MinersLandPlugin plugin;
    private ArrayList<String> regions;
    private String regionId;

    public ServerConfig(MinersLandPlugin p)
    {
        plugin = p;
        regions = new ArrayList<>();
    }

    public void load() throws SQLException, PluginException
    {
        try (Connection conn = plugin.getDatabase().getConnection())
        {
            try (PreparedStatement pStmt = conn.prepareStatement("SELECT RegionId FROM Regions;"))
            {
                try (ResultSet resultSet = pStmt.executeQuery())
                {
                    while (resultSet.next())
                    {
                        String regionId = resultSet.getString(1);
                        regions.add(regionId);
                        plugin.getLogger().info("Loaded region '" + regionId + "'.");
                    }
                }
            }

            conn.commit();
        }

        YamlConfiguration yamlConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "server.yml"));
        yamlConfig.addDefault("region-id", null);

        regionId = yamlConfig.getString("region-id");

        if (regionId == null)
        {
            throw new PluginException("missing region-id");
        }
        else if (!regions.contains(regionId))
        {
            throw new PluginException("invalid region");
        }
    }

    public String getRegionId()
    {
        return regionId;
    }
}
