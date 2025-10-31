package dev.zorsh.zorshDeltarune.battle

import dev.zorsh.zorshDeltarune.ZorshDeltarune
import dev.zorsh.zorshDeltarune.nms.PacketManager
import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.joml.Vector3d
import dev.zorsh.zorshDeltarune.utils.*
import org.bukkit.Input
import java.util.UUID

class DeltarunePlayer(val player: Player) {

    var myBattleUUID: UUID? = null

    var locked = false

    var hp = 100
    var maxhp = 100

    private var prevInput = InputHolder()

    fun freeFromBattle() {
        locked = false
        player.stopAllSounds()
        inputCallbacksLeft.clear()
        inputCallbacksRight.clear()
        inputCallbacksForward.clear()
        inputCallbacksBackward.clear()
        inputCallbacksJump.clear()
        inputCallbacksSneak.clear()
        inputCallbacksSprint.clear()
    }

    fun lockInBattle(location: Location) {
        val anchor = location.world?.spawnEntity(location, EntityType.BLOCK_DISPLAY) ?: return
        anchor.isPersistent = false

        locked = true
        anchor.addPassenger(player)
        object : BukkitRunnable() {
            override fun run() {
                if (!player.isOnline) {
                    locked = false
                }

                if (myBattleUUID == null || !BattleManager.hasBattle(myBattleUUID!!)) {
                    locked = false
                }

                if (!locked) {
                    if (anchor.isValid) {
                        anchor.removePassenger(player)
                        anchor.remove()
                    }
                    cancel()
                }

                PacketManager.playerLookAt(player.location + Vector3d(0.0, 0.0, 3.0), listOf(player))
            }
        }.runTaskTimer(ZorshDeltarune.instance, 1L, 1L)
    }

    private var inputCallbacksLeft = mutableListOf<() -> Unit>()
    private var inputCallbacksRight = mutableListOf<() -> Unit>()
    private var inputCallbacksForward = mutableListOf<() -> Unit>()
    private var inputCallbacksBackward = mutableListOf<() -> Unit>()
    private var inputCallbacksJump = mutableListOf<() -> Unit>()
    private var inputCallbacksSneak = mutableListOf<() -> Unit>()
    private var inputCallbacksSprint = mutableListOf<() -> Unit>()

    fun onLeftPressed(action: () -> Unit) {
        inputCallbacksLeft += action
    }

    fun onRightPressed(action: () -> Unit) {
        inputCallbacksRight += action
    }

    fun onForwardPressed(action: () -> Unit) {
        inputCallbacksForward += action
    }

    fun onBackwardPressed(action: () -> Unit) {
        inputCallbacksBackward += action
    }

    fun onJumpPressed(action: () -> Unit) {
        inputCallbacksJump += action
    }

    fun onSneakPressed(action: () -> Unit) {
        inputCallbacksSneak += action
    }

    fun onSprintPressed(action: () -> Unit) {
        inputCallbacksSprint += action
    }

    fun updateInputs(input: Input) {
        val newInput = InputHolder(input)
        if (newInput.left && !prevInput.left) {
            inputCallbacksLeft.map { it() }
        }
        if (newInput.right && !prevInput.right) {
            inputCallbacksRight.map { it() }
        }
        if (newInput.forward && !prevInput.forward) {
            inputCallbacksForward.map { it() }
        }
        if (newInput.backward && !prevInput.backward) {
            inputCallbacksBackward.map { it() }
        }
        if (newInput.jump && !prevInput.jump) {
            inputCallbacksJump.map { it() }
        }
        if (newInput.sneak && !prevInput.sneak) {
            inputCallbacksSneak.map { it() }
        }
        if (newInput.sprint && !prevInput.sprint) {
            inputCallbacksSprint.map { it() }
        }
        prevInput = newInput
    }
}