package com.pokeskies.velocitybroadcast.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.tree.LiteralCommandNode
import com.pokeskies.velocitybroadcast.VelocityBroadcast
import com.pokeskies.velocitybroadcast.commands.subcommands.AllCommand
import com.pokeskies.velocitybroadcast.commands.subcommands.DebugCommand
import com.pokeskies.velocitybroadcast.commands.subcommands.ReloadCommand
import me.lucko.fabric.api.permissions.v0.Permissions
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

class BaseCommand {
    private val aliases = listOf("velocitybroadcast", "vb")

    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        val rootCommands: List<LiteralCommandNode<CommandSourceStack>> = aliases.map {
            Commands.literal(it)
                .requires(Permissions.require("${VelocityBroadcast.MOD_ID}.command.base", 2))
                .build()
        }

        val subCommands: List<LiteralCommandNode<CommandSourceStack>> = listOf(
            ReloadCommand().build(),
            DebugCommand().build(),
            AllCommand().build()
        )

        rootCommands.forEach { root ->
            subCommands.forEach { sub -> root.addChild(sub) }
            dispatcher.root.addChild(root)
        }
    }
}
