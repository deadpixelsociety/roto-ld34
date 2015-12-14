package com.thedeadpixelsociety.ld34.scripts

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.thedeadpixelsociety.ld34.components.Box2DComponent

class WeightScript() : HazardScript() {
    override fun act(engine: Engine, entity: Entity) {
        val box2d = entity.getComponent(Box2DComponent::class.java)
        if (box2d != null && box2d.body != null) {
            if (box2d.body!!.linearVelocity.len() > 75f) {
                super.act(engine, entity)
            }
        }
    }

}
