package com.thedeadpixelsociety.ld34.scripts

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity

interface Script {
    fun act(engine: Engine, entity: Entity)
}