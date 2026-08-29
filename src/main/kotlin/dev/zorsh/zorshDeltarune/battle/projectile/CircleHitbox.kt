package dev.zorsh.zorshDeltarune.battle.projectile

class CircleHitbox(val radius: Float) : Hitbox {
    override fun isIn(px: Float, py: Float, soulWidth: Float) = px*px + py*py <= (radius+soulWidth)*(radius+soulWidth)
}