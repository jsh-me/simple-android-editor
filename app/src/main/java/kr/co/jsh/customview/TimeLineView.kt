package kr.co.jsh.customview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.util.LongSparseArray
import android.view.View
import kr.co.jsh.R
import kr.co.jsh.utils.ScreenSizeUtil


class TimeLineView @JvmOverloads constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int = 0) :
    View(context, attrs, defStyleAttr) {

    private var mVideoUri: Uri? = null //비디오 uri
    private var mHeightView: Int = 0
    private var mBitmapList: LongSparseArray<Bitmap>? = null

    init {
        init()
    }

    private fun init() {
        mHeightView = context.resources.getDimensionPixelOffset(R.dimen.frames_video_height)
    }

    fun drawView(bitmapList: LongSparseArray<Bitmap>?) {
        mBitmapList = bitmapList
        Log.i("bitmap:::", "${mBitmapList!!.size()}")
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
        setMeasuredDimension(mBitmapList!!.size() * ScreenSizeUtil(context).widthPixels/4 + ScreenSizeUtil(context).widthPixels , h)

    }

    //스크롤뷰
    //현재 캔버스 너비는 944 이고, x 는 2400 임 ( 240*10 )
    //캔버스 너비를 2400으로 맞추어야함....
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mBitmapList != null) {
            canvas.save()

//             var x = 0
            var x = ScreenSizeUtil(context).widthPixels / 2
            for (i in 0 until (mBitmapList?.size() ?: 0)) {
                val bitmap = mBitmapList?.get(i.toLong())
                if (bitmap != null) {
                    canvas.drawBitmap(bitmap, x.toFloat(), 0f, null)
                    x += bitmap.width
                    Log.i("x:", "${x}")
                }
            }
            Log.i(
                "onDraw: ",
                "${canvas.width} 는 canvas width, 그리고 bitmap List size 는  ${mBitmapList?.size()}"
            )
        }
    }

}