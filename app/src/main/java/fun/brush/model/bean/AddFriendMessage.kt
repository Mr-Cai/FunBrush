package `fun`.brush.model.bean

import `fun`.brush.viewmodel.util.Const.Companion.STATUS_VERIFY_NONE
import android.text.TextUtils
import cn.bmob.newim.bean.BmobIMExtraMessage
import cn.bmob.newim.bean.BmobIMMessage
import org.json.JSONObject

class AddFriendMessage : BmobIMExtraMessage() { //自定义消息类型，用于发送添加好友请求
    override fun getMsgType() = ADD
    override fun isTransient() = true //暂态消息不存入本地

    companion object {
        const val ADD = "ADD"   // 自定义添加好友的消息类型
        fun convert(msg: BmobIMMessage): NewFriend { //将普通消息转成好友请求
            val add = NewFriend()
            val content = msg.content
            add.msg = content
            add.time = msg.createTime
            add.status = STATUS_VERIFY_NONE
            try {
                val extra = msg.extra
                if (!TextUtils.isEmpty(extra)) {
                    val json = JSONObject(extra)
                    val name = json.getString("name")
                    add.name = name
                    val avatar = json.getString("avatar")
                    add.avatar = avatar
                    add.uid = json.getString("uid")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return add
        }
    }
}
