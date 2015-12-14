package com.thedeadpixelsociety.ld34.systems

import com.badlogic.ashley.core.ComponentMapper
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.thedeadpixelsociety.ld34.components.AnimationComponent

class AnimationSystem() : IteratingSystem(Family.one(AnimationComponent::class.java).get()) {
    private val animationMapper = ComponentMapper.getFor(AnimationComponent::class.java)

    override fun processEntity(entity: Entity?, deltaTime: Float) {
        val animation = animationMapper.get(entity)

        if (animation.animationTime > 0f) animation.t = animation.time / animation.animationTime
        if (animation.time >= animation.animationTime) animation.time = 0f else animation.time += deltaTime
    }
}
