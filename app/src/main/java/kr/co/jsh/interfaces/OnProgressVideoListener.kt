package kr.co.jsh.interfaces

interface OnProgressVideoListener {
    fun updateProgress(time: Float, max: Float, scale: Float)
}