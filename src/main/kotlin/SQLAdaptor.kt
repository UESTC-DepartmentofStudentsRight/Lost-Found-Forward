package org.Reforward.mirai.plugin


import net.mamoe.mirai.contact.isFriend
import java.sql.DriverManager

object MysqlManager {
    fun sqlDriverInit() {
        try {
            Class.forName("com.mysql.jdbc.Driver")
        } catch (e : Exception) {
            e.printStackTrace()
            PluginMain.logger.error("Database Driver loading error")
        }

        try {
            DriverManager.getConnection(Config.url, Config.user, Config.password)
            PluginMain.logger.info("Database loading successful")
        } catch (e : Exception) {
            e.printStackTrace()
            PluginMain.logger.error("Database loading error")
        }
    }

    fun sqlInit() {
        val administrators = bot.getGroupOrFail(Config.originGroup).members.filter { it.isFriend }

        /*
            此处初始化数据库（1. 判别是否有重名table 2. 初始化table）
         */

        for(admin in administrators) {
            /*
                将管理员作为主键插入table
             */
        }
    }
}