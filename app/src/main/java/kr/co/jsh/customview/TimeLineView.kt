package kr.co.jsh.customview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import androidx.appcompat.widget.AppCompatImageView
import kr.co.jsh.R
import kr.co.jsh.utils.ScreenSizeUtil


class TimeLineView @JvmOverloads constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int = 0) :
    AppCompatImageView(context, attrs, defStyleAttr) {

    private var mVideoUri: Uri? = null //비디오 uri
    private var mHeightView: Int = 0
    private var mBitmapList: ArrayList<Bitmap>? = null

    //lateinit var b: Bitmap

    init {
        init()
    }

    private fun init() {
        mHeightView = context.resources.getDimensionPixelOffset(R.dimen.frames_video_height)
    }

    fun drawView(bitmapList: ArrayList<Bitmap>?) {
        mBitmapList = bitmapList
        Log.i("bitmap:::", "${mBitmapList!!.size}")
    }


    //widthMeasureSpec, heightMeasureSpec : 부모 컨테이너의 가로, 세로 크기
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val h = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> (paddingTop + paddingBottom + mHeightView)
                .coerceAtMost(heightSize)
            else -> heightMeasureSpec
        }
        setMeasuredDimension(
            mBitmapList!!.size * ScreenSizeUtil(context).widthPixels / 4 + ScreenSizeUtil(context).widthPixels, h
        )

    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        var x = ScreenSizeUtil(context).widthPixels / 2

        if (mBitmapList != null) {
            for (i in 0 until (mBitmapList?.size ?: 0)) {
                val bitmap = mBitmapList?.get(i)
                if (bitmap != null) {
                    canvas.drawBitmap(bitmap, x.toFloat(), 0f, null)
                    x += bitmap.width
                    Log.i("x:", "${x}")

                }
            }
        }
    }

}