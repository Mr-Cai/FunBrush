package `fun`.brush.view.activity.options

import `fun`.brush.R
import `fun`.brush.model.bean.Note
import `fun`.brush.model.bean.User
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cn.bmob.v3.BmobACL
import cn.bmob.v3.BmobUser
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.SaveListener
import cn.bmob.v3.listener.UpdateListener
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_note_edit.*

class NoteEditActivity : AppCompatActivity() {

    private var isAdd: Boolean = false
    private lateinit var contentBefore: String
    private lateinit var objectID: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_edit)
        title = "记录"
        isAdd = intent.getBooleanExtra("data", true)
        if (isAdd) {
            Snackbar.make(noteET, "可以使用便签了\uD83D\uDE03", 2000).show()
        } else {
            contentBefore = intent.getStringExtra("content")
            objectID = intent.getStringExtra("objectID")
            noteET!!.setText(contentBefore)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> updateOrSaveData()
        }
        return true
    }

    private fun updateOrSaveData() {
        val contentAfter = noteET!!.text.toString()
        if (isAdd) {
            if (contentAfter == "") {
                finish()
            } else {
                val user = BmobUser.getCurrentUser(User::class.java)
                val note = Note()
                note.content = contentAfter
                note.user = user
                val acl = BmobACL()
                acl.setReadAccess(user, true)
                acl.setWriteAccess(user, true)
                note.acl = acl
                note.save(object : SaveListener<String>() {
                    override fun done(s: String, e: BmobException?) {
                        if (e == null) {
                            Snackbar.make(noteET, "保存成功", 1000).show()
                            finish()
                        } else {
                            Snackbar.make(noteET, "保存失败", 1000).show()
                        }
                    }
                })
            }
        } else {
            if (contentAfter == contentBefore) {
                finish()
            } else {
                val note = Note()
                note.content = contentAfter
                note.update(objectID, object : UpdateListener() {
                    override fun done(e: BmobException?) {
                        if (e == null) {
                            Toast.makeText(this@NoteEditActivity, "便签更新成功!", Toast.LENGTH_LONG).show()
                            finish()
                        } else {
                            Snackbar.make(noteET, "出现异常:${e.message}", 2000).show()
                        }
                    }
                })
            }
        }
    }

    override fun onBackPressed() {
        updateOrSaveData()
    }
}
