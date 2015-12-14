package com.thedeadpixelsociety.ld34

import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.utils.Disposable
import kotlin.properties.Delegates

object Fonts : Disposable {
    var font32 by Delegates.notNull<BitmapFont>()

    override fun dispose() {
        font32.dispose()
    }
}