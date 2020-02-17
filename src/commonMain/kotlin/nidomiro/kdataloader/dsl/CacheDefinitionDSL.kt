package nidomiro.kdataloader.dsl

import nidomiro.kdataloader.Cache
import nidomiro.kdataloader.CoroutineMapCache

class CacheDefinitionDSL<K, R> {
    private var cacheState: State<K, R> = State.Default()

    /**
     * Disable cache
     */
    val disabled: Unit
        get() {
            cacheState = State.Called(null)
        }

    /**
     * Enable cache
     */
    val enabled = CacheEnabledDSL()

    /**
     * Cache Exceptional States?
     */
    var cacheExceptions: Boolean = true

    fun getCacheInstance(): Cache<K, R>? {
        return when (val currentCacheState = cacheState) {
            is State.Default -> CoroutineMapCache()
            is State.Called -> currentCacheState.instance
        }
    }


    private sealed class State<K, R> {
        data class Called<K, R>(val instance: Cache<K, R>?) : State<K, R>()
        class Default<K, R> : State<K, R>()
    }

    inner class CacheEnabledDSL {
        infix fun with(instance: Cache<K, R>) {
            cacheState = State.Called(instance)
        }
    }
}
