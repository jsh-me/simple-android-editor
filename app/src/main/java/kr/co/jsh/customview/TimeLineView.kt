package kr.co.jsh.customview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.util.LongSparseArray
import android.view.View
import androidx.core.graphics.get
import kr.co.jsh.R
import kr.co.jsh.paint.Mypainter.Companion.count
import kr.co.jsh.utils.ScreenSizeUtil


class TimeLineView @JvmOverloads constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int = 0) :
    View(context, attrs, defStyleAttr) {

    private var mVideoUri: Uri? = null //비디오 uri
    private var mHeightView: Int = 0
    private var mBitmapList: LongSparseArray<Bitmap>? = null

    private var crop_x = 0
    private var crop_x2 = 0
    private var crop_count =0


    var crop_list_1 = ArrayList<Bitmap>()
    var crop_list_2 = ArrayList<Bitmap>()
    var crop_list_3 = ArrayList<Bitmap>()
    var crop_list_4 = ArrayList<Bitmap>()
    lateinit var b: Bitmap

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

    fun cropView(bitmapList: LongSparseArray<Bitmap>?, x1: Int, x2:Int, count: Int) {
        //requestLayout()
        invalidate()
        mBitmapList = bitmapList
        crop_x = x1 //첫번째로 자른 위치
        crop_x2 = x2 //두번째로 자른 위치, 첫번째만 잘랐다면 x2 = 0임.
        crop_count = count
    }

    fun resetView(count:Int){
        invalidate()
        crop_count = count
        crop_list_1.clear()
        crop_list_2.clear()
        crop_list_3.clear()
        crop_list_4.clear()
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



    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        var x = ScreenSizeUtil(context).widthPixels / 2

        if (mBitmapList != null && crop_count == 0) {
            for (i in 0 until (mBitmapList?.size() ?: 0)) {
                val bitmap = mBitmapList?.get(i.toLong())
                if (bitmap != null) {
                    canvas.drawBitmap(bitmap, x.toFloat(), 0f, null)
                    x += bitmap.width
                    Log.i("x:", "${x}")

                }
            }
        }
        else if(mBitmapList != null && crop_count ==1) {
            //canvas.save()
            //몫
            Log.i("다시그리기", "${crop_x / (ScreenSizeUtil(context).widthPixels / 4)}")
            Log.i("crop_x 와 screensizeUtil","${crop_x} and ${ScreenSizeUtil(context).widthPixels/ 4}")

            repeat(crop_x / (ScreenSizeUtil(context).widthPixels / 4)) { i ->
                b = mBitmapList!!.get(i.toLong())
                crop_list_1.add(b)
            }
            b = Bitmap.createBitmap(mBitmapList!!.get(crop_x / (ScreenSizeUtil(context).widthPixels / 4L)), 0,0, crop_x % mBitmapList!![0].width , mBitmapList!![0].height)
            crop_list_1.add(b)

            b = Bitmap.createBitmap(mBitmapList!!.get(crop_x / (ScreenSizeUtil(context).widthPixels / 4L)),crop_x % mBitmapList!![0].width,0, mBitmapList!![0].width - (crop_x % mBitmapList!![0].width) , mBitmapList!![0].height)
            crop_list_2.add(b)

            for ( i  in crop_x / (ScreenSizeUtil(context).widthPixels / 4) + 1 until mBitmapList!!.size() ) {
                b = mBitmapList!!.get(i.toLong())
                crop_list_2.add(b)
            }


            repeat(crop_list_1.size) { i ->
                crop_list_1[i]?.let {
                    canvas.drawBitmap(it, x.toFloat(), 0f, null)
                    // x += bitmap.width
                    x += crop_list_1[i].width
                }
            }
            x+=7 //Margin
            repeat(crop_list_2.size) { i ->
                crop_list_2[i]?.let {
                    canvas.drawBitmap(it, x.toFloat(), 0f, null)
                    // x += bitmap.width
                    x += crop_list_2[i].width
                }
            }

        }
        else if (mBitmapList != null && crop_count ==2){
            if( crop_x > crop_x2) { //crop_list_1 을 한번 더 쪼개야 함
                repeat(crop_x2 / (ScreenSizeUtil(context).widthPixels / 4)) { i ->
                    b = mBitmapList!!.get(i.toLong())
                    crop_list_3.add(b)
                }
                b = Bitmap.createBitmap(crop_list_1[crop_x2 / (ScreenSizeUtil(context).widthPixels / 4)] , 0,0, crop_x2 % crop_list_1!![0].width , crop_list_1!![0].height)
                crop_list_3.add(b)

                b = Bitmap.createBitmap(crop_list_1[crop_x2 / (ScreenSizeUtil(context).widthPixels / 4)],crop_x2 % crop_list_1!![0].width ,0, crop_list_1[crop_x2 / (ScreenSizeUtil(context).widthPixels / 4)].width - crop_x2 % crop_list_1!![0].width , mBitmapList!![0].height)
                crop_list_4.add(b)

                for (i in crop_x2 / (ScreenSizeUtil(context).widthPixels / 4L) +1 until crop_list_1.size) {
                    b=crop_list_1[i.toInt()]
                    crop_list_4.add(b)
                }
                repeat(crop_list_3.size) { i ->
                    crop_list_3[i]?.let {
                        canvas.drawBitmap(it, x.toFloat(), 0f, null)
                        // x += bitmap.width
                        x += crop_list_3[i].width
                    }
                }
                x+=7
                repeat(crop_list_4.size) { i ->
                    crop_list_4[i]?.let {
                        canvas.drawBitmap(it, x.toFloat(), 0f, null)
                        // x += bitmap.width
                        x += crop_list_4[i].width
                    }
                }
                x+=7
                repeat(crop_list_2.size) { i ->
                    crop_list_2[i]?.let {
                        canvas.drawBitmap(it, x.toFloat(), 0f, null)
                        // x += bitmap.width
                        x += crop_list_2[i].width
                    }
                }

            }
            else { //crop_list_2 를 한번 더 쪼개야 함

                if (crop_x2 - crop_x - crop_list_2[0].width > 0) {
                    crop_list_3.add(crop_list_2[0])
                    for (i in 1..(crop_x2 - crop_x - crop_list_3[0].width) / (ScreenSizeUtil(context).widthPixels / 4)) {
                        b = crop_list_2[i]
                        crop_list_3.add(b)
                    }

                    b = Bitmap.createBitmap(
                        crop_list_2[((crop_x2 - crop_x - crop_list_3[0].width) / ScreenSizeUtil(
                            context
                        ).widthPixels / 4) + 1],
                        0,
                        0,
                        crop_x2 % (ScreenSizeUtil(context).widthPixels / 4),
                        mBitmapList!![0].height
                    )

                    crop_list_3.add(b)
                    b = Bitmap.createBitmap(
                        crop_list_2[((crop_x2 - crop_x - crop_list_3[0].width) / ScreenSizeUtil(
                            context
                        ).widthPixels / 4) + 1],
                        crop_x2 % (ScreenSizeUtil(context).widthPixels / 4),
                        0,
                        ScreenSizeUtil(context).widthPixels / 4 - (crop_x2 % (ScreenSizeUtil(context).widthPixels / 4)),
                        mBitmapList!![0].height
                    )
                    crop_list_4.add(b)

                    for (i in ((crop_x2 - crop_x - crop_list_3[0].width) / ScreenSizeUtil(context).widthPixels / 4) + 2 until crop_list_2.size) {
                        b = crop_list_2[i]
                        crop_list_4.add(b)
                    }

                } else if (crop_x2 - crop_x - crop_list_2[0].width < 0) {
                    crop_list_3.clear()
                    b = Bitmap.createBitmap(
                        crop_list_2[0],
                        0,
                        0,
                        crop_x2 - crop_x,
                        crop_list_2[0].height
                    )
                    crop_list_3.add(b)

                    b = Bitmap.createBitmap(
                        crop_list_2[0],
                        crop_x2 - crop_x,
                        0,
                        crop_list_2[0].width - (crop_x2 - crop_x),
                        crop_list_2[0].height
                    )
                    crop_list_4.add(b)

                    for (i in 1 until crop_list_2.size) {
                        b = crop_list_2[i]
                        crop_list_4.add(b)
                    }


                }

                repeat(crop_list_1.size) { i ->
                    crop_list_1[i]?.let {
                        canvas.drawBitmap(it, x.toFloat(), 0f, null)
                        // x += bitmap.width
                        x += crop_list_1[i].width
                    }
                }
                x += 7

                repeat(crop_list_3.size) { i ->
                    crop_list_3[i]?.let {
                        canvas.drawBitmap(it, x.toFloat(), 0f, null)
                        // x += bitmap.width
                        x += crop_list_3[i].width
                    }
                }
                x += 7
                repeat(crop_list_4.size) { i ->
                    crop_list_4[i]?.let {
                        canvas.drawBitmap(it, x.toFloat(), 0f, null)
                        // x += bitmap.width
                        x += crop_list_4[i].width
                    }
                }
            }
        }
    }
}