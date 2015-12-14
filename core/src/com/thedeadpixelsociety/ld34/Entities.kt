package com.thedeadpixelsociety.ld34

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.maps.MapLayer
import com.badlogic.gdx.maps.objects.EllipseMapObject
import com.badlogic.gdx.maps.objects.PolylineMapObject
import com.badlogic.gdx.maps.objects.RectangleMapObject
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.physics.box2d.*
import com.thedeadpixelsociety.ld34.components.*
import com.thedeadpixelsociety.ld34.graphics.Palette
import com.thedeadpixelsociety.ld34.scripts.CoinScript
import com.thedeadpixelsociety.ld34.scripts.GoalScript
import com.thedeadpixelsociety.ld34.scripts.HazardScript
import com.thedeadpixelsociety.ld34.scripts.WallScript
import com.thedeadpixelsociety.ld34.systems.Box2DSystem

object Entities {
    const val NAME_GOAL = "goal"
    const val NAME_PLAYER = "player"
    const val TYPE_HAZARD = "hazard"
    const val TYPE_WALL = "wall"
    const val TYPE_COIN = "coin"
    const val TYPE_TEXT = "text"

    fun text(engine: Engine, data: TextData): Entity {
        val entity = Entity()
        val world = engine.getSystem(Box2DSystem::class.java).world

        val body = world.createBody(BodyDef().apply {
            position.set(data.x + data.w * .5f, data.y + data.h * .5f)
            type = BodyDef.BodyType.StaticBody
        })

        PolygonShape().apply { setAsBox(data.w * .5f, data.h * .5f) }.using {
            body.createFixture(FixtureDef().apply {
                filter.categoryBits = Collision.WALL.toShort()
                filter.maskBits = Collision.PLAYER.toShort()
                density = 10f
                friction = .4f
                shape = it
            })
        }

        entity.add(TransformComponent().apply {
            position.set(data.x + data.w * .5f, data.y + data.h * .5f)
        }).add(BoundsComponent().apply {
            rect.set(data.x - data.w * .5f, data.y - data.h * .5f, data.w, data.h)
        }).add(Box2DComponent(body))
                .add(GroupComponent("text"))
                .add(RenderComponent(RenderType.TEXT))
                .add(TextComponent(data.text))

        body.userData = entity

        engine.addEntity(entity)

        return entity
    }

    fun coin(engine: Engine, data: CoinData): Entity {
        val entity = Entity()
        val world = engine.getSystem(Box2DSystem::class.java).world

        val body = world.createBody(BodyDef().apply {
            position.set(data.x + data.radius, data.y + data.radius)
            type = BodyDef.BodyType.DynamicBody
            gravityScale = 0f
        })

        CircleShape().apply { radius = data.radius }.using {
            body.createFixture(FixtureDef().apply {
                filter.categoryBits = Collision.COIN.toShort()
                filter.maskBits = Collision.PLAYER.toShort()
                density = 1f
                isSensor = true
                shape = it
            })
        }

        entity.add(TransformComponent().apply {
            position.set(data.x + data.radius, data.y + data.radius)
        }).add(BoundsComponent().apply {
            rect.set(data.x - data.radius, data.y - data.radius, data.radius * 2f, data.radius * 2f)
        }).add(Box2DComponent(body))
                .add(ScriptComponent().apply {
                    scripts.add(CoinScript())
                })
                .add(GroupComponent("coin"))
                .add(AnimationComponent(2f, time = MathUtils.random() * 2f))
                .add(RenderComponent(RenderType.CIRCLE, .4f))
                .add(TintComponent(Color(Palette.COINY)))

        body.userData = entity

        engine.addEntity(entity)

        return entity
    }

    fun goal(engine: Engine, data: GoalData): Entity {
        val entity = Entity()
        val world = engine.getSystem(Box2DSystem::class.java).world

        val body = world.createBody(BodyDef().apply {
            position.set(data.x + data.w * .5f, data.y + data.h * .5f)
            type = BodyDef.BodyType.StaticBody
        })

        PolygonShape().apply { setAsBox(data.w * .5f, data.h * .5f) }.using {
            body.createFixture(FixtureDef().apply {
                filter.categoryBits = Collision.GOAL.toShort()
                filter.maskBits = Collision.PLAYER.toShort()
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
                .add(RenderComponent(RenderType.RECTANGLE, .1f))
                .add(TintComponent(Color(Palette.SEEKING_SAGE)))
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
            position.set(data.x + data.radius, data.y + data.radius)
            type = BodyDef.BodyType.DynamicBody
            bullet = true
        })

        CircleShape().apply { radius = data.radius }.using {
            body.createFixture(FixtureDef().apply {
                filter.categoryBits = Collision.PLAYER.toShort()
                filter.maskBits = Collision.ALL.toShort()
                density = 3f
                restitution = .3f
                shape = it
            })
        }

        entity.add(TransformComponent().apply {
            position.set(data.x + data.radius, data.y + data.radius)
        }).add(BoundsComponent().apply {
            rect.set(data.x - data.radius, data.y - data.radius, data.radius * 2f, data.radius * 2f)
        }).add(Box2DComponent(body))
                .add(TagComponent("player"))
                .add(RenderComponent(RenderType.CIRCLE))
                .add(TintComponent(Color(Palette.COMFORTABLE_SKIN)))

        body.userData = entity

        engine.addEntity(entity)

        return entity
    }

    fun weight(engine: Engine, data: WeightData): Entity {
        val entity = Entity()
        val world = engine.getSystem(Box2DSystem::class.java).world

        val body = world.createBody(BodyDef().apply {
            position.set(data.cx, data.cy)
            type = BodyDef.BodyType.DynamicBody
            bullet = true
        })

        PolygonShape().apply { setAsBox(data.w * .5f, data.h * .5f) }.using {
            body.createFixture(FixtureDef().apply {
                filter.categoryBits = Collision.HAZARD.toShort()
                filter.maskBits = (Collision.WALL or Collision.PLAYER).toShort()
                density = 100f
                friction = .1f
                shape = it
            })
        }

        entity.add(TransformComponent().apply {
            position.set(data.cx, data.cy)
        }).add(BoundsComponent().apply {
            rect.set(data.x - data.w * .5f, data.y - data.h * .5f, data.w, data.h)
        }).add(Box2DComponent(body))
                .add(GroupComponent("hazard"))
                .add(RenderComponent(RenderType.RECTANGLE))
                .add(TintComponent(Color(Palette.COMPLETE_CORAL)))
                .add(ScriptComponent().apply {
                    scripts.add(HazardScript())
                })

        body.userData = entity

        engine.addEntity(entity)

        return entity
    }

    fun wall(engine: Engine, data: WallData): Entity {
        val entity = Entity()
        val world = engine.getSystem(Box2DSystem::class.java).world

        val body = world.createBody(BodyDef().apply {
            position.set(data.cx, data.cy)
            type = BodyDef.BodyType.StaticBody
        })

        PolygonShape().apply { setAsBox(data.w * .5f, data.h * .5f) }.using {
            body.createFixture(FixtureDef().apply {
                filter.categoryBits = Collision.WALL.toShort()
                filter.maskBits = (Collision.PLAYER or Collision.HAZARD).toShort()
                density = 10f
                friction = .4f
                shape = it
            })
        }

        entity.add(TransformComponent().apply {
            position.set(data.cx, data.cy)
        }).add(BoundsComponent().apply {
            rect.set(data.x - data.w * .5f, data.y - data.h * .5f, data.w, data.h)
        }).add(Box2DComponent(body))
                .add(GroupComponent("wall"))
                .add(RenderComponent(RenderType.RECTANGLE))
                .add(TintComponent(Color(Palette.MELONJOLYY)))
                .add(ScriptComponent().apply {
                    scripts.add(WallScript())
                })

        body.userData = entity

        engine.addEntity(entity)

        return entity
    }

    fun spike(engine: Engine, data: SpikeData): Entity {
        val entity = Entity()
        val world = engine.getSystem(Box2DSystem::class.java).world

        val body = world.createBody(BodyDef().apply {
            position.set(data.x0, data.y0)
            type = BodyDef.BodyType.StaticBody
        })

        EdgeShape().apply { this.set(0f, 0f, data.x1 - data.x0, data.y1 - data.y0) }.using {
            body.createFixture(FixtureDef().apply {
                filter.categoryBits = Collision.HAZARD.toShort()
                filter.maskBits = Collision.PLAYER.toShort()
                density = 10f
                shape = it
            })
        }

        entity.add(TransformComponent().apply {
            position.set(data.x0, data.y0)
        }).add(BoundsComponent().apply {
            rect.set(data.x0, data.y0, data.x1, data.y1)
        }).add(Box2DComponent(body))
                .add(GroupComponent("hazard"))
                .add(ScriptComponent().apply {
                    scripts.add(HazardScript())
                })
                .add(RenderComponent(RenderType.LINE, .3f))
                .add(TintComponent(Palette.COMPLETE_CORAL))

        body.userData = entity

        engine.addEntity(entity)

        return entity
    }
}

data class CoinData(val x: Float, val y: Float, val radius: Float)
data class GoalData(val x: Float, val y: Float, val w: Float, val h: Float)
data class PlayerData(val x: Float, val y: Float, val radius: Float)
data class SpikeData(val x0: Float, val y0: Float, val x1: Float, val y1: Float)
data class TextData(val x: Float, val y: Float, val w: Float, val h: Float, val text: String?)
data class WeightData(val x: Float, val y: Float, val cx: Float, val cy: Float, val w: Float, val h: Float)
data class WallData(val x: Float, val y: Float, val cx: Float, val cy: Float, val w: Float, val h: Float)

fun createEntitiesFromMapLayer(layer: MapLayer, engine: Engine) {
    createPlayer(engine, layer)
    createWalls(engine, layer)
    createGoal(engine, layer)
    createHazards(engine, layer)
    createCoins(engine, layer)
    createText(engine, layer)
}

private fun createText(engine: Engine, layer: MapLayer) {
    val obj = layer.objects.filter { it.properties["type"] == Entities.TYPE_TEXT }.map { it as RectangleMapObject }.forEach {
        Entities.text(engine, TextData(it.rectangle.x,
                it.rectangle.y,
                it.rectangle.width,
                it.rectangle.height,
                if (it.properties.containsKey("text")) it.properties["text"] as String else null))
    }
}

private fun createCoins(engine: Engine, layer: MapLayer) {
    layer.objects
            .filter { it.properties["type"] == Entities.TYPE_COIN }
            .forEach {
                if (it is EllipseMapObject) {
                    createCoin(engine, it)
                }
            }
}

private fun createCoin(engine: Engine, obj: EllipseMapObject) {
    Entities.coin(engine, CoinData(obj.ellipse.x, obj.ellipse.y, obj.ellipse.width * .5f))
}

private fun createHazards(engine: Engine, layer: MapLayer) {
    layer.objects
            .filter { it.properties["type"] == Entities.TYPE_HAZARD }
            .forEach {
                if (it is PolylineMapObject) {
                    createSpikeHazard(engine, it)
                } else if (it is RectangleMapObject) {
                    createWeightHazard(engine, it)
                }
            }
}

fun createWeightHazard(engine: Engine, obj: RectangleMapObject) {
    val x0 = obj.rectangle.x
    val y0 = obj.rectangle.y
    val w = obj.rectangle.width
    val h = obj.rectangle.height

    Entities.weight(engine, WeightData(x0, y0, x0 + w * .5f, y0 + h * .5f, w, h))
}

private fun createSpikeHazard(engine: Engine, obj: PolylineMapObject) {
    Entities.spike(engine, SpikeData(obj.polyline.x, obj.polyline.y, obj.polyline.x + obj.polyline.vertices[2], obj.polyline.y + obj.polyline.vertices[3]))
}

private fun createGoal(engine: Engine, layer: MapLayer) {
    val obj = layer.objects.get(Entities.NAME_GOAL) as RectangleMapObject
    Entities.goal(engine, GoalData(obj.rectangle.x, obj.rectangle.y, obj.rectangle.width, obj.rectangle.height))
}

private fun createPlayer(engine: Engine, layer: MapLayer) {
    val obj = layer.objects.get(Entities.NAME_PLAYER) as EllipseMapObject
    Entities.player(engine, PlayerData(obj.ellipse.x, obj.ellipse.y, obj.ellipse.width * .5f))
}

private fun createWall(engine: Engine, obj: RectangleMapObject) {
    val x0 = obj.rectangle.x
    val y0 = obj.rectangle.y
    val w = obj.rectangle.width
    val h = obj.rectangle.height

    Entities.wall(engine, WallData(x0, y0, x0 + w * .5f, y0 + h * .5f, w, h))
}

private fun createWalls(engine: Engine, layer: MapLayer) {
    layer.objects
            .filter { it.properties["type"] == Entities.TYPE_WALL }
            .map { it as RectangleMapObject }
            .forEach { createWall(engine, it) }
}