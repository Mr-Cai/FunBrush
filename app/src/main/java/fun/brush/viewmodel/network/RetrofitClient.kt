package `fun`.brush.viewmodel.network

import android.annotation.SuppressLint
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitClient(url: String) {
    private var retrofit: Retrofit = Retrofit.Builder() //构建网络请求库
            .client(OkHttpClient()) //封装了OkHttp类库,现在创建一个客户端
            .addConverterFactory(GsonConverterFactory.create()) //转换对象简谱
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create()) //回调适配器工厂方法
            .baseUrl(url) //加载源地址
            .build() ///构造出来

    companion object { //伴生对象闭包下写的是可全局调用的常量或静态方法
        @SuppressLint("StaticFieldLeak")
        private lateinit var instance: RetrofitClient

        fun getInstance(baseUrl: String): RetrofitClient { //静态实例方法传入源地址
            synchronized(RetrofitClient::class) {
                RetrofitClient.instance = RetrofitClient(baseUrl)
            }
            return RetrofitClient.instance
        }
    }

    fun <T> create(service: Class<T>?): T { //通过反射加载类地址并请求接口内的API数据
        if (service == null) throw  RuntimeException("开放接口不存在")
        return retrofit.create(service) //处理并返回用递归创建的类实例
    }
}