package com.pokeskies.velocitybroadcast.utils

import com.pokeskies.velocitybroadcast.VelocityBroadcast
import net.minecraft.network.chat.Component

object TextUtils {
    fun toNative(text: String): Component {
        return VelocityBroadcast.INSTANCE.adventure.toNative(VelocityBroadcast.MINI_MESSAGE.deserialize(text))
    }

    fun toComponent(text: String): net.kyori.adventure.text.Component {
        return VelocityBroadcast.MINI_MESSAGE.deserialize(text)
    }
}
