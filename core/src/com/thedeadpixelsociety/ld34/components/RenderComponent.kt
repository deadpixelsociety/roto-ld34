package com.thedeadpixelsociety.ld34.components

import com.badlogic.ashley.core.Component

enum class RenderType {
    CIRCLE,
    LINE,
    RECTANGLE,
    POLYGON,
    TEXT
}

data class RenderComponent(val type: RenderType, val zOrder: Float = .5f) : Component