package `fun`.brush.model.bean

import android.os.Parcelable
import cn.bmob.v3.BmobObject
import cn.bmob.v3.BmobUser
import cn.bmob.v3.datatype.BmobFile
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

class User : BmobUser() {
    var avatar: BmobFile? = null
} //继承比目用户实现用户托管

class NewFriend : Serializable {
    var id: Long? = null
    var uid: String? = null
    var msg: String? = null
    var name: String? = null
    var avatar: String? = null
    var status: Int? = null
    var time: Long? = null
}

class MSG(var content: String, var type: Int) {
    companion object {
        const val RECEIVED = 0
        const val SENT = 1
    }
}

class Note : BmobObject() {
    lateinit var content: String
    lateinit var user: User
}

class Translate { //翻译所需实体
    var translation: Array<String>? = null
    var basic: Basic? = null
    var query: String? = null
    var errorCode: Int = 0
    var web: List<Web>? = null

    class Basic {
        var phonetic: String? = null
        var explains: Array<String>? = null
    }

    class Web {
        var value: Array<String>? = null
        var key: String? = null
        @Suppress("unused")
        private val finalValue: String? = null
    }
}

class InfoItem(var icon: Int?, var name: String?, var info: String?)
//上传至服务器
class MindSet(val name: String, val title: String, val content: String,
              val image: BmobFile?) : BmobObject()

//从服务器拉取
class PullMSG(val name: String, val time: String, val title: String,
              val content: String, val image: String?) {
    var author: MindSet? = null
}

class NewsBean { //新闻API
    @SerializedName("newslist")
    lateinit var newslist: ArrayList<News>

    @Parcelize
    class News(
            var title: String?,
            var description: String?,
            var ctime: String?,
            var picUrl: String?,
            var url: String?
    ) : Parcelable
}

class GrowBean {
    data class GridOptions(var logo: Int, var name: String)
}

data class MovieBean(
        var nextPageUrl: String?,
        var nextPublishTime: Long,
        var newestIssueType: String?,
        var dialog: Any?,
        var issueList: List<IssueListBean>?
) {
    data class IssueListBean(
            var releaseTime: Long,
            var type: String?,
            var date: Long,
            var publishTime: Long,
            var count: Int,
            var itemList: List<ItemListBean>?
    ) {
        data class ItemListBean(
                var type: String?,
                var data: DataBean,
                var tag: Any?
        ) {
            data class DataBean(
                    var dataType: String?,
                    var id: Int,
                    var title: String?,
                    var description: String?,
                    var image: String?,
                    var actionUrl: String?,
                    var adTrack: Any?,
                    var isShade: Boolean,
                    var label: Any?,
                    var labelList: Any?,
                    var header: Any?,
                    var category: String?,
                    var duration: Long?,
                    var playUrl: String,
                    var cover: CoverBean?,
                    var author: AuthorBean?,
                    var releaseTime: Long?,
                    var consumption: ConsumptionBean?
            ) {
                data class CoverBean(
                        var feed: String?,
                        var detail: String?,
                        var blurred: String?,
                        var sharing: String?,
                        var homepage: String?
                )

                data class ConsumptionBean(
                        var collectionCount: Int,
                        var shareCount: Int,
                        var replyCount: Int
                )

                data class AuthorBean(var icon: String)
            }
        }
    }
}

@Parcelize
data class VideoBean(
        var feed: String?,
        var title: String?,
        var description: String?,
        var duration: Long?,
        var playUrl: String?,
        var category: String?,
        var blurred: String?,
        var collect: Int?,
        var share: Int?,
        var reply: Int?,
        var time: Long
) : Parcelable, Serializable

data class WeatherBean(
        @SerializedName("HeWeather6")
        var weatherSet: List<HeWeather6>
) {
    data class HeWeather6(
            var basic: Basic,
            @SerializedName("daily_forecast")
            var dailyForecast: List<DailyForecast>,
            var hourly: List<Hourly>,
            var lifestyle: List<Lifestyle>,
            var now: Now,
            var status: String,
            var update: Update
    ) {
        data class Basic(
                var admin_area: String,
                var cid: String,
                var cnty: String,
                var lat: String,
                var location: String,
                var lon: String,
                var parent_city: String,
                var tz: String
        )

        data class DailyForecast(
                var cond_code_d: String,
                var cond_code_n: String,
                @SerializedName("cond_txt_d")
                var conditionDay: String,
                @SerializedName("cond_txt_n")
                var conditionNight: String,
                var date: String,
                var hum: String,
                var mr: String,
                var ms: String,
                var pcpn: String,
                var pop: String,
                var pres: String,
                var sr: String,
                var ss: String,
                @SerializedName("tmp_max")
                var tmpMax: String,
                @SerializedName("tmp_min")
                var tmpMin: String,
                var uv_index: String,
                var vis: String,
                var wind_deg: String,
                var wind_dir: String,
                var wind_sc: String,
                var wind_spd: String
        )

        data class Hourly(
                var cloud: String,
                var cond_code: String,
                var cond_txt: String,
                var dew: String,
                var hum: String,
                var pop: String,
                var pres: String,
                var time: String,
                var tmp: String,
                var wind_deg: String,
                var wind_dir: String,
                var wind_sc: String,
                var wind_spd: String
        )

        data class Now(
                var cloud: String,
                var cond_code: String,
                @SerializedName("cond_txt")
                var condition: String,
                var fl: String,
                var hum: String,
                var pcpn: String,
                var pres: String,
                var tmp: String,
                var vis: String,
                var wind_deg: String,
                var wind_dir: String,
                var wind_sc: String,
                var wind_spd: String
        )

        data class Lifestyle(
                var brf: String,
                var txt: String,
                var type: String
        )

        data class Update(
                var loc: String,
                var utc: String
        )
    }
}