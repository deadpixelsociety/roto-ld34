package com.thedeadpixelsociety.ld34.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.math.Rectangle

data class BoundsComponent(val rect: Rectangle = Rectangle(0f, 0f, 1f, 1f)) : Component
