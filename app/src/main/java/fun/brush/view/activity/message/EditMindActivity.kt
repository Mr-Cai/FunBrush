package `fun`.brush.view.activity.message

import `fun`.brush.R
import `fun`.brush.model.bean.MindSet
import `fun`.brush.viewmodel.util.Const.Companion.PICK
import `fun`.brush.viewmodel.util.Const.Companion.STORAGE
import `fun`.brush.viewmodel.util.Tool
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_DENIED
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.MediaStore.Images.Media.getBitmap
import android.view.Menu
import android.view.MenuItem
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat.checkSelfPermission
import cn.bmob.v3.BmobUser
import cn.bmob.v3.datatype.BmobFile
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.SaveListener
import cn.bmob.v3.listener.UploadFileListener
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_edit_mind.*
import java.io.File

class EditMindActivity : AppCompatActivity() {
    private lateinit var userName: String
    private lateinit var titleTxT: String
    private lateinit var sendTxT: String
    private var imageUri: Uri? = null  //初始化图片标识为空
    private var isBackFromNetwork = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_mind)
        setSupportActionBar(toolbar)
        title = null
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        choosePic.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate))
        locationTxT.startAnimation(AnimationUtils.loadAnimation(this, R.anim.alpha))
        choosePic.setOnClickListener {
            //如果使用兼容上下文调用自检权限被禁用
            if (checkSelfPermission(this, READ_EXTERNAL_STORAGE) == PERMISSION_DENIED) {
                //使用兼容活动请求权限
                requestPermissions(this, arrayOf(READ_EXTERNAL_STORAGE), STORAGE)
            } else {
                pickImage() //选择图片
            }
        }
        hint.setOnClickListener {
            Glide.with(this).load(R.drawable.nothing).into(choosePic)
        }
    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK) //设置选择器意图
        intent.type = "image/*" //类型为图片
        startActivityForResult(intent, PICK) //打开图库选择页面
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.publish_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            R.id.publishMind -> {
                postDynamic() //发布动态
            }
        }
        return true
    }

    private fun postDynamic() {
        //提交至云端之前要进行网络判断
        if (Tool.isNetWorkConnected(this)) { //如果网络已连接
            uploadItem() //上传一条记录至云端
        } else {
            isBackFromNetwork = true
            //跳转至网络设置页面
        }
    }

    private fun uploadItem() {
        if (BmobUser.getCurrentUser() != null) { //如果当前有用户登录
            userName = BmobUser.getCurrentUser().username
            titleTxT = titleET.text.toString()
            sendTxT = contentET.text.toString()
            if (titleTxT.isEmpty() || sendTxT.isEmpty() || imageUri == null) {
                Snackbar.make(titleET, "请检查有没有忘填的东西", 1000).show()
            }
            if (imageUri != null) { //图片已选择
                //获取当前选择图片的Uri,这比较复杂,封装在了工具类
                val imageFile = BmobFile(File(Tool.getPath(this, imageUri!!)))
                imageFile.uploadblock(object : UploadFileListener() {
                    override fun done(exception: BmobException?) {
                        if (exception == null) { //如果无异常代表上传文件的通道打开了
                            uploadItem(imageFile) //上传一条记录至云端
                        } else {
                            Snackbar.make(titleET, "上传失败", 1000).show()
                        }
                    }
                })
            } else uploadItem(null) //上传一条无图片记录
        }
    }

    private fun uploadItem(userIcon: BmobFile?) {
        val mindSet = MindSet(userName, titleTxT, sendTxT, userIcon) //实体类要和表名一致
        mindSet.save(object : SaveListener<String>() { //保存至服务器表里
            override fun done(objectId: String, exception: BmobException?) {
                if (exception == null) {
                    Snackbar.make(choosePic, "发送成功", 1000).show()
                    object : CountDownTimer(1000, 1000) {
                        override fun onFinish() {
                            startActivity(Intent(this@EditMindActivity, PublishActivity::class.java))
                        }

                        override fun onTick(millisUntilFinished: Long) {
                        }
                    }
                    finish()
                } else {
                    Snackbar.make(choosePic, "发送失败${exception.message}", 1000).show()
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if (isBackFromNetwork && Tool.isBackFromSetNetwork) { //网络已连接
            isBackFromNetwork = false
            Tool.isBackFromSetNetwork = false
            if (Tool.isNetWorkConnected(this)) {
                uploadItem()
            } else {
                Snackbar.make(choosePic, "网络连接失败!", 1000).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) { //活动结果集处理
        super.onActivityResult(requestCode, resultCode, data)
        imageUri = data!!.data //图片标识
        if (requestCode == PICK && resultCode == Activity.RESULT_OK && data.data != null) {
            Glide.with(this).load(getBitmap(contentResolver, data.data)).into(choosePic)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE && grantResults.isNotEmpty()
                && grantResults[0] == PERMISSION_GRANTED) {
            pickImage() //权限授予可以选择图片
        } else { //未获取权限重启弹出对话框
            requestPermissions(this, arrayOf(READ_EXTERNAL_STORAGE), STORAGE)
        }
    }

}
