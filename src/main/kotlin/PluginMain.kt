@file:Suppress("unused")

package org.Reforward.mirai.plugin


import com.google.auto.service.AutoService
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.utils.info
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.sendTo
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import org.jetbrains.kotlinx.serialization.compiler.backend.jvm.ARRAY
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.uploadAsImage

val PluginID = "org.example.mirai-plugin"
val PluginVersion = "0.0.1"

@AutoService(KotlinPlugin::class)
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

    override fun onDisable() {
        super.onDisable()
        logger.error("Reforward Disable")
    }

    /*
    private fun ForwardtheMsg() {
        subscribeGroupMessages {
            always {

            }
        }
    }
     */

    private fun send(group: Group, messagechain: MessageChain, bot: Bot) {
        val groups = Mydata.groups
        for (id: Long in groups) {
            launch { bot.getGroup(id).sendMessage(messagechain) }
        }
    }
}

object Mydata : AutoSavePluginConfig("Groups") {
    val groups: Array<Long> by value(emptyArray<Long>())

}