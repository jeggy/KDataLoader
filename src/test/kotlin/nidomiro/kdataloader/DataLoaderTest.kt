/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package nidomiro.kdataloader

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.test.Test

/**
 * Tests for [DataLoader].
 *
 * The tests are a inspired by the existing tests in
 * the <a href="https://github.com/facebook/dataloader">facebook/dataloader</a> project.
 *
 *
 * @author Niclas Roßberger
 */
class DataLoaderTest {

    @Test
    fun `get One to test basic functionality`(): Unit = runBlocking {
        withTimeout(1000L) {

            val dataLoader = DataLoader(identityBatchLoader<Int>())

            val deferredOne = dataLoader.loadAsync(1)
            assertThat(deferredOne.isCompleted).isFalse()

            dataLoader.dispatch()
            assertThat(deferredOne.await()).isEqualTo(1)
        }
    }

    @Test
    fun `load many in one call`(): Unit = runBlocking {
        withTimeout(1000L) {

            val dataLoader = DataLoader(identityBatchLoader<Int>())

            val items = listOf(1, 2, 3, 5, 4).toTypedArray()

            val deferredAll = dataLoader.loadManyAsync(*items)

            dataLoader.dispatch()
            assertThat(deferredAll.await()).isEqualTo(items.toList())
        }

    }

    @Test
    fun `load many with empty key-list`(): Unit = runBlocking {
        withTimeout(1000L) {

            val dataLoader = DataLoader(identityBatchLoader<Int>())

            val deferredAll = dataLoader.loadManyAsync(*listOf<Int>().toTypedArray())

            dataLoader.dispatch()
            assertThat(deferredAll.await().size).isEqualTo(0)
        }

    }

    @Test
    fun `completables are completed in batch with dispatch()`(): Unit = runBlocking {
        withTimeout(1000L) {

            val dataLoader = DataLoader(identityBatchLoader<Int>())

            val deferredOne = dataLoader.loadAsync(1)
            val deferredTwo = dataLoader.loadAsync(2)
            val deferredThree = dataLoader.loadAsync(3)

            assertThat(deferredOne.isCompleted).isFalse()
            assertThat(deferredTwo.isCompleted).isFalse()
            assertThat(deferredThree.isCompleted).isFalse()

            dataLoader.dispatch()
            assertThat(deferredOne.await()).isEqualTo(1)
            assertThat(deferredTwo.await()).isEqualTo(2)
            assertThat(deferredThree.await()).isEqualTo(3)
        }

    }

    @Test
    fun `enqueue of same id results in same Deferred`(): Unit = runBlocking {
        withTimeout(1000L) {
            val dataLoader = DataLoader(identityBatchLoader<Int>())

            val deferredOne = dataLoader.loadAsync(1)
            val deferredTwo = dataLoader.loadAsync(2)
            val deferredOneDuplicate = dataLoader.loadAsync(1)

            assertThat(deferredOne, "loading the same id must result in the same Deferred").isEqualTo(
                deferredOneDuplicate
            )

            assertThat(deferredOne.isCompleted).isFalse()
            assertThat(deferredTwo.isCompleted).isFalse()
            assertThat(deferredOneDuplicate.isCompleted).isFalse()


            dataLoader.dispatch()
            assertThat(deferredOne.await()).isEqualTo(1)
            assertThat(deferredTwo.await()).isEqualTo(2)
            assertThat(deferredOneDuplicate.await()).isEqualTo(1)

        }
    }

    @Test
    fun `cache repeated requests`(): Unit = runBlocking {
        withTimeout(1000L) {
            val loadCalls = mutableListOf<List<String>>()
            val dataLoader = DataLoader(identityBatchLoader(loadCalls))

            val deferredA = dataLoader.loadAsync("A")
            val deferredB = dataLoader.loadAsync("B")


            assertThat(loadCalls).isEmpty()
            dataLoader.dispatch()

            assertThat(deferredA.await()).isEqualTo("A")
            assertThat(deferredB.await()).isEqualTo("B")

            assertThat(loadCalls).isEqualTo(listOf(listOf("A", "B")))


            val deferredA1 = dataLoader.loadAsync("A")
            val deferredC = dataLoader.loadAsync("C")
            dataLoader.dispatch()

            assertThat(deferredA1.await()).isEqualTo("A")
            assertThat(deferredC.await()).isEqualTo("C")
            assertThat(loadCalls).isEqualTo(listOf(listOf("A", "B"), listOf("C")))


            val deferredA2 = dataLoader.loadAsync("A")
            val deferredB1 = dataLoader.loadAsync("B")
            val deferredC1 = dataLoader.loadAsync("C")
            dataLoader.dispatch()

            assertThat(deferredA2.await()).isEqualTo("A")
            assertThat(deferredB1.await()).isEqualTo("B")
            assertThat(deferredC1.await()).isEqualTo("C")
            assertThat(loadCalls).isEqualTo(listOf(listOf("A", "B"), listOf("C")))
        }
    }

    @Test
    fun `should not redispatch previous load`(): Unit = runBlocking {
        withTimeout(1000L) {
            val loadCalls = mutableListOf<List<String>>()
            val dataLoader = DataLoader(identityBatchLoader(loadCalls))

            val deferredA = dataLoader.loadAsync("A")
            dataLoader.dispatch()
            assertThat(deferredA.await()).isEqualTo("A")

            val deferredB = dataLoader.loadAsync("B")
            dataLoader.dispatch()
            assertThat(deferredB.await()).isEqualTo("B")
            assertThat(loadCalls).isEqualTo(listOf(listOf("A"), listOf("B")))
        }
    }

    @Test
    fun `should cache redispatch on redispatch`(): Unit = runBlocking {
        withTimeout(1000L) {
            val loadCalls = mutableListOf<List<String>>()
            val dataLoader = DataLoader(identityBatchLoader(loadCalls))

            val deferredA = dataLoader.loadAsync("A")
            dataLoader.dispatch()

            assertThat(deferredA.await()).isEqualTo("A")

            val deferredAB = dataLoader.loadManyAsync("A", "B")
            dataLoader.dispatch()
            assertThat(deferredAB.await()).isEqualTo(listOf("A", "B"))

            assertThat(loadCalls).isEqualTo(listOf(listOf("A"), listOf("B")))
        }

    }

}
