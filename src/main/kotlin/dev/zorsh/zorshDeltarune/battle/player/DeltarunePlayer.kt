package dev.zorsh.zorshDeltarune.battle.player

import dev.zorsh.zorshDeltarune.ZorshDeltarune
import dev.zorsh.zorshDeltarune.battle.BattleCanvas
import dev.zorsh.zorshDeltarune.battle.BattleManager
import dev.zorsh.zorshDeltarune.nms.FakeDisplay
import dev.zorsh.zorshDeltarune.nms.FakeTextDisplay
import dev.zorsh.zorshDeltarune.nms.PacketManager
import dev.zorsh.zorshDeltarune.ui.CanvasSprite
import dev.zorsh.zorshDeltarune.ui.ShaderTextColor
import dev.zorsh.zorshDeltarune.utils.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title.Times
import net.kyori.adventure.title.Title.title
import net.minecraft.world.entity.ai.attributes.Attributes
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Input
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import org.joml.Vector3d
import java.time.Duration
import java.util.UUID
import kotlin.math.max
import kotlin.math.min

class DeltarunePlayer(val uuid: UUID) {

    var player: Player? = null

    init {
        player = Bukkit.getPlayer(uuid)
    }

    var myBattleUUID: UUID? = null

    var locked = false

    var initialLocation: Location? = null

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

    var shakingTime = 0
    var shakingMult = 1.0

    var playerSelectedButton = 0

    var playerButtonTexts = mutableListOf<FakeDisplay>()

    private var prevInput = InputHolder()

    var soul: FakeTextDisplay? = null
    var soulOutline: FakeTextDisplay? = null
    var soulForOthers: FakeTextDisplay? = null

    var canMoveSoul = false

    private var gameMode = GameMode.SURVIVAL

    var onHpUpdated: (Int) -> Unit = {}

    var actionStage = PlayerActionStage.SELECT_BUTTON
    var battleInfoText: FakeTextDisplay? = null
    var moveMenuTexts: MutableList<FakeDisplay> = mutableListOf()

    fun updatePlayer() {
        player = Bukkit.getPlayer(uuid)
    }

    fun clearMenu() {
        moveMenuTexts.forEach { it.destroy() }
        moveMenuTexts.clear()
    }

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
        if (myBattleUUID == null) return
        player?.playSound(player!!, "soul_hurt", 1f, 1f)
        hp = max(hp - amount, 0)
        onHpUpdated(hp)
        healthCounter?.changeTransformation(healthCounter!!.transformation, Component.text("$hp / $maxhp"))
        healthBar?.changeTransformation(healthBar!!.transformation,
            Component.text(" ".repeat(hp)).style(Style.style(TextDecoration.UNDERLINED)).color("#00ff00")
                .append(Component.text(" ".repeat(maxhp-hp)).style(Style.style(TextDecoration.UNDERLINED)).color("#aa0000"))
        )
        if (hp == 0) {
            freeFromBattle(myBattleUUID!!)
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

    fun freeFromBattle(targetUUID: UUID) {
        if (myBattleUUID == null) return
        if (myBattleUUID != targetUUID) return
        player?.sendMessage(Component.text("Freeing from battle $myBattleUUID"))
        myBattleUUID = null
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
                player?.showToEveryone()
                PacketManager.setAttribute(
                    Attributes.JUMP_STRENGTH,
                    0.42,
                    player!!.entityId,
                    listOf(player!!)
                )
            }
        }
        perPlayerEntities.toList().forEach { it.destroy() }
        playerButtonTexts.toList().forEach { it.destroy() }
        perPlayerEntities.clear()
        playerButtonTexts.clear()
        playerSelectedButton = 0
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
            if (initialLocation != null) {
                player?.teleport(initialLocation!!)
            }
        }
    }

    var battleCenterLoc: Location? = null

    fun lockInBattle(location: Location) {
        val uuid = myBattleUUID ?: return
        if (player != null) {
            player?.sendMessage(Component.text("Locking to battle $uuid"))
            val myPlayer = player!!
            initialLocation = myPlayer.location
            myPlayer.teleport(location)
            myPlayer.isGliding = true
            gameMode = myPlayer.gameMode
            myPlayer.gameMode = GameMode.ADVENTURE
            locked = true
            myPlayer.hideFromEveryone()
            battleCenterLoc = location
            runInfinite(1) { i, action ->
                if (!myPlayer.isOnline) {
                    locked = false
                }

                if (myBattleUUID == null || !BattleManager.Companion.hasBattle(myBattleUUID!!)) {
                    locked = false
                }

                if (!locked) {
                    action.cancel()
                    freeFromBattle(uuid)
                } else {
                    if (i % 20 == 0) {
                        myPlayer.hideFromEveryone()
                        PacketManager.setAttribute(
                            Attributes.JUMP_STRENGTH,
                            0.0,
                            myPlayer.entityId,
                            listOf(myPlayer)
                        )
                    }
                    if (tpGain > 0) {
                        tpGain--
                        tpAmount = min(tpAmount + 0.5, 100.0)
                        updateTpCounter()
                    }

                    if (shakingTime > 0) {
                        PacketManager.playerLookAt(
                            myPlayer.location + Vector3d(ZorshDeltarune.random.nextDouble() * shakingTime * shakingMult * ((shakingTime % 2) * 2 - 1), 0.0, ZorshDeltarune.random.nextDouble() * shakingTime * shakingMult * 1000000) + Vector3d(0.0, -1000000000.0, 100.0),
                            listOf(myPlayer)
                        )
                    } else {
                        PacketManager.playerLookAt(
                            myPlayer.location + Vector3d(0.0, -1000000000.0, 100.0),
                            listOf(myPlayer)
                        )
                    }

                    if (shakingTime > 0) {
                        shakingTime--
                    }

                    val target = location
                    target.y = myPlayer.location.y
                    val playerOffset = myPlayer.location - target

                    if (!canMoveSoul) {
                        if (target.distance(myPlayer.location) > 0.02) {
                            val v = Vector(playerOffset.x * -0.15, playerOffset.y * -0.15, playerOffset.z * -0.15)
                            myPlayer.velocity = v
                        }
                    }
                }
            }
        }
    }

    val savedSouls = mutableMapOf<String, UUID>()
    fun unlockSoul(canvas: BattleCanvas, players: List<Player>) {
        canMoveSoul = true
        players.forEach { pl ->
            val uuid = UUID.randomUUID()
            canvas.myCanvas.drawSprite(
                0f, 20f, 1f, 1f, 5,
                CanvasSprite.SOUL_OTHER, ShaderTextColor.pure("#880000"), "soul_for_others_$uuid", pl
            )
            savedSouls["soul_for_others_$uuid"] = pl.uniqueId
        }
        val list = savedSouls.toMap()
        runInfinite(1) { _, action ->
            if (!locked || !canMoveSoul || battleCenterLoc == null || player == null) {
                savedSouls.forEach { (name, uuid) ->
                    canvas.myCanvas.remove(name, uuid)
                }
                savedSouls.clear()
                action.cancel()
            } else {
                val pos = player!!.location - battleCenterLoc!!
                val x = -pos.x.toFloat() * 16f * 8f
                val y = pos.z.toFloat() * 16f * 8f
                list.forEach { (name, uuid) ->
                    canvas.myCanvas.setPosition(x, y + 20f,name, uuid)
                }
            }
        }
    }

    fun lockSoul() {
        canMoveSoul = false
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
            inputCallbacksLeft.forEach { it() }
        }
        if (newInput.right && !prevInput.right) {
            inputCallbacksRight.forEach { it() }
        }
        if (newInput.forward && !prevInput.forward) {
            inputCallbacksForward.forEach { it() }
        }
        if (newInput.backward && !prevInput.backward) {
            inputCallbacksBackward.forEach { it() }
        }
        if (newInput.jump && !prevInput.jump) {
            inputCallbacksJump.forEach { it() }
        }
        if (newInput.sneak && !prevInput.sneak) {
            inputCallbacksSneak.forEach { it() }
        }
        if (newInput.sprint && !prevInput.sprint) {
            inputCallbacksSprint.forEach { it() }
        }
        prevInput = newInput
    }
}