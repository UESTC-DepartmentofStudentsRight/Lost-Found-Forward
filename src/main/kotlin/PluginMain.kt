@file:Suppress("unused")

package org.Reforward.mirai.plugin


import com.google.auto.service.AutoService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.alsoLogin
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.utils.info
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.event.subscribeTempMessages
import net.mamoe.mirai.message.data.*


val PluginID = "org.Reforward.mirai-plugin"
val PluginVersion = "0.0.8"


@AutoService(KotlinPlugin::class)
object PluginMain : KotlinPlugin(
    JvmPluginDescription(
        id = PluginID,
        version = PluginVersion
    )
) {
    private val BotId = 2026338927L
    private val pwd = "QYXW2020"

    @ConsoleExperimentalApi
    override fun onEnable() {
        logger.info { "由权益小窝开发组出品。你的全心，我的权益！" }
        Mydata.reload()
        CommandRegister.commandRegister()
        ForwardtheMsg()
        Replytempmessage()
        autoLogin()
        Mydata.botId = BotId
    }

    override fun onDisable() {
        super.onDisable()
        CommandRegister.commandUnregister()
        logger.error("插件卸载!")
    }

    @ConsoleExperimentalApi
    fun autoLogin() {
        PluginMain.launch {
            MiraiConsole.addBot(BotId, pwd) {
                fileBasedDeviceInfo()
            }.alsoLogin()
        }
    }



    private fun ForwardtheMsg() {
        subscribeGroupMessages {
            always {
                PluginMain.logger.info("接收到了新的消息！")
                val id: Long = group.id
                val originGroup: Long = Mydata.originGroup
                //logger.info("id = ${id}, oringinGroup = ${originGroup}")
                if (id == originGroup && sender.id in Mydata.senderid) {
                    //logger.info("准备发送")
                    val messageChainBuilder = MessageChainBuilder()
                    if (message.contentToString()[0] == '#' && message.contentToString().length > 1) {
                        message.forEachContent {
                            if (it is PlainText) {
                                messageChainBuilder.add(it.content.replaceFirst("#".toRegex(), ""))
                                return@forEachContent
                            }
                            messageChainBuilder.add(it)
                        }
                        send(messageChainBuilder.asMessageChain(), bot)
                        bot.getGroup(originGroup).sendMessage("失物招领已转发！")
                        return@always
                    }
                }
            }
        }
    }

    private fun Replytempmessage() {
        subscribeTempMessages() {
            always {
                PluginMain.logger.info("接收到了一个临时会话")
                val id: Long = sender.id
                val group: Long = group.id
                val tempset = Botdata.cachesender
                if (group in Mydata.groups && id !in tempset) {
                    launch {
                        sender.sendMessage("小天使是自动转发的BOT, 这是一条自动回复消息，请找群里的其他管理员哦！")
                        tempset.add(sender.id)
                        if (tempset.size > 20)
                            tempset.drop(1)
                    }
                }
            }
        }
    }


    private fun send(messagechain: MessageChain, bot: Bot) {
        val groups = Mydata.groups
        for (id: Long in groups) {
            launch {
                val time: Long = (0L..15000L).random()
                delay(time)
                bot.getGroup(id).sendMessage(messagechain)
            }
        }
    }
}

object Mydata : AutoSavePluginConfig("Groups") {
    var groups: MutableSet<Long> by value(mutableSetOf<Long>())
    var senderid: MutableSet<Long> by value(mutableSetOf<Long>())
    var originGroup: Long by value(445786154L)
    var botId: Long by value(2026338927L)
}

object Botdata : AutoSavePluginData("bot") {
    var cachesender by value(mutableSetOf<Long>())
}