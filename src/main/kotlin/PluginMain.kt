@file:Suppress("unused")

package org.Reforward.mirai.plugin


import com.google.auto.service.AutoService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.mamoe.mirai.Bot
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.utils.info
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.*
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageRecallEvent
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.BotConfiguration
import java.text.SimpleDateFormat
import java.util.*
import java.util.Arrays.equals
import java.util.concurrent.atomic.AtomicInteger
import java.util.regex.Pattern


const val PluginID = "org.Forward.mirai.plugin"
const val PluginVersion = "1.0.6"
val bot = BotFactory.newBot(Config.botId, Config.botPwd) {
    protocol = BotConfiguration.MiraiProtocol.ANDROID_PAD
}




@AutoService(KotlinPlugin::class)
object PluginMain : KotlinPlugin(
    JvmPluginDescription(
        id = PluginID,
        name = "lost&found",
        version = PluginVersion
    )
) {
    private var cacheMessage = Collections.synchronizedMap(mutableMapOf<IntArray, MutableSet<MessageReceipt<Group>>>())
    private var date = SimpleDateFormat("yyyy-MM-dd").format(Date())

    @ConsoleExperimentalApi
    override fun onEnable() {
        logger.info { "由权益小窝开发组出品。你的全心，我的权益！" }
        Config.reload()
        CommandRegister.commandRegister()
        PluginMain.launch {
            autoLogin()
        }
        forwardMsg()
        replyTempMsg()
        subscribeRecall()
    }

    override fun onDisable() {
        super.onDisable()
        CommandRegister.commandUnregister()
        logger.error("插件卸载!")
    }

    /**
     *自动登录模块
     *在[onEnable]时调用，可以自动登录机器人（修改后）
     */
    @ConsoleExperimentalApi
    suspend fun autoLogin(): Bot? {
        bot.login()
        timeAction()
        return null
    }

    /**
     * 监听失物招领管理员群消息模块，可以在监听到新的消息后自动转发到失物招领群里边，其中：
     * message.forEachContent为遍历所有消息内容[MessageContent]（有关消息元[MessageMetadata]与消息内容[MessageContent]的区别与关系请见文档）
     * 字符串处理将消息链[MessageChain]中的转发关键字符“#”删除后整体转发
     * 若检测到#recall关键字，则撤回回复消息[QuoteReply]的消息源[MessageSource]所转发到失物招领群的消息 （见[msgRecall]）
     */



    private fun forwardMsg() {
        bot.eventChannel.subscribeGroupMessages {
            always {
                logger.debug("接收到了新的消息！")
                val id: Long = group.id
                val originGroup: Long = Config.originGroup
                if (id == originGroup && sender.id in Config.senderid) {
                    val messageChainBuilder = MessageChainBuilder()
                    if (message[QuoteReply] == null && message.contentToString()[0] == '#' && message.contentToString().length > 1) {
                        message.forEach {
                            if (it is PlainText) {
                                messageChainBuilder.add(it.content.replaceFirst("#".toRegex(), ""))
                                return@forEach
                            }
                            messageChainBuilder.add(it)
                        }
                        send(messageChainBuilder.asMessageChain(), bot, message.ids)
                        if (Data.MessageCnt[sender.id] == null) {
                            Data.MessageCnt[sender.id] = mutableSetOf()
                        }
                        Data.MessageCnt[sender.id]!!.add(message.ids)
                        logger.debug("${sender.nameCardOrNick}的条数为${Data.MessageCnt[sender.id]!!.size}")
                        bot.getGroup(originGroup)?.sendMessage("失物招领已转发！")
                        return@always
                    } else if (message[QuoteReply] != null && Pattern.matches(
                            ".*#recall.*",
                            message.findIsInstance<PlainText>().toString()
                        )
                    ) {
                        logger.debug("开始处理撤回！")
                        val cnt = Data.MessageCnt[sender.id]
                        //如果不是本人发送的，则不处理
                        cnt?.contains(message[QuoteReply]!!.source.ids) ?: return@always
                        val msgID = message[QuoteReply]!!.source.ids
                        msgRecall(msgID)
                        Data.MessageCnt[sender.id]!!.remove(message.ids)
                        bot.getGroup(originGroup)?.sendMessage("失物招领已撤回")
                        logger.info("${sender.nameCardOrNick}的条数为${Data.MessageCnt[sender.id]!!.size}")
                    }
                }
            }
        }
    }

    /**
     * 监听失物招领管理员群消息模块，可以在监听到新的消息后自动转发到失物招领群里边，其中：
     * message.forEachContent为遍历所有消息内容[MessageContent]（有关消息元[MessageMetadata]与消息内容[MessageContent]的区别与关系请见文档）
     * 字符串处理将消息链[MessageChain]中的转发关键字符“#”删除后整体转发
     * 若检测到#recall关键字，则撤回回复消息[QuoteReply]的消息源[MessageSource]所转发到失物招领群的消息 （见[msgRecall]）
     */
    private val mutex = Mutex()

    private fun replyTempMsg() {
        bot.eventChannel.subscribeGroupTempMessages {
            always {
                logger.debug("接收到了一个临时会话")
                val id: Long = sender.id
                if (id in Data.messagecontact.values) {
                    for (iter in Data.messagecontact) {
                        if (iter.value == id) {
                            bot.getFriend(iter.key)?.sendMessage(message)
                        }
                    }
                } else {
                   mutex.withLock {
                       var flag = true
                       val newOrderSender =
                           Collections.synchronizedList(Config.senderid.sortedBy { Data.MessageCnt[it]?.size ?: 0 })
                       while (flag) {
                           for (Mem in newOrderSender) {
                               if (Data.messagecontact[Mem] == null) {
                                   sender.sendMessage(
                                       "正在为同学接入管理员，请稍后"
                                   )
                                   Data.messagecontact[Mem] = id
                                   bot.getFriend(Mem)?.sendMessage("本消息来自于${group.name}, 同学的QQ号为${sender.id}")
                                   PluginMain.logger.debug(
                                       "本消息来自于${group.name}, 同学的QQ号为${sender.id}，管理员为${
                                           bot.getFriend(
                                               Mem
                                           )?.nameCardOrNick
                                       }"
                                   )
                                   bot.getFriend(Mem)?.sendMessage("这是一个新的对话，结束时请输入英文的 #stop 结束")
                                   bot.getFriend(Mem)?.sendMessage(message)
                                   sender.sendMessage("已与管理员建立对话，请同学继续发送消息")
                                   flag = false
                                   break
                               }
                           }
                           if (flag)
                               delay(3000L)
                       }
                   }
                }
            }
        }
        /**
         * 自动转发管理员回复的临时消息内容，文档撰写ing
         */

        bot.eventChannel.subscribeFriendMessages {
            always {
                if (sender.id in Config.senderid && Data.messagecontact[sender.id] != null) {
                    val receiver = bot.groups.asSequence().flatMap { it.members.asSequence() }
                        .firstOrNull { it.id == Data.messagecontact[sender.id] }
                    if (Pattern.matches(
                            ".*#stop.*",
                            message.findIsInstance<PlainText>().toString())
                    ){
                        bot.getFriend(sender.id)?.sendMessage("已经结束此对话，可以接入下一个同学的失物招领！")
                        receiver?.sendMessage("管理员已经断开会话，如果有需要请继续发消息，会为同学转接新的管理员！")
                        Data.messagecontact.remove(sender.id)
                        return@always
                    }
                    logger.info("同学的id为${receiver!!.id},管理员为${bot.getFriend(sender.id)?.nameCardOrNick}")
                    receiver.sendMessage(message)
                }
            }
        }
    }


    /**
     *监听撤回消息（用于两分钟以内的消息）
     */
    private fun subscribeRecall() {
        bot.eventChannel.subscribeAlways<MessageRecallEvent.GroupRecall> {
            logger.debug("监听到撤回事件")
            if (authorId in Config.senderid && group.id == Config.originGroup && Data.MessageCnt[authorId] != null) {
                logger.debug("准备撤回群内消息！")
                Data.MessageCnt[authorId]!!.remove(messageIds)
                msgRecall(messageIds)
            }
        }

        bot.eventChannel.subscribeAlways<GroupMessageEvent> {
            logger.debug("监听到来自${group.name}的消息, 发送人是${senderName}")
        }
    }

    /**
     * 将记录在[cacheMessage]内的消息回执[MessageReceipt]逐个执行[MessageReceipt.recall]，
     * 从而撤回所有失物招领群内的本条失物招领
     */
    private suspend fun msgRecall(messageIDs: IntArray) {
        var recallmessage = mutableSetOf<MessageReceipt<Group>>()
        while (recallmessage.isEmpty()) {
            for (i in cacheMessage.keys) {
                if (equals(i, messageIDs)) {
                    while (recallmessage.size != Config.groups.size) {
                        delay(1000L)
                        recallmessage = cacheMessage.getValue(i)
                    }
                    break
                }
            }
        }
        for (msg in recallmessage) {
            launch {
                val time: Long = (2000L..15000L).random()
                msg.recallIn(time)
            }
        }
    }

    /**
     * 将失物招领由管理员群[Config.originGroup]转发到失物招领群[Config.groups]内
     */
    private fun send(messagechain: MessageChain, bot: Bot, messageIDs: IntArray) {
        val groups = Config.groups
        val cnt = AtomicInteger(0)
        val cacheReceipt = Collections.synchronizedSet(mutableSetOf<MessageReceipt<Group>>())
        for (id: Long in groups) {
            launch {
                val time: Long = (2000L..15000L).random()
                delay(time)
                cnt.incrementAndGet()
                cacheReceipt.add(bot.getGroup(id)?.sendMessage(messagechain))
                if (cnt.toInt() == Config.groups.size) {
                    cacheMessage[messageIDs] = cacheReceipt
                }
            }
        }
    }

    /**
     * 每日清理消息回执[MessageReceipt]以及劳模统计
     */
    @ConsoleExperimentalApi
    private suspend fun timeAction() {
        while (true) {
            if (date == SimpleDateFormat("yyyy-MM-dd").format((Date()))) {
                delay(600000L)
                continue
            }
            date = SimpleDateFormat("yyyy-MM-dd").format(Date())
            bot.getGroup(Config.originGroup)?.sendMessage("将开始清理撤回列表以及统计劳模")
            delay(10000L)
            cacheMessage.clear()
            bot.getGroup(Config.originGroup)?.sendMessage("撤回列表清理完毕,小窝将无法撤回之前的消息")
            delay(4000L)
            val tot = mutableMapOf<Long, Int>()
            var flag = 0
            for (it in Data.MessageCnt) {
                if (it.value.size > flag) {
                    flag = it.value.size
                    tot.clear()
                    tot[it.key] = it.value.size
                } else if (it.value.size == flag) {
                    tot[it.key] = it.value.size
                }

            }
            bot.getGroup(Config.originGroup)?.sendMessage(
                "今日的劳模是:"
            )
            for (it in tot) {
                val sender = bot.getFriend(it.key)
                bot.getGroup(Config.originGroup)?.sendMessage("${sender?.nameCardOrNick}, 条数共${it.value}条")
            }
            Data.MessageCnt.clear()
        }
    }
}

object Config : AutoSavePluginConfig("Groups") {
    /**
     * 失物招领管理员群[groups]
     */
    var groups by value(mutableSetOf<Long>())

    /**
     * 失物招领管理员[senderid]
     */
    var senderid by value(mutableSetOf<Long>())

    /**
     * 失物招领管理员群[originGroup]
     */
    var originGroup by value<Long>()

    /**
     * 机器人QQ号
     */
    var botId by value<Long>()

    /**
     * 机器人密码
     */
    var botPwd by value<String>()



    var url by value<String>()

    var user by value<String>()

    var password by value<String>()
}

object Data : AutoSavePluginData("bot") {
    /**
     * 转发的失物招领条数[MessageCnt]
     */
    var MessageCnt by value(mutableMapOf<Long, MutableSet<IntArray>>())

    /**
     * 失物招领对话记录[messagecontact]
     */
    var messagecontact by value(mutableMapOf<Long, Long>())

}
