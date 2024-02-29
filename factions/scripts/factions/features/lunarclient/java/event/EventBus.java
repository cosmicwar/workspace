/*
 * This file is part of Apollo, licensed under the MIT License.
 *
 * Copyright (c) 2023 Moonsworth
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package scripts.factions.features.lunarclient.java.event;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;

/**
 * Provides a bus for {@link Event}s.
 *
 * @since 1.0.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EventBus {

    /**
     * Returns the {@link EventBus}.
     *
     * @return the event bus
     * @since 1.0.0
     */
    @Getter private static final EventBus bus = new EventBus();

    private final Map<Class<? extends Event>, CopyOnWriteArrayList<Consumer<? extends Event>>> events = new ConcurrentHashMap<>();

    /**
     * Registers methods decorated with {@link Listen} in the provided
     * instance as event listeners.
     *
     * @param instance the event listeners instance
     * @since 1.0.0
     */
    @SuppressWarnings("unchecked")
    public void register(@NonNull Object instance) {
        for (Method method : this.getEventMethods(instance)) {
            this.events.computeIfAbsent((Class<? extends Event>) method.getParameterTypes()[0], k -> new CopyOnWriteArrayList<>())
                .add(new ReflectiveConsumer<>(instance, method));
        }
    }

    /**
     * Registers the provided {@link Consumer} as an event listener for the
     * provided event class of type {@code T}.
     *
     * @param event    the event class
     * @param consumer the listener
     * @param <T>      the event type
     * @return true if the listener was registered, otherwise false
     * @since 1.0.0
     */
    public <T extends Event> boolean register(@NonNull Class<T> event, @NonNull Consumer<T> consumer) {
        return this.events.computeIfAbsent(event, key -> new CopyOnWriteArrayList<>()).add(consumer);
    }

    /**
     * Unregisters methods decorated with {@link Listen} in the provided
     * instance.
     *
     * @param instance the event listeners instance
     * @since 1.0.0
     */
    public void unregister(@NonNull Object instance) {
        for (Method method : this.getEventMethods(instance)) {
            List<Consumer<? extends Event>> listeners = this.events.get(method.getParameterTypes()[0]);
            if (listeners != null) {
                listeners.removeIf(consumer -> consumer instanceof ReflectiveConsumer && ((ReflectiveConsumer<?>) consumer).getInstance() == instance);
            }
        }
    }

    /**
     * Unregisters the provided {@link Consumer} for the provided event class
     * of type {@code T}.
     *
     * @param event    the event class
     * @param consumer the listener
     * @param <T>      the event type
     * @return true if the listener was unregistered, otherwise false
     * @since 1.0.0
     */
    public <T extends Event> boolean unregister(@NonNull Class<T> event, @NonNull Consumer<T> consumer) {
        CopyOnWriteArrayList<Consumer<? extends Event>> consumers = this.events.get(event);
        return consumers != null && consumers.remove(consumer);
    }

    /**
     * Posts the provided {@code T} event to listeners.
     *
     * @param event the event
     * @param <T>   the event type
     * @return the event result
     * @since 1.0.0
     */
    @SuppressWarnings("unchecked")
    public <T extends Event> EventResult<T> post(@NonNull T event) {
        CopyOnWriteArrayList<Consumer<? extends Event>> consumers = this.events.get(event.getClass());
        List<Throwable> throwables = new ArrayList<>();
        if (consumers != null) {
            for (Consumer<? extends Event> consumer : consumers) {
                try {
                    ((Consumer<T>) consumer).accept(event);
                } catch (Throwable throwable) {
                    throwables.add(throwable);
                }
            }
        }
        return new EventResult<>(event, throwables);
    }

    private List<Method> getEventMethods(Object instance) {
        return Arrays.stream(instance.getClass().getDeclaredMethods())
            .filter(method -> method.isAnnotationPresent(Listen.class)
                && method.getParameterCount() == 1
                && Event.class.isAssignableFrom(method.getParameterTypes()[0])
            )
            .collect(Collectors.toList());
    }

    /**
     * Represents the result of posting an {@link Event}.
     *
     * @param <T> the event type
     * @since 1.0.0
     */
    @Value
    public static class EventResult<T extends Event> {

        /**
         * Returns the {@link Event}.
         *
         * @since 1.0.0
         */
        T event;

        /**
         * Returns a {@link List} of {@link Throwable}s that were thrown by
         * the event listeners handling the event.
         *
         * @since 1.0.0
         */
        List<Throwable> throwing;

    }

}
