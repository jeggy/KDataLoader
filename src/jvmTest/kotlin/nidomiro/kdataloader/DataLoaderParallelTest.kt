package nidomiro.kdataloader


import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.junit.jupiter.api.Test

class DataLoaderParallelTest {

    @Test
    fun `test calling dispatch in parallel`() = runBlockingWithTimeout {
        val loadCalls = mutableListOf<List<Int>>()
        val dataLoader = SimpleDataLoaderImpl(
            DataLoaderOptions(cacheEnabled = false),
            identityBatchLoader(loadCalls)
        )

        val elementCount = 100

        (1..elementCount).forEach {
            @Suppress("DeferredResultUnused")
            dataLoader.loadAsync(it)
        }

        val job1 = GlobalScope.launch {
            log("Job1 Start")
            dataLoader.dispatch()
            dataLoader.dispatch()
            log("Job1 End")
        }
        val job2 = GlobalScope.launch {
            log("Job2 Start")
            dataLoader.dispatch()
            dataLoader.dispatch()
            log("Job2 End")
        }

        job1.join()
        job2.join()

        assertThat(loadCalls.size).isEqualTo(1)
        assertThat(loadCalls[0].size).isEqualTo(elementCount)
    }
}