package kr.co.jsh.feature.paint

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatImageView


//painter 객체가 늘어날수록 펜 색을 늘릴 수 있음
//arraylist<> 로 point 저장
class Mypainter : AppCompatImageView {
    var oldX = -1f
    var oldY = -1f
    lateinit var mbitmap: Bitmap
    lateinit var mcanvas: Canvas
    var mpaint = Paint()
    var path = Path()

    companion object {
        var pixeldata = ArrayList<pixelData>()
        var count=0
        var Flag:Boolean = false //False 라면 Arraylist 다시 생성.

        var fit_height=0
        var fit_width=0 //해상도에 맞춰진 사진의 가로 세로 (확대)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        mpaint.setARGB(150, 255, 144, 99)
        mpaint.strokeWidth = 10f
        mpaint.strokeCap = Paint.Cap.ROUND
        mpaint.strokeJoin = Paint.Join.ROUND
        this.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
    }

    constructor(context: Context):super(context){
        mpaint.setARGB(150, 255, 144, 99)
        mpaint.strokeWidth = 10f
        mpaint.strokeCap = Paint.Cap.ROUND
        mpaint.strokeJoin = Paint.Join.ROUND
        this.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        fit_height = h
        fit_width = w

        Log.i("size:", "W,H: $fit_width, $fit_height")
        mbitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        mcanvas = Canvas()
        //빗맵과 캔버스 연결
        mcanvas.setBitmap(mbitmap)
        mcanvas.drawColor(Color.TRANSPARENT) //캔버스 바탕 색
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mbitmap != null) {
            //canvas.drawBitmap(bit, 0, 0, null);
            canvas.drawBitmap(mbitmap, 0f, 0f, null)
            canvas.drawPath(path,mpaint)
        }

    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        var pointer_count = event.pointerCount
        Log.i("pointer", "pointer count : ${pointer_count}")

        val X = event.x
        val Y = event.y

        if (event.action == MotionEvent.ACTION_DOWN) {
            oldX = X
            oldY = Y
            pixeldata.add(
                pixelData(
                    X,
                    Y
                )
            )
            path.moveTo(X,Y)

        } else if (event.action == MotionEvent.ACTION_MOVE) {

            path.lineTo(X,Y)
            pixeldata.add(
                pixelData(
                    X,
                    Y
                )
            )

            invalidate()
            oldX = X
            oldY = Y

        } else if (event.action == MotionEvent.ACTION_UP) {

            pixeldata.add(
                pixelData(
                    X,
                    Y
                )
            )
            path.lineTo(X,Y)

            invalidate()
            oldX = -1f
            oldY = -1f
        }
        Log.i("pixeldata size:", "${pixeldata.size}")
        // Log.i("x, y 좌표","${X} and ${Y}")
        return true
        }

    fun clear(){
        path.reset()
       pixeldata.clear()
        invalidate()
    }

//    fun createPixelList(x:Int, y:Int){
//        if(!Flag) { //리스트 clear 되었으므로 index 0부터 다시 시작.
//            count = 0
//            pixeldata.clear()
//        }
//
//
//        Log.i("origin w,h","w: ${FrameCaptureDialog.origin_width}, and ${FrameCaptureDialog.origin_height}")
//        Log.i("fit w,h","w: ${fit_width} and h: ${fit_height}")
//
//
//        Log.i("before X,Y", "x:$x and y: $y")
//        var fixed_x = (x * ((FrameCaptureDialog.origin_height).toFloat() / fit_height.toFloat())).toInt()
//        var fixed_y = (y * ((FrameCaptureDialog.origin_width).toFloat() / fit_width.toFloat())).toInt()
//        Log.i("after X,Y", "x:$fixed_x and y: $fixed_y")
//
//     //   pixeldata.add(count, pixelData(fixed_x, fixed_y))
//        count += 1
//        Flag = true //리스트 재생성 하지 않고 그대로 add 한다
//    }

}

//if (event.action == MotionEvent.ACTION_DOWN) {
//    oldX = X
//    oldY = Y
//    pixeldata.add(pixelData(X,Y))
//    path.moveTo(X,Y)
//
//} else if (event.action == MotionEvent.ACTION_MOVE) {
//    //mcanvas.drawLine(oldX.toFloat(), oldY.toFloat(), X.toFloat(), Y.toFloat(), mpaint)
//    //mcanvas.drawLines(pixeldata, mpaint)
//    // pixeldata.add(pixelData(X,Y,true))
//
//    //createPixelList(X,Y)
//    path.lineTo(X,Y)
//    pixeldata.add(pixelData(X,Y))
//
//    invalidate()
//    oldX = X
//    oldY = Y
//
//} else if (event.action == MotionEvent.ACTION_UP) {
//    //mcanvas.drawLine(oldX.toFloat(), oldY.toFloat(), X.toFloat(), Y.toFloat(), mpaint)
//    //createPixelList(X,Y)
//    pixeldata.add(pixelData(X,Y))
//    path.lineTo(X,Y)
//
//    invalidate()
//    oldX = -1f
//    oldY = -1f
//}
//Log.i("pixeldata size:", "${pixeldata.size}")
//// Log.i("x, y 좌표","${X} and ${Y}")
//return true
//}