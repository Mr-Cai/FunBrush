package `fun`.brush.view.fragment

import `fun`.brush.R
import `fun`.brush.model.bean.NewsBean
import `fun`.brush.model.bean.NewsBean.News
import `fun`.brush.viewmodel.adapter.NewsAdapter
import android.R.color.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_home.*
import okhttp3.*
import java.io.IOException

class HomeFragment : Fragment() {
    private val newsSet = ArrayList<News>() //创建新闻实体集
    private lateinit var adapter: NewsAdapter //声明适配器
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState:
    Bundle?) = inflater.inflate(R.layout.fragment_home, container, false)!!

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        requestData() //请求新闻
        adapter = NewsAdapter(newsSet) //构造适配器传入集合
        adapter.attachView(newsRecycler) //增加滑动删除与拖动排序
        newsRecycler.adapter = adapter //设置适配器
        newsRecycler.layoutManager = LinearLayoutManager(context) //设置布局管理器为默认的线性布局
        refreshLayout.setColorSchemeResources( //刷新时箭头的颜色集合
                holo_blue_light,
                holo_red_light,
                holo_orange_light,
                holo_green_light
        )
        refreshLayout.setOnRefreshListener {
            requestData() //刷新数据
        }
        newsRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy < 0) backTopFB.hide() else if (dy > 0) backTopFB.show()
            }
        })
        backTopFB.setOnClickListener {
            newsRecycler.smoothScrollToPosition(0)
        }
    }

    private fun requestData() {
        newsSet.clear() //请求之前清空集合防止数据重叠
        OkHttpClient().newCall(Request.Builder().url(api).build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Snackbar.make(newsRecycler, e.toString(), 2000).show()
            }

            override fun onResponse(call: Call, response: Response) {
                val parseData = Gson().fromJson(response.body()!!.string(), NewsBean::class.java)
                for (iteration in parseData.newslist) { //遍历集合,并调用字段去添加数据
                    newsSet.add(News(
                            iteration.title,
                            iteration.description,
                            iteration.ctime,
                            iteration.picUrl,
                            iteration.url
                    ))
                }
                activity!!.runOnUiThread {
                    //在界面线程内通知数据集改变
                    adapter.notifyDataSetChanged() //数据加载完毕通知适配器数据集改变
                    refreshLayout.isRefreshing = false //加载完成停止刷新
                }
            }
        })
    }

    companion object {
        const val api = "http://api.tianapi.com/wxnew/?key=d4f074e5a8866ff604bd1ee6b981eb96&num=50"
    }
}
