package `fun`.brush.view.activity.message

import `fun`.brush.R
import `fun`.brush.model.bean.AddFriendMessage
import `fun`.brush.model.bean.User
import `fun`.brush.viewmodel.util.Const.Companion.AVATAR
import `fun`.brush.viewmodel.util.Const.Companion.CALL
import `fun`.brush.viewmodel.util.Const.Companion.TITLE
import `fun`.brush.viewmodel.util.Const.Companion.USER_ID
import `fun`.brush.viewmodel.util.Const.Companion.USER_NAME
import `fun`.brush.viewmodel.util.Tool
import android.Manifest.permission.CALL_PHONE
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_DENIED
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat.checkSelfPermission
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import cn.bmob.newim.BmobIM
import cn.bmob.newim.bean.BmobIMConversation.obtain
import cn.bmob.newim.bean.BmobIMMessage
import cn.bmob.newim.bean.BmobIMUserInfo
import cn.bmob.newim.core.BmobIMClient
import cn.bmob.newim.listener.MessageSendListener
import cn.bmob.v3.BmobQuery
import cn.bmob.v3.BmobUser
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.FindListener
import com.google.android.material.snackbar.Snackbar
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_people.*
import kotlinx.android.synthetic.main.info_dialog.*
import kotlinx.android.synthetic.main.people_item.view.*

class PeopleActivity : AppCompatActivity() {
    private var adapter = GroupAdapter<ViewHolder>()
    lateinit var userInfo: BmobIMUserInfo
    lateinit var phone: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_people)
        setSupportActionBar(toolBar)
        title = intent.getStringExtra(TITLE)
        obtainAllUsers() //获取所有用户
        swipeRefreshLayout.setOnRefreshListener {
            obtainAllUsers()
        }
    }

    private fun obtainAllUsers() {
        adapter.clear()
        val query = BmobQuery<User>()
        query.findObjects(object : FindListener<User>() {
            override fun done(users: MutableList<User>, exception: BmobException?) {
                if (exception == null) { //如果老铁没毛病,就执行下一行
                    if (users.isNotEmpty()) {
                        users.map {
                            recyclerPeople.layoutManager = GridLayoutManager(this@PeopleActivity, 4)
                            recyclerPeople.adapter = adapter
                            recyclerPeople.itemAnimator = DefaultItemAnimator()
                            val user = User()
                            user.objectId = it.objectId
                            user.username = it.username
                            user.email = it.email
                            user.mobilePhoneNumber = it.mobilePhoneNumber
                            phone = it.mobilePhoneNumber
                            user.avatar = it.avatar
                            adapter.add(PeopleItem(user))
                        }
                    }
                }
            }
        })
        swipeRefreshLayout.isRefreshing = false
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar_main, menu)
        val searchView = menu.findItem(R.id.searchView).actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean = false
            override fun onQueryTextChange(newText: String): Boolean {
                return true
            }
        })
        val editText: EditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text)
        editText.background = null
        editText.hint = "请输入要查找的联系人姓名"
        val field = TextView::class.java.getDeclaredField("mCursorDrawableRes")
        field.isAccessible = true
        field.set(editText, R.drawable.cursor_text)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

        }
        return true
    }

    inner class PeopleItem(var user: User) : Item<ViewHolder>() {
        override fun getLayout(): Int = R.layout.people_item

        override fun bind(holder: ViewHolder, position: Int) {
            val context = holder.itemView.context
            val itemView = holder.itemView
            Tool.setImageToView(context, user.username,
                    user.avatar.toString(), itemView.avatarPic) //拉取的头像
            itemView.peopleNameTxT.text = user.username
            itemView.avatarPic.setOnClickListener { it ->
                val popMenu = PopupMenu(context, it) //构造一个气泡菜单
                popMenu.inflate(R.menu.pop_item) //展开菜单
                val popupMenuField = PopupMenu::class.java.getDeclaredField("mPopup") //通过反射获取原生字段
                popupMenuField.isAccessible = true //设置可访问权限
                val mPopup = popupMenuField.get(popMenu) //获取到对象
                mPopup.javaClass.getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                        .invoke(mPopup, true) //强制显示图标
                popMenu.setOnMenuItemClickListener { item: MenuItem ->
                    when (item.itemId) {
                        R.id.addFriend -> {
                            userInfo = BmobIMUserInfo(user.objectId, user.username, "${user.avatar}")
                            val requestEntrance = BmobIM.getInstance().startPrivateConversation(
                                    userInfo, true, null) //创建一个会话入口
                            //获取消息管理
                            val messageManager = obtain(BmobIMClient.getInstance(), requestEntrance)
                            val addFriendMessage = AddFriendMessage()
                            val currentUser = BmobUser.getCurrentUser(User::class.java)
                            addFriendMessage.content = "你好,很高兴认识你!"
                            val msgMap = HashMap<String, Any>()
                            msgMap[USER_ID] = currentUser.objectId
                            msgMap[USER_NAME] = currentUser.username
                            msgMap[AVATAR] = currentUser.avatar.toString()
                            addFriendMessage.setExtraMap(msgMap)
                            messageManager.sendMessage(addFriendMessage, object : MessageSendListener() {
                                override fun done(imMessage: BmobIMMessage?, exception: BmobException?) {
                                    if (exception == null) {
                                        Snackbar.make(itemView, "已发送添加好友申请,请等待验证.", 1000).show()
                                    } else {
                                        Snackbar.make(itemView, "发送申请失败${exception.message}", 1000).show()
                                    }
                                }
                            })
                        }
                        R.id.sendMessage -> {
                            startActivity(Intent(context, ChatActivity::class.java)
                                    .putExtra(USER_NAME, user.username)
                                    .putExtra(USER_ID, user.objectId))
                        }
                        R.id.checkInfo -> {
                            val dialog = Dialog(context, R.style.BottomDialog)
                            val parent: ViewGroup? = null
                            val layout = LayoutInflater.from(this@PeopleActivity)
                                    .inflate(R.layout.info_dialog, parent, false)
                            dialog.setContentView(layout)
                            val params = layout.layoutParams as ViewGroup.MarginLayoutParams
                            params.width = resources.displayMetrics
                                    .widthPixels - Tool.dp2px(this@PeopleActivity, 16F)
                            params.bottomMargin = Tool.dp2px(this@PeopleActivity, 64F)
                            params.topMargin = Tool.dp2px(this@PeopleActivity, 64F)
                            layout.layoutParams = params
                            Tool.setImageToView(context, user.username,
                                    user.avatar.toString(), dialog.userIconPic)
                            dialog.userNameTxT.text = user.username
                            dialog.phoneTxT.text = user.mobilePhoneNumber
                            dialog.emailTxT.text = user.email
                            dialog.phoneTxT.setOnClickListener {
                                if (checkSelfPermission(this@PeopleActivity, CALL_PHONE) == PERMISSION_DENIED) {
                                    requestPermissions(this@PeopleActivity, arrayOf(CALL_PHONE), CALL)
                                } else {
                                    val call = Intent(Intent.ACTION_CALL)
                                    call.data = Uri.parse("tel:" + user.mobilePhoneNumber)
                                    startActivity(call)
                                }
                            }
                            dialog.closeIcon.setOnClickListener { dialog.dismiss() }
                            dialog.setCanceledOnTouchOutside(false)
                            dialog.show()
                        }
                    }
                    false
                }
                popMenu.show()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CALL && grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED) {
            val call = Intent(Intent.ACTION_CALL)
            call.data = Uri.parse("tel:$phone")
            startActivity(call)
        } else {
            Snackbar.make(phoneTxT, "未授权", 1000).show()
        }
    }
}
