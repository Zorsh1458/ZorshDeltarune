package dev.zorsh.zorshDeltarune.battle

import dev.zorsh.zorshDeltarune.ZorshDeltarune
import dev.zorsh.zorshDeltarune.nms.FakeDisplay
import dev.zorsh.zorshDeltarune.nms.FakeTextDisplay
import dev.zorsh.zorshDeltarune.nms.PacketManager
import dev.zorsh.zorshDeltarune.utils.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title.Times
import net.kyori.adventure.title.Title.title
import org.bukkit.Bukkit
import org.bukkit.Effect
import org.bukkit.EntityEffect
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.Input
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.util.Vector
import org.joml.Vector2f
import org.joml.Vector3d
import org.joml.Vector3f
import java.time.Duration
import java.util.UUID
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class DeltarunePlayer(private val uuid: UUID) {

    val player by lazy {  Bukkit.getPlayer(uuid) }

    var myBattleUUID: UUID? = null

    var locked = false

    var hp = 1000
    var maxhp = 1000

    private var passengers = mutableListOf<FakeDisplay>()

    var perPlayerEntities = mutableListOf<FakeDisplay>()

    var playerButtons: FakeDisplay? = null

    var healthCounter: FakeDisplay? = null
    var healthBar: FakeDisplay? = null
    var noDamageTicks = 0

    var tpCounter: FakeDisplay? = null
    var tpBar: FakeDisplay? = null
    var tpGain = 0

    var tpAmount = 0.0

    var playerSelectedButton = 1

    var playerButtonTexts = mutableListOf<FakeDisplay>()

    private var prevInput = InputHolder()

    var soul: FakeTextDisplay? = null
    var soulOutline: FakeTextDisplay? = null

    var canMoveSoul = false

    private var gameMode = GameMode.SURVIVAL

    private val soulSpeed = 0.12

    var onHpUpdated: (Int) -> Unit = {}

    private var anchor: Entity? = null

    fun mountEntity(ent: FakeDisplay) {
        if (player != null) {
            passengers.removeIf { !it.exists }
            passengers += ent
            PacketManager.mountEntities(player!!.entityId, passengers.map { it.entityId }, listOf(player!!))
        }
    }

    fun updateTpCounter() {
//        tpCounter?.changeTransformation(tpCounter!!.transformation, Component.text("X: ${(soulLocation.x * 10).roundToInt() / 10.0} | Y: ${(soulLocation.y * 10).roundToInt() / 10.0}"))
        if (tpAmount == 100.0) {
            tpCounter?.changeTransformation(tpCounter!!.transformation, Component.text("MAKC.").font("space:smooth"))
            tpBar?.changeTransformation(
                tpBar!!.transformation,
                Component.text(" ".repeat(100)).style(Style.style(TextDecoration.UNDERLINED)).color("#ffff00")
            )
        } else {
            tpCounter?.changeTransformation(tpCounter!!.transformation, Component.text("${tpAmount.toInt()}").font("space:smooth")
                .append(Component.text("%").font("minecraft:default")))
            tpBar?.changeTransformation(
                tpBar!!.transformation,
                Component.text(" ".repeat(max(tpAmount.toInt() - 2, 0))).style(Style.style(TextDecoration.UNDERLINED))
                    .color("#ffb24d")
                    .append(Component.text("  ").style(Style.style(TextDecoration.UNDERLINED)).color("#ffffff"))
                    .append(
                        Component.text(" ".repeat(100 - tpAmount.toInt())).style(Style.style(TextDecoration.UNDERLINED))
                            .color("#770000")
                    )
            )
        }
    }

    fun damage(amount: Int) {
        player?.playSound(player!!, "soul_hurt", 1f, 1f)
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
        runRepeating(40) { i, _ ->
            noDamageTicks--
            if ((i / 4) % 2 == 0) {
                soul?.changeTransformation(soul!!.transformation, fontText("❤", "#772222", "space:default"))
            } else {
                soul?.changeTransformation(soul!!.transformation, fontText("❤", "#ff2222", "space:default"))
            }
        }
        runLater(41) {
            soul?.changeTransformation(soul!!.transformation, fontText("❤", "#ff2222", "space:default"))
        }
    }

    fun tpGain() {
        if (soulOutline != null) {
            soulOutline?.changeTransformation(soulOutline!!.transformation, newOpacity = 128.toByte())
            if (tpGain <= 0) {
                player?.playSound(player!!, "tp_gain", 1f, 1f)
            }
            tpGain = 3
            runLater(3) {
                if (tpGain <= 0) {
                    soulOutline?.changeTransformation(soulOutline!!.transformation, newOpacity = 0)
                }
            }
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
        //
        passengers.clear()
        //
        playerButtons?.destroy()
        playerButtons = null
        soul?.destroy()
        soul = null
        soulOutline?.destroy()
        soulOutline = null
        tpBar?.destroy()
        tpBar = null
        tpCounter?.destroy()
        tpCounter = null
        runLater(1) {
            if (player != null) {
                anchor?.removePassenger(player!!)
                PacketManager.setAttribute(
                    net.minecraft.world.entity.ai.attributes.Attributes.JUMP_STRENGTH,
                    0.42,
                    player!!.entityId,
                    listOf(player!!)
                )
            }
            anchor?.remove()
            anchor = null
        }
        perPlayerEntities.toList().forEach { it.destroy() }
        playerButtonTexts.toList().forEach { it.destroy() }
        perPlayerEntities.clear()
        playerButtonTexts.clear()
        playerSelectedButton = 1
        //player?.flySpeed = 0.1f
        runSync {
            if (player != null) {
                Bukkit.dispatchCommand(
                    Bukkit.getServer().consoleSender,
                    "sendshaderdata ${player!!.name} 0"
                )
            }
        }
        player?.showTitle(
            title(
            fontText("\uD701", "#000000", "space:default"),
            Component.text(""),
            Times.times(Duration.ZERO, Duration.ofMillis(1000), Duration.ofMillis(100))
        ))
        runLater(6) {
            player?.gameMode = gameMode
        }
    }

    fun lockInBattle(location: Location) {
//        val battle = BattleManager.getBattle(myBattleUUID ?: return) ?: return
        if (player != null) {
            val myPlayer = player!!
            myPlayer.teleport(location)
            myPlayer.isGliding = true
            PacketManager.setAttribute(
                net.minecraft.world.entity.ai.attributes.Attributes.JUMP_STRENGTH,
                0.0,
                myPlayer.entityId,
                listOf(myPlayer)
            )
//            gameMode = myPlayer.gameMode
//            myPlayer.gameMode = GameMode.SPECTATOR
//            anchor = location.world.spawnEntity(location, EntityType.BLOCK_DISPLAY)
//            anchor?.isPersistent = false
//            anchor?.addPassenger(myPlayer)
//            myPlayer.sendActionBar(Component.text(""))
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
                        if (tpGain > 0) {
                            tpGain--
                            tpAmount = min(tpAmount + 0.5, 100.0)
                            updateTpCounter()
                        }

                        PacketManager.playerLookAt(
                            myPlayer.location + Vector3d(0.0, -1000000000.0, 100.0),
                            listOf(myPlayer)
                        )
                        val target = location
                        target.y = myPlayer.location.y

//                        soulLocation = Vector2f((myPlayer.location.x - target.x).toFloat() * 8f / -1.8f, (myPlayer.location.z - target.z).toFloat() * 8f / -1.8f)

                        if (!canMoveSoul && soul != null) {
                            if (target.distance(myPlayer.location) > 0.02) {
                                val l = target - myPlayer.location
                                val v = Vector(l.x * 0.15, l.y * 0.15, l.z * 0.15)
                                myPlayer.velocity = v
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