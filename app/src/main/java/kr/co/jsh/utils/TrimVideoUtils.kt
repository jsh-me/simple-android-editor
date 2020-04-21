package kr.co.jsh.utils

import java.util.*

object TrimVideoUtils {

    fun stringForTime(timeMs: Float): String {
        val totalSeconds = timeMs / 1000f
        val seconds = totalSeconds % 60f
        val minutes = (totalSeconds / 60 % 60).toInt()
        val hours = (totalSeconds / 3600).toInt()
        val mFormatter = Formatter()
        return if (hours > 0) {
            mFormatter.format("%d:%02d:%02.02f", hours, minutes, seconds).toString()
        } else {
            mFormatter.format("%02d:%02.02f", minutes, seconds).toString()
        }
    }
}
