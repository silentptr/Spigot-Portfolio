package com.minersland.plugin.landclaim;

import org.bukkit.Location;

public class ClaimPoints
{
    public Location point1, point2;

    public ClaimPoints() { }

    public ClaimPoints(Location p1, Location p2)
    {
        point1 = p1;
        point2 = p2;
    }
}
