@file:Suppress("unused")

package org.Reforward.mirai.plugin

import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregister
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.contact.nameCardOrNick


object CommandRegister {
    internal fun commandRegister() {
        AddSenderId.register()
        AddGroupID.register()
        DelAllSenderId.register()
        DelSenderId.register()
        DelGroupId.register()
        DelAllGroupId.register()
        ShowAllGroup.register()
        ShowAllSenderId.register()
        ChangeOriginGroup.register()
        ShowOriginGroup.register()
    }

    internal fun commandUnregister() {
        AddSenderId.unregister()
        AddGroupID.unregister()
        DelAllSenderId.unregister()
        DelSenderId.unregister()
        DelGroupId.unregister()
        DelAllGroupId.unregister()
        ShowAllGroup.unregister()
        ShowAllSenderId.unregister()
        ChangeOriginGroup.unregister()
        ShowOriginGroup.unregister()
    }
}

object AddSenderId : SimpleCommand(
    PluginMain, "AddSender",
    description = "添加允许转发的人的QQ号",
) {
    @Handler
    fun CommandSender.addSender(Id: Long) {
        if (!Config.senderid.add(Id)) {
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
    fun CommandSender.delSender(Id: Long) {
        val tempBot = Bot.getInstanceOrNull(Config.botId)
        if (tempBot == null) {
            PluginMain.logger.error("bot不存在")
        } else {
            if (!Config.senderid.remove(Id)) {
                PluginMain.logger.error("删除小窝${tempBot.getGroup(Config.originGroup).get(Id).nameCardOrNick}的权限失败")
            } else {
                PluginMain.logger.info("删除小窝${tempBot.getGroup(Config.originGroup).get(Id).nameCardOrNick}的权限成功")
            }
        }

    }
}

object DelAllSenderId : SimpleCommand(
    PluginMain, "DelAllSender",
    description = "移除所有小窝的转发权限",
) {
    @Handler
    fun CommandSender.DelAllSender() {
        Config.senderid.clear()
        PluginMain.logger.info("删除所有小窝的权限成功")
    }
}

object ShowAllSenderId : SimpleCommand(
    PluginMain, "ShowAllSender",
    description = "查看所有拥有权限的小窝"
) {
    @Handler
    fun CommandSender.ShowAllSender() {
        val tempBot = Bot.getInstanceOrNull(Config.botId)
        if (tempBot == null) {
            PluginMain.logger.error("bot不存在")
        } else {
            var flag = true
            for (i in Config.senderid) {
                PluginMain.logger.info("${tempBot.getGroup(Config.originGroup).get(i).nameCardOrNick}拥有权限")
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
    fun CommandSender.addGroup(Id: Long) {
        if (!Config.groups.add(Id)) {
            PluginMain.logger.error("你已经添加过这群了！")
        } else {
            PluginMain.logger.info("添加转发的群成功！")
        }
    }
}

object DelGroupId : SimpleCommand(
    PluginMain, "DelGroup",
    description = "删除转发群组"
) {
    @Handler
    fun CommandSender.DelGroup(Id: Long) {
        if (!Config.groups.remove(Id)) {
            PluginMain.logger.error("删除群${Id}失败")
        } else {
            PluginMain.logger.info("删除群${Id}成功")
        }

    }
}

object DelAllGroupId : SimpleCommand(
    PluginMain, "DelAllGroup",
    description = "删除所有转发的群",
) {
    @Handler
    fun CommandSender.DelAllGroup() {
        Config.groups.clear()
        PluginMain.logger.info("删除所有转发群组成功")
    }
}

object ShowAllGroup : SimpleCommand(
    PluginMain, "ShowAllGroup",
    description = "查看目前的转发群列表"
) {
    @Handler
    fun CommandSender.ShowAllGroup() {
        val tempBot = Bot.getInstanceOrNull(Config.botId)
        if (tempBot == null) {
            PluginMain.logger.error("bot不存在")
        } else {
            if (Config.groups.isEmpty()) {
                PluginMain.logger.info("群组列表为空")
            } else {
                for (i in Config.groups) {
                    PluginMain.logger.info("群名:${tempBot.getGroup(i).name},群号:${i}")
                }
            }
        }
    }
}


object ChangeOriginGroup : SimpleCommand(
    PluginMain, "ChangeOriginGroup",
    description = "更改失物招领管理员群"
) {
    @Handler
    fun CommandSender.ChangeOriginGroup(Id: Long) {
        Config.originGroup = Id
        PluginMain.logger.info("已修改失物招领管理员群为: $Id")
    }
}

object ShowOriginGroup : SimpleCommand(
    PluginMain, "ShowOriginGroup",
    description = "显示当前失物招领管理员群群号"
) {
    @Handler
    fun CommandSender.ShowOriginGroup() {
        PluginMain.logger.info("当前失物招领管理员群为: ${Config.originGroup}")
    }
}