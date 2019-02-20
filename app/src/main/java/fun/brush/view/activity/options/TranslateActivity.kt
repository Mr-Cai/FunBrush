package `fun`.brush.view.activity.options

import `fun`.brush.R
import `fun`.brush.model.bean.Translate
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_translate.*
import kotlinx.android.synthetic.main.edit_area.*
import kotlinx.android.synthetic.main.explain.*
import kotlinx.android.synthetic.main.explains_item.view.*
import kotlinx.android.synthetic.main.phrase.*
import kotlinx.android.synthetic.main.phrase_item.view.*
import kotlinx.android.synthetic.main.query_layout.*
import kotlinx.android.synthetic.main.translate.*
import kotlinx.android.synthetic.main.translate_item.view.*
import okhttp3.*
import java.io.IOException

class TranslateActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_translate)
        setSupportActionBar(toolbar)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.statusBarColor = Color.parseColor("#ffff4444")
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        wordET.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (wordET.editableText.toString().isNotEmpty()) { //如果有文字输入就显示清除和翻译按钮
                    clearInput.visibility = View.VISIBLE
                } else { // 否则设为隐藏
                    clearInput.visibility = View.INVISIBLE
                }
            }
        })
        clearInput.setOnClickListener { wordET.text = null } //清理文字
        translateFB.setOnClickListener {
            //执行翻译过程
            if (wordET.text.toString().isEmpty()) {
                Snackbar.make(wordET, "您忘记输入文字了\uD83D\uDE05", 1000).show()
            } else {
                progressBar.visibility = View.VISIBLE //显示加载进度圈
                requestData() //请求API
            }
        }
    }

    private fun requestData() {
        val word = wordET.text
        OkHttpClient().newCall(Request.Builder().url("$translateApi$word").build())
                .enqueue(object : Callback {
                    override fun onFailure(p0: Call?, p1: IOException?) {
                        runOnUiThread {
                            //即时网络请求失败依然不能忘记开启子线程
                            progressBar.visibility = View.GONE
                            Snackbar.make(wordET, "您忘记输入文字了\uD83D\uDE05", 1000).show()

                        }
                    }

                    override fun onResponse(call: Call?, response: Response) {
                        val translate = Gson().fromJson(response.body()!!.string(), Translate::class.java)
                        runOnUiThread {
                            when {
                                translate.errorCode == 0 -> { //没有错误表示可以翻译
                                    showTranslateInfo(translate)
                                }
                                translate.errorCode == 20 -> {
                                    progressBar!!.visibility = View.GONE
                                    Snackbar.make(wordET, "输入文本过长\uD83D\uDE05", 1000).show()
                                }
                                else -> {
                                    progressBar!!.visibility = View.GONE
                                    Snackbar.make(wordET, "连接超时\uD83D\uDE05", 1000).show()
                                }
                            }
                        }
                    }

                })
    }

    @SuppressLint("SetTextI18n")
    private fun showTranslateInfo(translate: Translate) { //显示翻译文字
        transitionLayout.removeAllViews() //清空上次翻译的布局
        explainsLayout.removeAllViews() //清空上次翻译的详情
        phraseLayout.removeAllViews() //清空上次翻译的短语
        for (i in 0 until translate.translation!!.size) {
            val view = LayoutInflater.from(this).inflate(R.layout.translate_item, transitionLayout, false)
            val translateTxT: TextView = view.translateTxT
            translateTxT.text = translate.translation!![i] //迭代出翻译的文字
            transitionLayout.addView(view)
        }
        queryTxT.text = translate.query
        if (translate.basic == null) { //如果翻译没有音标隐藏音标布局和详细释义
            phoneticTxT.visibility = View.INVISIBLE
            explainsLayout.visibility = View.INVISIBLE
        } else {
            phoneticTxT.text = "[${translate.basic!!.phonetic}]"
            for (i in 0 until translate.basic!!.explains!!.size) {
                val view = LayoutInflater.from(this).inflate(R.layout.explains_item, explainsLayout, false)
                val explainsTxT: TextView = view.explainsTxT
                explainsTxT.text = translate.basic!!.explains!![i]
                explainsLayout.addView(view)
            }
        }
        if (translate.web == null) {
            phraseLayout.visibility = View.INVISIBLE
        } else {
            for (i in 0 until translate.web!!.size) {
                val view = LayoutInflater.from(this).inflate(R.layout.phrase_item, phraseLayout, false)
                val keyText: TextView = view.key_text
                val valueText: TextView = view.value_text
                keyText.text = translate.web!![i].key
                val values = getFinalValue(translate.web!![i].value!!) //获取查询的短语
                valueText.text = values
                phraseLayout.addView(view)
                phraseLayout.visibility = View.VISIBLE
            }
        }
        progressBar.visibility = View.GONE
        copy.setOnClickListener {
            //单击复制翻译文本至剪贴板
            val manager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("text", translate.translation!![0])
            manager.primaryClip = clipData
            Snackbar.make(translateFB!!, "复制成功", 1000).show()
        }
        share.setOnClickListener {
            //分享翻译文本
            val intent = Intent()
            intent.setAction(Intent.ACTION_SEND).type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, translate.translation!![0])
            startActivity(Intent.createChooser(intent, "请选择要分享的应用"))
        }
        mixLayout.visibility = View.VISIBLE
    }

    private fun getFinalValue(value: Array<String>): CharSequence? {
        var finalValue = ""
        for (i in value.indices) {
            if (i == value.size - 1) {
                finalValue += value[i]
            } else {
                finalValue = finalValue + value[i] + ","
            }
        }
        return finalValue
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) finish()
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val translateApi = "http://fanyi.youdao.com/openapi.do?keyfrom=zhaotranslator&key=" +
                "1681711370&type=data&doctype=json&version=1.1&q="
    }
}
