package kr.co.jsh.utils


import android.graphics.*


fun RoundCornerBitmap(bitmap: Bitmap): Bitmap {
    val output = Bitmap.createBitmap(
        bitmap.width, bitmap
            .height, Bitmap.Config.ARGB_8888
    )

    val canvas = Canvas(output)
    val color = -0xbdbdbe
    val paint = Paint()
    val rect = Rect(0, 0, bitmap.width, bitmap.height)
    val rectF = RectF(rect)
    val roundPx = 15f
    paint.isAntiAlias = true

    canvas.drawARGB(0, 0, 0, 0)
    paint.color = color

    canvas.drawRoundRect(rectF, roundPx, roundPx, paint)
    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    canvas.drawBitmap(bitmap, rect, rect, paint)

    return output
}