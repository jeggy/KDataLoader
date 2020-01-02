package nidomiro.kdataloader

import kotlinx.coroutines.CompletableDeferred

interface LoaderQueue<K, V> {

    suspend fun enqueue(key: K, deferred: CompletableDeferred<V>)

    /**
     * returns all stored Items as List and clears the queue
     * (Coroutine-Save)
     */
    suspend fun getAllItemsAsList(): List<LoaderQueueEntry<K, CompletableDeferred<V>>>

}


data class LoaderQueueEntry<K, V>(
    val key: K,
    val value: V
)