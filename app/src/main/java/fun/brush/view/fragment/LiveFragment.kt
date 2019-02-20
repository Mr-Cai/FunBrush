package `fun`.brush.view.fragment

import `fun`.brush.R
import `fun`.brush.viewmodel.adapter.VideoAdapter
import `fun`.brush.model.bean.MovieBean
import `fun`.brush.model.bean.MovieBean.IssueListBean.ItemListBean
import `fun`.brush.model.Presenter
import `fun`.brush.model.VideoPresenter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE
import cn.jzvd.JzvdStd
import kotlinx.android.synthetic.main.fragment_live.*
import java.util.regex.Pattern

class LiveFragment : Fragment(), Presenter.View {
    private lateinit var presenter: VideoPresenter
    private var videoSet = ArrayList<ItemListBean>()
    private lateinit var adapter: VideoAdapter
    lateinit var data: String
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState:
    Bundle?) = inflater.inflate(R.layout.fragment_live, container, false)!!

    override fun setData(bean: MovieBean) {
        val matcher = Pattern.compile("[^0-9]").matcher(bean.nextPageUrl) //匹配关键字
        //将数字替换为空串(去除多余数字),只有这样才能不断刷新下一页
        data = matcher.replaceAll("").subSequence(1, matcher.replaceAll("").length - 1).toString()
        bean.issueList!!
                .flatMap { it.itemList!! }
                .filter { it.type.equals("video") } //筛选出视频字段
                .forEach { videoSet.add(it) } //遍历迭代出对应元素并加入数据集
        adapter.notifyDataSetChanged() //通知数据集已改变
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        presenter = VideoPresenter(context!!, this)
        presenter.requestData()
        liveRecycler.layoutManager = LinearLayoutManager(context)
        adapter = VideoAdapter(videoSet)
        liveRecycler.adapter = adapter
        refreshLayout.setOnRefreshListener {
            presenter.requestData()
            videoSet.clear() //清空数据集防止首页重复加载
            refreshLayout.isRefreshing = false
        }
        liveRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            //监听页面滑动到结尾
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val layoutManager = liveRecycler.layoutManager as LinearLayoutManager
                val lastPosition = layoutManager.findLastVisibleItemPosition()
                if (newState == SCROLL_STATE_IDLE && lastPosition == videoSet.size - 1) {
                    presenter.moreData(data) //第一页加载完成后继续获取更多数据
                }
            }
        })
    }

    override fun onStop() {
        super.onStop()
        JzvdStd.releaseAllVideos() //页面关闭同时关闭后台播放
    }
}