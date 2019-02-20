package `fun`.brush.viewmodel.adapter

import `fun`.brush.R
import `fun`.brush.model.bean.MovieBean.IssueListBean.ItemListBean
import `fun`.brush.model.bean.VideoBean
import `fun`.brush.view.custom.RoundImage
import `fun`.brush.viewmodel.adapter.VideoAdapter.ViewHolder
import android.content.Intent
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import cn.jzvd.JZMediaSystem
import cn.jzvd.JzvdStd
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.video_item.view.*

class VideoAdapter(private var videoList: ArrayList<ItemListBean>) :
        RecyclerView.Adapter<ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(LayoutInflater
            .from(parent.context).inflate(R.layout.video_item, parent, false))

    override fun getItemCount(): Int = videoList.size
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val context = holder.itemView.context
        val bean = videoList[position]
        val title = bean.data.title
        val photo = bean.data.cover?.feed
        val author = bean.data.author
        val category = bean.data.category
        val desc = bean.data.description
        val duration = bean.data.duration
        val playUrl = bean.data.playUrl
        val blurred = bean.data.cover?.blurred
        val collect = bean.data.consumption?.collectionCount
        val share = bean.data.consumption?.shareCount
        val reply = bean.data.consumption?.replyCount
        val time = System.currentTimeMillis()
        holder.itemView.startAnimation(AnimationUtils.loadAnimation(context,
                androidx.appcompat.R.anim.abc_grow_fade_in_from_bottom)) //底部渐隐动画
        val videoBean = VideoBean(photo, title, desc, duration, playUrl, category,
                blurred, collect, share, reply, time)
        holder.channelTxT.text = videoBean.category //频道
        Glide.with(context).load(photo).into(holder.videoPlayer.thumbImageView) //封面
        Glide.with(context).load(author!!.icon).into(holder.authorIcon) //作者头像
        holder.videoPlayer.thumbImageView.scaleType = ImageView.ScaleType.FIT_XY //去除播放器封面黑边
        //设置播放地址
        holder.videoPlayer.setUp(videoBean.playUrl, videoBean.title, JzvdStd.NORMAL_ORIENTATION)
        holder.videoPlayer.startWindowTiny() //小窗口播放
        JzvdStd.setMediaInterface(JZMediaSystem())
        holder.authorIcon.setOnClickListener {
            val popMenu = PopupMenu(context, it) //构造一个气泡菜单
            popMenu.inflate(R.menu.pop_menu_item) //展开菜单
            val popupMenuField = PopupMenu::class.java.getDeclaredField("mPopup") //通过反射获取原生字段
            popupMenuField.isAccessible = true //设置可访问权限
            val mPopup = popupMenuField.get(popMenu) //获取到对象
            mPopup.javaClass.getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                    .invoke(mPopup, true) //强制显示图标
            popMenu.menu.getItem(0).title = "喜欢\t${videoBean.collect}"
            popMenu.menu.getItem(1).title = "转发\t${videoBean.share}"
            popMenu.menu.getItem(2).title = "评论\t${videoBean.reply}"
            popMenu.setOnMenuItemClickListener { item: MenuItem ->
                when (item.itemId) {
                    R.id.like -> {
                    }
                    R.id.forward -> {
                        val intent = Intent()
                        intent.action = Intent.ACTION_SEND
                        intent.putExtra(Intent.EXTRA_TEXT, videoBean.playUrl)
                        intent.type = "text/plain"
                        context.startActivity(Intent.createChooser(intent, "分享视频给好友"))
                    }
                    R.id.comment -> {
                    }
                    R.id.download -> {
                        Snackbar.make(holder.itemView, "下载", 1000).show()
                    }
                }
                false
            }
            popMenu.show()
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val videoPlayer: JzvdStd = itemView.videoPlayer
        val authorIcon: RoundImage = itemView.authorIcon
        val channelTxT: TextView = itemView.channelTxT
    }
}