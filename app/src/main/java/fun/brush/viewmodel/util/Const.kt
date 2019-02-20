package `fun`.brush.viewmodel.util

import android.app.Application
import cn.bmob.newim.BmobIM
import cn.bmob.newim.event.MessageEvent
import cn.bmob.newim.event.OfflineMessageEvent
import cn.bmob.newim.listener.BmobIMMessageHandler
import cn.bmob.v3.Bmob
import org.greenrobot.eventbus.EventBus
import java.io.BufferedReader
import java.io.File
import java.io.FileReader


@Suppress("unused")
class FunBrush : Application() {
    override fun onCreate() {
        super.onCreate()
        Bmob.initialize(this, "f7426d37bc76d0da45471d521b00218e")
        if (applicationInfo.packageName == getMyProcessName()) {
            BmobIM.init(this)
            BmobIM.registerDefaultMessageHandler(DemoMessageHandler())
        }
    }

    //获取当前运行的进程名
    private fun getMyProcessName() = try {
        val file = File("/proc/" + android.os.Process.myPid() + "/" + "cmdline")
        val mBufferedReader = BufferedReader(FileReader(file))
        val processName = mBufferedReader.readLine().trim { it <= ' ' }
        mBufferedReader.close()
        processName
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

//自定义消息接收器处理在线消息和离线消息
class DemoMessageHandler : BmobIMMessageHandler() {
    override fun onMessageReceive(event: MessageEvent?) { //在线消息
        EventBus.getDefault().post(event)
    }

    override fun onOfflineReceive(event: OfflineMessageEvent?) { //离线消息
        val map = event!!.eventMap
        map.forEach { (_, list) ->
            val size = list.size
            for (i in 0 until size) {
                EventBus.getDefault().post(list[i])
            }
        }
    }
}


class Const {
    companion object { //常量作为传参的键名或者占位的请求码或者是消息编号
        const val PICK = 0xaaa  //选择照片请求码
        const val STORAGE = 0xaaa  //读写存储权限
        const val PRINT = "打印" //日志打印标签
        const val URL = "URL"
        const val TITLE = "TITLE"
        const val PIC = "PIC"
        const val USER_ID = "用户编号"
        const val USER_NAME = "用户名"
        const val AVATAR = "头像"
        const val CHAT = "聊天"
        const val CALL = 0xccc //打电话
        const val IS_LOG_OUT = "退出登录"
        const val DEBUG = true //是否是debug模式
        const val STATUS_VERIFY_NONE = 0 //接收验证好友请求初始状态
        const val STATUS_VERIFY_READED = 2 //接收验证好友请求已读状态
        const val STATUS_VERIFIED = 1 //好友请求：已添加
    }
}
