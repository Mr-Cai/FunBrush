package `fun`.brush.viewmodel.util

import `fun`.brush.R
import `fun`.brush.model.bean.User
import `fun`.brush.viewmodel.util.Const.Companion.PRINT
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore.*
import android.util.Log
import android.util.TypedValue.COMPLEX_UNIT_DIP
import android.util.TypedValue.applyDimension
import android.widget.ImageView
import cn.bmob.v3.BmobQuery
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.FindListener
import com.bumptech.glide.Glide

object Tool {
    var isBackFromSetNetwork: Boolean = false
    fun dp2px(context: Context, dpVal: Float) = applyDimension(COMPLEX_UNIT_DIP, dpVal,
            context.resources.displayMetrics).toInt() //dp转px

    fun isNetWorkConnected(context: Context): Boolean { //判断网络是否连接
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    fun getPath(context: Context, uri: Uri): String? {
        when {
            DocumentsContract.isDocumentUri(context, uri) -> if (isSD(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }
            } else {
                when {
                    isDownload(uri) -> {
                        val id = DocumentsContract.getDocumentId(uri)
                        val contentUri = ContentUris.withAppendedId(Uri
                                .parse("content://downloads/public_downloads"), id.toLong())
                        return getDataColumn(context, contentUri, null, null)
                    }
                    isMedia(uri) -> {
                        val docId = DocumentsContract.getDocumentId(uri)
                        val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                        val type = split[0]
                        var contentUri: Uri? = null
                        when (type) {
                            "image" -> contentUri = Images.Media.EXTERNAL_CONTENT_URI
                            "video" -> contentUri = Video.Media.EXTERNAL_CONTENT_URI
                            "audio" -> contentUri = Audio.Media.EXTERNAL_CONTENT_URI
                        }
                        val selection = "_id=?"
                        val selectionArgs = arrayOf(split[1])
                        if (contentUri != null)
                            return getDataColumn(context, contentUri, selection, selectionArgs)
                    }
                }
            }
            "content".equals(uri.scheme, ignoreCase = true) -> {
                if (isGooglePhoto(uri)) return uri.lastPathSegment
                return getDataColumn(context, uri, null, null)
            }
            "file".equals(uri.scheme, ignoreCase = true) -> return uri.path
        }
        return null
    }

    private fun getDataColumn(context: Context, uri: Uri, selection: String?,
                              selectionArgs: Array<String>?): String? {
        val cursor: Cursor?
        val column = "_data"
        val projection = arrayOf(column)
        cursor = context.contentResolver.query(uri, projection, selection,
                selectionArgs, null)
        if (cursor != null && cursor.moveToFirst())
            return cursor.getString(cursor.getColumnIndexOrThrow(column))
        cursor.close()
        return null
    }

    private fun isSD(uri: Uri) = "com.android.externalstorage.documents" == uri.authority
    private fun isDownload(uri: Uri) = "com.android.providers.downloads.documents" == uri.authority
    private fun isMedia(uri: Uri) = "com.android.providers.media.documents" == uri.authority
    private fun isGooglePhoto(uri: Uri) = "com.google.android.apps.photos.content" == uri.authority
    fun setImageToView(context: Context, username: String?, imageUri: String?, picture: ImageView) {
        when {
            username != null -> {
                val query = BmobQuery<User>()
                query.addWhereEqualTo("username", username)
                query.findObjects(object : FindListener<User>() {
                    override fun done(user: List<User>, exception: BmobException?) {
                        if (exception == null) {
                            if (user.isNotEmpty())
                                Glide.with(context).load(user[0].avatar?.fileUrl).into(picture)
                            else Log.i(PRINT, "${username}不存在了")
                        } else {
                            Log.i(PRINT, "${exception.errorCode}:${exception.message}")
                        }
                    }
                })
            }
            imageUri != null -> Glide.with(context).load(imageUri).into(picture)
            else -> Glide.with(context).load(R.drawable.nothing).into(picture)
        }
    }

}