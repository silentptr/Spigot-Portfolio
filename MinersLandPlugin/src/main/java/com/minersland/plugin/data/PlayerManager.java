package com.minersland.plugin.data;

import com.minersland.plugin.MinersLandPlugin;
import com.minersland.plugin.PluginException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.scheduler.BukkitRunnable;

import java.math.BigDecimal;
import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Future;
import java.util.function.Predicate;

public class PlayerManager implements Listener
{
    private MinersLandPlugin plugin;
    private Database database;

    private ArrayList<ServerPlayer> players;
    private ArrayList<Rank> ranks;
    private ArrayList<RankPermission> rankPermissions;
    private HashMap<UUID, PermissionAttachment> playerPermissions;

    public PlayerManager(MinersLandPlugin p)
    {
        plugin = p;
        database = plugin.getDatabase();
        players = new ArrayList<>();
        ranks = new ArrayList<>();
        rankPermissions = new ArrayList<>();
        playerPermissions = new HashMap<>();
    }

    public Optional<ServerPlayer> getServerPlayer(Predicate<ServerPlayer> predicate)
    {
        for (ServerPlayer player : players)
        {
            if (predicate.test(player))
            {
                return Optional.of(player);
            }
        }

        return Optional.empty();
    }

    public Optional<Rank> getRank(Predicate<Rank> predicate)
    {
        for (Rank rank : ranks)
        {
            if (predicate.test(rank))
            {
                return Optional.of(rank);
            }
        }

        return Optional.empty();
    }

    public void load() throws SQLException, PluginException
    {
        try (Connection conn = plugin.getDatabase().getConnection())
        {
            try (PreparedStatement pStmt = conn.prepareStatement("SELECT UUID,RankId,Money,FirstJoined,LastJoined FROM ServerPlayers WHERE RegionId=?;"))
            {
                pStmt.setString(1, plugin.getServerConfig().getRegionId());

                try (ResultSet resultSet = pStmt.executeQuery())
                {
                    while (resultSet.next())
                    {
                        players.add(new ServerPlayer(
                                UUID.fromString(resultSet.getString(1)),
                                resultSet.getString(2),
                                resultSet.getBigDecimal(3),
                                resultSet.getTimestamp(4).toInstant(),
                                resultSet.getTimestamp(5).toInstant()));
                    }
                }
            }

            try (PreparedStatement pStmt = conn.prepareStatement("SELECT RankId,RankName,PrimaryColour,SecondaryColour FROM Ranks;"))
            {
                try (ResultSet resultSet = pStmt.executeQuery())
                {
                    while (resultSet.next())
                    {
                        ranks.add(new Rank(
                                resultSet.getString(1),
                                resultSet.getString(2),
                                ChatColor.getByChar(resultSet.getString(3)),
                                ChatColor.getByChar(resultSet.getString(4))));
                    }
                }
            }

            try (PreparedStatement pStmt = conn.prepareStatement("SELECT RankId,Permission FROM RankPermissions;"))
            {
                try (ResultSet resultSet = pStmt.executeQuery())
                {
                    while (resultSet.next())
                    {
                        rankPermissions.add(new RankPermission(resultSet.getString(1), resultSet.getString(2)));
                    }
                }
            }

            conn.commit();
        }
    }

    public void unload() throws SQLException
    {
        try (Connection conn = plugin.getDatabase().getConnection())
        {
            try (PreparedStatement pStmt = conn.prepareStatement("UPDATE ServerPlayers SET RankId=?,Money=?,LastJoined=? WHERE UUID=? AND RegionId=?;"))
            {
                for (ServerPlayer player : players)
                {
                    pStmt.setString(1, player.getRankId());
                    pStmt.setBigDecimal(2, player.getMoney());
                    pStmt.setTimestamp(3, Timestamp.from(player.getLastJoined()));
                    pStmt.setString(4, player.getUUID().toString());
                    pStmt.setString(5, plugin.getServerConfig().getRegionId());
                    pStmt.execute();
                }
            }

            conn.commit();
        }
    }

    public void addPlayerPermissions(Player player)
    {
        ServerPlayer serverPlayer = getServerPlayer(p -> p.getUUID().equals(player.getUniqueId())).get();
        PermissionAttachment attachment = player.addAttachment(plugin);

        for (RankPermission rankPermission : rankPermissions)
        {
            if (rankPermission.getRankId().equals(serverPlayer.getRankId()))
            {
                attachment.setPermission(rankPermission.getPermission(), true);
            }
        }

        playerPermissions.put(player.getUniqueId(), attachment);
    }

    public void removePlayerPermissions(Player player)
    {
        PermissionAttachment oldAttachment = playerPermissions.remove(player.getUniqueId());

        if (oldAttachment != null)
        {
            player.removeAttachment(oldAttachment);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e)
    {
        Player player = e.getPlayer();
        ServerPlayer serverPlayer;
        Optional<ServerPlayer> opt = getServerPlayer(p -> p.getUUID().equals(player.getUniqueId()));

        if (opt.isEmpty())
        {
            Instant now = Instant.now();
            serverPlayer = new ServerPlayer(
                    player.getUniqueId(),
                    "default", new BigDecimal(0.0d),
                    Instant.ofEpochMilli(now.toEpochMilli()),
                    Instant.ofEpochMilli(now.toEpochMilli()));
            players.add(serverPlayer);
            final UUID uuidFinal = serverPlayer.getUUID();
            final String rankIdFinal = serverPlayer.getRankId();
            final BigDecimal moneyFinal = serverPlayer.getMoney();
            final Instant firstJoinedFinal = serverPlayer.getFirstJoined();
            final Instant lastJoinedFinal = serverPlayer.getLastJoined();

            new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        try (Connection conn = database.getConnection())
                        {
                            try (PreparedStatement pStmt = conn.prepareStatement("INSERT INTO ServerPlayers (RegionId,UUID,RankId,Money,FirstJoined,LastJoined) VALUES (?,?,?,?,?,?);"))
                            {
                                pStmt.setString(1, plugin.getServerConfig().getRegionId());
                                pStmt.setString(2, uuidFinal.toString());
                                pStmt.setString(3, rankIdFinal);
                                pStmt.setBigDecimal(4, moneyFinal);
                                pStmt.setTimestamp(5, Timestamp.from(firstJoinedFinal));
                                pStmt.setTimestamp(6, Timestamp.from(lastJoinedFinal));
                                pStmt.execute();
                            }

                            conn.commit();
                        }
                    }
                    catch (SQLException e)
                    {
                        plugin.getLogger().severe("SQL error while adding player to database: " + e.getMessage());
                    }
                    catch (Exception e)
                    {
                        plugin.getLogger().severe("Error while adding player to database: " + e.getMessage());
                    }
                }
            }.runTaskAsynchronously(plugin);
        }

        addPlayerPermissions(player);
        e.setJoinMessage(ChatColor.GREEN + "Join> " + ChatColor.GRAY + player.getName());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e)
    {
        ServerPlayer serverPlayer = getServerPlayer(p -> p.getUUID().equals(e.getPlayer().getUniqueId())).get();
        serverPlayer.setLastJoined(Instant.now());
        removePlayerPermissions(e.getPlayer());
        e.setQuitMessage(ChatColor.RED + "Quit> " + ChatColor.GRAY + e.getPlayer().getName());
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent e)
    {
        final UUID uuidFinal = e.getPlayer().getUniqueId();
        Future<Optional<ServerPlayer>> future = Bukkit.getScheduler().callSyncMethod(plugin, () ->
        {
             return plugin.getPlayerManager().getServerPlayer(p -> p.getUUID().equals(uuidFinal));
        });

        Optional<ServerPlayer> serverPlayerOpt;
        final ServerPlayer serverPlayer;

        try
        {
            serverPlayerOpt = future.get();

            if (serverPlayerOpt.isEmpty())
            {
                throw new Exception();
            }
            else
            {
                serverPlayer = serverPlayerOpt.get();
            }
        }
        catch (Throwable t)
        {
            plugin.getLogger().warning(uuidFinal.toString() + " tried to chat but their server player data couldn't be found.");
            return;
        }

        Future<Optional<Rank>> rankFuture = Bukkit.getScheduler().callSyncMethod(plugin, () ->
        {
            return plugin.getPlayerManager().getRank(r -> r.getRankId().equals(serverPlayer.getRankId()));
        });

        Optional<Rank> rankOpt;
        final Rank rank;

        try
        {
            rankOpt = rankFuture.get();

            if (rankOpt.isEmpty())
            {
                throw new Exception();
            }
            else
            {
                rank = rankOpt.get();
            }
        }
        catch (Throwable t)
        {
            plugin.getLogger().warning(uuidFinal.toString() + " tried to chat but they had an invalid rank.");
            return;
        }

        e.setFormat(rank.getPrimaryColour() + rank.getName() + rank.getSecondaryColour() + " %s " + ChatColor.DARK_GRAY + "» " + ChatColor.WHITE + "%s");
    }
}
