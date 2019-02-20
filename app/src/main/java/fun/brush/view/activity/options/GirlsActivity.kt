package `fun`.brush.view.activity.options

import `fun`.brush.R
import `fun`.brush.model.bean.NewsBean
import `fun`.brush.model.bean.NewsBean.News
import android.app.Dialog
import android.app.DownloadManager
import android.app.DownloadManager.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.view.View
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager.HORIZONTAL
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy.NONE
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_grils.*
import kotlinx.android.synthetic.main.girl_item.*
import kotlinx.android.synthetic.main.girl_item.view.*
import okhttp3.*
import okhttp3.Request
import java.io.IOException

class GirlsActivity : AppCompatActivity() {
    var adapter = GroupAdapter<ViewHolder>() //适配器
    private var downloadID: Long? = null
    private lateinit var picture: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grils)
        window.setFlags(FLAG_FULLSCREEN, FLAG_FULLSCREEN)
        window.statusBarColor = Color.TRANSPARENT
        requestData() //请求图片
        girlsRecycler.adapter = adapter
        girlsRecycler.layoutManager = StaggeredGridLayoutManager(3, HORIZONTAL)
        swipeRefreshLayout.setOnRefreshListener {
            requestData()
        }
    }

    private fun requestData() {
        adapter.clear()
        OkHttpClient().newCall(Request.Builder().url(girlUrl).build()).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val parseData = Gson().fromJson(response.body()!!.string(), NewsBean::class.java)
                runOnUiThread {
                    parseData.newslist.forEach {
                        //遍历集合内的链接
                        picture = it.picUrl!!
                        adapter.add(PicItem(News(it.title, null, null, it.picUrl, null)))
                    }
                    //刷新数据必须开启一个界面线程,否则无法连接网络
                    adapter.notifyDataSetChanged() //通知数据集改变
                    swipeRefreshLayout.isRefreshing = false //数据请求完毕停止刷新
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                Snackbar.make(girlsRecycler, e.toString(), 2000).show()
            }
        })
    }

    private fun downloadStatus(): Int {
        val query = Query()
        query.setFilterById(downloadID!!.toLong())
        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val cursor = downloadManager.query(query)
        if (cursor.moveToFirst()) {
            val columnIndex = cursor.getColumnIndex(COLUMN_STATUS)
            return cursor.getInt(columnIndex)
        }
        return ERROR_UNKNOWN
    }

    inner class PicItem(private val girls: News) : Item<ViewHolder>() {
        override fun getLayout(): Int = R.layout.girl_item

        override fun bind(holder: ViewHolder, position: Int) {
            val context = holder.itemView.context
            val requestOptions = RequestOptions.placeholderOf(R.drawable.ic_loading)
                    .diskCacheStrategy(NONE)
            Glide.with(context).load(girls.picUrl)
                    .apply(requestOptions).into(holder.itemView.girlPic)
            holder.itemView.setOnClickListener { _ ->
                val girlsDialog = Dialog(context)
                girlsDialog.setContentView(R.layout.girl_item)
                Glide.with(holder.itemView.context).load(girls.picUrl)
                        .apply(requestOptions).into(girlsDialog.girlPic)
                girlsDialog.shareBtn.visibility = View.VISIBLE
                girlsDialog.saveBtn.visibility = View.VISIBLE
                var flag = 0
                girlsDialog.girlPic.setOnClickListener {
                    when (flag) {
                        0 -> {
                            flag++
                            girlsDialog.shareBtn.visibility = View.INVISIBLE
                            girlsDialog.saveBtn.visibility = View.INVISIBLE
                        }
                        1 -> {
                            flag--
                            girlsDialog.shareBtn.visibility = View.VISIBLE
                            girlsDialog.saveBtn.visibility = View.VISIBLE
                        }
                    }
                }
                girlsDialog.shareBtn.setOnClickListener {
                    val intent = Intent()
                    intent.action = Intent.ACTION_SEND
                    intent.type = "image/*"
                    startActivity(Intent.createChooser(intent, girls.title))
                }
                girlsDialog.saveBtn.setOnClickListener {
                    val saveRequest = DownloadManager.Request(girls.picUrl!!.toUri())
                    saveRequest.setTitle(girls.title)
                    saveRequest.setDestinationInExternalFilesDir(context, DIRECTORY_DOWNLOADS, "${girls.title}.jpg")
                    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    downloadID = downloadManager.enqueue(saveRequest)
                    val filter = IntentFilter(ACTION_VIEW_DOWNLOADS)
                    context.registerReceiver(object : BroadcastReceiver() {
                        override fun onReceive(context: Context?, intent: Intent?) {
                            val broadcastDownID = intent!!.getLongExtra(EXTRA_DOWNLOAD_ID, -1)
                            if (broadcastDownID == downloadID) {
                                if (downloadStatus() == STATUS_SUCCESSFUL) {
                                    Snackbar.make(girlPic, "已保存\uD83D\uDE04", 1000).show()
                                } else {
                                    Snackbar.make(girlPic, "未保存☹️", 1000).show()
                                }
                            }
                        }
                    }, filter)
                }
                girlsDialog.show()
            }
        }
    }

    companion object {
        const val girlUrl = "http://api.tianapi.com/meinv/?key=d4f074e5a8866ff604bd1ee6b981eb96&num=50"
    }
}
