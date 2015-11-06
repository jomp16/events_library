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

package tk.jomp16.event.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EventHandler {
    /**
     * Define the priority that the event will run. When priority is higher, the event will run first than the others with less priority.
     * <p>
     * Min is 0, max is 100. Don't try to use Integer.MAX_VALUE
     *
     * @return the priority, defaults to 50
     */
    byte priority() default 50;
}
