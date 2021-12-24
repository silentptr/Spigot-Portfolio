package com.minersland.plugin.commands;

import com.minersland.plugin.MLCommand;
import com.minersland.plugin.MinersLandPlugin;
import com.minersland.plugin.data.ServerPlayer;
import com.minersland.plugin.landclaim.ClaimPoints;
import com.minersland.plugin.landclaim.LandClaim;
import com.minersland.plugin.util.ChatFormatter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class LandClaimCmd extends MLCommand
{
    public LandClaimCmd(MinersLandPlugin p)
    {
        super(p);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void doCommand(Player player, String[] args)
    {
        ServerPlayer serverPlayer = plugin.getPlayerManager().getServerPlayer(p -> p.getUUID().equals(player.getUniqueId())).get();

        switch (args.length)
        {
            case 1:
                ClaimPoints claimPoints;

                switch (args[0])
                {
                    case "setpoint1":
                        claimPoints = plugin.getLandClaimManager().getClaimPoints(player);

                        if (claimPoints == null)
                        {
                            claimPoints = new ClaimPoints();
                            plugin.getLandClaimManager().addClaimPoints(player, claimPoints);
                        }

                        claimPoints.point1 = player.getLocation();
                        player.sendMessage(ChatFormatter.createCmdMessage("Land Claim", "Set point 1."));
                        break;
                    case "setpoint2":
                        claimPoints = plugin.getLandClaimManager().getClaimPoints(player);

                        if (claimPoints == null)
                        {
                            claimPoints = new ClaimPoints();
                            plugin.getLandClaimManager().addClaimPoints(player, claimPoints);
                        }

                        claimPoints.point2 = player.getLocation();
                        player.sendMessage(ChatFormatter.createCmdMessage("Land Claim", "Set point 2."));
                        break;
                    default:
                        player.sendMessage(ChatFormatter.createCmdMessage("Land Claim", "Correct usage: /landclaim <setpoint1/setpoint2/create/delete/addmember/removemember>"));
                        break;
                }

                break;
            case 2:
                switch (args[0])
                {
                    case "create":
                        if (!validLandClaimName(args[1]))
                        {
                            player.sendMessage(ChatFormatter.createCmdMessage("Land Claim", "Invalid claim name."));
                            return;
                        }
                        else if (plugin.getLandClaimManager().getLandClaim(c -> c.getName().equalsIgnoreCase(args[1])).isPresent())
                        {
                            player.sendMessage(ChatFormatter.createCmdMessage("Land Claim", "You already have a claim with that name."));
                            return;
                        }

                        claimPoints = plugin.getLandClaimManager().getClaimPoints(player);

                        if (claimPoints == null)
                        {
                            player.sendMessage(ChatFormatter.createCmdMessage("Land Claim", "Invalid claim points."));
                            return;
                        }
                        else if (claimPoints.point1 == null || claimPoints.point2 == null || !(claimPoints.point1.getWorld().getUID().equals(claimPoints.point2.getWorld().getUID())))
                        {
                            player.sendMessage(ChatFormatter.createCmdMessage("Land Claim", "Invalid claim points."));
                            return;
                        }

                        ClaimPoints correctPoints = plugin.getLandClaimManager().correctClaimPoints(claimPoints);
                        Optional<LandClaim> intersects = plugin.getLandClaimManager().intersectsClaim(correctPoints);

                        if (intersects.isPresent())
                        {
                            player.sendMessage(ChatFormatter.createCmdMessage("Land Claim", "The area you have selected intersects with "
                            + Bukkit.getOfflinePlayer(intersects.get().getOwner()).getName() + "'s claim."));
                            return;
                        }

                        BigDecimal cost = calculateCost(claimPoints);

                        if (cost.doubleValue() < 1)
                        {
                            player.sendMessage(ChatFormatter.createCmdMessage("Land Claim", "Invalid claim points."));
                            return;
                        }
                        else if (serverPlayer.getMoney().compareTo(cost) < 0)
                        {
                            player.sendMessage(ChatFormatter.createCmdMessage("Land Claim", ChatFormatter.MSG_INVALID_FUNDS));
                            return;
                        }

                        serverPlayer.removeMoney(cost);
                        plugin.getLandClaimManager().createLandClaim(player, args[1], correctPoints);
                        plugin.getLandClaimManager().removeClaimPoints(player);
                        player.sendMessage(ChatFormatter.createCmdMessage("Land Claim", "Successfully created claim '" + args[1] + "'. " +
                                NumberFormat.getCurrencyInstance(Locale.US).format(cost) + " has been removed from your balance."));
                        break;
                    case "delete":
                        if (!validLandClaimName(args[1]))
                        {
                            player.sendMessage(ChatFormatter.createCmdMessage("Land Claim", "Invalid claim."));
                            return;
                        }

                        Optional<LandClaim> opt = plugin.getLandClaimManager().getLandClaim(c -> c.getName().equalsIgnoreCase(args[1]));

                        if (opt.isEmpty())
                        {
                            player.sendMessage(ChatFormatter.createCmdMessage("Land Claim", "Invalid claim."));
                            return;
                        }

                        plugin.getLandClaimManager().removeLandClaim(opt.get());
                        player.sendMessage(ChatFormatter.createCmdMessage("Land Claim", "Successfully deleted claim '" + args[1] + "'."));
                        break;
                    default:
                        player.sendMessage(ChatFormatter.createCmdMessage("Land Claim", "Correct usage: /landclaim <setpoint1/setpoint2/create/delete/addmember/removemember>"));
                        break;
                }

                break;
            case 3:
                Optional<LandClaim> landClaimOpt;
                LandClaim landClaim;
                OfflinePlayer member;

                switch (args[0])
                {
                    case "addmember":
                        if (!validLandClaimName(args[1]))
                        {
                            player.sendMessage(ChatFormatter.createCmdMessage("Land Claim", "Invalid claim."));
                            return;
                        }

                        landClaimOpt = plugin.getLandClaimManager().getLandClaim(c -> c.getOwner().equals(player.getUniqueId()) && c.getName().equalsIgnoreCase(args[1]));

                        if (landClaimOpt.isEmpty())
                        {
                            player.sendMessage(ChatFormatter.createCmdMessage("Land Claim", "Invalid claim."));
                            return;
                        }

                        landClaim = landClaimOpt.get();
                        member = Bukkit.getOfflinePlayer(args[2]);

                        if (plugin.getPlayerManager().getServerPlayer(p -> p.getUUID().equals(member.getUniqueId())).isEmpty())
                        {
                            player.sendMessage(ChatFormatter.createCmdMessage("Land Claim", ChatFormatter.MSG_INVALID_PLAYER));
                            return;
                        }
                        else if (landClaim.getOwner().equals(member.getUniqueId()))
                        {
                            player.sendMessage(ChatFormatter.createCmdMessage("Land Claim", "You cannot be a member of your own claim."));
                            return;
                        }
                        else if (landClaim.isMember(member.getUniqueId()))
                        {
                            player.sendMessage(ChatFormatter.createCmdMessage("Land Claim", "That player is already a member."));
                            return;
                        }

                        landClaim.addMember(plugin, member.getUniqueId());
                        player.sendMessage(ChatFormatter.createCmdMessage("Land Claim", "Added " + member.getName() + " as a member to your claim."));
                        break;
                    case  "removemember":
                        if (!validLandClaimName(args[1]))
                        {
                            player.sendMessage(ChatFormatter.createCmdMessage("Land Claim", "Invalid claim."));
                            return;
                        }

                        landClaimOpt = plugin.getLandClaimManager().getLandClaim(c -> c.getOwner().equals(player.getUniqueId()) && c.getName().equalsIgnoreCase(args[1]));

                        if (landClaimOpt.isEmpty())
                        {
                            player.sendMessage(ChatFormatter.createCmdMessage("Land Claim", "Invalid claim."));
                            return;
                        }

                        landClaim = landClaimOpt.get();
                        member = Bukkit.getOfflinePlayer(args[2]);

                        if (plugin.getPlayerManager().getServerPlayer(p -> p.getUUID().equals(member.getUniqueId())).isEmpty())
                        {
                            player.sendMessage(ChatFormatter.createCmdMessage("Land Claim", ChatFormatter.MSG_INVALID_PLAYER));
                            return;
                        }
                        else if (landClaim.getOwner().equals(member.getUniqueId()))
                        {
                            player.sendMessage(ChatFormatter.createCmdMessage("Land Claim", "You cannot be a member of your own claim."));
                            return;
                        }
                        else if (!landClaim.isMember(member.getUniqueId()))
                        {
                            player.sendMessage(ChatFormatter.createCmdMessage("Land Claim", "That player is already not a member."));
                            return;
                        }

                        landClaim.removeMember(plugin, member.getUniqueId());
                        player.sendMessage(ChatFormatter.createCmdMessage("Land Claim", "Removed " + member.getName() + " as a member to your claim."));
                        break;
                    default:
                        player.sendMessage(ChatFormatter.createCmdMessage("Land Claim", "Correct usage: /landclaim <setpoint1/setpoint2/create/delete/addmember/removemember>"));
                        break;
                }

                break;
            default:
                player.sendMessage(ChatFormatter.createCmdMessage("Land Claim", "Correct usage: /landclaim <setpoint1/setpoint2/create/delete/addmember/removemember>"));
                break;
        }
    }

    @Override
    public List<String> doTabComplete(Player player, String[] args)
    {
        switch (args.length)
        {
            case 1:
                return tabList("setpoint1", "setpoint2", "create", "delete", "addmember", "removemember");
            case 2:
                switch (args[0])
                {
                    case "addmember":
                    case "delete":
                        return plugin.getLandClaimManager().getClaimList(player);
                    default:
                        return emptyTabList();
                }
            case 3:
                switch (args[0])
                {
                    case "addmember":
                        return null;
                    case "removemember":
                        return plugin.getLandClaimManager().getMemberList(player, args[1]);
                    default:
                        return emptyTabList();
                }
            default:
                return emptyTabList();
        }
    }

    private boolean validLandClaimName(String name)
    {
        return name.length() > 0 && name.length() < 10 && name.matches("^[a-zA-Z0-9_-]+$");
    }

    private BigDecimal calculateCost(ClaimPoints claimPoints)
    {
        return new BigDecimal(  (Math.max(claimPoints.point1.getBlockX(), claimPoints.point2.getBlockX()) - Math.min(claimPoints.point1.getBlockX(), claimPoints.point2.getBlockX())) *
                                    (Math.max(claimPoints.point1.getBlockY(), claimPoints.point2.getBlockY()) - Math.min(claimPoints.point1.getBlockY(), claimPoints.point2.getBlockY())) *
                                    (Math.max(claimPoints.point1.getBlockZ(), claimPoints.point2.getBlockZ()) - Math.min(claimPoints.point1.getBlockZ(), claimPoints.point2.getBlockZ())))
                .multiply(new BigDecimal(1.5d));
    }
}
