package dev.zorsh.zorshDeltarune.battle

import org.bukkit.Input

data class InputHolder(
    var left: Boolean = false,
    var right: Boolean = false,
    var forward: Boolean = false,
    var backward: Boolean = false,
    var jump: Boolean = false,
    var sneak: Boolean = false,
    var sprint: Boolean = false,
) {
    var a
        get() = left
        set(value) { left = value }
    var d
        get() = right
        set(value) { right = value }
    var w
        get() = forward
        set(value) { forward = value }
    var s
        get() = backward
        set(value) { backward = value }
    var space
        get() = jump
        set(value) { jump = value }
    var shift
        get() = sneak
        set(value) { sneak = value }
    var ctrl
        get() = sprint
        set(value) { sprint = value }

    constructor(input: Input) : this(
        input.isLeft,
        input.isRight,
        input.isForward,
        input.isBackward,
        input.isJump,
        input.isSneak,
        input.isSprint
    )
}