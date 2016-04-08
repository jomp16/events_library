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
import tk.jomp16.event.internal.dispatcher.default.DefaultEventDispatcher
import java.util.*

class EventsTest {
    private var eventDispatcher: IEventDispatcher = DefaultEventDispatcher()

    @Test
    fun testDispatch() {
        val startTime: Long = System.nanoTime()

        for (i in 0..java.lang.Byte.MAX_VALUE - 1) {
            val uuid1 = UUID.randomUUID().toString()
            val uuid2 = UUID.randomUUID().toString()
            val uuid3 = UUID.randomUUID().toString()
            val uuid4 = UUID.randomUUID().toString()

            val eventListener = object : IEventListener {
                @EventHandler
                fun lol1(eventTest: EventTest1) = Assert.assertEquals(uuid1, eventTest.value)

                @EventHandler
                fun lol2(eventTest: EventTest2) = Assert.assertEquals(uuid2, eventTest.value)

                @EventHandler
                fun lol3(eventTest: EventTest3) = Assert.assertEquals(uuid3, eventTest.value)

                @EventHandler
                fun lol4(eventTest: EventTest4) = Assert.assertEquals(uuid4, eventTest.value)
            }

            this.eventDispatcher += eventListener

            Assert.assertTrue(this.eventDispatcher.dispatchEvent(EventTest1(uuid1)))
            Assert.assertTrue(this.eventDispatcher.dispatchEvent(EventTest2(uuid2)))
            Assert.assertTrue(this.eventDispatcher.dispatchEvent(EventTest3(uuid3)))
            Assert.assertTrue(this.eventDispatcher.dispatchEvent(EventTest4(uuid4)))

            this.eventDispatcher -= eventListener;
        }

        println("Took ${(System.nanoTime() - startTime) / 1000000000.0}s")
    }

    class EventTest1(val value: String) : IEvent

    class EventTest2(val value: String) : IEvent

    class EventTest3(val value: String) : IEvent

    class EventTest4(val value: String) : IEvent
}