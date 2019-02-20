package `fun`.brush.viewmodel.adapter

import `fun`.brush.R
import `fun`.brush.model.bean.PullMSG
import `fun`.brush.view.activity.message.ChatActivity
import `fun`.brush.viewmodel.util.Const.Companion.USER_NAME
import `fun`.brush.viewmodel.util.Tool
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.publish_item.view.*

class PublishAdapter(private var postSet: MutableList<PullMSG>) :
        RecyclerView.Adapter<PublishAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(LayoutInflater
            .from(parent.context).inflate(R.layout.publish_item, parent, false))

    override fun getItemCount(): Int = postSet.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val context = holder.itemView.context //当前上下文
        val itemView = holder.itemView //当前列表项
        Tool.setImageToView(holder.itemView.context, postSet[position].name,
                null, holder.itemView.avatarPic) //拉取的头像
        itemView.userNameTxT.text = postSet[position].name //发送者用户名
        itemView.titleTxT.text = postSet[position].title //拉取的标题
        itemView.timeTxT.text = postSet[position].time //拉取的更新时间
        itemView.sendTxT.text = postSet[position].content //拉取的想法
        Glide.with(context).load(postSet[position].image).into(itemView.sendPic) //拉取的图片
        itemView.locationTxT.text = "常州市武进区常州信息"
        itemView.avatarPic.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java)
//                intent.putExtra(USER_ID, postSet[position].author!!.objectId)
                intent.putExtra(USER_NAME, postSet[position].name)
            context.startActivity(intent)
        }

    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}