package dev.zorsh.zorshDeltarune.battle

import dev.zorsh.zorshDeltarune.ZorshDeltarune
import dev.zorsh.zorshDeltarune.nms.FakeDisplay
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.Input
import org.bukkit.craftbukkit.entity.CraftPlayer
import java.util.UUID

class DeltarunePlayer(private val uuid: UUID) {

    val player by lazy {  Bukkit.getPlayer(uuid) }

    var myBattleUUID: UUID? = null

    var locked = false

    var hp = 100
    var maxhp = 100

    var perPlayerEntities = mutableListOf<FakeDisplay>()

    var playerButtons: FakeDisplay? = null

    var playerSelectedButton = 1

    var playerButtonTexts = mutableListOf<FakeDisplay>()

    private var prevInput = InputHolder()

    fun freeFromBattle() {
        locked = false
        player?.stopAllSounds()
        inputCallbacksLeft.clear()
        inputCallbacksRight.clear()
        inputCallbacksForward.clear()
        inputCallbacksBackward.clear()
        inputCallbacksJump.clear()
        inputCallbacksSneak.clear()
        inputCallbacksSprint.clear()
        playerButtons?.destroy()
        playerButtons = null
        perPlayerEntities.map { it.destroy() }
        playerButtonTexts.map { it.destroy() }
        perPlayerEntities.clear()
        playerButtonTexts.clear()
        playerSelectedButton = 1
        //player?.flySpeed = 0.1f
    }

    fun lockInBattle(location: Location) {
        if (player != null) {
            val myPlayer = player!!
            locked = true
            object : BukkitRunnable() {
                override fun run() {
                    if (!myPlayer.isOnline) {
                        locked = false
                    }

                    if (myBattleUUID == null || !BattleManager.hasBattle(myBattleUUID!!)) {
                        locked = false
                    }

                    if (!locked) {
                        cancel()
                        freeFromBattle()
                    } else {
                        //myPlayer.flySpeed = 0.1f
                        myPlayer.teleport(location)
                    }
                }
            }.runTaskTimer(ZorshDeltarune.instance, 1L, 1L)
        }
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