package com.minersland.plugin;

import com.minersland.plugin.commands.BalanceCmd;
import com.minersland.plugin.commands.InvseeCmd;
import com.minersland.plugin.commands.LandClaimCmd;
import com.minersland.plugin.commands.PlayerInfoCmd;
import com.minersland.plugin.data.Database;
import com.minersland.plugin.data.PlayerManager;
import com.minersland.plugin.data.ServerConfig;
import com.minersland.plugin.data.ServerPlayer;
import com.minersland.plugin.landclaim.LandClaimManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Instant;
import java.util.logging.Logger;

public class MinersLandPlugin extends JavaPlugin
{
    private boolean properStartup = true;

    private Database database;
    private ServerConfig serverConfig;
    private PlayerManager playerManager;
    private LandClaimManager landClaimManager;

    @Override
    public void onEnable()
    {
        if (!getDataFolder().isDirectory())
        {
            getDataFolder().mkdir();
        }

        database = new Database(this);
        Logger logger = getLogger();

        try
        {
            database.load();
        }
        catch (IOException e)
        {
            properStartup = false;
            logger.severe("IO error when loading database: " + e.getMessage());
        }
        catch (Exception e)
        {
            properStartup = false;
            logger.severe("Error when loading database: " + e.getMessage());
        }

        serverConfig = new ServerConfig(this);

        try
        {
            serverConfig.load();
        }
        catch (SQLException e)
        {
            properStartup = false;
            logger.severe("IO error when loading server configuration: " + e.getMessage());
        }
        catch (PluginException e)
        {
            properStartup = false;
            logger.severe("Plugin error when loading server configuration: " + e.getMessage());
        }
        catch (Exception e)
        {
            properStartup = false;
            logger.severe("Error when loading server configuration: " + e.getMessage());
        }

        playerManager = new PlayerManager(this);

        try
        {
            playerManager.load();
        }
        catch (SQLException e)
        {
            properStartup = false;
            logger.severe("SQL error when loading player manager: " + e.getMessage());
        }
        catch (PluginException e)
        {
            properStartup = false;
            logger.severe("Plugin error when loading player manager: " + e.getMessage());
        }
        catch (Exception e)
        {
            properStartup = false;
            logger.severe("Error when loading player manager: " + e.getMessage());
        }

        landClaimManager = new LandClaimManager(this);

        try
        {
            landClaimManager.load();
        }
        catch (SQLException e)
        {
            properStartup = false;
            logger.severe("SQL error when loading land claim manager: " + e.getMessage());
        }
        catch (Exception e)
        {
            properStartup = false;
            logger.severe("Error when loading land claim manager: " + e.getMessage());
        }

        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new Listener()
        {
            @EventHandler(priority = EventPriority.HIGHEST)
            public void onPlayerJoin(PlayerJoinEvent e)
            {
                if (!properStartup)
                {
                    Player player = e.getPlayer();
                    player.sendMessage(ChatColor.RED + "WARNING: SERVER HAD IMPROPER STARTUP");

                    if (!player.hasPermission("server.startupbypass"))
                    {
                        e.getPlayer().kickPlayer("Server is currently experiencing issues. Try again later.");
                    }
                }
            }
        }, this);
        pluginManager.registerEvents(playerManager, this);
        pluginManager.registerEvents(landClaimManager, this);

        registerCommand("playerinfo", new PlayerInfoCmd(this));
        registerCommand("balance", new BalanceCmd(this));
        registerCommand("landclaim", new LandClaimCmd(this));
        registerCommand("inventorysee", new InvseeCmd(this));
    }

    private void registerCommand(String name, MLCommand cmd)
    {
        getCommand(name).setExecutor(cmd);
        getCommand(name).setTabCompleter(cmd);
    }

    @Override
    public void onDisable()
    {
        Logger logger = getLogger();

        if (playerManager != null)
        {
            for (Player player : Bukkit.getOnlinePlayers())
            {
                ServerPlayer serverPlayer = playerManager.getServerPlayer(p -> p.getUUID().equals(player.getUniqueId())).get();
                serverPlayer.setLastJoined(Instant.now());
            }

            try
            {
                playerManager.unload();
            }
            catch (SQLException e)
            {
                logger.severe("SQL error when unloading player manager: " + e.getMessage());
            }
        }

        if (landClaimManager != null)
        {
            try
            {
                landClaimManager.unload();
            }
            catch (Exception e)
            {
                logger.severe("Error when unloading land claim manager: " + e.getMessage());
            }
        }

        database.unload();
    }

    public boolean isProperStartup()
    {
        return properStartup;
    }

    public Database getDatabase()
    {
        return database;
    }

    public ServerConfig getServerConfig()
    {
        return serverConfig;
    }

    public PlayerManager getPlayerManager()
    {
        return playerManager;
    }

    public LandClaimManager getLandClaimManager()
    {
        return landClaimManager;
    }
}
