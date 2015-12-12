package com.thedeadpixelsociety.ld34

import com.badlogic.gdx.physics.box2d.Shape
import com.badlogic.gdx.utils.Disposable

fun <T : Disposable> T.using(func: (T) -> Unit) {
    try {
        func(this)
    } finally {
        dispose()
    }
}

fun <T : Shape> T.using(func: (T) -> Unit) {
    try {
        func(this)
    } finally {
        dispose()
    }
}
