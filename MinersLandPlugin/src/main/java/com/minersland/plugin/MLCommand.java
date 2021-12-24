package com.minersland.plugin;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class MLCommand implements CommandExecutor, TabCompleter
{
    protected MinersLandPlugin plugin;

    public MLCommand(MinersLandPlugin p)
    {
        plugin = p;
    }

    @Override
    public final boolean onCommand(CommandSender sender, Command command, String alias, String[] args)
    {
        if (sender instanceof Player)
        {
            doCommand((Player)sender, args);
            return true;
        }
        else
        {
            sender.sendMessage(ChatColor.RED + "Commands can only be used by players!");
            return false;
        }
    }

    @Override
    public final List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
    {
        if (sender instanceof Player)
        {
            return doTabComplete((Player)sender, args);
        }
        else
        {
            return null;
        }
    }

    protected ArrayList<String> emptyTabList()
    {
        return new ArrayList<>();
    }

    protected List<String> tabList(String... content)
    {
        return Arrays.asList(content);
    }

    public abstract void doCommand(Player player, String[] args);
    public abstract List<String> doTabComplete(Player player, String[] args);
}
