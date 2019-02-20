package `fun`.brush.view.activity.options

import `fun`.brush.R
import `fun`.brush.model.bean.Note
import `fun`.brush.model.bean.User
import `fun`.brush.viewmodel.adapter.NoteAdapter
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager.VERTICAL
import cn.bmob.v3.BmobQuery
import cn.bmob.v3.BmobUser
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.FindListener
import cn.bmob.v3.listener.UpdateListener
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_note.*
import java.util.*

class NoteActivity : AppCompatActivity() {
    private lateinit var noteAdapter: NoteAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)
        val user = BmobUser.getCurrentUser(User::class.java)
        if (user == null) {
            Snackbar.make(noteRecycler, "尚未登录,登录后方可使用!", 1000).show()
            return
        }
        title = "便签"
        updateData()
    }

    private fun updateData() {
        val query = BmobQuery<Note>()
        query.findObjects(object : FindListener<Note>() {
            override fun done(list: MutableList<Note>, e: BmobException?) {
                if (e == null) {
                    if (list.size == 0) {
                        Snackbar.make(noteRecycler, "你还没有任何笔记", 1000).show()
                        showOneItem()
                        return
                    }
                    val layoutManager = StaggeredGridLayoutManager(2, VERTICAL)
                    noteRecycler!!.layoutManager = layoutManager
                    noteRecycler!!.isNestedScrollingEnabled = false
                    val note = Note()
                    note.content = ""
                    list.add(note)
                    list.reverse()
                    noteAdapter = NoteAdapter(list, this@NoteActivity)
                    noteRecycler!!.adapter = noteAdapter
                } else {
                    Snackbar.make(noteRecycler, "出现异常${e.message}", 1000).show()
                    showOneItem()
                }
            }
        })
    }

    fun showDeleteDialog(objectID: String) {
        val strings = arrayOf("确定要删除?")
        AlertDialog.Builder(this)
                .setItems(strings) { _, i ->
                    if (i == 0) {
                        val note = Note()
                        note.objectId = objectID
                        note.delete(object : UpdateListener() {
                            override fun done(e: BmobException?) {
                                if (e == null) {
                                    updateData()
                                    Snackbar.make(noteRecycler, "删除成功", 1000).show()
                                } else {
                                    Snackbar.make(noteRecycler, "删除失败", 1000).show()
                                }
                            }
                        })
                    }
                }.create().show()
    }

    fun showOneItem() {
        val noteList = ArrayList<Note>()
        val layoutManager = StaggeredGridLayoutManager(2, VERTICAL)
        noteRecycler!!.layoutManager = layoutManager
        noteRecycler!!.isNestedScrollingEnabled = false
        val note = Note()
        note.content = ""
        noteList.add(note)
        noteAdapter = NoteAdapter(noteList, this@NoteActivity)
        noteRecycler!!.adapter = noteAdapter
    }

    override fun onRestart() {
        super.onRestart()
        updateData()
    }
}
