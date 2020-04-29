package kr.co.jsh.paint

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.view.ScaleGestureDetector
import android.widget.ImageView
import android.widget.Toast


class onPinchListener(context: Context, srcImageView: ImageView) : ScaleGestureDetector.SimpleOnScaleGestureListener() {

    companion object{
        val TAG_PINCH_LISTENER = "PINCH_LISTENER"
    }

    private var srcImageView: ImageView ?= null
    private var context: Context?=null

    init {
        this.context=context
        this.srcImageView=srcImageView
    }

    override fun onScale(detector: ScaleGestureDetector?): Boolean {

        if(detector!=null) {

            var scaleFactor:Float = detector.scaleFactor

            if (srcImageView != null) {

                // Scale the image with pinch zoom value.
                scaleImage(scaleFactor, scaleFactor);

            } else {
                if (context != null) {
                    Toast.makeText(context, "", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e(TAG_PINCH_LISTENER, "Both context and srcImageView is null.")
                }
            }
        }else
        {
            Log.e(TAG_PINCH_LISTENER, "Pinch listener onScale detector parameter is null.")
        }

        return true
    }

    fun scaleImage(xScale:Float, yScale:Float){
        var srcBitmapDrawable: BitmapDrawable = srcImageView?.drawable as BitmapDrawable
        var srcBitmap: Bitmap = srcBitmapDrawable.bitmap

        var srcImageWidth = srcBitmap.width
        var srcImageHeight = srcBitmap.height

        var srcImageConfig = srcBitmap.config
        var scaleBitmap = Bitmap.createBitmap((srcImageWidth*xScale).toInt(), (srcImageHeight*yScale).toInt(), srcImageConfig)

        var scaleCanvas = Canvas()
        var scaleMatrix = Matrix()
        scaleMatrix.setScale(xScale,yScale)

        var paint= Paint()
        scaleCanvas.drawBitmap(srcBitmap,scaleMatrix,paint)

        srcImageView?.setImageBitmap(scaleBitmap)
    }
}