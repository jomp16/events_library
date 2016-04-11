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
import tk.jomp16.event.api.event.IEvent
import tk.jomp16.event.api.listener.IEventListener
import java.util.*

/**
 * Created with IntelliJ IDEA.
 *
 * @author jomp16
 * Date: 10/04/16
 * Time: 20:57
 */
class BoolDefaultEventDispatcher : DefaultEventDispatcher() {
    override fun dispatchEvent(iEvent: IEvent): Boolean {
        val eventMethodInfo = eventMethodInfoMap[iEvent.javaClass]

        if (eventMethodInfo == null || eventMethodInfo.isEmpty()) return false

        var result = true

        eventMethodInfo.sortedWith(eventMethodInfoComparator).forEach {
            val tmp = it.method.invoke(it.eventListener, iEvent)

            if (result) {
                // Unneeded, because I already check at registerEventHandlerMethods if method returns Boolean, but Kotlin has a smart cast
                if (tmp is Boolean) result = tmp
            }
        }

        return result
    }

    override fun registerEventHandlerMethods(eventListener: IEventListener) {
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

            if (method.returnType != Boolean::class.javaPrimitiveType) continue

            if (!IEvent::class.java.isAssignableFrom(param)) continue

            @Suppress("UNCHECKED_CAST")
            val realParam = param as Class<out IEvent>

            if (!eventMethodInfoMap.containsKey(realParam)) eventMethodInfoMap.put(realParam, LinkedList())

            eventMethodInfoMap[realParam]?.add(EventMethodInfo(priority, eventListener, method))
        }
    }
}