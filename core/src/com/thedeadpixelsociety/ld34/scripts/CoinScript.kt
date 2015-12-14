package com.thedeadpixelsociety.ld34.scripts

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.math.MathUtils
import com.thedeadpixelsociety.ld34.Events
import com.thedeadpixelsociety.ld34.components.AnimationComponent
import com.thedeadpixelsociety.ld34.components.BoundsComponent
import com.thedeadpixelsociety.ld34.components.Box2DComponent
import com.thedeadpixelsociety.ld34.components.TransformComponent
import com.thedeadpixelsociety.ld34.systems.TagSystem

class CoinScript() : Script {
    private var collected = false
    private var growing = MathUtils.randomBoolean()

    override fun act(engine: Engine, entity: Entity) {
        val box2d = entity.getComponent(Box2DComponent::class.java)
        if (box2d != null && box2d.collision) {
            val player = engine.getSystem(TagSystem::class.java)["player"]

            if (!collected && player != null && box2d.collided == player) {
                collected = true
                Events.coin(entity, player)
            }
        }

        val animation = entity.getComponent(AnimationComponent::class.java)
        if (animation != null) {
            val bounds = entity.getComponent(BoundsComponent::class.java)
            val transform = entity.getComponent(TransformComponent::class.java)
            if (transform != null && bounds != null) {
                if (growing) transform.scale.x = .4f + (animation.t * 60f / 100f) else transform.scale.x = .4f + (.6f - animation.t * 60f / 100f)

                if (animation.t >= 1f) growing = !growing
            }
        }
    }
}
