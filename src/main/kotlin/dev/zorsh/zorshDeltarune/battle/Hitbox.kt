package dev.zorsh.zorshDeltarune.battle

import org.bukkit.Location
import org.joml.Vector2d
import org.joml.Vector3d

//WHOLE CLASS IS DEPRECATED!!!

class Hitbox(var center: Vector2d, private val sides: List<Pair<Vector2d, Vector2d>>) {

    constructor(center: Location, vectors2: List<Pair<Vector2d, Vector2d>>) : this(
        Vector2d(center.x, center.y),
        vectors2
    )

    fun isIn(location: Location) = isIn(Vector3d(location.x, location.y, location.z))

    fun isIn(point: Vector3d) = isIn(Vector2d(point.x, point.y))

    fun isIn(point: Vector2d): Boolean {
        val actualPoint = Vector2d(point.x - center.x, point.y - center.y)
        for (points in sides) {
            val side = Vector2d(points.second.x - points.first.x, points.second.y - points.first.y)
            val toRight = Vector2d(side.y, -side.x)
            if (toRight.dot(
                    Vector2d(actualPoint.x - points.first.x, actualPoint.y - points.first.y)
                ) < 0
            ) {
                return false
            }
        }
        return true
    }
}