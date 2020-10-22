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
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.Listener
import net.mamoe.mirai.event.events.MessageRecallEvent
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.event.subscribeTempMessages
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.recall
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.atomic.AtomicInteger


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
    private var cacheMessage = Collections.synchronizedMap(mutableMapOf<Int, MutableSet<MessageReceipt<Group>>>())
    private var date = SimpleDateFormat("yyyy-MM-dd").format(Date())

    @ConsoleExperimentalApi
    private val thisBot = MiraiConsole.addBot(BotId, pwd) {
        fileBasedDeviceInfo()
    }


    @ConsoleExperimentalApi
    override fun onEnable() {
        logger.info { "由权益小窝开发组出品。你的全心，我的权益！" }
        Config.reload()
        CommandRegister.commandRegister()
        ForwardtheMsg()
        Replytempmessage()
        SubcribeRecall()
        autoLogin()
        timeAction()
        Config.botId = BotId
    }

    override fun onDisable() {
        super.onDisable()
        CommandRegister.commandUnregister()
        logger.error("插件卸载!")
    }

    @ConsoleExperimentalApi
    fun autoLogin() {
        PluginMain.launch {
            thisBot.alsoLogin()
        }
    }


    private fun ForwardtheMsg() {
        subscribeGroupMessages {
            always {
                PluginMain.logger.info("接收到了新的消息！")
                val id: Long = group.id
                val originGroup: Long = Config.originGroup
                //logger.info("id = ${id}, oringinGroup = ${originGroup}")
                if (id == originGroup && sender.id in Config.senderid) {
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
                        send(messageChainBuilder.asMessageChain(), bot, message.id)
                        if (Data.MessageCnt[sender.id] == null) {
                            Data.MessageCnt[sender.id] = 0
                        } else {
                            Data.MessageCnt[sender.id]!!.plus(1L)
                        }
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
                val tempset = Data.cachesender
                if (group in Config.groups && id !in tempset) {
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

    private fun SubcribeRecall() {
        subscribeAlways<MessageRecallEvent.GroupRecall>(priority = Listener.EventPriority.HIGHEST) {
            if (authorId in Config.senderid && group.id == Config.originGroup) {
                PluginMain.logger.info("准备撤回群内消息！")
                Data.MessageCnt[authorId]!!.minus(1)
                var recallmessage = cacheMessage[messageId]
                while (recallmessage == null || recallmessage.size != Config.groups.size) {
                    delay(1000L)
                    recallmessage = cacheMessage[messageId]
                }
                for (msg in recallmessage) {
                    launch {
                        val time: Long = (2L..15000L).random()
                        delay(time)
                        msg.recall()
                    }
                }

            }
        }
    }


    private fun send(messagechain: MessageChain, bot: Bot, messageID: Int) {
        val groups = Config.groups
        val cnt = AtomicInteger(0)
        val cacheReceipt = Collections.synchronizedSet(mutableSetOf<MessageReceipt<Group>>())
        for (id: Long in groups) {
            launch {
                val time: Long = (2L..15000L).random()
                delay(time)
                cnt.incrementAndGet()
                cacheReceipt.add(bot.getGroup(id).sendMessage(messagechain))
                if (cnt.toInt() == Config.groups.size) {
                    cacheMessage[messageID] = cacheReceipt
                }
            }
        }
    }

    @ConsoleExperimentalApi
    private fun timeAction() {
        launch {
            while (true) {
                if (date == SimpleDateFormat("yyyy-MM-dd").format((Date()))) {
                    delay(600000L)
                    continue
                }
                date = SimpleDateFormat("yyyy-MM-dd").format(Date())
                thisBot.getGroup(Config.originGroup).sendMessage("将开始清理撤回列表以及统计劳模")
                delay(10000L)
                cacheMessage.clear()
                thisBot.getGroup(Config.originGroup).sendMessage("撤回列表清理完毕,小窝将无法撤回之前的消息")
                delay(4000L)
                val maxId = Data.MessageCnt.maxByOrNull { it.value }
                thisBot.getGroup(Config.originGroup).sendMessage(
                    "今日的劳模是：${
                        thisBot.getGroup(Config.originGroup).get(maxId!!.key).nameCardOrNick
                    }，发送条数为${maxId.value}"
                )
                Data.MessageCnt.clear()
            }
        }
    }
}

object Config : AutoSavePluginConfig("Groups") {
    var groups: MutableSet<Long> by value(mutableSetOf<Long>())
    var senderid: MutableSet<Long> by value(mutableSetOf<Long>())
    var originGroup: Long by value(445786154L)
    var botId: Long by value(2026338927L)
}

object Data : AutoSavePluginData("bot") {
    var cachesender by value(mutableSetOf<Long>())
    var MessageCnt by value(mutableMapOf<Long, Long>())
}
