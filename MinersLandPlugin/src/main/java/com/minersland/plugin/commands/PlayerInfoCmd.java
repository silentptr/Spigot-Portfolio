package com.minersland.plugin.commands;

import com.minersland.plugin.MLCommand;
import com.minersland.plugin.MinersLandPlugin;
import com.minersland.plugin.data.ServerPlayer;
import com.minersland.plugin.util.ChatFormatter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.text.NumberFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class PlayerInfoCmd extends MLCommand
{
    public PlayerInfoCmd(MinersLandPlugin p)
    {
        super(p);
    }

    @Override
    public void doCommand(Player player, String[] args)
    {
        if (args.length != 1)
        {
            player.sendMessage(ChatFormatter.createCmdMessage("Player Info", "Correct usage: /playerinfo <player>"));
            return;
        }

        @SuppressWarnings("deprecation")
        OfflinePlayer getter = Bukkit.getOfflinePlayer(args[0]);
        Optional<ServerPlayer> opt = plugin.getPlayerManager().getServerPlayer(p -> p.getUUID().equals(getter.getUniqueId()));

        if (opt.isEmpty())
        {
            player.sendMessage(ChatFormatter.createCmdMessage("Player Info", ChatFormatter.MSG_INVALID_PLAYER));
            return;
        }

        DateTimeFormatter instantFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(Locale.UK).withZone(ZoneId.systemDefault());
        ServerPlayer serverPlayer = opt.get();
        Instant now = Instant.now();
        player.sendMessage(ChatFormatter.createCmdMessage("Player Info", "Player information for " + getter.getName() + ":"),
                ChatColor.GRAY + "Username: " + getter.getName(),
                ChatColor.GRAY + "UUID: " + getter.getUniqueId().toString(),
                ChatColor.GRAY + "Rank: " + serverPlayer.getRankId(),
                ChatColor.GRAY + "Balance: " + NumberFormat.getCurrencyInstance(Locale.US).format(serverPlayer.getMoney()),
                ChatColor.GRAY + "First joined: " + instantFormatter.format(serverPlayer.getFirstJoined()) + " (" + minimumTimeDifference(serverPlayer.getFirstJoined(), now) + " ago)",
                ChatColor.GRAY + "Last joined: " + (getter.isOnline() ? "now" : instantFormatter.format(serverPlayer.getLastJoined()) + " (" + minimumTimeDifference(serverPlayer.getLastJoined(), now) + " ago)"));
    }

    private String minimumTimeDifference(Instant i1, Instant i2)
    {
        Duration d = Duration.between(i1, i2);
        long time = d.toSeconds();

        if (time < 60)
        {
            return time + (time == 1 ? " second" : " seconds");
        }

        time = d.toMinutes();

        if (time < 60)
        {
            return time + (time == 1 ? " minute" : " minutes");
        }

        time = d.toHours();

        if (time < 60)
        {
            return time + (time == 1 ? " hour" : " hours");
        }

        time = d.toDays();
        return d.toDays() + (time == 1 ? " day" : " days");
    }

    @Override
    public List<String> doTabComplete(Player player, String[] args)
    {
        if (args.length == 1)
        {
            return null;
        }
        else
        {
            return new ArrayList<>();
        }
    }
}
