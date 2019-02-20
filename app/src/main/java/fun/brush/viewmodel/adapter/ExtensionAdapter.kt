package `fun`.brush.viewmodel.adapter

import `fun`.brush.R
import `fun`.brush.model.bean.GrowBean
import `fun`.brush.view.activity.options.GirlsActivity
import `fun`.brush.view.activity.options.NoteActivity
import `fun`.brush.view.activity.options.TranslateActivity
import `fun`.brush.view.activity.options.WeatherActivity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.grid_options_item.view.*

class ExtensionAdapter(private var options: List<GrowBean.GridOptions>) :
        RecyclerView.Adapter<ExtensionAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(LayoutInflater
            .from(parent.context).inflate(R.layout.grid_options_item, parent, false))

    override fun getItemCount(): Int = options.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val context = holder.itemView.context
        Glide.with(context).load(options[position].logo).into(holder.optionLogoPic)
        holder.nameTxT.text = options[position].name
        holder.itemView.setOnClickListener {
            val intent = Intent()
            intent.putExtra(NAME, options[position].name)
            when (position) {
                0 -> {
                    context.startActivity(intent.setClass(context, WeatherActivity::class.java))
                }
                1 -> {
                    context.startActivity(intent.setClass(context, NoteActivity::class.java))
                }
                2 -> {
                    context.startActivity(intent.setClass(context, TranslateActivity::class.java))
                }
                3 -> {
                    context.startActivity(Intent(context, GirlsActivity::class.java))
                }
            }
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val optionLogoPic: ImageView = itemView.optionLogoPic
        val nameTxT: TextView = itemView.nameTxT
    }

    companion object {
        const val LOGO = "LOGO"
        const val NAME = "NAME"
    }
}