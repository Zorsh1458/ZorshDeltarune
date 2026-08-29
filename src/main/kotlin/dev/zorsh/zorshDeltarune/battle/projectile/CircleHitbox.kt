package dev.zorsh.zorshDeltarune.battle.projectile

class CircleHitbox(val radius: Float) : Hitbox {
    override fun isIn(px: Float, py: Float) = px*px + py*py <= radius*radius
}