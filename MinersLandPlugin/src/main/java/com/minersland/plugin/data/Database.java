package com.minersland.plugin.data;

import com.minersland.plugin.MinersLandPlugin;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class Database
{
    private MinersLandPlugin plugin;
    private HikariDataSource dataSource;

    public Database(MinersLandPlugin p)
    {
        plugin = p;
    }

    public void load() throws IOException
    {
        Properties properties = new Properties();

        try (FileInputStream stream = new FileInputStream(new File(plugin.getDataFolder(), "database.properties")))
        {
            properties.load(stream);
        }

        dataSource = new HikariDataSource(new HikariConfig(properties));
    }

    public void unload()
    {
        if (dataSource != null)
        {
            dataSource.close();
        }
    }

    public Connection getConnection() throws SQLException
    {
        Connection c = dataSource.getConnection();
        c.setAutoCommit(false);
        return c;
    }
}
