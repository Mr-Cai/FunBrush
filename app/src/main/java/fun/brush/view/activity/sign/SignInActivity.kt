package `fun`.brush.view.activity.sign

import `fun`.brush.R
import `fun`.brush.view.activity.MainActivity
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.R.anim.*
import androidx.appcompat.app.AppCompatActivity
import cn.bmob.v3.BmobSMS
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.QueryListener
import cn.bmob.v3.listener.UpdateListener
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_sign_in.*

class SignInActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager //身体传感器
    private lateinit var acceleration: Sensor //加速传感
    lateinit var phone: String
    lateinit var captcha: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        shakeIcon.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake)) //摇一摇动画
        moreLogin.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade)) //箭头渐隐动画
        moreLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = FLAG_ACTIVITY_CLEAR_TASK or FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            //从底部弹出页面动画
            overridePendingTransition(abc_grow_fade_in_from_bottom, abc_slide_out_bottom)
        }
        sendCaptcha.setOnClickListener {
            //发送验证码
            phone = phoneET.text.toString().trim()
            captcha = captchaET.text.toString().trim()
            BmobSMS.requestSMSCode(phone, null, object : QueryListener<Int>() {
                override fun done(smsId: Int?, exception: BmobException?) {
                    if (exception == null) {
                        loginButton.text = "验证码发送成功"
                        object : CountDownTimer(1000, 1000) {
                            override fun onFinish() {
                                sendCaptcha.text = "登录"
                            }

                            override fun onTick(millisUntilFinished: Long) {}
                        }.start()
                        object : CountDownTimer(30000, 1000) {
                            override fun onFinish() {
                                sendCaptcha.text = "发送验证码"
                            }

                            override fun onTick(millisUntilFinished: Long) {
                                sendCaptcha.text = "${millisUntilFinished.toInt() / 1000}"
                                sendCaptcha.isClickable = false
                            }
                        }.start() //启动倒计时
                    } else {
                        Snackbar.make(loginButton, "出现异常:${exception.message}", 1000).show()
                    }
                }
            })
        }
        loginButton.setOnClickListener { _ ->
            phone = phoneET.text.toString().trim()
            captcha = captchaET.text.toString().trim()
            if (phone.isEmpty() || captcha.isEmpty()) {
                Snackbar.make(loginButton, "手机号或验证码不能为空", 1000).show()
                return@setOnClickListener
            }
            /*  BmobUser.signOrLoginByMobilePhone(phone, captcha, object : LogInListener<BmobUser>() {
                  override fun done(user: BmobUser, exception: BmobException?) {
                      if (exception == null) {
                          loginButton.text = "登录成功"
                          startActivity(Intent(this@SignInActivity, MainActivity::class.java))
                      } else {
                          Snackbar.make(loginButton, "登录失败->异常原因:${exception.message}", 1000).show()
                      }
                  }
              }) //绑定手机号
              BmobSMS.verifySmsCode(phone, captcha, object : UpdateListener() {
                  override fun done(exception: BmobException?) {
                      if (exception == null) {
                          val user = BmobUser.getCurrentUser<User>(User::class.java)
                          user.mobilePhoneNumber = phone
                          user.mobilePhoneNumberVerified = true
                          user.update(object : UpdateListener() {
                              override fun done(exception: BmobException?) {
                                  if (exception == null) {
                                      loginButton.text = "手机号绑定成功"
                                      startActivity(Intent(this@SignInActivity, MainActivity::class.java))
                                  }
                              }
                          })
                      } else {
                          Snackbar.make(loginButton, "验证码验证失败->异常原因:${exception.message}", 1000).show()
                      }
                  }

              })*/
            BmobSMS.verifySmsCode(phone, captcha, object : UpdateListener() {
                override fun done(exception: BmobException?) {
                    if (exception == null) {
                        loginButton.text = "验证成功"
                        startActivity(Intent(this@SignInActivity, MainActivity::class.java))
                    } else {
                        Snackbar.make(loginButton, "验证失败${exception.message}", 1000).show()
                    }
                }
            })
        }
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        acceleration = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, acceleration, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {//加速传感器返回3个坐标值
            if (System.currentTimeMillis() < 50) return////传感器检测时间过短
            val autoX = event.values[0]
            val autoY = event.values[1]
            val autoZ = event.values[2]
            if (Math.sqrt((autoX * autoX + autoY * autoY + autoZ * autoZ).toDouble()) >= 20) {
                val intent = Intent(this, SignUpActivity::class.java)
                intent.flags = FLAG_ACTIVITY_CLEAR_TASK or FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
        }
    }

}
