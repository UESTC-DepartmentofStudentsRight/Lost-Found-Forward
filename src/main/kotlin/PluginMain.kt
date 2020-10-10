package org.example.mirai.plugin

import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.utils.info
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.value
import org.jetbrains.kotlinx.serialization.compiler.backend.jvm.ARRAY

object PluginMain : KotlinPlugin(
    JvmPluginDescription(
        id = "org.example.mirai-plugin",
        version = "0.1.0"
    )
) {
    override fun onEnable() {
        logger.info { "Writen by QYXW " }
    }
}

object Mydata : AutoSavePluginConfig("Empty") {
    val groups: Array<String> by value(emptyArray<String>())


}