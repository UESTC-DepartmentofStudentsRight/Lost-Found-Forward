package org.Reforward.mirai.plugin

import net.mamoe.mirai.console.extension.PluginComponentStorage
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.utils.info

object PluginMain : KotlinPlugin(
    JvmPluginDescription(
        id = "org.Reforward.mirai.plugin",
        version = "0.0.1"
    )
) {

    override fun onEnable() {
        logger.info { "Welcome to 权益小窝‘s 转发插件" }
    }

    override fun onLoad() {
        super.onLoad()
        TODO("Not yet implemented")
    }

    override fun onDisable() {
        super.onDisable()
    }
}