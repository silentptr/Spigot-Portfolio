package com.minersland.plugin.util;

import org.bukkit.ChatColor;

public class ChatFormatter
{
    private ChatFormatter() { }

    public static final String MSG_INVALID_PLAYER = "Invalid player.";
    public static final String MSG_INVALID_FUNDS = "Insufficient funds.";
    public static final String MSG_NO_PERMS = "You do not have permission to use this command.";

    public static String createCmdMessage(String cmdName, String message)
    {
        return ChatColor.GOLD + cmdName + "> " + ChatColor.GRAY + message;
    }
}
