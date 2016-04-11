/*
 * Copyright (C) 2016 jomp16
 *
 * This file is part of events_library.
 *
 * events_library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * events_library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with events_library. If not, see <http://www.gnu.org/licenses/>.
 */

package tk.jomp16.event

import org.junit.Assert
import org.junit.Test
import tk.jomp16.event.api.annotations.EventHandler
import tk.jomp16.event.api.dispatcher.IEventDispatcher
import tk.jomp16.event.api.event.IEvent
import tk.jomp16.event.api.listener.IEventListener
import tk.jomp16.event.internal.dispatcher.default.BoolDefaultEventDispatcher
import java.util.*

class BoolEventsTest {
    private var eventDispatcher: IEventDispatcher = BoolDefaultEventDispatcher()

    @Test
    fun testDispatch() {
        val startTime: Long = System.nanoTime()

        for (i in 0..Byte.MAX_VALUE - 1) {
            val uuid1 = UUID.randomUUID().toString()
            val uuid2 = UUID.randomUUID().toString()
            val uuid3 = UUID.randomUUID().toString()
            val uuid4 = UUID.randomUUID().toString()

            val eventListener = object : IEventListener {
                @EventHandler
                fun lol1(eventTest: EventTest1): Boolean {
                    Assert.assertEquals(uuid1, eventTest.value)

                    return true
                }

                @EventHandler
                fun lol2(eventTest: EventTest2): Boolean {
                    Assert.assertEquals(uuid2, eventTest.value)

                    return true
                }

                @EventHandler
                fun lol3(eventTest: EventTest3): Boolean {
                    Assert.assertEquals(uuid3, eventTest.value)

                    return true
                }

                @EventHandler
                fun lol4(eventTest: EventTest4): Boolean {
                    Assert.assertEquals(uuid4, eventTest.value)

                    return true
                }

                @EventHandler
                fun lol5(eventTest: EventTest5): Boolean {
                    return false
                }

                @EventHandler
                fun lol6(eventTest: EventTest5): Boolean {
                    return true
                }
            }

            eventDispatcher += eventListener

            Assert.assertTrue(eventDispatcher.dispatchEvent(EventTest1(uuid1)))
            Assert.assertTrue(eventDispatcher.dispatchEvent(EventTest2(uuid2)))
            Assert.assertTrue(eventDispatcher.dispatchEvent(EventTest3(uuid3)))
            Assert.assertTrue(eventDispatcher.dispatchEvent(EventTest4(uuid4)))
            Assert.assertFalse(eventDispatcher.dispatchEvent(EventTest5()))

            eventDispatcher -= eventListener;
        }

        println("Took ${(System.nanoTime() - startTime) / 1000000000.0}s")
    }

    class EventTest1(val value: String) : IEvent

    class EventTest2(val value: String) : IEvent

    class EventTest3(val value: String) : IEvent

    class EventTest4(val value: String) : IEvent

    class EventTest5() : IEvent
}