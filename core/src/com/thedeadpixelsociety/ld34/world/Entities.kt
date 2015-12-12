package com.thedeadpixelsociety.ld34.world

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.maps.MapLayer
import com.badlogic.gdx.maps.objects.EllipseMapObject
import com.badlogic.gdx.maps.objects.PolygonMapObject
import com.badlogic.gdx.maps.objects.RectangleMapObject
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.CircleShape
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import com.thedeadpixelsociety.ld34.components.*
import com.thedeadpixelsociety.ld34.graphics.Palette
import com.thedeadpixelsociety.ld34.scripts.GoalScript
import com.thedeadpixelsociety.ld34.systems.Box2DSystem
import com.thedeadpixelsociety.ld34.using

object Entities {
    const val NAME_GOAL = "goal"
    const val NAME_PLAYER = "player"
    const val TYPE_POLYWALL = "polywall"
    const val TYPE_WALL = "wall"

    fun goal(engine: Engine, data: GoalData): Entity {
        val entity = Entity()
        val world = engine.getSystem(Box2DSystem::class.java).world

        val body = world.createBody(BodyDef().apply {
            position.set(data.x + data.w * .5f, data.y + data.h * .5f)
            type = BodyDef.BodyType.StaticBody
        })

        PolygonShape().apply { setAsBox(data.w * .5f, data.h * .5f) }.using {
            body.createFixture(FixtureDef().apply {
                density = 10f
                friction = .6f
                shape = it
                isSensor = true
            })
        }

        entity.add(TransformComponent().apply {
            position.set(data.x, data.y)
        }).add(BoundsComponent().apply {
            rect.set(data.x - data.w * .5f, data.y - data.h * .5f, data.w, data.h)
        }).add(Box2DComponent(body))
                .add(TagComponent("goal"))
                .add(RenderComponent(RenderShape.RECTANGLE, .1f))
                .add(TintComponent(Palette.SEEKING_SAGE))
                .add(ScriptComponent().apply {
                    scripts.add(GoalScript())
                })

        body.userData = entity

        engine.addEntity(entity)

        return entity
    }

    fun player(engine: Engine, data: PlayerData): Entity {
        val entity = Entity()
        val world = engine.getSystem(Box2DSystem::class.java).world

        val body = world.createBody(BodyDef().apply {
            position.set(data.x, data.y)
            fixedRotation = true
            type = BodyDef.BodyType.DynamicBody
        })

        CircleShape().apply { radius = data.radius }.using {
            body.createFixture(FixtureDef().apply {
                density = 3f
                restitution = .4f
                friction = .4f
                shape = it
            })
        }

        entity.add(TransformComponent().apply {
            position.set(data.x, data.y)
        }).add(BoundsComponent().apply {
            rect.set(data.x - data.radius, data.y - data.radius, data.radius * 2f, data.radius * 2f)
        }).add(Box2DComponent(body))
                .add(TagComponent("player"))
                .add(RenderComponent(RenderShape.CIRCLE))
                .add(TintComponent(Palette.COMFORTABLE_SKIN))

        body.userData = entity

        engine.addEntity(entity)

        return entity
    }

    fun polywall(engine: Engine, data: PolyWallData): Entity {
        val entity = Entity()
        val world = engine.getSystem(Box2DSystem::class.java).world

        val body = world.createBody(BodyDef().apply {
            position.set(data.x, data.y)
            type = BodyDef.BodyType.StaticBody
        })

        PolygonShape().apply { this.set(data.vertices.toTypedArray()) }.using {
            body.createFixture(FixtureDef().apply {
                density = 10f
                friction = .6f
                shape = it
            })
        }

        entity.add(TransformComponent().apply {
            position.set(data.x, data.y)
        }).add(BoundsComponent().apply {
            rect.set(data.x, data.y, 1f, 1f)
        }).add(Box2DComponent(body))
                .add(GroupComponent("wall"))
                .add(RenderComponent(RenderShape.POLYGON))
                .add(TintComponent(Palette.MELONJOLYY))

        body.userData = entity
        engine.addEntity(entity)

        return entity
    }

    fun wall(engine: Engine, data: WallData): Entity {
        val entity = Entity()
        val world = engine.getSystem(Box2DSystem::class.java).world

        val body = world.createBody(BodyDef().apply {
            position.set(data.x + data.w * .5f, data.y + data.h * .5f)
            type = BodyDef.BodyType.StaticBody
        })

        PolygonShape().apply { setAsBox(data.w * .5f, data.h * .5f) }.using {
            body.createFixture(FixtureDef().apply {
                density = 10f
                friction = .6f
                shape = it
            })
        }

        entity.add(TransformComponent().apply {
            position.set(data.x, data.y)
        }).add(BoundsComponent().apply {
            rect.set(data.x - data.w * .5f, data.y - data.h * .5f, data.w, data.h)
        }).add(Box2DComponent(body))
                .add(GroupComponent("wall"))
                .add(RenderComponent(RenderShape.RECTANGLE))
                .add(TintComponent(Palette.MELONJOLYY))

        body.userData = entity

        engine.addEntity(entity)

        return entity
    }
}

data class GoalData(val x: Float, val y: Float, val w: Float, val h: Float)
data class PlayerData(val x: Float, val y: Float, val radius: Float)
data class PolyWallData(val x: Float, val y: Float, val vertices: List<Vector2>)
data class WallData(val x: Float, val y: Float, val w: Float, val h: Float)

fun createEntitiesFromMapLayer(layer: MapLayer, engine: Engine) {
    createPlayer(engine, layer)
    createWalls(engine, layer)
    createPolyWalls(engine, layer)
    createGoal(engine, layer)
}

fun createGoal(engine: Engine, layer: MapLayer) {
    val obj = layer.objects.get(Entities.NAME_GOAL) as RectangleMapObject
    Entities.goal(engine, GoalData(obj.rectangle.x, obj.rectangle.y, obj.rectangle.width, obj.rectangle.height))
}

private fun createPlayer(engine: Engine, layer: MapLayer) {
    val obj = layer.objects.get(Entities.NAME_PLAYER) as EllipseMapObject
    Entities.player(engine, PlayerData(obj.ellipse.x, obj.ellipse.y, obj.ellipse.width * .5f))
}

private fun createPolyWall(engine: Engine, it: PolygonMapObject) {
    val vectors = arrayListOf<Vector2>()
    for (i in 0..it.polygon.vertices.size - 1 step 2) {
        val x = it.polygon.vertices[i]
        val y = it.polygon.vertices[i + 1]
        vectors.add(Vector2(x, y))
    }

    Entities.polywall(engine, PolyWallData(it.polygon.x, it.polygon.y, vectors))
}

private fun createPolyWalls(engine: Engine, layer: MapLayer) {
    layer.objects
            .filter { it.properties["type"] == Entities.TYPE_POLYWALL }
            .map { it as PolygonMapObject }
            .forEach { createPolyWall(engine, it) }
}

private fun createWall(engine: Engine, obj: RectangleMapObject) {
    Entities.wall(engine, WallData(obj.rectangle.x, obj.rectangle.y, obj.rectangle.width, obj.rectangle.height))
}

private fun createWalls(engine: Engine, layer: MapLayer) {
    layer.objects
            .filter { it.properties["type"] == Entities.TYPE_WALL }
            .map { it as RectangleMapObject }
            .forEach { createWall(engine, it) }
}