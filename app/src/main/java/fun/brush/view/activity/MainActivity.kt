package `fun`.brush.view.activity

import `fun`.brush.R
import `fun`.brush.model.bean.User
import `fun`.brush.view.activity.message.PeopleActivity
import `fun`.brush.view.activity.message.PublishActivity
import `fun`.brush.view.activity.sign.LoginActivity
import `fun`.brush.view.fragment.ExtensionFragment
import `fun`.brush.view.fragment.HomeFragment
import `fun`.brush.view.fragment.LiveFragment
import `fun`.brush.view.fragment.MessageFragment
import `fun`.brush.viewmodel.util.Const.Companion.IS_LOG_OUT
import `fun`.brush.viewmodel.util.Const.Companion.TITLE
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import androidx.appcompat.R.id.search_src_text
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import cn.bmob.v3.BmobUser
import cn.jzvd.JzvdStd
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.nav_header_main.view.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val bottomNavListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.homePage -> {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.containerF, HomeFragment()).commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.moviePage -> {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.containerF, LiveFragment()).commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.extensionPage -> {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.containerF, ExtensionFragment()).commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.messagePage -> {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.containerF, MessageFragment()).commit()
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        title = null
        window.statusBarColor = Color.TRANSPARENT
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_fingerprint)
        bottomNav.setOnNavigationItemSelectedListener(bottomNavListener)
        bottomNav.selectedItemId = R.id.homePage
        val toggle = ActionBarDrawerToggle(//操作栏切换抽屉按键
                this,
                drawerLayout,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close
        )
        drawerLayout.addDrawerListener(toggle)//添加抽屉监听
        toggle.syncState()//实现点击空白关闭和滑动关闭抽屉
        navigationView.setNavigationItemSelectedListener(this)
        val currentUser = BmobUser.getCurrentUser(User::class.java)
        val userIcon = navigationView.getHeaderView(0).avatar
        val place = RequestOptions.placeholderOf(R.drawable.ic_user_default)
        when (currentUser) {
            null -> {
                Glide.with(this).load(R.drawable.ic_user_default).apply(place).into(userIcon)
                navigationView.getHeaderView(0).currentUserName.text = "用户名"
            }
            else -> {
                navigationView.getHeaderView(0).currentUserName.text = currentUser.username
                Glide.with(this).load(currentUser.avatar!!.fileUrl).apply(place).into(userIcon)
            }
        }
        if (intent.getStringExtra(IS_LOG_OUT) == "已退出") {
            Glide.with(this).load(R.drawable.ic_user_default).apply(place).into(userIcon)
            navigationView.getHeaderView(0).currentUserName.text = "用户名"
        }
        userIcon.setOnClickListener {
            if (BmobUser.getCurrentUser(User::class.java) != null) { //有用户登录跳转到设置页面
                startActivity(Intent(this, SettingActivity::class.java))
            } else { //没登录返回登录页面
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = FLAG_ACTIVITY_CLEAR_TASK or FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
        }
    }

    override fun onBackPressed() {
        //否则退出程序
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)//抽屉打开时,按返回键关闭抽屉
        } else super.onBackPressed()
        if (JzvdStd.backPress()) return
    }

    override fun onPause() {
        super.onPause()
        JzvdStd.releaseAllVideos() //活动暂停时关闭后台播放
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar_main, menu)//填充菜单至操作栏
        val searchView = menu.findItem(R.id.searchView).actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
        val editText: EditText = searchView.findViewById(search_src_text)
        editText.background = null
        editText.isCursorVisible = false
        return true
    }

    /* private fun filter(newText: String?): ArrayList<String> {
         val filterSet = ArrayList<String>()
         for (filter in filterSet) {
             val newsAdapter: NewsAdapter? = null
             newsAdapter!!.notifyDataSetChanged()
         }
         return filterSet
     }*/

    override fun onOptionsItemSelected(item: MenuItem): Boolean {//处理操作栏菜单项已选中
        when (item.itemId) {

        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {//根据选项编号切换页面
            R.id.navPeople -> {
                startActivity(Intent(this, PeopleActivity::class.java).putExtra(TITLE, item.title))
                return true
            }
            R.id.navPublish -> {
                startActivity(Intent(this, PublishActivity::class.java))
                return true
            }
            R.id.navSetting -> {
                startActivity(Intent(this, SettingActivity::class.java))
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)//关闭抽屉
        return true
    }
}
