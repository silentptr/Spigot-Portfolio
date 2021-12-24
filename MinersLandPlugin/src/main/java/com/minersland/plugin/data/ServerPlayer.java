package com.minersland.plugin.data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class ServerPlayer
{
    private UUID uuid;
    private String rankId;
    private BigDecimal money;
    private Instant firstJoined;
    private Instant lastJoined;

    public ServerPlayer(UUID uuid, String rankId, BigDecimal money, Instant firstJoined, Instant lastJoined)
    {
        this.uuid = uuid;
        this.rankId = rankId;
        this.money = money;
        this.firstJoined = firstJoined;
        this.lastJoined = lastJoined;
    }

    public UUID getUUID()
    {
        return uuid;
    }

    public String getRankId()
    {
        return rankId;
    }

    public void setRankId(String rankId)
    {
        this.rankId = rankId;
    }

    public BigDecimal getMoney()
    {
        return money;
    }

    public void setMoney(BigDecimal money)
    {
        this.money = money;
    }

    public void addMoney(BigDecimal money)
    {
        this.money = this.money.add(money);
    }

    public void removeMoney(BigDecimal money)
    {
        this.money = this.money.subtract(money);
    }

    public Instant getFirstJoined()
    {
        return firstJoined;
    }

    public Instant getLastJoined()
    {
        return lastJoined;
    }

    public void setLastJoined(Instant lastJoined)
    {
        this.lastJoined = lastJoined;
    }
}
