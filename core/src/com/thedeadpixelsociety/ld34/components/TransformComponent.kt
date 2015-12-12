package com.thedeadpixelsociety.ld34.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.math.Vector2

data class TransformComponent(val position: Vector2 = Vector2(),
                              val origin: Vector2 = Vector2(),
                              var rotation: Float = 0f,
                              val scale: Vector2 = Vector2(1f, 1f)) : Component