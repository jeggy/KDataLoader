package nidomiro.kdataloader

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

fun runBlockingWithTimeout(millis: Long = 1000L, block: suspend CoroutineScope.() -> Unit): Unit = runBlocking {
    withTimeout(millis) {
        block()
    }
}