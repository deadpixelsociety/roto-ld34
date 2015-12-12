package com.thedeadpixelsociety.ld34

import com.badlogic.gdx.utils.Disposable
import java.util.*
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
internal object GameServices : Disposable {
    private val services = HashMap<KClass<*>, Any>()

    operator fun <T : Any> get(serviceClass: KClass<T>) = services[serviceClass] as T

    fun put(service: Any) {
        services[service.javaClass.kotlin] = service
    }

    override fun dispose() {
        services.map { it.value as? Disposable }.filterNotNull().forEach { it.dispose() }
        services.clear()
    }
}