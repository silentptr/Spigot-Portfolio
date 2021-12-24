package com.minersland.plugin.data;

public class RankPermission
{
    private String rankId;
    private String permission;

    public RankPermission(String rankId, String permission)
    {
        this.rankId = rankId;
        this.permission = permission;
    }

    public String getRankId()
    {
        return rankId;
    }

    public String getPermission()
    {
        return permission;
    }
}
