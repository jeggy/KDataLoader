package nidomiro.kdataloader

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class CoroutineMapCache<K, V>(
    private val cacheMap: MutableMap<K, V> = mutableMapOf()
) : Cache<K, V> {
    private val mutex = Mutex()


    override suspend fun store(key: K, value: V): V {
        mutex.withLock {
            cacheMap[key] = value
        }
        return value
    }

    override suspend fun get(key: K): V? =
        mutex.withLock {
            cacheMap[key]
        }

    override suspend fun getOrCreate(key: K, generator: suspend (key: K) -> V, callOnCacheHit: suspend () -> Unit): V =
        mutex.withLock {
            val currentVal = cacheMap[key]
            if (currentVal == null) {
                val generated = generator(key)
                cacheMap[key] = generated
                return@withLock generated
            } else {
                callOnCacheHit()
                return@withLock currentVal
            }
        }


    override suspend fun clear(key: K): V? =
        mutex.withLock {
            cacheMap.remove(key)
        }

    override suspend fun clear() =
        mutex.withLock {
            cacheMap.clear()
        }


}