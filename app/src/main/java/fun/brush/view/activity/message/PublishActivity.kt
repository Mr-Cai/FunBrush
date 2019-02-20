package `fun`.brush.view.activity.message

import `fun`.brush.R
import `fun`.brush.model.bean.MindSet
import `fun`.brush.model.bean.PullMSG
import `fun`.brush.model.bean.User
import `fun`.brush.view.activity.sign.LoginActivity
import `fun`.brush.viewmodel.adapter.PublishAdapter
import `fun`.brush.viewmodel.util.Const.Companion.PRINT
import `fun`.brush.viewmodel.util.Tool
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.bmob.v3.BmobQuery
import cn.bmob.v3.BmobUser
import cn.bmob.v3.datatype.BmobDate
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.FindListener
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_publish.*
import java.text.SimpleDateFormat
import java.util.*

class PublishActivity : AppCompatActivity() {
    private val infoSet = ArrayList<PullMSG>() //拉取信息的集合
    private lateinit var publishAdapter: PublishAdapter //拉取信息的适配器
    private var isBackFromNetwork = false //是否回去设置网络,默认不用
    private var isRefresh = false //是否刷新了的标记
    private var isRefreshPullDown = false //是否全部刷新了的标记
    private var publishCount = 0 //帖子的数量
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_publish)
        setSupportActionBar(toolbar)
        title = null
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        loadDynamic() //从服务器拉取动态
        dynamicRecycler.hasFixedSize() //让循环视图具备固定好的尺寸,以避免列表项改动重新计算大小而浪费资源
        dynamicRecycler.itemAnimator = DefaultItemAnimator() //刷新列表的动画特效
        dynamicRecycler.layoutManager = LinearLayoutManager(this) //默认的垂直线性布局
        publishAdapter = PublishAdapter(infoSet) //构造适配器
        dynamicRecycler.adapter = publishAdapter //绑定适配器
        dynamicRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                //向上垂直滚动隐藏浮动按钮否则显示
                if (dy > 0) publishMindFB.hide() else if (dy < 0) publishMindFB.show()
            }
        })
        refreshLayout.setOnRefreshListener {
            isRefreshPullDown = true //表示所有数据都要加载
            loadDynamic() //加载动态
        }
        publishMindFB.setOnClickListener { _ ->
            val userInfo = BmobUser.getCurrentUser(User::class.java) //获取当前用户
            if (userInfo == null) { //没登录跳转回登录页面
                Snackbar.make(publishMindFB, "请先登录再发表想法", 1000).setAction("登录") {
                    startActivity(Intent(this, LoginActivity::class.java))
                }.show()
            } else {
                startActivity(Intent(this, EditMindActivity::class.java))
            }
        }
    }

    private fun loadDynamic() {
        if (Tool.isNetWorkConnected(this)) { //如果网络已连接
            queryData() //查询数据表
        } else { //网络断开
            isBackFromNetwork = true //是否回去设置网络:是
            Snackbar.make(publishMindFB, "网络已断开", 1000).show()
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun queryData() { //查询表
        refreshLayout.isRefreshing = true //刷新布局打开刷新
        isRefresh = true //激活刷新
        val query = BmobQuery<MindSet>() //查询表
        query.setLimit(50)
        //在循环视图顶部显示最新数据,首次加载为降序排列,刷新后为升序,所以要以更新时间做索引
        if (isRefreshPullDown) query.order("updatedAt") else query.order("-updatedAt")
        if (publishCount > 0) { //如果有人发帖子
            val lastDate = infoSet[0].time
            val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(lastDate)
            date.seconds += 1
            query.addWhereGreaterThan("updatedAt", BmobDate(date))
        }
        query.findObjects(object : FindListener<MindSet>() {
            override fun done(postSet: MutableList<MindSet>, exception: BmobException?) {
                if (exception == null) {
                    if (postSet.isNotEmpty()) {
                        postSet.map {
                            val bulletin = PullMSG(
                                    it.name,
                                    it.updatedAt,
                                    it.title,
                                    it.content,
                                    it.image?.fileUrl  //要保持图片字段可空
                            )
                            if (isRefreshPullDown) { //如果全部加载了
                                infoSet.add(0, bulletin) //在顶部刷新一条记录
                            } else {
                                infoSet.add(bulletin) //按照降序追加
                            }
                        }
                    }
                    if (infoSet.size > publishCount) { //如果数据集的大小大于未刷新之前
                        publishCount = infoSet.size //改变为新数据集大小
                        publishAdapter.notifyDataSetChanged() //通知适配器改变数据集
                    }
                    //没有刷出动态就显示占位图
                    if (infoSet.size == 0 && no_dynamic.visibility == View.GONE) {
                        no_dynamic.visibility = View.VISIBLE
                    } else if (infoSet.size > 0 && no_dynamic.visibility == View.VISIBLE) {
                        no_dynamic.visibility = View.GONE
                    }
                } else {
                    Log.i(PRINT, "${exception.errorCode}:${exception.message}")
                }
                refreshLayout.isRefreshing = false
                isRefresh = false
                isRefreshPullDown = false
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        if (isBackFromNetwork && Tool.isBackFromSetNetwork) { //没有联网
            isBackFromNetwork = false
            Tool.isBackFromSetNetwork = false
            if (Tool.isNetWorkConnected(this)) {
                queryData() //刷新数据
            } else { //停止刷新
                refreshLayout.isRefreshing = false
                isRefresh = false
                isRefreshPullDown = false
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        infoSet.clear() //清空集合
    }
}
