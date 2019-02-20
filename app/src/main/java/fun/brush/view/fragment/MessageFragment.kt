package `fun`.brush.view.fragment

import `fun`.brush.R
import `fun`.brush.model.bean.User
import `fun`.brush.viewmodel.adapter.MessageAdapter
import android.R.color.*
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import cn.bmob.newim.BmobIM
import cn.bmob.newim.listener.ConnectListener
import cn.bmob.v3.BmobUser
import cn.bmob.v3.exception.BmobException
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_message.*

class MessageFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState:
    Bundle?) = inflater.inflate(R.layout.fragment_message, container, false)!!

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        refreshMessage.setOnRefreshListener {
            loadConversation()
            noMessagePic.visibility = INVISIBLE
        }
        refreshMessage.setColorSchemeResources(holo_blue_light, holo_purple, holo_green_light)
        val userInfo = BmobUser.getCurrentUser(User::class.java)
        if (userInfo == null) Snackbar.make(messageRecycler, "请先登录后聊天", 1000).show()
        if (userInfo.objectId.isNotEmpty()) {
            BmobIM.connect(userInfo.objectId, object : ConnectListener() {
                override fun done(userID: String?, exception: BmobException?) {
                    if (exception == null) {
                        loadConversation()
                    } else {
                        Snackbar.make(messageRecycler, "服务器连接失败\uD83D\uDE14", 1000).show()
                    }
                }
            })
        }
    }

    private fun loadConversation() {
        val conversations = BmobIM.getInstance().loadAllConversation()
        when {
            conversations.size == 0 -> { //没有人发消息给你
                Snackbar.make(refreshMessage, "没有消息了", 1000).show()
                Handler().postDelayed({ noMessagePic.visibility = View.VISIBLE }, 300)
                refreshMessage.isRefreshing = false
            }
            conversations[0] != null -> { //有人发消息给你了
                messageRecycler.layoutManager = LinearLayoutManager(context)
                messageRecycler.adapter = MessageAdapter(conversations, activity!!)
                refreshMessage.isRefreshing = false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshMessage.isRefreshing = true
        Handler().postDelayed({ refreshMessage.isRefreshing = false }, 500)
    }
}
