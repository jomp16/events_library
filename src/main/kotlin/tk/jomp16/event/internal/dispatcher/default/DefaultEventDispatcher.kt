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

package tk.jomp16.event.internal.dispatcher.default

import tk.jomp16.event.api.annotations.EventHandler
import tk.jomp16.event.api.dispatcher.IEventDispatcher
import tk.jomp16.event.api.event.IEvent
import tk.jomp16.event.api.listener.IEventListener
import tk.jomp16.event.internal.EventMethodInfoComparator
import java.util.*

class DefaultEventDispatcher : IEventDispatcher {
    val eventListeners: MutableList<IEventListener> = LinkedList()
    val eventMethodInfoMap: MutableMap<Class<out IEvent>, MutableList<EventMethodInfo>> = LinkedHashMap()

    private val eventMethodInfoComparator: Comparator<EventMethodInfo> = EventMethodInfoComparator()

    override fun addListener(eventListener: IEventListener) {
        if (eventListeners.contains(eventListener)) {
            return;
        }

        eventListeners += eventListener

        registerEventHandlerMethods(eventListener)
    }

    override fun removeListener(eventListener: IEventListener) {
        if (!eventListeners.contains(eventListener)) {
            return
        }

        eventListeners -= eventListener

        eventMethodInfoMap.values.forEach {
            val eventMethodInfoIterator = it.iterator()

            while (eventMethodInfoIterator.hasNext()) {
                val eventMethodInfo = eventMethodInfoIterator.next()

                if (eventMethodInfo.eventListener == eventListener) {
                    eventMethodInfoIterator.remove()
                }
            }
        }
    }

    override fun dispatchEvent(iEvent: IEvent): Boolean {
        val eventMethodInfoQueue = eventMethodInfoMap[iEvent.javaClass]

        if (eventMethodInfoQueue == null || eventMethodInfoQueue.isEmpty()) {
            return false
        }

        eventMethodInfoQueue.sortedWith(eventMethodInfoComparator).forEach { it.method.invoke(it.eventListener, iEvent) }

        return true
    }

    override fun plusAssign(other: IEventListener) = addListener(other)

    override fun minusAssign(other: IEventListener) = removeListener(other)

    fun registerEventHandlerMethods(eventListener: IEventListener) {
        val methods = eventListener.javaClass.declaredMethods

        for (method in methods) {
            if (!method.isAccessible) method.isAccessible = true

            val eventHandler = method.getAnnotation(EventHandler::class.java) ?: continue

            var priority = eventHandler.priority

            when {
                priority < 0 -> priority = 0
                priority > 100 -> priority = 100
            }

            val parameters = method.parameterTypes

            if (parameters.size != 1) continue

            val param = parameters[0]

            if (method.returnType != Void.TYPE) continue

            if (!IEvent::class.java.isAssignableFrom(param)) continue

            @Suppress("UNCHECKED_CAST")
            val realParam = param as Class<out IEvent>

            if (!eventMethodInfoMap.containsKey(realParam)) eventMethodInfoMap.put(realParam, LinkedList())

            eventMethodInfoMap[realParam]?.add(EventMethodInfo(priority, eventListener, method))
        }
    }
}