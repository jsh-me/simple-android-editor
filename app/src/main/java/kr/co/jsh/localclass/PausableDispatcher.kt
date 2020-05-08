package kr.co.jsh.localclass

import android.os.Handler
import kotlinx.coroutines.CoroutineDispatcher
import java.util.*
import kotlin.coroutines.CoroutineContext

class PausableDispatcher(private val handler: Handler): CoroutineDispatcher() {
    private val queue: Queue<Runnable> = LinkedList()
    private var isPaused: Boolean = false

    @Synchronized override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (isPaused) {
            queue.add(block)
        } else {
            handler.post(block)
        }
    }

    @Synchronized fun pause() {
        isPaused = true
    }

    @Synchronized fun resume() {
        isPaused = false
        runQueue()
    }

    private fun runQueue() {
        queue.iterator().let {
            while (it.hasNext()) {
                val block = it.next()
                it.remove()
                handler.post(block)
            }
        }
    }
}