package com.minersland.plugin.landclaim;

import com.minersland.plugin.MinersLandPlugin;
import com.minersland.plugin.util.ChatFormatter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.logging.Logger;

public class LandClaimManager implements Listener
{
    private MinersLandPlugin plugin;
    private ArrayList<LandClaim> landClaims;
    private HashMap<Player, ClaimPoints> claimPoints;

    public LandClaimManager(MinersLandPlugin p)
    {
        plugin = p;
        landClaims = new ArrayList<>();
        claimPoints = new HashMap<>();
    }

    public void load() throws SQLException
    {
        Logger logger = plugin.getLogger();

        try (Connection conn = plugin.getDatabase().getConnection())
        {
            try (PreparedStatement pStmt = conn.prepareStatement("SELECT UUID,OwnerUUID,Name,WorldUUID,Point1X,Point1Y,Point1Z,Point2X,Point2Y,Point2Z FROM LandClaims WHERE RegionId=?;"))
            {
                pStmt.setString(1, plugin.getServerConfig().getRegionId());

                try (ResultSet resultSet = pStmt.executeQuery())
                {
                    while (resultSet.next())
                    {
                        World world = Bukkit.getWorld(UUID.fromString(resultSet.getString(4)));

                        if (world == null)
                        {
                            logger.warning("Land claim manager: land claim " + resultSet.getString(1 + " has invalid world UUID"));
                            continue;
                        }

                        UUID uuid = UUID.fromString(resultSet.getString(1));
                        UUID owner = UUID.fromString(resultSet.getString(2));
                        ArrayList<UUID> members = new ArrayList<>();
                        String name = resultSet.getString(3);
                        Location point1 = new Location(world, resultSet.getInt(5), resultSet.getInt(6), resultSet.getInt(7));
                        Location point2 = new Location(world, resultSet.getInt(8), resultSet.getInt(9), resultSet.getInt(10));

                        try (PreparedStatement pStmt2 = conn.prepareStatement("SELECT MemberUUID FROM LandClaimMembers WHERE ClaimUUID=?;"))
                        {
                            pStmt2.setString(1, uuid.toString());

                            try (ResultSet resultSet2 = pStmt2.executeQuery())
                            {
                                while (resultSet2.next())
                                {
                                    members.add(UUID.fromString(resultSet.getString(1)));
                                }
                            }
                        }

                        landClaims.add(new LandClaim(uuid, owner, members, name, point1, point2));
                    }
                }
            }

            conn.commit();
        }
    }

    public void unload()
    {
        
    }

    public Optional<LandClaim> getLandClaim(Predicate<LandClaim> predicate)
    {
        for (LandClaim claim : landClaims)
        {
            if (predicate.test(claim))
            {
                return Optional.of(claim);
            }
        }

        return Optional.empty();
    }

    public ClaimPoints correctClaimPoints(ClaimPoints claimPoints)
    {
        return new ClaimPoints(
                new Location(claimPoints.point1.getWorld(), Math.min(claimPoints.point1.getBlockX(), claimPoints.point2.getBlockX()), Math.min(claimPoints.point1.getBlockY(), claimPoints.point2.getBlockY()), Math.min(claimPoints.point1.getBlockZ(), claimPoints.point2.getBlockZ())),
                new Location(claimPoints.point2.getWorld(), Math.max(claimPoints.point1.getBlockX(), claimPoints.point2.getBlockX()), Math.max(claimPoints.point1.getBlockY(), claimPoints.point2.getBlockY()), Math.max(claimPoints.point1.getBlockZ(), claimPoints.point2.getBlockZ())));
    }

    public Optional<LandClaim> intersectsClaim(ClaimPoints claimPoints)
    {
        for (LandClaim claim : landClaims)
        {
            if (claim.intersects(claimPoints))
            {
                return Optional.of(claim);
            }
        }

        return Optional.empty();
    }

    public ArrayList<String> getClaimList(Player player)
    {
        ArrayList<String> claims = new ArrayList<>();

        for (LandClaim claim : landClaims)
        {
            if (claim.getOwner().equals(player.getUniqueId()))
            {
                claims.add(claim.getName());
            }
        }

        return claims;
    }

    public ArrayList<String> getMemberList(Player player, String name)
    {
        ArrayList<String> members = new ArrayList<>();

        for (LandClaim claim : landClaims)
        {
            if (claim.getOwner().equals(player.getUniqueId()) && claim.getName().equalsIgnoreCase(name))
            {
                members.add("no-one");
            }
        }

        return members;
    }

    public ClaimPoints getClaimPoints(Player player)
    {
        return claimPoints.get(player);
    }

    public void addClaimPoints(Player player, ClaimPoints claimPoint)
    {
        claimPoints.put(player, claimPoint);
    }

    public void removeClaimPoints(Player player)
    {
        claimPoints.remove(player);
    }

    public void createLandClaim(Player player, String name, ClaimPoints claimPoints)
    {
        final UUID uuidFinal = UUID.randomUUID();
        LandClaim claim = new LandClaim(uuidFinal, player.getUniqueId(), new ArrayList<>(), name, claimPoints.point1, claimPoints.point2);
        landClaims.add(claim);
        final UUID ownerFinal = player.getUniqueId();
        final String nameFinal = name;
        final Location point1Final = claimPoints.point1;
        final Location point2Final = claimPoints.point2;
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                try
                {
                    try (Connection conn = plugin.getDatabase().getConnection())
                    {
                        try (PreparedStatement pStmt = conn.prepareStatement("INSERT INTO LandClaims (RegionId,UUID,OwnerUUID,Name,WorldUUID,Point1X,Point1Y,Point1Z,Point2X,Point2Y,Point2Z) VALUES (?,?,?,?,?,?,?,?,?,?,?);"))
                        {
                            pStmt.setString(1, plugin.getServerConfig().getRegionId());
                            pStmt.setString(2, uuidFinal.toString());
                            pStmt.setString(3, ownerFinal.toString());
                            pStmt.setString(4, nameFinal);
                            pStmt.setString(5, point1Final.getWorld().getUID().toString());
                            pStmt.setInt(6, point1Final.getBlockX());
                            pStmt.setInt(7, point1Final.getBlockY());
                            pStmt.setInt(8, point1Final.getBlockZ());
                            pStmt.setInt(9, point2Final.getBlockX());
                            pStmt.setInt(10, point2Final.getBlockY());
                            pStmt.setInt(11, point2Final.getBlockZ());
                            pStmt.execute();
                        }

                        conn.commit();
                    }
                }
                catch (Exception e)
                {
                    plugin.getLogger().severe("Error while adding land claim to database: " + e.getMessage());
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    public void removeLandClaim(LandClaim landClaim)
    {
        landClaims.remove(landClaim);
        final UUID uuidFinal = landClaim.getUUID();
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                try
                {
                    try (Connection conn = plugin.getDatabase().getConnection())
                    {
                        try (PreparedStatement pStmt = conn.prepareStatement("DELETE FROM LandClaims WHERE UUID=? AND RegionID=?;"))
                        {
                            pStmt.setString(1, uuidFinal.toString());
                            pStmt.setString(2, plugin.getServerConfig().getRegionId());
                            pStmt.execute();
                        }

                        conn.commit();
                    }
                }
                catch (Exception e)
                {
                    plugin.getLogger().severe("Error while removing land claim from database: " + e.getMessage());
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e)
    {
        removeClaimPoints(e.getPlayer());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e)
    {
        for (LandClaim claim : landClaims)
        {
            if (claim.contains(e.getBlock().getLocation()) && !claim.hasAccess(e.getPlayer()))
            {
                e.setCancelled(true);
                OfflinePlayer owner = Bukkit.getOfflinePlayer(claim.getOwner());
                e.getPlayer().sendMessage(ChatFormatter.createCmdMessage("Land Claim", "This claim belongs to " + owner.getName() + "."));
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e)
    {
        for (LandClaim claim : landClaims)
        {
            if (claim.contains(e.getBlock().getLocation()) && !claim.hasAccess(e.getPlayer()))
            {
                e.setCancelled(true);
                OfflinePlayer owner = Bukkit.getOfflinePlayer(claim.getOwner());
                e.getPlayer().sendMessage(ChatFormatter.createCmdMessage("Land Claim", "This claim belongs to " + owner.getName() + "."));
            }
        }
    }
}
