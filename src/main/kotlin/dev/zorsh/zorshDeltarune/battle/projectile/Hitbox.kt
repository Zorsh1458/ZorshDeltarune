package dev.zorsh.zorshDeltarune.battle.projectile

interface Hitbox {
    fun isIn(px: Float, py: Float): Boolean
}