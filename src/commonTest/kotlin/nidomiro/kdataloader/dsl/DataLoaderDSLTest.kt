package nidomiro.kdataloader.dsl

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import nidomiro.kdataloader.BatchMode
import nidomiro.kdataloader.ExecutionResult
import nidomiro.kdataloader.runBlockingWithTimeout
import kotlin.test.Test

class DataLoaderDSLTest {

    @Test
    fun create_a_basic_DataLoader() = runBlockingWithTimeout {

        val dataLoader = dataLoader({ keys: List<Int> ->
            keys.map {
                ExecutionResult.Success(it.toString())
            }
        })


        val deferred1 = dataLoader.loadAsync(1)
        dataLoader.dispatch()
        assertThat(deferred1.await()).isEqualTo("1")

    }

    @Test
    fun create_DataLoader_with_prime() = runBlockingWithTimeout {

        val dataLoader = dataLoader({ keys: List<Int> ->
            keys.map {
                ExecutionResult.Success(it.toString())
            }
        }) {

            prime(
                1 to "1",
                2 to "2"
            )

        }


        val deferred1 = dataLoader.loadAsync(1)
        val deferred2 = dataLoader.loadAsync(2)
        assertThat(deferred1.await()).isEqualTo("1")
        assertThat(deferred2.await()).isEqualTo("2")

    }

    @Test
    fun create_DataLoader_with_prime_throwables() =
        runBlockingWithTimeout {

            val dataLoader =
                dataLoader({ keys: List<Int> ->
                    keys.map {
                        ExecutionResult.Success(
                            it.toString()
                        )
                    }
                }) {

                    prime(
                        1 to IllegalArgumentException("1"),
                        2 to IllegalArgumentException("2")
                    )

                }


            val deferred1 = dataLoader.loadAsync(1)
            val deferred2 = dataLoader.loadAsync(2)

            assertThat { deferred1.await() }
                .isFailure()
                .isInstanceOf(IllegalArgumentException::class)
                .transform { it.message }
                .isEqualTo("1")

            assertThat { deferred2.await() }
                .isFailure()
                .isInstanceOf(IllegalArgumentException::class)
                .transform { it.message }
                .isEqualTo("2")

        }

    @Test
    fun create_a_basic_DataLoader_with_options() =
        runBlockingWithTimeout {

            val dataLoader =
                dataLoader({ keys: List<Int> ->
                    keys.map {
                        ExecutionResult.Success(
                            it.toString()
                        )
                    }
                }) {
                    configure {
                        cache {
                            disabled
                            cacheExceptions = false
                        }
                        batchMode = BatchMode.LoadInBatch(batchSize = 1)
                    }
                }

            assertThat(dataLoader.options.batchMode).isInstanceOf(BatchMode.LoadInBatch::class)
            assertThat(dataLoader.options.batchMode)
                .transform { (it as? BatchMode.LoadInBatch)?.batchSize }
                .isEqualTo(1)
            assertThat(dataLoader.options.cache).isNull()
            assertThat(dataLoader.options.cacheExceptions).isEqualTo(false)


        }
}