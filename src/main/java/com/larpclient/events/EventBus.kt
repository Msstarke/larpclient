package com.larpclient.events

import com.larpclient.LarpClient
import java.util.concurrent.ConcurrentHashMap

/**
 * Simple event bus for posting and subscribing to events.
 */
object EventBus {
    private val listeners = ConcurrentHashMap<Class<*>, MutableList<(Any) -> Unit>>()

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : Any> subscribe(noinline handler: (T) -> Unit) {
        subscribe(T::class.java, handler as (Any) -> Unit)
    }

    fun subscribe(eventClass: Class<*>, handler: (Any) -> Unit) {
        listeners.getOrPut(eventClass) { mutableListOf() }.add(handler)
    }

    fun post(event: Any) {
        listeners[event::class.java]?.forEach { handler ->
            try {
                handler(event)
            } catch (e: Exception) {
                LarpClient.logger.error("Error handling event ${event::class.simpleName}", e)
            }
        }
    }
}
