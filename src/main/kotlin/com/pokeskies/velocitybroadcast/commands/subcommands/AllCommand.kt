package com.pokeskies.velocitybroadcast.commands.subcommands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.pokeskies.velocitybroadcast.BroadcastManager
import com.pokeskies.velocitybroadcast.VelocityBroadcast
import com.pokeskies.velocitybroadcast.utils.SubCommand
import com.pokeskies.velocitybroadcast.utils.Utils
import me.lucko.fabric.api.permissions.v0.Permissions
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component

class AllCommand : SubCommand {
    override fun build(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("all")
            .requires(Permissions.require("${VelocityBroadcast.MOD_ID}.command.all", 2))
            .then(Commands.argument("message", StringArgumentType.greedyString())
                .executes(Companion::executeAll)
            )
            .build()
    }

    companion object {
        fun executeAll(ctx: CommandContext<CommandSourceStack>): Int {
            val player = ctx.source.server.playerList.players.firstOrNull() ?: run {
                Utils.printDebug("No players found to be able to broadcast through!")
                ctx.source.sendFailure(Component.literal("No players were online! This command requires at least one player to be online."))
                return 0
            }

            val message = StringArgumentType.getString(ctx, "message")
            Utils.printDebug("Attempting a broadcast message to all servers of: $message")
            BroadcastManager.createBroadcast(message, player)

            return 1
        }
    }
}
