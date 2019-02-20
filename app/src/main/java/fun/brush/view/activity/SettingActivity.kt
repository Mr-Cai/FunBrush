package `fun`.brush.view.activity

import `fun`.brush.R
import `fun`.brush.R.drawable.*
import `fun`.brush.model.bean.InfoItem
import `fun`.brush.model.bean.User
import `fun`.brush.view.activity.SettingActivity.AppBarLayoutStateChangeListener.State.*
import `fun`.brush.viewmodel.util.Const.Companion.IS_LOG_OUT
import android.content.Intent
import android.graphics.Color.TRANSPARENT
import android.os.Bundle
import android.view.MenuItem
import android.view.View.*
import android.view.animation.AnimationUtils.loadAnimation
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import cn.bmob.newim.BmobIM
import cn.bmob.v3.BmobUser
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy.NONE
import com.bumptech.glide.request.RequestOptions.placeholderOf
import com.google.android.material.appbar.AppBarLayout
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_setting.*
import kotlinx.android.synthetic.main.info_item.view.*
import java.util.*


class SettingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        setSupportActionBar(toolbar)
        window.statusBarColor = TRANSPARENT
        window.decorView.systemUiVisibility = SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        title = null
        val adapter = GroupAdapter<ViewHolder>()
        settingRecycler.layoutManager = LinearLayoutManager(this)
        settingRecycler.adapter = adapter
        adapter.add(Option(InfoItem(ic_birth, "生日", "2018-10-12")))
        adapter.add(Option(InfoItem(ic_gender, "性别", "男")))
        adapter.add(Option(InfoItem(ic_location, "所在地", "江苏省常州市")))
        adapter.add(Option(InfoItem(null, null, null))) //分隔线
        adapter.add(Option(InfoItem(ic_phone, "手机号", "110")))
        adapter.add(Option(InfoItem(ic_bind, "绑定", "Github")))
        val requestOptions = placeholderOf(bg_default).diskCacheStrategy(NONE)
        val currentUser = BmobUser.getCurrentUser(User::class.java)
        Glide.with(this).load(picUrl).apply(requestOptions).into(picBG)
        Glide.with(this).load(currentUser.avatar!!.fileUrl).apply(requestOptions).into(avatarPic)
        refreshBG.startAnimation(loadAnimation(this, R.anim.refresh_bg)) //旋转动画
        refreshBG.setOnClickListener {
            refreshBG.startAnimation(loadAnimation(this, R.anim.refresh_bg))
            Glide.with(this).load(picUrl).into(picBG)
        }
        appbar.addOnOffsetChangedListener(object : AppBarLayoutStateChangeListener() {
            override fun onStateChanged(appBarLayout: AppBarLayout?, state: State) = when (state) {
                EXPAND -> refreshBG.visibility = VISIBLE
                COLLAPSED -> refreshBG.visibility = INVISIBLE
                INTERMEDIATE -> TODO()
            }
        })
        userNameET.setText(currentUser.username)
        userIDTxT.text = currentUser.objectId
        exitLogin.setOnClickListener {
            BmobUser.logOut() //退出当前账号
            BmobIM.getInstance().disConnect() //断开消息服务
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra(IS_LOG_OUT, "已退出")
            startActivity(intent)
            finish()
        }
    }

    inner class Option(private var items: InfoItem) : Item<ViewHolder>() {
        override fun getLayout(): Int = R.layout.info_item

        override fun bind(holder: ViewHolder, position: Int) {
            val context = holder.itemView.context
            val itemView = holder.itemView
            Glide.with(context).load(items.icon).into(itemView.icon)
            itemView.nameTxT.text = items.name
            itemView.infoTxT.text = items.info
            if (position == 3) {
                itemView.divider.visibility = VISIBLE
                itemView.icon.visibility = GONE
                itemView.nameTxT.visibility = GONE
                itemView.infoTxT.visibility = GONE
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) finish()
        return super.onOptionsItemSelected(item)
    }

    abstract class AppBarLayoutStateChangeListener : AppBarLayout.OnOffsetChangedListener {
        enum class State {
            EXPAND,
            COLLAPSED,
            INTERMEDIATE
        }

        private var currentState = INTERMEDIATE
        override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
            when {
                verticalOffset == 0 -> {
                    if (currentState != EXPAND) onStateChanged(appBarLayout, EXPAND)
                    EXPAND
                }
                Math.abs(verticalOffset) >= appBarLayout.totalScrollRange -> {
                    if (currentState != COLLAPSED) onStateChanged(appBarLayout, COLLAPSED)
                    COLLAPSED
                }
                else -> {
                    if (currentState != INTERMEDIATE) onStateChanged(appBarLayout, INTERMEDIATE)
                    INTERMEDIATE
                }
            }
        }

        abstract fun onStateChanged(appBarLayout: AppBarLayout?, state: State)
    }

    companion object {
        private val suffix = Random().nextInt(986) + 19001
        val picUrl = "https://kbdevstorage1.blob.core.windows.net/asset-blobs/${suffix}_en_1"
    }
}
