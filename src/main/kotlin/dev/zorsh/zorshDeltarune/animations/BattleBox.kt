package dev.zorsh.zorshDeltarune.animations

import dev.zorsh.zorshDeltarune.nms.FakeTextDisplay
import dev.zorsh.zorshDeltarune.utils.runRepeating
import org.bukkit.util.Transformation
import org.joml.AxisAngle4f
import org.joml.Vector3f

class BattleBox(
    var outerPart: FakeTextDisplay? = null,
    var innerPart: FakeTextDisplay? = null
) {

    fun openAnimation() {
        runRepeating(15) { i ->
            val innerTransform = Transformation(
                Vector3f(0f, 0f, 0.00003f),
                AxisAngle4f(),
                Vector3f(i * 3f),
                AxisAngle4f((42f - i*3) * 0.3f, 0f, 0f, 1f)
            )
            innerPart?.changeTransformation(innerTransform)
            val outerTransform = Transformation(
                Vector3f(0f),
                AxisAngle4f(),
                Vector3f(i * 3.1f),
                AxisAngle4f((42f - i*3) * 0.3f, 0f, 0f, 1f)
            )
            outerPart?.changeTransformation(outerTransform)
        }
    }

    fun closeAnimation() {
        runRepeating(15) { t ->
            val i = 14-t
            val innerTransform = Transformation(
                Vector3f(0f, 0f, 0.00003f),
                AxisAngle4f(),
                Vector3f(i * 3f),
                AxisAngle4f((14f - i) * -0.3f, 0f, 0f, 1f)
            )
            innerPart?.changeTransformation(innerTransform)
            val outerTransform = Transformation(
                Vector3f(0f),
                AxisAngle4f(),
                Vector3f(i * 3.1f),
                AxisAngle4f((14f - i) * -0.3f, 0f, 0f, 1f)
            )
            outerPart?.changeTransformation(outerTransform)
        }
    }
}