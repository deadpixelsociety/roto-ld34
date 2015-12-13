package com.thedeadpixelsociety.ld34.systems

import com.badlogic.ashley.core.ComponentMapper
import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.thedeadpixelsociety.ld34.components.Box2DComponent
import com.thedeadpixelsociety.ld34.components.DeadComponent
import com.thedeadpixelsociety.ld34.components.TransformComponent
import com.thedeadpixelsociety.ld34.has

class Box2DSystem(private val gravity: Vector2) : EntitySystem() {
    private val box2dMapper = ComponentMapper.getFor(Box2DComponent::class.java)
    private val transformMapper = ComponentMapper.getFor(TransformComponent::class.java)
    val world = World(gravity, false)

    init {
        world.setContactListener(object : ContactListener {
            override fun beginContact(contact: Contact?) {
                val entityA = contact?.fixtureA?.body?.userData as? Entity
                val entityB = contact?.fixtureB?.body?.userData as? Entity

                if (entityA != null && !entityA.has(DeadComponent::class.java)) {
                    val box2d = box2dMapper.get(entityA)
                    if (box2d != null) {
                        box2d.collision = true
                        box2d.collided = entityB
                    }
                }

                if (entityB != null && !entityB.has(DeadComponent::class.java)) {
                    val box2d = box2dMapper.get(entityB)
                    if (box2d != null) {
                        box2d.collision = true
                        box2d.collided = entityA
                    }
                }
            }

            override fun endContact(contact: Contact?) {
                val entityA = contact?.fixtureA?.body?.userData as? com.badlogic.ashley.core.Entity
                val entityB = contact?.fixtureB?.body?.userData as? com.badlogic.ashley.core.Entity

                if (entityA != null) {
                    val box2d = box2dMapper.get(entityA)
                    if (box2d != null && (box2d.collided == null || box2d.collided == entityB)) {
                        box2d.collision = false
                        box2d.collided = null
                    }
                }

                if (entityB != null) {
                    val box2d = box2dMapper.get(entityA)
                    if (box2d != null && (box2d.collided == null || box2d.collided == entityA)) {
                        box2d.collision = false
                        box2d.collided = null
                    }
                }
            }

            override fun postSolve(contact: Contact?, impulse: ContactImpulse?) {
            }

            override fun preSolve(contact: Contact?, oldManifold: Manifold?) {
            }
        })
    }

    override fun update(deltaTime: Float) {
        world.step(deltaTime, 5, 3)

        val bodies = com.badlogic.gdx.utils.Array<Body>()
        world.getBodies(bodies)

        bodies.forEach {
            val entity = it.userData as? Entity
            if (entity != null) {
                val transform = transformMapper.get(entity)
                if (transform != null) {
                    transform.position.set(it.position)
                    transform.rotation = -it.angle * MathUtils.radiansToDegrees
                }
            }
        }
    }

    override fun removedFromEngine(engine: Engine?) {
        world.dispose()
    }
}
