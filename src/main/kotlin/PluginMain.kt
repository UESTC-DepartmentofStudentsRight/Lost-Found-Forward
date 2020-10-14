@file:Suppress("unused")

package org.Reforward.mirai.plugin


import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.Bot.Companion.getInstance
import net.mamoe.mirai.Bot.Companion.getInstanceOrNull
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregister
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.command.getBotOrNull
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.utils.info
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.console.plugin.info
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.MessageChain
import java.awt.color.CMMException
import java.lang.Exception
import kotlin.math.log


val PluginID = "org.Reforward.mirai-plugin"
val PluginVersion = "0.0.1"

object PluginMain : KotlinPlugin(
    JvmPluginDescription(
        id = PluginID,
        version = PluginVersion
    )
) {
    override fun onEnable() {
        logger.info { "由权益小窝开发组出品。你的全心，我的权益！" }
        Mydata.reload()
        ForwardtheMsg()
    }

    override fun onDisable() {
        super.onDisable()
        logger.error("插件卸载!")
    }


    private fun commandRegister() {
        AddSenderId.register()
        AddGroupID.register()
        DelAllSenderId.register()
        DelSenderId.register()
        DelGroupId.register()
        DelAllGroupId.register()
        ShowAllGroup.register()
        ShowAllSenderId.register()
        ChangeBotId.register()
    }

    private fun commandUnregister() {
        AddSenderId.unregister()
        AddGroupID.unregister()
        DelAllSenderId.unregister()
        DelSenderId.unregister()
        DelGroupId.unregister()
        DelAllGroupId.unregister()
        ShowAllGroup.unregister()
        ShowAllSenderId.unregister()
        ChangeBotId.unregister()
    }

    private fun ForwardtheMsg() {
        subscribeGroupMessages {
            always {
                //PluginMain.logger.info("接收到了新的消息！")
                val id: Long = group.id
                val originGroup: Long = Mydata.originGroup
                logger.info("id = ${id}, oringinGroup = ${originGroup}")
                if (id == originGroup && sender.id in Mydata.senderid) {
                    //logger.info("准备发送")
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
    var groups: MutableSet<Long> by value(mutableSetOf<Long>())
    var senderid: MutableSet<Long> by value(mutableSetOf<Long>())
    var originGroup: Long by value(445786154L)
    var botId: Long by value(103833821L)
}

object AddSenderId : SimpleCommand(
    PluginMain, "AddSender",
    description = "添加允许转发的人的QQ号",
) {
    @Handler
    suspend fun CommandSender.addSender(Id: Long) {
        if (!Mydata.senderid.add(Id)) {
            PluginMain.logger.error("你已经添加过这名小窝了")
        } else {
            PluginMain.logger.info("添加小窝成功！")
        }
    }
}

object DelSenderId : SimpleCommand(
    PluginMain, "DelSender",
    description = "移除小窝的转发权限",
) {
    @Handler
    suspend fun CommandSender.delSender(Id: Long) {
        val tempBot = getInstanceOrNull(Mydata.botId)
        if(tempBot == null){
            PluginMain.logger.error("bot不存在")
        }
        else{
            if (!Mydata.senderid.remove(Id)) {
                PluginMain.logger.error("删除小窝${tempBot.getGroup(Mydata.originGroup).get(Id).nameCardOrNick}的权限失败")
            }
            else {
                PluginMain.logger.info("删除小窝${tempBot.getGroup(Mydata.originGroup).get(Id).nameCardOrNick}的权限成功")
            }
        }

    }
}

object DelAllSenderId : SimpleCommand(
    PluginMain, "DelAllSender",
    description = "移除所有小窝的转发权限",
) {
    @Handler
    suspend fun CommandSender.DelAllSender() {
        Mydata.senderid.clear()
        PluginMain.logger.info("删除所有小窝的权限成功")
    }
}

object ShowAllSenderId : SimpleCommand(
    PluginMain, "ShowAllSender",
    description = "查看所有拥有权限的小窝"
) {
    @Handler
    suspend fun CommandSender.ShowAllSender() {
        val tempBot = getInstanceOrNull(Mydata.botId)
        if(tempBot == null){
            PluginMain.logger.error("bot不存在")
        }
        else{
            var flag = true
            for (i in Mydata.senderid) {
                PluginMain.logger.info("${tempBot.getGroup(Mydata.originGroup).get(i).nameCardOrNick}拥有权限")
                flag = false
            }
            if (flag) PluginMain.logger.info("当前没有小窝拥有权限")
        }

    }
}

object AddGroupID : SimpleCommand(
    PluginMain, "AddGroup",
    description = "添加转发的群",
) {
    @Handler
    suspend fun CommandSender.addGroup(Id: Long) {
        if (!Mydata.groups.add(Id)) {
            PluginMain.logger.error("你已经添加过这群了！")
        } else {
            PluginMain.logger.info("添加转发的群成功！")
        }
    }
}

object DelGroupId: SimpleCommand(
    PluginMain, "DelGroup",
    description = "删除转发群组"
){
    @Handler
    suspend fun CommandSender.DelGroup(Id: Long){
        if(!Mydata.groups.remove(Id)){
            PluginMain.logger.error("删除群${Id}失败")
        }
        else{
            PluginMain.logger.info("删除群${Id}成功")
        }

    }
}

object DelAllGroupId : SimpleCommand(
    PluginMain, "DelAllGroup",
    description = "删除所有转发的群",
){
    @Handler
    suspend fun CommandSender.DelAllGroup(){
        Mydata.groups.clear()
        PluginMain.logger.info("删除所有转发群组成功")
    }
}

object ShowAllGroup : SimpleCommand(
    PluginMain, "ShowAllGroup",
    description = "查看目前的转发群列表"
){
    @Handler
    suspend fun CommandSender.ShowAllGroup(){
        val tempBot = getInstanceOrNull(Mydata.botId)
        if(tempBot == null){
            PluginMain.logger.error("bot不存在")
        }
        else{
            if(Mydata.groups.isEmpty()){
                PluginMain.logger.info("群组列表为空")
            }
            else{
                for(i in Mydata.groups){
                    PluginMain.logger.info("群名:${tempBot.getGroup(i).name},群号:${i}")
                }
            }
        }
    }
}

object ChangeBotId : SimpleCommand(
    PluginMain, "ChangeBot",
    description = "在配置中改变bot的qq号"
){
    @Handler
    suspend fun CommandSender.ChgBotId(Id: Long){
        Mydata.botId = Id
        PluginMain.logger.info("改变Bot的qq号为：${Id}")
    }
}