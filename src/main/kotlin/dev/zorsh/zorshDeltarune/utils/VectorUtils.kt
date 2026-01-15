package dev.zorsh.zorshDeltarune.utils

import org.bukkit.Location
import org.joml.Vector3d
import org.joml.Vector3f
import org.joml.Vector3i

// Location

operator fun Location.plus(l: Location): Location {
    return Location(world, x + l.x, y + l.y, z + l.z, yaw + l.yaw, pitch + l.pitch)
}

operator fun Location.plus(l: Vector3i): Location {
    return Location(world, x + l.x, y + l.y, z + l.z, yaw, pitch)
}

operator fun Location.plus(l: Vector3f): Location {
    return Location(world, x + l.x, y + l.y, z + l.z, yaw, pitch)
}

operator fun Location.plus(l: Vector3d): Location {
    return Location(world, x + l.x, y + l.y, z + l.z, yaw, pitch)
}

operator fun Location.times(d: Double): Location {
    return Location(world, x * d, y * d, z * d, yaw, pitch)
}

operator fun Location.div(d: Double): Location {
    return this * (1.0 / d)
}

operator fun Location.minus(l: Location): Location {
    return this + l * -1.0
}

operator fun Location.minus(l: Vector3i): Location {
    return this + l * -1.0
}

operator fun Location.minus(l: Vector3f): Location {
    return this + l * -1.0
}

operator fun Location.minus(l: Vector3d): Location {
    return this + l * -1.0
}

// Int Vector

operator fun Vector3i.plus(l: Location): Vector3i {
    return Vector3i(x + l.x.toInt(), y + l.y.toInt(), z + l.z.toInt())
}

operator fun Vector3i.plus(l: Vector3i): Vector3i {
    return Vector3i(x + l.x, y + l.y, z + l.z)
}

operator fun Vector3i.plus(l: Vector3f): Vector3i {
    return Vector3i(x + l.x.toInt(), y + l.y.toInt(), z + l.z.toInt())
}

operator fun Vector3i.plus(l: Vector3d): Vector3i {
    return Vector3i(x + l.x.toInt(), y + l.y.toInt(), z + l.z.toInt())
}

operator fun Vector3i.times(d: Double): Vector3i {
    return Vector3i((x * d).toInt(), (y * d).toInt(), (z * d).toInt())
}

operator fun Vector3i.div(d: Double): Vector3i {
    return this * (1.0 / d)
}

operator fun Vector3i.minus(l: Location): Vector3i {
    return this + l * -1.0
}

operator fun Vector3i.minus(l: Vector3i): Vector3i {
    return this + l * -1.0
}

operator fun Vector3i.minus(l: Vector3f): Vector3i {
    return this + l * -1.0
}

operator fun Vector3i.minus(l: Vector3d): Vector3i {
    return this + l * -1.0
}

// Float Vector

operator fun Vector3f.plus(l: Location): Vector3f {
    return Vector3f(x + l.x.toFloat(), y + l.y.toFloat(), z + l.z.toFloat())
}

operator fun Vector3f.plus(l: Vector3i): Vector3f {
    return Vector3f(x + l.x, y + l.y, z + l.z)
}

operator fun Vector3f.plus(l: Vector3f): Vector3f {
    return Vector3f(x + l.x, y + l.y, z + l.z)
}

operator fun Vector3f.plus(l: Vector3d): Vector3f {
    return Vector3f(x + l.x.toFloat(), y + l.y.toFloat(), z + l.z.toFloat())
}

operator fun Vector3f.times(v: Vector3f): Vector3f {
    return Vector3f(x * v.x, y * v.y, z * v.z)
}

operator fun Vector3f.times(d: Double): Vector3f {
    return Vector3f((x * d).toFloat(), (y * d).toFloat(), (z * d).toFloat())
}

operator fun Vector3f.div(d: Double): Vector3f {
    return this * (1.0 / d)
}

operator fun Vector3f.minus(l: Location): Vector3f {
    return this + l * -1.0
}

operator fun Vector3f.minus(l: Vector3i): Vector3f {
    return this + l * -1.0
}

operator fun Vector3f.minus(l: Vector3f): Vector3f {
    return this + l * -1.0
}

operator fun Vector3f.minus(l: Vector3d): Vector3f {
    return this + l * -1.0
}

// Double Vector

operator fun Vector3d.plus(l: Location): Vector3d {
    return Vector3d(x + l.x, y + l.y, z + l.z)
}

operator fun Vector3d.plus(l: Vector3i): Vector3d {
    return Vector3d(x + l.x, y + l.y, z + l.z)
}

operator fun Vector3d.plus(l: Vector3f): Vector3d {
    return Vector3d(x + l.x, y + l.y, z + l.z)
}

operator fun Vector3d.plus(l: Vector3d): Vector3d {
    return Vector3d(x + l.x, y + l.y, z + l.z)
}

operator fun Vector3d.times(d: Double): Vector3d {
    return Vector3d(x * d, y * d, z * d)
}

operator fun Vector3d.minus(l: Location): Vector3d {
    return this + l * -1.0
}

operator fun Vector3d.minus(l: Vector3i): Vector3d {
    return this + l * -1.0
}

operator fun Vector3d.minus(l: Vector3f): Vector3d {
    return this + l * -1.0
}

operator fun Vector3d.minus(l: Vector3d): Vector3d {
    return this + l * -1.0
}