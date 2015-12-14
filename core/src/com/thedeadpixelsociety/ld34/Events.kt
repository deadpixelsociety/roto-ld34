package com.thedeadpixelsociety.ld34

import com.badlogic.ashley.core.Entity
import kotlin.properties.Delegates

object Events {
    var coin by Delegates.notNull<(Entity, Entity) -> Unit>()
    var dead by Delegates.notNull<(Entity) -> Unit>()
    var goal by Delegates.notNull<() -> Unit>()
    var wall by Delegates.notNull<(Entity, Entity) -> Unit>()
}
