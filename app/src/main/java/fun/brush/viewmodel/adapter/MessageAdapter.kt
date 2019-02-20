package `fun`.brush.viewmodel.adapter

import `fun`.brush.R
import `fun`.brush.model.bean.User
import `fun`.brush.view.activity.message.ChatActivity
import `fun`.brush.viewmodel.util.Const.Companion.USER_ID
import `fun`.brush.viewmodel.util.Const.Companion.USER_NAME
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.bmob.newim.bean.BmobIMConversation
import cn.bmob.v3.BmobQuery
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.QueryListener
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.message_item.view.*
import java.text.SimpleDateFormat.getTimeInstance

class MessageAdapter(private var messageSet: List<BmobIMConversation>, val context: Context) :
        RecyclerView.Adapter<MessageAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(LayoutInflater
            .from(parent.context).inflate(R.layout.message_item, parent, false))

    override fun getItemCount() = messageSet.size
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val conversation = messageSet[position]
        val itemView = holder.itemView
        BmobQuery<User>().getObject(conversation.conversationId, object : QueryListener<User>() {
            override fun done(user: User?, exception: BmobException?) {
                Glide.with(context).load(user!!.avatar!!.fileUrl).into(itemView.userIconPic)
            }
        })
        itemView.nickNameTxT.text = conversation.conversationTitle
        itemView.messageTxT.text = conversation.messages[0].content
        itemView.receiveTimeTxT.text = getTimeInstance().format(conversation.updateTime)
        if (conversation.unreadCount == 0) {
            itemView.unReadCountTxT.visibility = View.GONE
        } else {
            itemView.unReadCountTxT.text = conversation.unreadCount.toString()
        }
        itemView.setOnClickListener {
            context.startActivity(Intent(context, ChatActivity::class.java)
                    .putExtra(USER_NAME, conversation.conversationTitle)
                    .putExtra(USER_ID, conversation.conversationId))
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}