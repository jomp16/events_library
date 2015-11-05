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

package tk.jomp16.misc.event.internal.dispatcher;

import tk.jomp16.misc.event.api.annotations.EventHandler;
import tk.jomp16.misc.event.api.dispatcher.IEventDispatcher;
import tk.jomp16.misc.event.api.event.IEvent;
import tk.jomp16.misc.event.api.listener.IEventListener;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

// Code from the following URLs, adapted to my requirements. Thanks!
// http://codereview.stackexchange.com/questions/36153/my-event-handling-system
// https://gmarabout.wordpress.com/2010/09/23/annotation-based-event-handling-in-java
public class DefaultEventDispatcher implements IEventDispatcher {
    private final ExecutorService executor;

    private final List<IEventListener> eventListeners;
    private final Comparator<EventMethodInfo> eventMethodInfoComparator;

    private final Map<Class<? extends IEvent>, List<EventMethodInfo>> eventMethodInfoMap;

    public DefaultEventDispatcher() {
        this(Executors.newCachedThreadPool());
    }

    public DefaultEventDispatcher(final ExecutorService executor) {
        this.executor = executor;

        this.eventListeners = Collections.synchronizedList(new LinkedList<>());
        this.eventMethodInfoComparator = new EventMethodInfoComparator();

        this.eventMethodInfoMap = Collections.synchronizedMap(new LinkedHashMap<>());
    }

    @Override
    public void addListener(final IEventListener eventListener) {
        if (eventListener == null || this.eventListeners.contains(eventListener)) {
            return;
        }

        this.eventListeners.add(eventListener);

        this.registerEventHandlerMethods(eventListener);
    }

    @Override
    public void removeListener(final IEventListener eventListener) {
        if (eventListener == null || !this.eventListeners.contains(eventListener)) {
            return;
        }

        this.eventListeners.remove(eventListener);

        this.eventMethodInfoMap.values().stream().forEach(eventMethodInfos -> {
            final Iterator<EventMethodInfo> eventMethodInfoIterator = eventMethodInfos.iterator();

            while (eventMethodInfoIterator.hasNext()) {
                final EventMethodInfo eventMethodInfo = eventMethodInfoIterator.next();

                if (eventMethodInfo.eventListener.equals(eventListener)) {
                    eventMethodInfoIterator.remove();
                }
            }
        });
    }

    @Override
    public Future<Void> dispatchEvent(final IEvent iEvent) {
        if (iEvent == null) {
            return null;
        }

        final List<EventMethodInfo> eventMethodInfoQueue = this.eventMethodInfoMap.get(iEvent.getClass());

        if (eventMethodInfoQueue == null || eventMethodInfoQueue.isEmpty()) {
            return null;
        }

        final FutureTask<Void> future = new FutureTask<>(() -> {
            eventMethodInfoQueue.stream().sorted(this.eventMethodInfoComparator).forEach(eventMethodInfo -> {
                try {
                    eventMethodInfo.method.invoke(eventMethodInfo.eventListener, iEvent);
                } catch (final IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            });

            return null;
        });

        this.executor.execute(future);

        return future;
    }

    private void registerEventHandlerMethods(final IEventListener eventListener) {
        final Method[] methods = eventListener.getClass().getDeclaredMethods();

        for (final Method method : methods) {
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }

            final EventHandler eventHandler = method.getAnnotation(EventHandler.class);

            if (eventHandler != null) {
                byte priority = eventHandler.priority();

                if (priority < 0) {
                    priority = 0;
                } else if (priority > 100) {
                    priority = 100;
                }

                final Class<?>[] parameters = method.getParameterTypes();

                if (parameters.length != 1) {
                    continue;
                }

                final Class<?> param = parameters[0];

                if (!method.getReturnType().equals(void.class)) {
                    continue;
                }

                if (!IEvent.class.isAssignableFrom(param)) {
                    continue;
                }

                @SuppressWarnings("unchecked")
                final Class<? extends IEvent> realParam = (Class<? extends IEvent>) param;

                if (!this.eventMethodInfoMap.containsKey(realParam)) {
                    this.eventMethodInfoMap.put(realParam, Collections.synchronizedList(new LinkedList<>()));
                }

                this.eventMethodInfoMap.get(realParam).add(new EventMethodInfo(priority, eventListener, method));
            }
        }
    }

    @Override
    public void close() throws IOException {
        this.executor.shutdown();
    }

    private class EventMethodInfo {
        private final byte priority;
        private final IEventListener eventListener;
        private final Method method;

        private EventMethodInfo(final byte priority, final IEventListener eventListener, final Method method) {
            this.priority = priority;
            this.eventListener = eventListener;
            this.method = method;
        }
    }

    private class EventMethodInfoComparator implements Comparator<EventMethodInfo> {
        @Override
        public int compare(final EventMethodInfo o1, final EventMethodInfo o2) {
            return Byte.valueOf(o2.priority).compareTo(o1.priority);
        }
    }
}
