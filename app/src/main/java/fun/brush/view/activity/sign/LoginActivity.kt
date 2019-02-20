package `fun`.brush.view.activity.sign

import `fun`.brush.R
import `fun`.brush.model.bean.User
import `fun`.brush.view.activity.MainActivity
import `fun`.brush.viewmodel.util.Const.Companion.AVATAR
import `fun`.brush.viewmodel.util.Const.Companion.USER_NAME
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Bundle
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
import android.view.animation.AnimationUtils
import androidx.appcompat.R.anim.abc_slide_in_top
import androidx.appcompat.R.anim.abc_slide_out_bottom
import androidx.appcompat.app.AppCompatActivity
import cn.bmob.v3.BmobUser
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.SaveListener
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_more_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_more_login)
        window.decorView.systemUiVisibility = SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        if (BmobUser.getCurrentUser(User::class.java) != null) {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = FLAG_ACTIVITY_CLEAR_TASK or FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
        moreLogin.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade))
        moreLogin.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            intent.flags = FLAG_ACTIVITY_CLEAR_TASK or FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            overridePendingTransition(abc_slide_in_top, abc_slide_out_bottom)
            finish()
        }


        loginButton.setOnClickListener {
            // 获取输入要在单击事件下
            val user = User()
            val userName = userNameET.text.toString()
            val password = passwordET.text.toString()
            user.username = userName
            user.setPassword(password)
            if (userName.isEmpty() || password.isEmpty()) { //判断输入是否为空
                Snackbar.make(loginButton, "用户名或密码为空!", 1000).show()
                return@setOnClickListener
            }
            user.login(object : SaveListener<User?>() { //回调接口
                override fun done(user: User?, exception: BmobException?) =
                        if (exception == null) { //无异常则登录成功
                            loginButton.text = "登录成功"
                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            intent.flags = FLAG_ACTIVITY_CLEAR_TASK or FLAG_ACTIVITY_NEW_TASK
                            intent.putExtra(USER_NAME, userName)
                            intent.putExtra(AVATAR, user!!.avatar!!.fileUrl)
                            startActivity(intent)
                        } else {
                            Snackbar.make(loginButton, "登录失败!原因:${exception.message}", 1000).show()
                        }
            })
        }
    }
}
