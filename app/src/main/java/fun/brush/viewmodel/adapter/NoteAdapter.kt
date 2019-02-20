package `fun`.brush.viewmodel.adapter

import `fun`.brush.R
import `fun`.brush.model.bean.Note
import `fun`.brush.view.activity.options.NoteActivity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.note_item.view.*
import `fun`.brush.view.activity.options.NoteEditActivity

class NoteAdapter(private val mNoteList: List<Note>, private val mContext: Context) :
        RecyclerView.Adapter<NoteAdapter.ViewHolder>() {

    companion object {
        private const val TAG = "NoteAdapter"
    }

    class ViewHolder(var noteView: View) : RecyclerView.ViewHolder(noteView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteAdapter.ViewHolder {
        val view = LayoutInflater.from(mContext)
                .inflate(R.layout.note_item, parent, false)
        val holder = ViewHolder(view)
        holder.noteView.setOnClickListener {
            val position = holder.adapterPosition
            val note = mNoteList[position]
            val intent = Intent(mContext, NoteEditActivity::class.java)
            if (position == 0) {
                intent.putExtra("data", true)
            } else {
                intent.putExtra("data", false)
                intent.putExtra("objectID", note.objectId)
                intent.putExtra("content", note.content)
                Log.d(TAG, note.content)
            }
            mContext.startActivity(intent)
        }
        return holder
    }

    override fun onBindViewHolder(holder: NoteAdapter.ViewHolder, position: Int) {
        val note = mNoteList[position]
        if (position == 0) {
            holder.itemView.iv_notes.visibility = View.VISIBLE
        }
        holder.itemView.tv_notes.text = note.content
        holder.noteView.setOnLongClickListener {
            if (position == 0) {
            } else {
                Log.d(TAG, position.toString())
                (mContext as? NoteActivity)?.showDeleteDialog(mNoteList[position].objectId)
            }
            true
        }
    }

    override fun getItemCount(): Int {
        return mNoteList.size
    }

}
