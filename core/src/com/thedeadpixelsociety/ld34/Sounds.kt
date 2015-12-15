package com.thedeadpixelsociety.ld34

import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.utils.Disposable
import kotlin.properties.Delegates

object Sounds : Disposable {
    var bounce by Delegates.notNull<Sound>()
    var coin by Delegates.notNull<Sound>()
    var dead by Delegates.notNull<Sound>()
    var music by Delegates.notNull<Music>()
    var soundMuted = false

    override fun dispose() {
        //music.dispose()
        bounce.dispose()
        dead.dispose()
        coin.dispose()
    }
}