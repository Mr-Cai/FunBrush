package `fun`.brush.view.activity.message

import `fun`.brush.R
import `fun`.brush.model.bean.User
import `fun`.brush.viewmodel.util.Const.Companion.USER_ID
import `fun`.brush.viewmodel.util.Const.Companion.USER_NAME
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import cn.bmob.newim.BmobIM
import cn.bmob.newim.bean.BmobIMConversation
import cn.bmob.newim.bean.BmobIMMessage
import cn.bmob.newim.bean.BmobIMTextMessage
import cn.bmob.newim.bean.BmobIMUserInfo
import cn.bmob.newim.core.BmobIMClient
import cn.bmob.newim.core.ConnectionStatus.CONNECTED
import cn.bmob.newim.listener.ConversationListener
import cn.bmob.newim.listener.MessageSendListener
import cn.bmob.v3.BmobQuery
import cn.bmob.v3.BmobUser
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.QueryListener
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.chat_send_msg.view.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.text.SimpleDateFormat.*

class ChatActivity : AppCompatActivity() {
    private lateinit var chatUserName: String
    private var chatAdapter = GroupAdapter<ViewHolder>()
    private var msgManager: BmobIMConversation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        chatUserName = intent.getStringExtra(USER_NAME)
        peopleNameTxT.text = String.format(getString(R.string.chat_user), chatUserName)
        val currentUser = BmobUser.getCurrentUser(User::class.java)
        if (BmobIM.getInstance().currentStatus.code == CONNECTED.code) { //服务器连上了
            chatRecycler.adapter = chatAdapter
            chatRecycler.layoutManager = LinearLayoutManager(this)
            sendButton.setOnClickListener { sendMessage() }
        } else {
            Snackbar.make(sendButton, "服务器连接失败\uD83D\uDE14", 1000).show()
        }
    }

    private fun sendMessage() {
        val text = sendTextET.text.toString()
        if (text.isEmpty()) {
            Snackbar.make(sendButton, "请先打几个字再发送吧", 1000).show()
            return
        }
        val info = BmobIMUserInfo()
        info.name = chatUserName
        info.userId = intent.getStringExtra(USER_ID)
        BmobIM.getInstance().startPrivateConversation(info, object : ConversationListener() {
            override fun done(imConversation: BmobIMConversation?, exception: BmobException?) {
                if (exception == null) {
                    msgManager = BmobIMConversation.obtain(BmobIMClient.getInstance(), imConversation)
                    val textMsg = BmobIMTextMessage()
                    textMsg.content = text
                    msgManager!!.sendMessage(textMsg, object : MessageSendListener() {
                        override fun done(message: BmobIMMessage?, exception: BmobException?) {
                            if (exception == null) {
                                sendTextET.text = null
                                val conversation = BmobIM.getInstance().loadAllConversation()
                                chatAdapter.add(SendItem(conversation[chatAdapter.itemCount]))
                            } else {
                                Snackbar.make(sendButton, "发送失败${exception.message}", 1000).show()
                            }
                        }
                    })
                } else {
                    Snackbar.make(sendButton, "开启会话出错:${exception.message}", 1000).show()
                }
            }
        })
    }

    inner class SendItem(private var message: BmobIMConversation) : Item<ViewHolder>() {
        override fun getLayout() = R.layout.chat_send_msg
        override fun bind(holder: ViewHolder, position: Int) {
            val context = holder.itemView.context
            val itemView = holder.itemView
            BmobQuery<User>().getObject(message.conversationId, object : QueryListener<User>() {
                override fun done(user: User?, exception: BmobException?) {
                    Glide.with(context).load(user!!.avatar!!.fileUrl).into(itemView.avatarSend)
                }
            })
            itemView.sendMessage.text = message.messages[position].content
            itemView.timestampSend.text = DateFormat.getDateTimeInstance().format(message.updateTime)
        }
    }

    inner class ReceiveItem(private var messageSet: BmobIMConversation) : Item<ViewHolder>() {
        override fun getLayout() = R.layout.chat_receive_msg
        override fun bind(holder: ViewHolder, position: Int) {
            val context = holder.itemView.context
            val itemView = holder.itemView
            BmobQuery<User>().getObject(messageSet.conversationId, object : QueryListener<User>() {
                override fun done(user: User?, exception: BmobException?) {
                    Glide.with(context).load(user!!.avatar!!.fileUrl).into(itemView.avatarSend)
                }
            })
            itemView.sendMessage.text = messageSet.messages[0].content
            itemView.timestampSend.text = messageSet.updateTime.toString()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) finish()
        return super.onOptionsItemSelected(item)
    }
}
