package dev.zorsh.zorshDeltarune.utils

import org.joml.Quaternionf

operator fun Quaternionf.times(b: Quaternionf): Quaternionf {
    return Quaternionf(
        x * b.w + b.x * w + y * b.z - z * b.y,
        y * b.w + b.y * w + z * b.x - x * b.z,
        z * b.w + b.z * w + x * b.y - y * b.x,
        w * b.w - x * b.x - y * b.y - z * b.z
    )
}