package `fun`.brush.view.custom

import `fun`.brush.R
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.TypedValue
import android.util.TypedValue.COMPLEX_UNIT_DIP
import androidx.appcompat.widget.AppCompatImageView

class RoundImage(context: Context, attrs: AttributeSet) : AppCompatImageView(context, attrs) {
    private var mImgType: Int = 0//图片类型
    private var mBorderRadius: Int = 0//圆角大小
    private val mBitmapPaint: Paint = Paint()//绘图画笔
    private var mRadius: Int = 0//圆角半径
    private val mMatrix: Matrix = Matrix()//缩放3x3矩阵
    private var mBitmapShader: BitmapShader? = null//渲染图像颜色
    private var mWidth: Int = 0//图像宽度
    private var mRoundRect: RectF? = null//圆角矩形

    companion object {
        const val TYPE_CIRCLE = 0//圆形图片
        const val TYPE_ROUND = 1//圆角图片
        const val BORDER_RADIUS_DEFAULT = 10//圆角图片
        const val STATE_INSTANCE = "STATE_INSTANCE"
        const val STATE_TYPE = "STATE_TYPE"
        const val STATE_BORDER_RADIUS = "STATE_BORDER_RADIUS"
    }

    init {
        mBitmapPaint.isAntiAlias = true
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.RoundImage)
        mBorderRadius = typedArray.getDimensionPixelSize(R.styleable.RoundImage_borderRadius,
                TypedValue.applyDimension(COMPLEX_UNIT_DIP, BORDER_RADIUS_DEFAULT.toFloat(),
                        resources.displayMetrics).toInt())
        mImgType = typedArray.getInt(R.styleable.RoundImage_type, TYPE_CIRCLE)//默认圆形图片
        typedArray.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (mImgType == TYPE_CIRCLE) {//如果改变图片类型为圆形,则强制图片宽高一致
            mWidth = Math.min(measuredWidth, measuredHeight)
            mRadius = mWidth / 2
            setMeasuredDimension(mWidth, mWidth)
        }
    }

    private fun setUpShader() {//设置渲染着色
        val drawable = drawable ?: return
        val bitmap = drawableToBitmap(drawable)//指定区域内绘制着色的位图
        mBitmapShader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        var scale = 1.0f
        if (mImgType == TYPE_CIRCLE) {
            val bitmapSize = Math.min(bitmap.width, bitmap.height)
            scale = mWidth * 1.0f / bitmapSize
        } else if (mImgType == TYPE_ROUND) {//如果图片宽高与自定义控件宽高不匹配,则计算缩放比例
            scale = Math.max(width * 1.0f / bitmap.width,
                    height * 1.0f / bitmap.height)
        }
        mMatrix.setScale(scale, scale)//缩放矩阵
        mBitmapShader!!.setLocalMatrix(mMatrix)//变换矩阵
        mBitmapPaint.shader = mBitmapShader//设置渲染器
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }
        val width = drawable.intrinsicWidth
        val height = drawable.intrinsicHeight
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ALPHA_8)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, width, height)
        drawable.draw(canvas)
        return bitmap
    }

    override fun onDraw(canvas: Canvas) {
        setUpShader()
        if (mImgType == TYPE_CIRCLE) {
            canvas.drawCircle(mRadius.toFloat(), mRadius.toFloat(),
                    mRadius.toFloat(), mBitmapPaint)
        } else {
            canvas.drawRoundRect(mRoundRect!!, mBorderRadius.toFloat(),
                    mBorderRadius.toFloat(), mBitmapPaint)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
        super.onSizeChanged(w, h, oldW, oldH)
        if (mImgType == TYPE_ROUND) {
            mRoundRect = RectF(0f, 0f, width.toFloat(), height.toFloat())
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        val bundle = Bundle()
        bundle.putParcelable(STATE_INSTANCE, super.onSaveInstanceState())
        bundle.putInt(STATE_TYPE, mImgType)
        bundle.putInt(STATE_BORDER_RADIUS, mBorderRadius)
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state is Bundle) {
            super.onRestoreInstanceState(state.getParcelable(STATE_INSTANCE))
            mImgType = state.getInt(STATE_TYPE)
            mBorderRadius = state.getInt(STATE_BORDER_RADIUS)
        } else {
            super.onRestoreInstanceState(state)
        }
    }
}
