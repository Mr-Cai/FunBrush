package `fun`.brush.view.activity

import `fun`.brush.R
import `fun`.brush.view.activity.sign.LoginActivity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_start.*

class StartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.statusBarColor = Color.TRANSPARENT
        startIcon.startAnimation(AnimationUtils.loadAnimation(this, R.anim.start_in))
        Handler().postDelayed({
            startIcon.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate))
            startActivity(Intent(this@StartActivity, LoginActivity::class.java))
            finish()
        }, 1000)
    }
}
