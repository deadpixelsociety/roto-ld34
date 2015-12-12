package com.thedeadpixelsociety.ld34.scripts

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.thedeadpixelsociety.ld34.components.Box2DComponent
import com.thedeadpixelsociety.ld34.systems.TagSystem

class GoalScript() : Script {
    private var goal = false

    override fun act(engine: Engine, entity: Entity) {
        val box2d = entity.getComponent(Box2DComponent::class.java)
        if (box2d != null && box2d.collision) {
            val player = engine.getSystem(TagSystem::class.java)["player"]

            if (box2d.collided == player && !goal) {
                goal = true
                println("GOAL!")
            }
        }
    }
}
