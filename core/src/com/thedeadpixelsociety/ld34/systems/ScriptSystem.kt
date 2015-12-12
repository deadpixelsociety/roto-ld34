package com.thedeadpixelsociety.ld34.systems

import com.badlogic.ashley.core.ComponentMapper
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.thedeadpixelsociety.ld34.components.ScriptComponent

class ScriptSystem() : IteratingSystem(Family.one(ScriptComponent::class.java).get()) {
    private val scriptMapper = ComponentMapper.getFor(ScriptComponent::class.java)

    override fun processEntity(entity: Entity?, deltaTime: Float) {
        val script = scriptMapper.get(entity)
        if (script != null) {
            script.scripts.forEach { it.act(engine, entity!!) }
        }
    }
}
