package com.minersland.plugin.landclaim;

import com.minersland.plugin.MinersLandPlugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.UUID;

public class LandClaim
{
    private UUID uuid, owner;
    private ArrayList<UUID> members;
    private String name;
    private Location point1, point2;

    public LandClaim(UUID uuid, UUID owner, ArrayList<UUID> members, String name, Location point1, Location point2)
    {
        this.uuid = uuid;
        this.owner = owner;
        this.members = members;
        this.name = name;
        this.point1 = point1;
        this.point2 = point2;
    }

    public boolean hasAccess(Player player)
    {
        if (owner.equals(player.getUniqueId()))
        {
            return true;
        }

        for (UUID member : members)
        {
            if (member.equals(player.getUniqueId()))
            {
                return true;
            }
        }

        return false;
    }

    public boolean isMember(UUID uuid)
    {
        return members.contains(uuid);
    }

    public void addMember(MinersLandPlugin plugin, UUID memberUUID)
    {
        members.add(memberUUID);
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                try
                {
                    try (Connection conn = plugin.getDatabase().getConnection())
                    {
                        try (PreparedStatement pStmt = conn.prepareStatement("INSERT INTO LandClaimMembers (ClaimUUID,MemberUUID) VALUES (?,?);"))
                        {
                            pStmt.setString(1, uuid.toString());
                            pStmt.setString(2, memberUUID.toString());
                            pStmt.execute();
                        }

                        conn.commit();
                    }
                }
                catch (Exception e)
                {
                    plugin.getLogger().severe("Error while add land claim member to database: " + e.getMessage());
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    public void removeMember(MinersLandPlugin plugin, UUID memberUUID)
    {
        members.remove(memberUUID);
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                try
                {
                    try (Connection conn = plugin.getDatabase().getConnection())
                    {
                        try (PreparedStatement pStmt = conn.prepareStatement("DELETE FROM LandClaimMembers WHERE ClaimUUID=? AND MemberUUID=?;"))
                        {
                            pStmt.setString(1, uuid.toString());
                            pStmt.setString(2, memberUUID.toString());
                            pStmt.execute();
                        }

                        conn.commit();
                    }
                }
                catch (Exception e)
                {
                    plugin.getLogger().severe("Error while add land claim member to database: " + e.getMessage());
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    public boolean contains(Location l)
    {
        return  l.getWorld().getUID().equals(point1.getWorld().getUID()) &&
                l.getBlockX() >= point1.getBlockX() && l.getBlockX() <= point2.getBlockX() &&
                l.getBlockY() >= point1.getBlockY() && l.getBlockY() <= point2.getBlockY() &&
                l.getBlockZ() >= point1.getBlockZ() && l.getBlockZ() <= point2.getBlockZ();
    }

    public boolean intersects(ClaimPoints claimPoints)
    {
        return  claimPoints.point1.getWorld().getUID().equals(point1.getWorld().getUID()) &&
                point1.getBlockY() <= claimPoints.point2.getBlockY() && point2.getBlockY() >= claimPoints.point1.getBlockY() &&
                point1.getBlockX() <= claimPoints.point2.getBlockX() && point2.getBlockX() >= claimPoints.point1.getBlockX() &&
                point1.getBlockZ() <= claimPoints.point2.getBlockZ() && point2.getBlockZ() >= claimPoints.point1.getBlockZ();
    }

    public UUID getUUID()
    {
        return uuid;
    }

    public UUID getOwner()
    {
        return owner;
    }

    public String getName()
    {
        return name;
    }

    public Location getPoint1()
    {
        return point1;
    }

    public Location getPoint2()
    {
        return point2;
    }
}
