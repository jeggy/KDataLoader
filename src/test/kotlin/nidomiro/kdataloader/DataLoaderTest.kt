/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package nidomiro.kdataloader

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class DataLoaderTest {

    @Test
    fun `completables are completed after dispatch()`() {

        val dataLoader = DataLoader { keys: List<Long> ->
            keys
        }

        val deferredOne: Deferred<Long> = dataLoader.load(1)
        val deferredTwo: Deferred<Long> = dataLoader.load(2)
        val deferredThree: Deferred<Long> = dataLoader.load(3)

        assertFalse(deferredOne.isCompleted)
        assertFalse(deferredTwo.isCompleted)
        assertFalse(deferredThree.isCompleted)

        runBlocking {
            dataLoader.dispatch()
            assertEquals(deferredOne.await(), 1)
            assertEquals(deferredTwo.await(), 2)
            assertEquals(deferredThree.await(), 3)
        }
    }

    @Test
    fun `enqueue of same id results in same Deferred`() {

        val dataLoader = DataLoader { keys: List<Long> ->
            keys
        }

        val deferredOne: Deferred<Long> = dataLoader.load(1)
        val deferredTwo: Deferred<Long> = dataLoader.load(2)
        val deferredOneDuplicate: Deferred<Long> = dataLoader.load(1)

        assertEquals(deferredOne, deferredOneDuplicate, "loading the same id must result in the same Deferred")

        assertFalse(deferredOne.isCompleted)
        assertFalse(deferredTwo.isCompleted)
        assertFalse(deferredOneDuplicate.isCompleted)

        runBlocking {
            dataLoader.dispatch()
            assertEquals(deferredOne.await(), 1)
            assertEquals(deferredTwo.await(), 2)
            assertEquals(deferredOneDuplicate.await(), 1)
        }
    }

    @Test
    fun `calling load with same id before and after dispatch`() {

        val dataLoader = DataLoader { keys: List<Long> ->
            keys
        }

        val deferredOne: Deferred<Long> = dataLoader.load(1)

        runBlocking {
            dataLoader.dispatch()
            assertEquals(deferredOne.await(), 1)
        }

        val deferredOneAfterDispatch = dataLoader.load(1)
        assertEquals(deferredOne, deferredOneAfterDispatch, "loading the same id must result in the same Deferred")

        runBlocking {
            assertEquals(deferredOneAfterDispatch.await(), 1)
        }

    }
}
