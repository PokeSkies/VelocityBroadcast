package com.pokeskies.velocitybroadcast

import com.google.common.io.ByteStreams
import com.pokeskies.fabricpluginmessaging.PluginMessageEvent
import com.pokeskies.fabricpluginmessaging.PluginMessagePacket
import com.pokeskies.velocitybroadcast.utils.TextUtils
import com.pokeskies.velocitybroadcast.utils.Utils
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

object BroadcastManager {
    private var currentBroadcast: Broadcast? = null

    fun init() {
        PluginMessageEvent.EVENT.register(PluginMessageEvent { payload, context ->
            val inputStream = ByteStreams.newDataInput(payload.data)
            val channel = inputStream.readUTF()
            Utils.printDebug("Received a plugin message on channel $channel")
            if (channel == "GetServers") {
                val serversList = inputStream.readUTF()
                Utils.printDebug("Received servers list: $serversList (currentBroadcast is $currentBroadcast)")
                currentBroadcast?.requestPlayers(
                    if (serversList.contains(","))
                        serversList.replace(" ", "").split(",")
                    else
                        listOf(serversList)
                )
            } else if (channel == "PlayerList") {
                val server = inputStream.readUTF()
                val playerList = inputStream.readUTF()
                Utils.printDebug("Received players list: $playerList (currentBroadcast is $currentBroadcast)")
                currentBroadcast?.sendServerBroadcast(server,
                    if (playerList.contains(","))
                        playerList.replace(" ", "").split(",")
                    else
                        listOf(playerList)
                )
            }
        })
    }

    // Create the broadcast and send a request to Velocity to get the list of servers to send down the line
    fun createBroadcast(message: String, player: ServerPlayer) {
        currentBroadcast = Broadcast(message)

        ByteStreams.newDataOutput().let {
            it.writeUTF("GetServers")
            ServerPlayNetworking.send(player, PluginMessagePacket(it.toByteArray()))
            Utils.printDebug("Sent GetServers request to Velocity through player ${player.name.string}!")
        }
    }

    class Broadcast(val message: String) {
        // This will track a list of servers that we have already sent a broadcast to (aka the players on that server)
        val sentServers = mutableListOf<String>()

        // When we receive the list of servers, we will request the players on each server and track it to sentServers
        fun requestPlayers(servers: List<String>) {
            val player = VelocityBroadcast.INSTANCE.server.playerList.players.firstOrNull() ?: run {
                Utils.printError("No players found to be able to broadcast through!")
                return
            }

            servers.forEach { server ->
                ByteStreams.newDataOutput().let {
                    it.writeUTF("PlayerList")
                    it.writeUTF(server)
                    ServerPlayNetworking.send(player, PluginMessagePacket(it.toByteArray()))
                }
            }
        }

        // Once we have a list of players on a server, send them the broadcast message
         fun sendServerBroadcast(server: String, players: List<String>) {
             if (sentServers.contains(server)) {
                 Utils.printDebug("Already sent a broadcast to $server, skipping...")
                 return
             }

             sendPlayersBroadcast(players)
             sentServers.add(server)
         }

        // Parses the broadcast message and sends to the list of players
        private fun sendPlayersBroadcast(players: List<String>) {
            val player = VelocityBroadcast.INSTANCE.server.playerList.players.firstOrNull() ?: run {
                Utils.printError("No players found to be able to broadcast through!")
                return
            }

            val parsedMessage = Component.Serializer.toJson(TextUtils.toNative(message), VelocityBroadcast.INSTANCE.server.registryAccess())

            Utils.printDebug("Parsed broadcast message is: $parsedMessage")

            players.forEach { p ->
                ByteStreams.newDataOutput().let {
                    it.writeUTF("MessageRaw")
                    it.writeUTF(p)
                    it.writeUTF(parsedMessage)

                    Utils.printDebug("Sending message to $p")
                    ServerPlayNetworking.send(player, PluginMessagePacket(it.toByteArray()))
                }
            }
        }
    }
}
