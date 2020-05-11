/*
 * Copyright (C) 2016 Piotr Wittchen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.beryukhov.reactivenetwork.network.observing.strategy

import android.content.BroadcastReceiver
import android.content.Context
import android.net.NetworkInfo
import at.florianschuster.test.flow.emission
import at.florianschuster.test.flow.expect
import at.florianschuster.test.flow.testIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Spy
import org.mockito.junit.MockitoJUnit
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import ru.beryukhov.reactivenetwork.network.observing.NetworkObservingStrategy

// We are suppressing PMD here because we want static imports in unit tests
@FlowPreview
@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
open class PreLollipopNetworkObservingStrategyTest {
    @get:Rule
    var rule = MockitoJUnit.rule()
    @Spy
    val strategy:PreLollipopNetworkObservingStrategy = PreLollipopNetworkObservingStrategy()
    @Mock
    lateinit var broadcastReceiver: BroadcastReceiver

    @Test
    fun shouldObserveConnectivity() { // given
        val strategy: NetworkObservingStrategy = PreLollipopNetworkObservingStrategy()
        val context = RuntimeEnvironment.application.applicationContext
        // when
        runBlockingTest {
            val testFlow = strategy.observeNetworkConnectivity(context).map { it.state() }.testIn(scope = this)
            advanceTimeBy(1000)
            // then
            testFlow expect emission(index = 0, expected = NetworkInfo.State.CONNECTED)

        }
    }

    //Rx specific test
    /*@Test
    fun shouldStopObservingConnectivity() { // given
        val strategy: NetworkObservingStrategy = PreLollipopNetworkObservingStrategy()
        val context =
            RuntimeEnvironment.application.applicationContext
        val observable: Observable<Connectivity> = strategy.observeNetworkConnectivity(context)
        val observer: TestObserver<Connectivity> = TestObserver()
        // when
        observable.subscribe(observer)
        observer.dispose()
        // then
        assertThat(observer.isDisposed()).isTrue()
    }*/

    @Test
    fun shouldCallOnError() { // given
        val message = "error message"
        val exception = Exception()
        // when
        strategy.onError(message, exception)
        // then
        Mockito.verify(strategy, Mockito.times(1))
            .onError(message, exception)
    }

    @Test
    fun shouldTryToUnregisterReceiver() { // given
        val strategy = PreLollipopNetworkObservingStrategy()
        val context = Mockito.spy(RuntimeEnvironment.application)
        // when
        strategy.tryToUnregisterReceiver(context, broadcastReceiver)
        // then
        Mockito.verify(context).unregisterReceiver(broadcastReceiver)
    }

    @Test
    fun shouldTryToUnregisterReceiverAfterDispose() { // given
        val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<Context>()

        // when
        runBlockingTest {
            val testFlow = strategy.observeNetworkConnectivity(context).testIn(scope = this)
            this.cancel()

            // then
            Mockito.verify(strategy).tryToUnregisterReceiver(
                ArgumentMatchers.eq(context),
                ArgumentMatchers.any(
                    BroadcastReceiver::class.java
                )
            )
        }
    }
}