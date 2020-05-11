/*
 * Copyright (C) 2017 Piotr Wittchen
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
package ru.beryukhov.reactivenetwork

import android.net.NetworkInfo

/**
 * ConnectivityPredicate is a class containing predefined methods, which can be used for filtering
 * reactive streams of network connectivity
 */
object ConnectivityPredicate {
    /**
     * Filter, which returns true if at least one given state occurred
     *
     * @param states NetworkInfo.State, which can have one or more states
     * @return true if at least one given state occurred
     */
    @JvmStatic
    fun hasState(vararg states: NetworkInfo.State): Predicate<Connectivity> {
        return object : Predicate<Connectivity> {
            @Throws(Exception::class)
            override fun test(connectivity: Connectivity): Boolean {
                for (state in states) {
                    if (connectivity.state() == state) {
                        return true
                    }
                }
                return false
            }
        }
    }

    /**
     * Filter, which returns true if at least one given type occurred
     *
     * @param types int, which can have one or more types
     * @return true if at least one given type occurred
     */
    @JvmStatic
    fun hasType(vararg types: Int): Predicate<Connectivity> {
        val extendedTypes =
            appendUnknownNetworkTypeToTypes(types)
        return object : Predicate<Connectivity> {
            @Throws(Exception::class)
            override fun test(connectivity: Connectivity): Boolean {
                for (type in extendedTypes) {
                    if (connectivity.type() == type) {
                        return true
                    }
                }
                return false
            }
        }
    }

    /**
     * Returns network types from the input with additional unknown type,
     * what helps during connections filtering when device
     * is being disconnected from a specific network
     *
     * @param types of the network as an array of ints
     * @return types of the network with unknown type as an array of ints
     */
    @JvmStatic
    fun appendUnknownNetworkTypeToTypes(types: IntArray): IntArray {
        var i = 0
        val extendedTypes = IntArray(types.size + 1)
        for (type in types) {
            extendedTypes[i] = type
            i++
        }
        extendedTypes[i] = Connectivity.UNKNOWN_TYPE
        return extendedTypes
    }
}