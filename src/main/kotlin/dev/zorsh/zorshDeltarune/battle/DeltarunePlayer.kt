package dev.zorsh.zorshDeltarune.battle

import dev.zorsh.zorshDeltarune.ZorshDeltarune
import dev.zorsh.zorshDeltarune.nms.FakeDisplay
import dev.zorsh.zorshDeltarune.nms.FakeTextDisplay
import dev.zorsh.zorshDeltarune.nms.PacketManager
import dev.zorsh.zorshDeltarune.utils.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.Input
import org.bukkit.entity.EntityType
import org.joml.Vector3d
import java.util.UUID
import kotlin.math.max

class DeltarunePlayer(private val uuid: UUID) {

    val player by lazy {  Bukkit.getPlayer(uuid) }

    var myBattleUUID: UUID? = null

    var locked = false

    var hp = 100
    var maxhp = 100

    var perPlayerEntities = mutableListOf<FakeDisplay>()

    var playerButtons: FakeDisplay? = null

    var healthCounter: FakeDisplay? = null
    var healthBar: FakeDisplay? = null
    var noDamageTicks = 0

    var playerSelectedButton = 1

    var playerButtonTexts = mutableListOf<FakeDisplay>()

    private var prevInput = InputHolder()

    var soul: FakeTextDisplay? = null

    var canMoveSoul = false

    private var gameMode = GameMode.SURVIVAL

    private val soulSpeed = 0.12

    var onHpUpdated: (Int) -> Unit = {}

    fun damage(amount: Int) {
        hp = max(hp - amount, 0)
        onHpUpdated(hp)
        healthCounter?.changeTransformation(healthCounter!!.transformation, Component.text("$hp / $maxhp"))
        healthBar?.changeTransformation(healthBar!!.transformation,
            Component.text(" ".repeat(hp)).style(Style.style(TextDecoration.UNDERLINED)).color("#00ff00")
                .append(Component.text(" ".repeat(maxhp-hp)).style(Style.style(TextDecoration.UNDERLINED)).color("#aa0000"))
        )
        if (hp == 0) {
            freeFromBattle()
        }
        noDamageTicks = 40
        runRepeating(40) { i ->
            noDamageTicks--
            if ((i / 4) % 2 == 0) {
                soul?.changeTransformation(soul!!.transformation, fontText("❤", "#992222", "space:default"))
            } else {
                soul?.changeTransformation(soul!!.transformation, fontText("❤", "#ff2222", "space:default"))
            }
        }
        runLater(41) {
            soul?.changeTransformation(soul!!.transformation, fontText("❤", "#ff2222", "space:default"))
        }
    }

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
        soul?.destroy()
        soul = null
        perPlayerEntities.map { it.destroy() }
        playerButtonTexts.map { it.destroy() }
        perPlayerEntities.clear()
        playerButtonTexts.clear()
        playerSelectedButton = 1
        //player?.flySpeed = 0.1f
        runLater(10) {
            player?.gameMode = gameMode
        }
    }

    fun lockInBattle(location: Location) {
        val battle = BattleManager.getBattle(myBattleUUID ?: return) ?: return
        if (player != null) {
            val myPlayer = player!!
            gameMode = myPlayer.gameMode
            myPlayer.gameMode = GameMode.SPECTATOR
            val anchor = location.world.spawnEntity(location, EntityType.BLOCK_DISPLAY)
            anchor.isPersistent = false
            anchor.addPassenger(myPlayer)
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
                        anchor.remove()
                        cancel()
                        freeFromBattle()
                    } else {
                        val box = battle.battleBox
//                        myPlayer.teleport(location)
                        PacketManager.playerLookAt(myPlayer.location + Vector3d(0.0, 0.0, 5.0), listOf(myPlayer))
                        val inputs = InputHolder(myPlayer.currentInput)
                        if (canMoveSoul && soul != null) {
                            val soulWidth = soul!!.transformation.scale.x * 0.18f
                            val soulHeight = soul!!.transformation.scale.y * 0.17f

                            var speed = soulSpeed
                            if (inputs.sneak) {
                                speed /= 1.5
                            }

                            if (inputs.left && !inputs.right) {
                                val new = soul!!.location + Vector3d(speed, 0.0, 0.0)
                                soul?.teleport(box.isInside(new, soulWidth, soulHeight).second)
                            }
                            if (inputs.right && !inputs.left) {
                                val new = soul!!.location + Vector3d(-speed, 0.0, 0.0)
                                soul?.teleport(box.isInside(new, soulWidth, soulHeight).second)
                            }
                            if (inputs.forward && !inputs.backward) {
                                val new = soul!!.location + Vector3d(0.0, speed, 0.0)
                                soul?.teleport(box.isInside(new, soulWidth, soulHeight).second)
                            }
                            if (inputs.backward && !inputs.forward) {
                                val new = soul!!.location + Vector3d(0.0, -speed, 0.0)
                                soul?.teleport(box.isInside(new, soulWidth, soulHeight).second)
                            }

                            val check = box.isInside(soul!!.location, soulWidth, soulHeight)
                            if (!check.first) {
                                soul?.teleport(check.second)
                            }
                        }
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