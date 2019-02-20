package `fun`.brush.viewmodel.adapter

import `fun`.brush.R
import `fun`.brush.view.activity.WebActivity
import `fun`.brush.viewmodel.util.Const.Companion.PIC
import `fun`.brush.viewmodel.util.Const.Companion.TITLE
import `fun`.brush.viewmodel.util.Const.Companion.URL
import `fun`.brush.model.bean.NewsBean.News
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.news_item.view.*

class NewsAdapter(private val news: ArrayList<News>) : //构造器传入新闻实体集
        RecyclerView.Adapter<NewsAdapter.ViewHolder>() { //继承循环视图适配器覆盖方法
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(LayoutInflater
            .from(parent.context).inflate(R.layout.news_item, parent, false)) //返回列表项视图

    override fun getItemCount(): Int = news.size //数据集大小
    override fun onBindViewHolder(holder: ViewHolder, position: Int) { //绑定固定器
        val context = holder.itemView.context
        holder.titleTxT.text = news[position].title //从实体类数据集的位置中获取标题
        holder.classifyTxT.text = news[position].description //获取分类名
        holder.timeTxT.text = news[position].ctime //获取更新时间
        Glide.with(context).load(news[position].picUrl) // 滑翔库用来加载图片
                .apply(RequestOptions().placeholder(R.mipmap.ic_launcher)) //占位图
                .into(holder.thumbnailPic)
        holder.itemView.startAnimation(AnimationUtils
                .loadAnimation(context, android.R.anim.slide_in_left)) //加载左滑入动画
        holder.itemView.setOnClickListener {
            val intent = Intent(context, WebActivity::class.java)
            intent.putExtra(URL, news[position].url) //摆放新闻链接
            intent.putExtra(TITLE, news[position].title) //摆放新闻标题
            intent.putExtra(PIC, news[position].picUrl) //摆放封面链接
            context.startActivity(intent) //通过意图传参并跳转活动
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) { //实例化需要调用的控件
        val titleTxT: TextView = itemView.titleTxT
        val classifyTxT: TextView = itemView.classifyTxT
        val timeTxT: TextView = itemView.timeTxT
        val thumbnailPic: ImageView = itemView.thumbnailPic
        val view: View = itemView.view
    }

    fun attachView(view: RecyclerView) = ItemTouchHelper(object : ItemTouchHelper.Callback() {
        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int =
                ItemTouchHelper.Callback.makeMovementFlags(UP or DOWN, START or END)

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target:
        RecyclerView.ViewHolder): Boolean {
            notifyItemMoved(viewHolder.adapterPosition, target.adapterPosition)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) { //交换列表项位置
            news.removeAt(viewHolder.adapterPosition) //移除指定位置选项
            notifyItemRemoved(viewHolder.adapterPosition) //通知适配器此位置已清空
        }

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            super.onSelectedChanged(viewHolder, actionState) //如果触摸状态不为闲置,则显示遮罩效果
            if (actionState != ACTION_STATE_IDLE)
                (viewHolder as ViewHolder).view.visibility = View.VISIBLE
        }

        //触摸结束要清理视图
        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            super.clearView(recyclerView, viewHolder)
            (viewHolder as ViewHolder).view.visibility = View.INVISIBLE
        }
    }).attachToRecyclerView(view) //依附于循环视图
}