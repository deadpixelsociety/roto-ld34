package com.thedeadpixelsociety.ld34.systems

import com.badlogic.ashley.core.*
import com.thedeadpixelsociety.ld34.components.GroupComponent
import java.util.*

class GroupSystem() : EntitySystem() {
    private val gm = ComponentMapper.getFor(GroupComponent::class.java)
    private val groupMap = HashMap<String, MutableList<Entity>>()
    private val listener = object : EntityListener {
        override fun entityAdded(entity: Entity?) {
            val group = gm.get(entity)
            if (group != null && entity != null) entities(group.group).add(entity)
        }

        override fun entityRemoved(entity: Entity?) {
            val group = gm.get(entity)
            if (group != null && entity != null) entities(group.group).remove(entity)
        }
    }

    operator fun get(group: String): List<Entity> {
        return entities(group)
    }

    override fun addedToEngine(engine: Engine?) {
        engine?.addEntityListener(Family.one(GroupComponent::class.java).get(), listener)
    }

    override fun removedFromEngine(engine: Engine?) {
        groupMap.clear()
        engine?.removeEntityListener(listener)
    }

    private fun entities(group: String): MutableList<Entity> {
        var list = groupMap[group]
        if (list == null) {
            list = arrayListOf()
            groupMap[group] = list
        }

        return list
    }
}
