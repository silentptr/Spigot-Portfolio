package com.minersland.plugin.commands;

import com.minersland.plugin.MLCommand;
import com.minersland.plugin.MinersLandPlugin;
import com.minersland.plugin.util.ChatFormatter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class InvseeCmd extends MLCommand
{
    public InvseeCmd(MinersLandPlugin p)
    {
        super(p);
    }

    @Override
    public void doCommand(Player player, String[] args)
    {
        if (!player.hasPermission("ml.invsee"))
        {
            player.sendMessage(ChatFormatter.createCmdMessage("Inventory See", ChatFormatter.MSG_NO_PERMS));
            return;
        }

        if (args.length == 1)
        {
            Player p = Bukkit.getPlayer(args[0]);

            if (p == null)
            {
                player.sendMessage(ChatFormatter.createCmdMessage("Inventory See", ChatFormatter.MSG_INVALID_PLAYER));
            }
            else
            {
                player.openInventory(p.getInventory());
            }
        }
        else
        {
            player.sendMessage(ChatFormatter.createCmdMessage("Inventory See", "Correct usage: /inventorysee <player>"));
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
