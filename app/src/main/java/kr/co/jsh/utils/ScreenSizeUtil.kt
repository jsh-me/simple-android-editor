package kr.co.jsh.utils


import android.content.Context
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager

//기기별 해상도 얻는 Util
fun ScreenSizeUtil(context: Context): DisplayMetrics {
    val metrics = DisplayMetrics()
    var windowManager:WindowManager = context.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    windowManager.defaultDisplay.getMetrics(metrics)
    //Log.i("ScreenSize:","${metrics.widthPixels} and ${metrics.heightPixels}")

    return metrics
}
