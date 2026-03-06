package dev.zorsh.zorshDeltarune.battle

import dev.zorsh.zorshDeltarune.nms.FakeTextDisplay
import dev.zorsh.zorshDeltarune.utils.plus
import dev.zorsh.zorshDeltarune.utils.times
import org.bukkit.util.Transformation
import org.joml.Vector3f
import kotlin.math.max
import kotlin.math.min

class MenuSelectorHeart(
    private val display: FakeTextDisplay,
    private val sceneScale: Vector3f
) {
    var myX = 0
    var myY = 0
    var boundX = 1
    var boundY = 1

    fun setBounds(bx: Int, by: Int) {
        boundX = bx
        boundY = by
    }

    fun hide() {
        display.changeOnlyTransformation(
            Transformation(
                display.transformation.translation,
                display.transformation.leftRotation,
                Vector3f(0f),
                display.transformation.rightRotation
            )
        )
    }

    fun show() {
        display.changeOnlyTransformation(
            Transformation(
                display.transformation.translation,
                display.transformation.leftRotation,
                Vector3f(1.1f) * sceneScale,
                display.transformation.rightRotation
            )
        )
    }

    fun offset(x: Int, y: Int) {
        val actualX = min(max(myX + x, 0), boundX - 1) - myX
        val actualY = min(max(myY + y, 0), boundY - 1) - myY
        display.changeOnlyTransformation(
            Transformation(
                display.transformation.translation + Vector3f(6f * actualX, -0.75f * actualY, 0f) * sceneScale,
                display.transformation.leftRotation,
                display.transformation.scale,
                display.transformation.rightRotation
            )
        )
        myX += actualX
        myY += actualY
    }

    fun setPosition(x: Int, y: Int) {
        display.changeOnlyTransformation(
            Transformation(
                display.transformation.translation + Vector3f(6f * (x - myX), -0.75f * (y - myY), 0f) * sceneScale,
                display.transformation.leftRotation,
                display.transformation.scale,
                display.transformation.rightRotation
            )
        )
        myX = x
        myY = y
    }
}