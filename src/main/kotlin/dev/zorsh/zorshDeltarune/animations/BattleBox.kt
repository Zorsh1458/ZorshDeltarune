package dev.zorsh.zorshDeltarune.animations

import dev.zorsh.zorshDeltarune.nms.FakeTextDisplay
import dev.zorsh.zorshDeltarune.utils.plus
import dev.zorsh.zorshDeltarune.utils.runRepeating
import dev.zorsh.zorshDeltarune.utils.times
import org.bukkit.Location
import org.bukkit.util.Transformation
import org.joml.AxisAngle4f
import org.joml.Vector3f

class BattleBox(
//    var location: Location? = null,
    var outerPart: FakeTextDisplay? = null,
    var innerPart: FakeTextDisplay? = null,
    var sizeX: Float = 10000f,
    var sizeY: Float = 10000f,
    val sceneScale: Vector3f
) {

    private val offset = Vector3f(0f * sceneScale.x, 0.5f * sceneScale.y, -0.45f)

    fun openAnimation() {
        val count = 10f
        runRepeating(count.toInt()) { i, _ ->
            val innerTransform = Transformation(
                Vector3f(0f, 0f, 0.00003f) * sceneScale + offset,
                AxisAngle4f(),
                Vector3f(i * sizeX / (count-1), i * sizeY / (count-1), 1f) * sceneScale,
                AxisAngle4f((18f - i*2) * 0.3f, 0f, 0f, 1f)
            )
            innerPart?.changeTransformation(innerTransform)
            val outerTransform = Transformation(
                offset,
                AxisAngle4f(),
                Vector3f(i * (sizeX / (count-1) + 0.15f), i * (sizeY / (count-1) + 0.15f), 1f) * sceneScale,
                AxisAngle4f((18f - i*2) * 0.3f, 0f, 0f, 1f)
            )
            outerPart?.changeTransformation(outerTransform)
        }
    }

    fun closeAnimation() {
        val count = 15f
        runRepeating(count.toInt()) { t, _ ->
            val i = count-1-t
            val innerTransform = Transformation(
                Vector3f(0f, 0f, 0.00003f) * sceneScale + offset,
                AxisAngle4f(),
                Vector3f(i * sizeX / (count-1), i * sizeY / (count-1), 1f) * sceneScale,
                AxisAngle4f((14f - i) * -0.3f, 0f, 0f, 1f)
            )
            innerPart?.changeTransformation(innerTransform)
            val outerTransform = Transformation(
                offset,
                AxisAngle4f(),
                Vector3f(i * (sizeX / (count-1) + 0.15f), i * (sizeY / (count-1) + 0.15f), 1f) * sceneScale,
                AxisAngle4f((14f - i) * -0.3f, 0f, 0f, 1f)
            )
            outerPart?.changeTransformation(outerTransform)
        }
    }
}