package `fun`.brush.model

import `fun`.brush.model.bean.MovieBean
import `fun`.brush.viewmodel.network.API
import `fun`.brush.viewmodel.network.RetrofitClient
import android.annotation.SuppressLint
import android.content.Context
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

@SuppressLint("CheckResult")
class VideoPresenter(
        private var context: Context,
        private var view: Presenter.View
) : Presenter {
    private val model: VideoModel by lazy {
        //懒加载:只有在使用到类时,才会实例化
        VideoModel()
    }

    override fun requestData() { //请求最新一页的视频
        //let函数省掉了判空处理
        val observable: Observable<MovieBean> = context.let { model.loadData(true, "0") }
        observable.applySchedulers().subscribe { movieBean: MovieBean ->
            view.setData(movieBean)
        }
    }

    fun moreData(data: String) { //第一页之后的视频
        val observable: Observable<MovieBean> = context.let { model.loadData(false, data) }
        observable.applySchedulers().subscribe { movieBean: MovieBean ->
            view.setData(movieBean)
        }
    }

    //T代表任意数据类型,可供任意类使用
    private fun <T> Observable<T>.applySchedulers(): Observable<T> = subscribeOn(Schedulers.io())
            .unsubscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
}

class VideoModel { //视频数据模型
    fun loadData(isFirst: Boolean, data: String): Observable<MovieBean> {
        val retrofitClient = RetrofitClient.getInstance(API.BASE_URL) //同步源数据
        val apiService = retrofitClient.create(API::class.java) //创建请求实例
        return when (isFirst) {
            true -> apiService.getHomeData() //最新数据
            false -> apiService.getHomeMoreData(data, "2") //更多数据
        }
    }
}

interface Presenter {
    fun requestData() //请求数据
    interface View {
        fun setData(bean: MovieBean) //配置数据
    }
}