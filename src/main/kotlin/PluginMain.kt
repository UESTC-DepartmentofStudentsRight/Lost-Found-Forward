package org.Reforward.mirai.plugin

import kotlinx.coroutines.launch
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.utils.info
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.sendTo
import org.jetbrains.kotlinx.serialization.compiler.backend.jvm.ARRAY

val PluginID = "org.example.mirai-plugin"
val PluginVersion = "0.0.1"

object PluginMain : KotlinPlugin(
    JvmPluginDescription(
        id = PluginID,
        version = PluginVersion
    )
) {
    override fun onEnable() {
        logger.info { "Writen by QYXW" }
        Mydata.reload()
    }

    private fun ForwardtheMsg() {
        subscribeGroupMessages {
            always {

            }
        }
    }

    private fun send(group : Group, messagechain : MessageChain, bot : Bot) {
        val groups = Mydata.groups
        for (id : Long in groups) {
            launch { bot.getGroups(id), sendMessage(messagechain) }
        }
    }
}

object Mydata : AutoSavePluginConfig("Groups") {
    val groups: Array<Long> by value(emptyArray<Long>())
    val sendlist : Array<>
}