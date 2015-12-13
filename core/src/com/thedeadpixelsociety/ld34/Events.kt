package com.thedeadpixelsociety.ld34

import com.badlogic.ashley.core.Entity
import kotlin.properties.Delegates

object Events {
    var goal by Delegates.notNull<() -> Unit>()
    var dead by Delegates.notNull<(Entity) -> Unit>()
}
