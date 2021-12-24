package com.minersland.plugin.commands;

import com.minersland.plugin.MLCommand;
import com.minersland.plugin.MinersLandPlugin;
import com.minersland.plugin.data.ServerPlayer;
import com.minersland.plugin.util.ChatFormatter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class BalanceCmd extends MLCommand
{
    public BalanceCmd(MinersLandPlugin p)
    {
        super(p);
    }

    @Override
    public void doCommand(Player player, String[] args)
    {
        ServerPlayer serverPlayer;
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);

        switch (args.length)
        {
            case 0:
                serverPlayer = plugin.getPlayerManager().getServerPlayer(p -> p.getUUID().equals(player.getUniqueId())).get();
                player.sendMessage(ChatFormatter.createCmdMessage("Balance", "Your balance is: " + formatter.format(serverPlayer.getMoney())));
                break;
            case 1:
                @SuppressWarnings("deprecation")
                OfflinePlayer op = Bukkit.getOfflinePlayer(args[0]);
                Optional<ServerPlayer> opt = plugin.getPlayerManager().getServerPlayer(p -> p.getUUID().equals(op.getUniqueId()));

                if (opt.isEmpty())
                {
                    player.sendMessage(ChatFormatter.createCmdMessage("Balance", ChatFormatter.MSG_INVALID_PLAYER));
                }
                else
                {
                    serverPlayer = opt.get();
                    player.sendMessage(ChatFormatter.createCmdMessage("Balance", op.getName() + "'s balance is: " + formatter.format(serverPlayer.getMoney())));
                }

                break;
            default:
                player.sendMessage(ChatFormatter.createCmdMessage("Balance", "Correct usage: /balance [player]"));
                break;
        }
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
