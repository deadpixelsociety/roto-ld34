package com.thedeadpixelsociety.ld34

object Collision {
    const val PLAYER: Int = 0x0001
    const val WALL: Int = 0x0002
    const val COIN: Int = 0x0004
    const val GOAL: Int = 0x0008
    const val HAZARD: Int = 0x0010
    const val ALL: Int = PLAYER or WALL or COIN or GOAL or HAZARD
}
