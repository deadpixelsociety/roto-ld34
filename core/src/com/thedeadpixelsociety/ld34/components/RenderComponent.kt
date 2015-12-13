package com.thedeadpixelsociety.ld34.components

import com.badlogic.ashley.core.Component

enum class RenderShape {
    CIRCLE,
    LINE,
    RECTANGLE,
    POLYGON
}

data class RenderComponent(val shape: RenderShape, val zOrder: Float = .5f) : Component