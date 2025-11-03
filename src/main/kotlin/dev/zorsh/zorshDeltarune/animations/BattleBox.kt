package dev.zorsh.zorshDeltarune.animations

import dev.zorsh.zorshDeltarune.nms.FakeTextDisplay
import dev.zorsh.zorshDeltarune.utils.runRepeating
import org.bukkit.Location
import org.bukkit.util.Transformation
import org.joml.AxisAngle4f
import org.joml.Vector3f

class BattleBox(
    var location: Location? = null,
    var outerPart: FakeTextDisplay? = null,
    var innerPart: FakeTextDisplay? = null,
    var sizeX: Float = 10000f,
    var sizeY: Float = 10000f
) {

    fun isInside(loc: Location, width: Float = 0f, height: Float = 0f): Pair<Boolean, Location> {
        val scaling = 0.05f
        val result = Location(loc.world, loc.x, loc.y, loc.z, loc.yaw, loc.pitch)
        if (location != null && loc.x + width > sizeX * scaling + location!!.x) {
            result.x = sizeX * scaling + location!!.x - width
        }
        if (location != null && loc.x - width < -sizeX * scaling + location!!.x) {
            result.x = -sizeX * scaling + location!!.x + width
        }
        if (location != null && loc.y + height > sizeY * scaling + location!!.y) {
            result.y = sizeY * scaling + location!!.y - height
        }
        if (location != null && loc.y - height < -sizeY * scaling + location!!.y) {
            result.y = -sizeY * scaling + location!!.y + height
        }
        return (result.x == loc.x && result.y == loc.y) to result
    }

    fun openAnimation() {
        val count = 10f
        runRepeating(count.toInt()) { i ->
            val innerTransform = Transformation(
                Vector3f(0f, 0f, 0.00003f),
                AxisAngle4f(),
                Vector3f(i * sizeX / (count-1), i * sizeY / (count-1), 1f),
                AxisAngle4f((18f - i*2) * 0.3f, 0f, 0f, 1f)
            )
            innerPart?.changeTransformation(innerTransform)
            val outerTransform = Transformation(
                Vector3f(0f),
                AxisAngle4f(),
                Vector3f(i * (sizeX / (count-1) + 0.15f), i * (sizeY / (count-1) + 0.15f), 1f),
                AxisAngle4f((18f - i*2) * 0.3f, 0f, 0f, 1f)
            )
            outerPart?.changeTransformation(outerTransform)
        }
    }

    fun closeAnimation() {
        val count = 15f
        runRepeating(count.toInt()) { t ->
            val i = count-1-t
            val innerTransform = Transformation(
                Vector3f(0f, 0f, 0.00003f),
                AxisAngle4f(),
                Vector3f(i * sizeX / (count-1), i * sizeY / (count-1), 1f),
                AxisAngle4f((14f - i) * -0.3f, 0f, 0f, 1f)
            )
            innerPart?.changeTransformation(innerTransform)
            val outerTransform = Transformation(
                Vector3f(0f),
                AxisAngle4f(),
                Vector3f(i * (sizeX / (count-1) + 0.15f), i * (sizeY / (count-1) + 0.15f), 1f),
                AxisAngle4f((14f - i) * -0.3f, 0f, 0f, 1f)
            )
            outerPart?.changeTransformation(outerTransform)
        }
    }
}