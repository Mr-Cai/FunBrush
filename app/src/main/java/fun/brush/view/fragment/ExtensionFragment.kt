package `fun`.brush.view.fragment

import `fun`.brush.R
import `fun`.brush.viewmodel.adapter.ExtensionAdapter
import `fun`.brush.model.bean.GrowBean.GridOptions
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.fragment_grow.*

class ExtensionFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState:
    Bundle?): View? = inflater.inflate(R.layout.fragment_grow, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        growRecycler.layoutManager = GridLayoutManager(context, 3)
        growRecycler.adapter = ExtensionAdapter(getOptions())
    }

    private fun getOptions(): List<GridOptions> {
        val options = ArrayList<GridOptions>()
        options.add(GridOptions(R.drawable.ic_weather, "天气"))
        options.add(GridOptions(R.drawable.ic_note, "便签"))
        options.add(GridOptions(R.drawable.ic_translate, "翻译"))
        options.add(GridOptions(R.drawable.ic_girl, "美女"))
        return options
    }
}