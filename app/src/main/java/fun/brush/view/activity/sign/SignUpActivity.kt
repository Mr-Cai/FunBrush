package `fun`.brush.view.activity.sign

import `fun`.brush.R
import `fun`.brush.R.anim.shake
import `fun`.brush.model.bean.User
import `fun`.brush.view.activity.MainActivity
import `fun`.brush.viewmodel.util.Const.Companion.PICK
import `fun`.brush.viewmodel.util.Tool
import android.content.Context
import android.content.Intent
import android.content.Intent.*
import android.hardware.Sensor
import android.hardware.Sensor.TYPE_ACCELEROMETER
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore.Images.Media.getBitmap
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
import android.view.animation.AnimationUtils.loadAnimation
import androidx.appcompat.app.AppCompatActivity
import cn.bmob.v3.BmobUser
import cn.bmob.v3.datatype.BmobFile
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.SaveListener
import cn.bmob.v3.listener.UploadFileListener
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_sign_up.*
import java.io.File

class SignUpActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private lateinit var acceleration: Sensor
    private var userIconUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        acceleration = sensorManager.getDefaultSensor(TYPE_ACCELEROMETER)
        window.decorView.systemUiVisibility = SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        shakeIcon.startAnimation(loadAnimation(this, shake))
        userIconPic.setOnClickListener {
            val intent = Intent(ACTION_PICK) //跳转至系统选择器界面
            intent.type = "image/*" //设置为选取图片
            startActivityForResult(intent, PICK)
        }
        registerButton.setOnClickListener {
            if (BmobUser.getCurrentUser() != null) {
                Snackbar.make(registerButton, "已有用户登录,请退出登录后再注册", 1000).show()
                return@setOnClickListener
            }
            val userName = userNameET.text.toString()
            val password = passwordET.text.toString()
            if (userName.isEmpty() || password.isEmpty() || userIconUri == null) {
                Snackbar.make(registerButton, "请确保头像|用户名|密码|均已设置", 1000).show()
                return@setOnClickListener
            }
            val avatarFile = BmobFile(File(Tool.getPath(this, userIconUri!!))) //获取头像

            avatarFile.uploadblock(object : UploadFileListener() {
                override fun done(exception: BmobException?) {
                    if (exception == null) {
                        val user = User() //上传头像
                        user.avatar = avatarFile
                        user.username = userName
                        user.setPassword(password)
                        user.signUp(object : SaveListener<User>() {
                            override fun done(user: User?, exception: BmobException?) {
                                if (user != null && exception == null) { //无异常代表注册成功
                                    val intent = Intent(this@SignUpActivity, MainActivity::class.java)
                                    intent.flags = FLAG_ACTIVITY_CLEAR_TASK or FLAG_ACTIVITY_NEW_TASK
                                    startActivity(intent)
                                } else {
                                    Snackbar.make(registerButton, "注册失败\n错误信息${exception!!.message}", 1000).show()
                                }
                            }
                        })
                    } else {
                        Snackbar.make(registerButton, "注册失败\n错误信息${exception.message}", 1000).show()
                    }
                }
            })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        userIconUri = data?.data
        if (requestCode == PICK && resultCode == RESULT_OK && data != null) //加载来自选择器的图片
            Glide.with(this).load(getBitmap(contentResolver, data.data)).into(userIconPic)
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, acceleration, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == TYPE_ACCELEROMETER) {//加速传感器返回3个坐标值
            if (System.currentTimeMillis() < 50) return //传感器检测时间过短
            val autoX = event.values[0]
            val autoY = event.values[1]
            val autoZ = event.values[2]
            if (Math.sqrt((autoX * autoX + autoY * autoY + autoZ * autoZ).toDouble()) >= 20) {
                val intent = Intent(this, SignInActivity::class.java)
                intent.flags = FLAG_ACTIVITY_CLEAR_TASK or FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
        }
    }
}
