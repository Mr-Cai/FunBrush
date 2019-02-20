package `fun`.brush.viewmodel.network

import `fun`.brush.model.bean.MovieBean
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface API {
    companion object {
        val BASE_URL: String get() = "http://baobab.kaiyanapp.com/api/" //视频简谱源地址
    }

    //获取首页数据
    @GET("v2/feed?num=2&udid=26868b32e808498db32fd51fb422d00175e179df&vc=83")
    fun getHomeData(): Observable<MovieBean>

    //获取第一页之后的数据
    @GET("v2/feed")
    fun getHomeMoreData(
            @Query("date") date: String, //查询字段,字段为站点目录
            @Query("num") num: String
    ): Observable<MovieBean> //使用RxJava观察实体类
}