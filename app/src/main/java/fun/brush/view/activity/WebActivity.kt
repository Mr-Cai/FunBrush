package `fun`.brush.view.activity

import `fun`.brush.R
import `fun`.brush.viewmodel.util.Const.Companion.PIC
import `fun`.brush.viewmodel.util.Const.Companion.TITLE
import `fun`.brush.viewmodel.util.Const.Companion.URL
import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_web.*

class WebActivity : AppCompatActivity() {

    @SuppressLint("SetJavaScriptEnabled", "RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)
        window.statusBarColor = Color.TRANSPARENT
        setSupportActionBar(toolbar) //设置标题栏为工具栏
        val actionBar = supportActionBar!!
        actionBar.setDisplayHomeAsUpEnabled(true) //显示左上角返回键
        actionBar.setDisplayShowTitleEnabled(true) //显示标题
        actionBar.title = intent.getStringExtra(TITLE) //获取点击某条新闻的标题
        webView.settings.javaScriptEnabled = true //激活在网页内部跳转
        webView.webViewClient = WebViewClient() //绑定自带的封装客户端类
        webView.loadUrl(intent.getStringExtra(URL)) //加载传入的新闻链接
        Glide.with(this).load(intent.getStringExtra(PIC)).into(bigPic) //传入封面
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish() //返回键退出界面
        }
        return super.onOptionsItemSelected(item)
    }
}
