package com.minersland.plugin.data;

import org.bukkit.ChatColor;

public class Rank
{
    private String rankId;
    private String name;
    private ChatColor primary, secondary;

    public Rank(String rankId, String name, ChatColor primary, ChatColor secondary)
    {
        this.rankId = rankId;
        this.name = name;
        this.primary = primary;
        this.secondary = secondary;
    }

    public String getRankId()
    {
        return rankId;
    }

    public String getName()
    {
        return name;
    }

    public ChatColor getPrimaryColour()
    {
        return primary;
    }

    public ChatColor getSecondaryColour()
    {
        return secondary;
    }
}
