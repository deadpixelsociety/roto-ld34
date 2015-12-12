package com.thedeadpixelsociety.ld34.components

import com.badlogic.ashley.core.Component
import com.thedeadpixelsociety.ld34.scripts.Script

data class ScriptComponent(val scripts: MutableList<Script> = arrayListOf()) : Component
