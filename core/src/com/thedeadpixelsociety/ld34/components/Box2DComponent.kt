package com.thedeadpixelsociety.ld34.components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.physics.box2d.Body

data class Box2DComponent(val body: Body, var collision: Boolean = false, var collided: Entity? = null) : Component

