@file:Suppress("unused")
package org.Reforward.mirai.plugin

import net.mamoe.mirai.alsoLogin
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.enable
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.load
import net.mamoe.mirai.console.terminal.MiraiConsoleTerminalLoader
import net.mamoe.mirai.console.util.ConsoleExperimentalApi

@ConsoleExperimentalApi
suspend fun main() {

    MiraiConsoleTerminalLoader.startAsDaemon()

    PluginMain.load()
    PluginMain.enable()

    val bot = MiraiConsole.addBot(103833821, "qyxw0521") {
        fileBasedDeviceInfo()
    }.alsoLogin()

    MiraiConsole.job.join()
}