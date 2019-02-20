package `fun`.brush.view.activity.options

import `fun`.brush.R
import `fun`.brush.model.bean.WeatherBean
import `fun`.brush.model.bean.WeatherBean.HeWeather6.DailyForecast
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_DENIED
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationListener
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_weather.*
import kotlinx.android.synthetic.main.week_forecast.view.*
import okhttp3.*
import java.io.IOException

class WeatherActivity : AppCompatActivity(), AMapLocationListener {

    private lateinit var locationClient: AMapLocationClient
    private var weekAdapter = GroupAdapter<ViewHolder>()
    private var distinct = ""
    override fun onLocationChanged(location: AMapLocation) = Unit
    val option = RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE)
            .placeholder(R.drawable.bg_default)
    private var locationListener: AMapLocationListener = AMapLocationListener { location ->
        locationTxT.text = location.district
        distinct = location.district
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)
        window.statusBarColor = Color.TRANSPARENT
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        if (checkSelfPermission(this, ACCESS_FINE_LOCATION) == PERMISSION_DENIED) {
            requestPermissions(this, arrayOf(ACCESS_FINE_LOCATION), 0xc)
        } else {
            setUpLocation()
        }
        loadIcon.startAnimation(AnimationUtils.loadAnimation(this, R.anim.refresh_bg))
        Handler().postDelayed({
            requestWeather()
        }, 3000)
        weatherRecycler.adapter = weekAdapter
        weatherRecycler.layoutManager = LinearLayoutManager(this, HORIZONTAL, false)
        refreshIcon.setOnClickListener {
            requestWeather()
            refreshIcon.startAnimation(AnimationUtils.loadAnimation(this, R.anim.refresh_bg))
        }
        locationTxT.setOnClickListener {
            val popMenu = PopupMenu(this, it)
            popMenu.inflate(R.menu.location)
            popMenu.setOnMenuItemClickListener { item: MenuItem ->
                when (item.itemId) {
                    R.id.addCity -> {

                    }
                }
                false
            }
            popMenu.show()
        }
    }

    private fun requestWeather() {
        weekAdapter.clear()
        loadIcon.visibility = View.GONE
        OkHttpClient().newCall(Request.Builder().url(weatherApi).post(FormBody.Builder()
                .add("key", "aa560659f4f64247aeded1d9ba485fca")
                .add("location", distinct).build())
                .build()).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val jsonParse = Gson().fromJson(response.body()!!.string(), WeatherBean::class.java)
                runOnUiThread {
                    jsonParse.weatherSet.forEach { current ->
                        temperatureTxT.text = String.format(getString(R.string.temperature,
                                current.now.tmp))
                        conditionTxT.text = current.now.condition
                        when (current.now.condition) {
                            "晴" -> {
                                Glide.with(applicationContext).load(R.drawable.ic_fine).into(weatherIcon)
                                Glide.with(applicationContext).load(fine).apply(option).into(weatherBG)
                            }
                            "多云" -> {
                                Glide.with(applicationContext).load(R.drawable.ic_cloudy).apply(option).into(weatherIcon)
                                Glide.with(applicationContext).load(cloudy).apply(option).into(weatherBG)
                            }
                            "小雨" -> {
                                Glide.with(applicationContext).load(R.drawable.ic_rain01).apply(option).into(weatherIcon)
                                Glide.with(applicationContext).load(rain01).apply(option).into(weatherBG)
                            }
                            "阴" -> {
                                Glide.with(applicationContext).load(R.drawable.ic_overcast).apply(option).into(weatherIcon)
                                Glide.with(applicationContext).load(overcast).apply(option).into(weatherBG)
                            }
                        }
                        updateTxT.text = current.update.loc
                        current.dailyForecast.forEach { week ->
                            weekAdapter.add(WeekItem(DailyForecast(
                                    week.cond_code_d, week.cond_code_n,
                                    week.conditionDay, week.conditionNight,
                                    week.date, week.hum, week.mr, week.ms, week.pcpn, week.pop,
                                    week.pres, week.sr, week.ss, week.tmpMax, week.tmpMin,
                                    week.uv_index, week.vis, week.wind_deg, week.wind_dir,
                                    week.wind_sc, week.wind_spd)))
                        }
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) = Snackbar.make(temperatureTxT,
                    "${e.message}", 1000).show()
        })
    }

    private fun setUpLocation() {
        locationClient = AMapLocationClient(this)
        locationClient.setLocationListener(locationListener)
        locationClient.startLocation()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0xc && grantResults.isNotEmpty() && grantResults[0] ==
                PackageManager.PERMISSION_GRANTED) {
            setUpLocation()
        } else {
            Snackbar.make(temperatureTxT, "未授权", 1000).show()
        }
    }

    inner class WeekItem(private var week: DailyForecast) : Item<ViewHolder>() {
        override fun getLayout() = R.layout.week_forecast

        override fun bind(holder: ViewHolder, position: Int) {
            val context = holder.itemView.context
            val itemView = holder.itemView
            itemView.dateTxT.text = week.date.substring(5)
            itemView.dayTxT.text = week.conditionDay
            itemView.tmpMaxTxT.text = String.format(getString(R.string.temperature), week.tmpMax)
            itemView.tmpMinTxT.text = String.format(getString(R.string.temperature), week.tmpMin)
            itemView.nightTxT.text = week.conditionNight
        }
    }

    companion object {
        const val weatherApi = "https://free-api.heweather.com/s6/weather"
        const val fine = "http://bbs-static.smartisan.cn/data/attachment/forum/201810/27/154311p7xx8p57x5xpd63a.jpg"
        const val cloudy = "http://pic.netbian.com/uploads/allimg/170712/101837-14998259172191.jpg"
        const val overcast = "http://file25.mafengwo.net/M00/01/CC/wKgB4lLgoAuAZcE9AAQYAPJZDf428.jpeg"
        const val rain01 = "http://imgsrc.baidu.com/imgad/pic/item/a686c9177f3e670979b572d130c79f3df8dc5562.jpg"
    }
}