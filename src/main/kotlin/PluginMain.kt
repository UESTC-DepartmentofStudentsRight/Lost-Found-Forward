@file:Suppress("unused")

package org.Reforward.mirai.plugin


import com.google.auto.service.AutoService
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.Command
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.SimpleCommand
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
import org.graalvm.compiler.nodes.calc.AddNode

val PluginID = "org.Reforward.mirai-plugin"
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
        Addsenderid.register()
        AddGroupID.register()
    }

    override fun onDisable() {
        super.onDisable()
        logger.error("Reforward Disable")
        Addsenderid.unregister()
        AddGroupID.unregister()
    }


    private fun ForwardtheMsg() {
        subscribeGroupMessages {
            always {
                if (group.id == Mydata.origingroup && sender.id in Mydata.senderid) {
                    send(message, bot)
                }
            }
        }
    }


    private fun send(messagechain: MessageChain, bot: Bot) {
        val groups = Mydata.groups
        for (id: Long in groups) {
            launch { bot.getGroup(id).sendMessage(messagechain) }
        }
    }
}

object Mydata : AutoSavePluginConfig("Groups") {
    var groups: MutableList<Long> by value(mutableListOf<Long>())
    var senderid : MutableList<Long> by value(mutableListOf<Long>())
    val origingroup : Long by value(445786154L)
}

object Addsenderid : SimpleCommand(
    PluginMain, "asi",
    description = "添加允许转发的人的QQ号"
) {
    @Handler
    suspend fun CommandSender.handle(id: Long){
        Mydata.senderid.add(id)
    }
}

object AddGroupID : SimpleCommand(
    PluginMain, "agi",
    description = "添加转发的群"
) {
    @Handler
    suspend fun CommandSender.handle(id: Long){
        Mydata.groups.add(id)
    }
}