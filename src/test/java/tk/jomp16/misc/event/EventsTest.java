/*
 * Copyright (C) 2015 jomp16
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

package tk.jomp16.misc.event;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import tk.jomp16.misc.event.api.annotations.EventHandler;
import tk.jomp16.misc.event.api.dispatcher.IEventDispatcher;
import tk.jomp16.misc.event.api.event.IEvent;
import tk.jomp16.misc.event.api.listener.IEventListener;
import tk.jomp16.misc.event.internal.dispatcher.DefaultEventDispatcher;

import java.util.UUID;

public class EventsTest {
    private IEventDispatcher eventDispatcher;

    @Before
    public void setup() {
        this.eventDispatcher = new DefaultEventDispatcher();
    }

    @Test
    public void testDispatch() {
        Assert.assertFalse(this.eventDispatcher.dispatchEvent(null));

        for (byte i = 0; i < Byte.MAX_VALUE; i++) {
            final String uuid1 = UUID.randomUUID().toString();
            final String uuid2 = UUID.randomUUID().toString();
            final String uuid3 = UUID.randomUUID().toString();
            final String uuid4 = UUID.randomUUID().toString();

            final IEventListener eventListener = new IEventListener() {
                @EventHandler
                public void lol1(final EventTest1 eventTest) {
                    Assert.assertEquals(uuid1, eventTest.value);
                }

                @EventHandler
                public void lol2(final EventTest2 eventTest) {
                    Assert.assertEquals(uuid2, eventTest.value);
                }

                @EventHandler
                public void lol3(final EventTest3 eventTest) {
                    Assert.assertEquals(uuid3, eventTest.value);
                }

                @EventHandler
                public void lol4(final EventTest4 eventTest) {
                    Assert.assertEquals(uuid4, eventTest.value);
                }
            };

            this.eventDispatcher.addListener(eventListener);

            Assert.assertTrue(this.eventDispatcher.dispatchEvent(new EventTest1(uuid1)));
            Assert.assertTrue(this.eventDispatcher.dispatchEvent(new EventTest2(uuid2)));
            Assert.assertTrue(this.eventDispatcher.dispatchEvent(new EventTest3(uuid3)));
            Assert.assertTrue(this.eventDispatcher.dispatchEvent(new EventTest4(uuid4)));

            this.eventDispatcher.removeListener(eventListener);
        }
    }

    public static class EventTest1 implements IEvent {
        protected final String value;

        public EventTest1(final String value) {
            this.value = value;
        }
    }

    public static class EventTest2 extends EventTest1 {
        public EventTest2(final String value) {
            super(value);
        }
    }

    public static class EventTest3 extends EventTest2 {
        public EventTest3(final String value) {
            super(value);
        }
    }

    public static class EventTest4 extends EventTest3 {
        public EventTest4(final String value) {
            super(value);
        }
    }
}
