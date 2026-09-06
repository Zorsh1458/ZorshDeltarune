package dev.zorsh.zorshDeltarune.battle

import dev.zorsh.zorshDeltarune.battle.enemy.DeltaruneEnemy
import dev.zorsh.zorshDeltarune.battle.player.DeltarunePlayer
import dev.zorsh.zorshDeltarune.battle.projectile.ProjectileData
import dev.zorsh.zorshDeltarune.nms.FakeDisplay
import dev.zorsh.zorshDeltarune.nms.FakeTextDisplay
import dev.zorsh.zorshDeltarune.nms.PacketManager
import dev.zorsh.zorshDeltarune.ui.CanvasSprite
import dev.zorsh.zorshDeltarune.ui.PlayerUICanvas
import dev.zorsh.zorshDeltarune.ui.ShaderTextColor
import dev.zorsh.zorshDeltarune.utils.FakeDisplayData
import dev.zorsh.zorshDeltarune.utils.fontText
import dev.zorsh.zorshDeltarune.utils.minus
import dev.zorsh.zorshDeltarune.utils.plus
import dev.zorsh.zorshDeltarune.utils.runLater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title.Times
import net.kyori.adventure.title.Title.title
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.bukkit.scheduler.BukkitTask
import org.bukkit.util.Transformation
import org.joml.AxisAngle4f
import org.joml.Vector3d
import org.joml.Vector3f
import java.time.Duration
import java.util.UUID
import kotlin.math.ceil

class NeverlandBattle(val players: List<DeltarunePlayer>, val enemies: List<DeltaruneEnemy>) : INeverlandBattle {

    var active = false

    lateinit var battleUUID: UUID

    private var onEndedAction = {}

    private val battleCanvas = BattleCanvas(players.mapNotNull { it.player }, this)

    private val playersNotQuit = players.toMutableList()

    private var loopTask: BukkitTask? = null

    private var battleJob: Job? = null

    private val scope = CoroutineScope(Dispatchers.IO)

    private val shulkerHitboxes = mutableSetOf<Int>()
    private val spawnedEntities = mutableSetOf<FakeDisplay>()

    private var theSoul: FakeTextDisplay? = null
    private var soulGraze: FakeTextDisplay? = null

    object BattleLocation {
//        val TEST = Location(Bukkit.getWorld("world"), 8.0, 100.0, 8.1)
        val UNDER_STATION = Location(Bukkit.getWorld("moon"), 952.0, 99.6, 1101.0)
    }

    private val battleCenterLocation = BattleLocation.UNDER_STATION

    private var battleBoxLocation = 0f to 20f

    override fun getBBLocation() = battleBoxLocation

    override fun isActive() = active

    private fun newHitboxEntity(
        loc: Location,
        scale: Double,
        playerToShow: List<Player> = players.mapNotNull { it.player },
    ) {
        PacketManager.spawnHitbox(loc, scale, playerToShow) { anchor, shulker ->
            shulkerHitboxes += anchor
            shulkerHitboxes += shulker
        }
    }

    override fun destroyBattle() {
        active = false
        for (pl in players) {
            try {
                pl.freeFromBattle(battleUUID, true)
            } catch (_: Exception) {
            }
        }
        val toDestroy = spawnedEntities.toList()
        for (ent in toDestroy) {
            try {
                ent.destroy()
            } catch (_: Exception) {
            }
        }
        spawnedEntities.clear()
        if (loopTask?.isCancelled == false) {
            loopTask?.cancel()
        }
        if (battleJob?.isCancelled == false) {
            battleJob?.cancel()
        }
        for (ent in shulkerHitboxes) {
            try {
                PacketManager.removeEntity(ent, getBattlePlayers().mapNotNull { it.player })
            } catch (_: Exception) {
            }
        }
        shulkerHitboxes.clear()
        scope.cancel()
        battleCanvas.myCanvas.clear()
    }

    override fun setUUID(uuid: UUID) {
        battleUUID = uuid
    }

    override fun getBattleInitialPlayers() = players

    override fun getBattlePlayers() = playersNotQuit.filter { it.player?.isOnline == true && it.myBattleUUID == battleUUID }

    override fun getBattleEnemies() = enemies

    override fun removePlayer(dPlayer: DeltarunePlayer) {
        dPlayer.player?.let { player ->
            battleCanvas.myCanvas.removePlayer(player)
            spawnedEntities.forEach { ent ->
                ent.destroy(player)
            }
        }
        playersNotQuit -= dPlayer
    }

    override fun createProjectile(
        px: Float,
        py: Float,
        projectileData: ProjectileData,
        afterCreated: (PlayerUICanvas, String, (Int) -> Unit) -> Unit,
    ) {
        val projId = UUID.randomUUID()
        battleCanvas.myCanvas.drawSprite(
            px,
            py,
            1f,
            1f,
            48,
            projectileData.sprites.first(),
            ShaderTextColor.pure("#ffffff"),
            "projectile_$projId"
        ) {
            if (projectileData.framesPerSprite != null) {
                battleCanvas.animateSprite(
                    projectileData.sprites,
                    projectileData.framesPerSprite,
                    "projectile_$projId",
                    stoppingCondition = { return@animateSprite !isActive() || !battleCanvas.myCanvas.hasObject("projectile_$projId") }
                )
            }
            afterCreated(battleCanvas.myCanvas, "projectile_$projId") { baseDamageAmount ->
                for (pl in getBattlePlayers()) {
                    if (pl.noDamageTicks > 0) continue

                    var px = pl.player!!.x - battleCenterLocation.x
                    var py = pl.player!!.z - battleCenterLocation.z
                    px *= -1f
                    px *= 16f * 8f
                    py *= 16f * 8f
                    px += battleBoxLocation.first
                    py += battleBoxLocation.second
                    val projPosition = battleCanvas.myCanvas.getPosition("projectile_$projId")
                    px -= projPosition.first
                    py -= projPosition.second
                    if (projectileData.hitbox.isIn(px.toFloat(), py.toFloat(), 6f)) {
                        pl.damage(baseDamageAmount) { hp ->
                            battleCanvas.updateHealthInfo(hp, pl.maxhp, pl.uuid)
                            if (hp <= 0) {
                                pl.freeFromBattle(battleUUID, false)
                            }
                        }
                    } else if (projectileData.hitbox.isIn(px.toFloat(), py.toFloat(), 20f)) {
                        if (pl.tpGain == 0) {
                            soulGraze?.let { ent ->
                                val (sx, sy) = CanvasSprite.SOUL_OUTLINE.getSizeRatios()
                                ent.changeOnlyTransformation(
                                    Transformation(
                                        ent.transformation.translation,
                                        ent.transformation.leftRotation,
                                        Vector3f(2.5f * sx / 8f, 2.5f * sy / 8f, 1f),
                                        ent.transformation.rightRotation
                                    ),
                                    listOf(pl.player!!)
                                )
                            }
                        }
                        pl.tpGain()
                        runLater(4) {
                            if (pl.tpGain == 0) {
                                soulGraze?.let { ent ->
                                    ent.changeOnlyTransformation(
                                        Transformation(
                                            ent.transformation.translation,
                                            ent.transformation.leftRotation,
                                            Vector3f(0f, 0f, 1f),
                                            ent.transformation.rightRotation
                                        ),
                                        listOf(pl.player!!)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun startBattle(onEnded: () -> Unit) {
        onEndedAction = onEnded

        for (pl in players) {
            pl.updatePlayer()
        }

        for (pl in getBattlePlayers()) {
            pl.player?.showTitle(
                title(
                    fontText("\uD701", "#000000", "space:default"),
                    Component.text(""),
                    Times.times(Duration.ZERO, Duration.ofMillis(1200), Duration.ZERO)
                )
            )
            runLater(3) {
                if (pl.player != null) {
                    Bukkit.dispatchCommand(
                        Bukkit.getServer().consoleSender,
                        "sendshaderdata ${pl.player!!.name} 1"
                    )
                }
            }
            pl.lockInBattle(battleCenterLocation)
            pl.player?.playSound(pl.player!!, "encounter", 1f, 1f)
            runLater(20) {
                pl.player?.playSound(pl.player!!, "weaponpull", 1f, 1f)
            }
            runLater(30) {
                pl.player?.playSound(pl.player!!, "battle", 1f, 1f)
            }
        }

        active = true

        enemies.forEach { enemy ->
            enemy.myBattle = this
        }

        runLater(2) {
            prepareBattle()
            battleCanvas.initCanvas()
            battleCanvas.setupLayout()
        }

        runLater(40) {
            CoroutineScope(Dispatchers.IO).launch {
                val job = scope.launch {
                    repeat(3) {
                        battleCanvas.showPlayerOptions()
                        delay(3000)
                        battleCanvas.hidePlayerOptions()
                        battleBoxOpen()
                        delay(500)
                        unlockSouls()
                        val jobs = mutableListOf<Job>()
                        for (enemy in enemies) {
                            jobs += launch {
                                enemy.attack()
                            }
                        }
                        jobs.joinAll()
                        delay(200)
                        lockSouls()
                        battleBoxClose()
                        delay(500)
                    }
                }
                battleJob = job
                job.join()
                endBattle()
            }
        }

//            loopTask = object : BukkitRunnable() {
//                override fun run() {
//                    if (players.all { !it.locked || it.myBattleUUID != battleUUID }) {
//                        if (battleJob?.isCancelled == false) {
//                            battleJob?.cancel()
//                        }
//                        cancel()
//                    }
//                }
//            }.runTaskTimer(ZorshDeltarune.instance, 1L, 1L)
    }

    override fun endBattle() {
        destroyBattle()
        onEndedAction()
    }

    private fun prepareBattle() {
        val loc = battleCenterLocation
        loc.yaw = 0f
        loc.pitch = -90f

        runLater(3) {
            // SOUL
            val sprite = CanvasSprite.SOUL
            PacketManager.spawnTextDisplay(
                loc,
                sprite.toTextValue().color(ShaderTextColor.pure("#ff0000").value),
                getBattlePlayers().mapNotNull { it.player },
                data = FakeDisplayData(
                    Transformation(
                        Vector3f(battleBoxLocation.first / 16f / 8f, battleBoxLocation.second / 16f / 8f, 0f),
                        AxisAngle4f(),
                        Vector3f(0f, 0f, 1f),
                        AxisAngle4f()
                    ),
                    opacity = 251.toByte()
                ),
                seeThrough = false,
                alignment = TextDisplay.TextAlignment.CENTER,
                lineWidth = 1000,
                isShadowed = false
            ) { ent ->
                spawnedEntities += ent
                theSoul = ent
            }

            // SOUL_GRAZE_OUTLINE
            val spriteGraze = CanvasSprite.SOUL_OUTLINE
            PacketManager.spawnTextDisplay(
                loc,
                spriteGraze.toTextValue().color(ShaderTextColor.effect(1, 6, 0).value),
                getBattlePlayers().mapNotNull { it.player },
                data = FakeDisplayData(
                    Transformation(
                        Vector3f(battleBoxLocation.first / 16f / 8f, battleBoxLocation.second / 16f / 8f, 0f),
                        AxisAngle4f(),
                        Vector3f(0f, 0f, 1f),
                        AxisAngle4f()
                    ),
                    interpolationDuration = 0,
                    opacity = 251.toByte()
                ),
                seeThrough = false,
                alignment = TextDisplay.TextAlignment.CENTER,
                lineWidth = 1000,
                isShadowed = false
            ) { ent ->
                spawnedEntities += ent
                soulGraze = ent
            }
        }
    }

    private fun unlockSouls() {
        val soul = theSoul ?: return
        val scaling = CanvasSprite.SOUL.getSizeRatios()
        theSoul?.changeOnlyTransformation(
            Transformation(
                soul.transformation.translation,
                soul.transformation.leftRotation,
                Vector3f(2.5f * scaling.first / 8f, 2.5f * scaling.second / 8f, 1f),
                soul.transformation.rightRotation
            )
        )
        getBattlePlayers().forEach { battlePlayer ->
            battlePlayer.unlockSoul(
                battleBoxLocation,
                battleCanvas,
                getBattlePlayers().mapNotNull { dp -> dp.player }.filter { pl -> pl.uniqueId != battlePlayer.uuid })
        }
    }

    private fun lockSouls() {
        val soul = theSoul ?: return
        theSoul?.changeOnlyTransformation(
            Transformation(
                soul.transformation.translation,
                soul.transformation.leftRotation,
                Vector3f(0f, 0f, 1f),
                soul.transformation.rightRotation
            )
        )
        getBattlePlayers().forEach {
            it.lockSoul()
        }
    }

    private fun battleBoxOpen() {
        val availableSizes = enemies.map { it.askBoxSize() }
        var sizeX = 60f
        var sizeZ = 60f
        if (availableSizes.isNotEmpty()) {
            val rand = availableSizes.toSet().random()
            sizeX = rand.first
            sizeZ = rand.second
        }
        battleCanvas.openBattleBox(0f, 20f, sizeX, sizeZ)
        val scale = 1 / 16.0 / 8.0
        val playerWidth = 0.3
        val soulWidth = 9.0 * scale
        val shulkScaleZ = ceil(sizeX * scale)
        val shulkScaleX = ceil(sizeZ * scale)
        val loc1 =
            battleCenterLocation + Vector3d(0.0, 0.0, sizeZ * scale + shulkScaleZ * 0.5 + playerWidth - soulWidth)
        newHitboxEntity(loc1, shulkScaleZ)
        val loc2 =
            battleCenterLocation - Vector3d(0.0, 0.0, sizeZ * scale + shulkScaleZ * 0.5 + playerWidth - soulWidth)
        newHitboxEntity(loc2, shulkScaleZ)
        val loc3 =
            battleCenterLocation + Vector3d(sizeX * scale + shulkScaleX * 0.5 + playerWidth - soulWidth, 0.0, 0.0)
        newHitboxEntity(loc3, shulkScaleX)
        val loc4 =
            battleCenterLocation - Vector3d(sizeX * scale + shulkScaleX * 0.5 + playerWidth - soulWidth, 0.0, 0.0)
        newHitboxEntity(loc4, shulkScaleX)
    }

    private fun battleBoxClose() {
        for (id in shulkerHitboxes) {
            PacketManager.removeEntity(id, players.mapNotNull { it.player })
        }
        shulkerHitboxes.clear()
        battleCanvas.closeBattleBox()
    }
}