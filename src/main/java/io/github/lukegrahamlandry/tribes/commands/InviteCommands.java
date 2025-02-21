package io.github.lukegrahamlandry.tribes.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.lukegrahamlandry.tribes.commands.util.OfflinePlayerArgumentType;
import io.github.lukegrahamlandry.tribes.tribe_data.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class InviteCommands {
    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("invite")
                .then(Commands.literal("send")
                        .then(Commands.argument("player", OfflinePlayerArgumentType.offlinePlayerID())
                                .executes(InviteCommands::invitePlayer)
                        )
                )
                .then(Commands.literal("revoke")
                        .then(Commands.argument("player", OfflinePlayerArgumentType.offlinePlayerID())
                                .executes(InviteCommands::uninvitePlayer)
                        )
                )
                .then(Commands.literal("toggle")
                        .then(Commands.argument("private", BoolArgumentType.bool())
                                .executes(InviteCommands::setPrivate)
                        )
                );
    }

    private static int invitePlayer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Player leader = context.getSource().getPlayerOrException();
        UUID toInvite = OfflinePlayerArgumentType.getOfflinePlayer(context, "player");

        Tribe tribe = TribesManager.getTribeOf(leader.getUUID());

        if (tribe == null) {
            context.getSource().sendFailure(TribeError.YOU_NOT_IN_TRIBE.getText());
            return 0;
        }
        if (!tribe.isPrivate()) {
            context.getSource().sendFailure(TribeError.NOT_PRIVATE.getText());
            return 0;
        }
        if (!tribe.isOfficerOrHigher(leader.getUUID())) {
            context.getSource().sendFailure(TribeError.RANK_TOO_LOW.getText());
            return 0;
        }
        if (TribesManager.getTribeOf(toInvite) != null) {
            context.getSource().sendFailure(TribeError.IN_OTHER_TRIBE.getText());
            return 0;
        }

        tribe.getPendingInvites().add(toInvite);
        TribeHelper.broadcastMessage(tribe, TribeSuccessType.INVITE_SENT, leader, context.getSource().getServer(), OfflinePlayerArgumentType.getPlayerName(toInvite));

        return Command.SINGLE_SUCCESS;
    }

    private static int uninvitePlayer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Player leader = context.getSource().getPlayerOrException();
        UUID toInvite = OfflinePlayerArgumentType.getOfflinePlayer(context, "player");

        Tribe tribe = TribesManager.getTribeOf(leader.getUUID());

        if (tribe == null) {
            context.getSource().sendFailure(TribeError.YOU_NOT_IN_TRIBE.getText());
            return 0;
        }
        if (!tribe.isOfficerOrHigher(leader.getUUID())) {
            context.getSource().sendFailure(TribeError.RANK_TOO_LOW.getText());
            return 0;
        }

        tribe.getPendingInvites().remove(toInvite);
        TribeHelper.broadcastMessage(tribe, TribeSuccessType.INVITE_REMOVED, leader, context.getSource().getServer(), OfflinePlayerArgumentType.getPlayerName(toInvite));

        return 0;
    }

    private static int setPrivate(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Player leader = context.getSource().getPlayerOrException();
        boolean flag = BoolArgumentType.getBool(context, "private");

        Tribe tribe = TribesManager.getTribeOf(leader.getUUID());

        if (tribe == null) {
            context.getSource().sendSuccess(TribeError.YOU_NOT_IN_TRIBE.getText(), false);
            return 0;
        }
        if (!tribe.isViceLeaderOrHigher(leader.getUUID())) {
            context.getSource().sendSuccess(TribeError.RANK_TOO_LOW.getText(), false);
            return 0;
        }

        tribe.setPrivate(flag);
        TribeHelper.broadcastMessage(tribe, flag ? TribeSuccessType.NOW_PRIVATE : TribeSuccessType.NO_LONGER_PRIVATE, leader, context.getSource().getServer());

        return Command.SINGLE_SUCCESS;
    }


}
