package dev.zorsh.zorshDeltarune.battle

import com.comphenix.protocol.PacketType
import dev.zorsh.zorshDeltarune.ZorshDeltarune
import dev.zorsh.zorshDeltarune.nms.PacketManager
import dev.zorsh.zorshDeltarune.utils.*
import kotlinx.coroutines.*
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import org.bukkit.util.Transformation
import org.joml.AxisAngle4f
import org.joml.Quaternionf
import org.joml.Vector3d
import org.joml.Vector3f

class DefaultBattle(players: List<DeltarunePlayer>, enemies: List<DeltaruneEnemy>) : DeltaruneBattle(players, enemies) {

    private var loopTask: BukkitTask? = null

    private var battleJob: Job? = null

    override fun destroyBattle() {
        if (loopTask?.isCancelled == false) {
            loopTask?.cancel()
        }
        if (battleJob?.isCancelled == false) {
            battleJob?.cancel()
        }
    }

    override fun startBattle() {
        for (pl in players) {
            pl.lockInBattle(battleCenterLocation)
            pl.player.playSound(pl.player, "battle", 1f, 1f)
        }

        prepareSprites()

        battleJob = scope.launch {
            val jobs = mutableListOf<Job>()
            for (enemy in enemies) {
                jobs += launch {
                    enemy.attack()
                }
            }
            jobs.joinAll()
            delay(200L)
            end()
        }

        loopTask = object : BukkitRunnable() {
            override fun run() {
                if (players.all { !it.locked }) {
                    if (battleJob?.isCancelled == false) {
                        battleJob?.cancel()
                    }
                    cancel()
                }
            }
        }.runTaskTimer(ZorshDeltarune.instance, 1L, 1L)
    }

    private fun prepareSprites() {
        val loc = battleBoxCenterLocation
        loc.yaw = 180f
        //- spawn text_display[brightness=<map[block=15;sky=15]>;text=<&color[#ff770a]>⬛;background_color=<color[#ffffff].with_alpha[0]>;scale=140,100,1;translation=-2,-10,-0.001;force_no_persist=true] <[pos].forward[5].face[<[pos]>]> save:main_bg
        newTextDisplay(
            loc,
            coloredText("⬛", "#ff770a"),
            data = FakeDisplayData(Transformation(
                Vector3f(-2f, -10f, -0.001f),
                Quaternionf(0f, 0f, 0f, 1f),
                Vector3f(140f, 100f, 1f),
                Quaternionf(0f, 0f, 0f, 1f)
            ))
        )

        //- spawn text_display[brightness=<map[block=15;sky=15]>;text=<&color[#000000]>⬛;background_color=<color[#ffffff].with_alpha[0]>;left_rotation=<location[0,0,1].to_axis_angle_quaternion[3.1415]>;scale=180,100,1;translation=-2,2.49,-0.0009;force_no_persist=true] <[pos].forward[5].face[<[pos]>]> save:main_bg
        newTextDisplay(
            loc,
            coloredText("⬛", "#000000"),
            data = FakeDisplayData(Transformation(
                Vector3f(-2f, 2.49f, -0.0009f),
                AxisAngle4f(3.1415f, 0f, 0f, 1f),
                Vector3f(180f, 100f, 1f),
                AxisAngle4f()
            ))
        )

        //- spawn text_display[brightness=<map[block=15;sky=15]>;text=<&color[#000000]>⬛;background_color=<color[#ffffff].with_alpha[0]>;left_rotation=<location[0,0,1].to_axis_angle_quaternion[3.1415]>;scale=180,100,1;translation=-2,1.49,0.01;force_no_persist=true] <[pos].forward[5].face[<[pos]>]> save:main_bg
        newTextDisplay(
            loc,
            coloredText("⬛", "#000000"),
            data = FakeDisplayData(Transformation(
                Vector3f(-2f, 1.49f, 0.01f),
                AxisAngle4f(3.1415f, 0f, 0f, 1f),
                Vector3f(180f, 100f, 1f),
                AxisAngle4f()
            ))
        )

        //- spawn text_display[brightness=<map[block=15;sky=15]>;text=<&color[#2e1e25]>-;background_color=<color[#ffffff].with_alpha[0]>;scale=160,2.2,1;translation=-2,-1.5225,0;force_no_persist=true] <[pos].forward[5].face[<[pos]>].forward[0.00095]> save:bg
        newTextDisplay(
            loc - Vector3d(0.0, 0.0, 0.00095),
            coloredText("-", "#2e1e25"),
            data = FakeDisplayData(Transformation(
                Vector3f(-2f, -1.5225f, 0f),
                AxisAngle4f(),
                Vector3f(200f, 2.2f, 1f),
                AxisAngle4f()
            ))
        )

        //- spawn text_display[brightness=<map[block=15;sky=15]>;text=<&color[#2e1e25]>-;background_color=<color[#ffffff].with_alpha[0]>;scale=160,2.2,1;translation=-2,-2.525,0.001;force_no_persist=true] <[pos].forward[5].face[<[pos]>].forward[0.00095]> save:bg
        newTextDisplay(
            loc - Vector3d(0.0, 0.0, 0.00095),
            coloredText("-", "#2e1e25"),
            data = FakeDisplayData(Transformation(
                Vector3f(-2f, -2.525f, 0.001f),
                AxisAngle4f(),
                Vector3f(200f, 2.2f, 1f),
                AxisAngle4f()
            ))
        )

        for (dPlayer in players) {
            val mcPlayer = dPlayer.player

            //- spawn item_display[teleport_duration=<[tp_dur]>;brightness=<map[block=15;sky=15]>;item=<[pl].skull_item>;right_rotation=<[rr]>;scale=1,1,0.005;translation=<[left_offset].add[0.65]>,-1.5,0.003;force_no_persist=true] <[pos].forward[5].face[<[pos]>].forward[0.001]> save:bg
            val item = ItemStack.of(Material.PLAYER_HEAD)
            val meta = item.itemMeta as SkullMeta
            meta.setOwningPlayer(mcPlayer)
            item.setItemMeta(meta)
            val rightRotation = Quaternionf(AxisAngle4f(0.5f, 1f, 0f, 0f)) *
                    Quaternionf(AxisAngle4f(225f / 180f * 3.1415f, 0f, 1f, 0f))
            val name = mcPlayer.name
            val width = name.length * 0.125f * 0.9f
            val leftOffset = -width - 1.95f
            newItemDisplay(
                loc - Vector3d(0.0, 0.0, 0.00095),
                item,
                playerToShow = listOf(mcPlayer),
                data = FakeDisplayData(
                    Transformation(
                        Vector3f(leftOffset + 0.65f, -1.5f, 0.003f),
                        Quaternionf(AxisAngle4f()),
                        Vector3f(1f, 1f, 0.005f),
                        rightRotation
                    )
                )
            )

            //        - spawn text_display[teleport_duration=<[tp_dur]>;brightness=<map[block=15;sky=15]>;text=<&color[#e3e3e3]><[n].font[space:smooth2]>;background_color=<color[#ffffff].with_alpha[0]>;scale=1.8,2.5,1;translation=<[n].font[space:smooth2].text_width.mul[0.025].mul[0.9].add[<[left_offset].add[1.15]>]>,-2.15,0.00015;force_no_persist=true] <[pos].forward[5].face[<[pos]>].forward[0.001]> save:bg
            newTextDisplay(
                loc - Vector3d(0.0, 0.0, 0.00095),
                fontText(name, "#e3e3e3", "space:smooth2"),
                data = FakeDisplayData(Transformation(
                    Vector3f(width + leftOffset + 1.15f, -2.15f, 0.00015f),
                    AxisAngle4f(),
                    Vector3f(1.8f, 2.5f, 1f),
                    AxisAngle4f()
                ))
            )

            //        - spawn text_display[teleport_duration=<[tp_dur]>;brightness=<map[block=15;sky=15]>;text=<&color[#e3e3e3]>HP;background_color=<color[#ffffff].with_alpha[0]>;scale=1,1.1,1;translation=<[n].font[space:smooth2].text_width.mul[0.025].mul[1.8].add[<[left_offset].add[1.55]>]>,-2.1,0.00015;force_no_persist=true] <[pos].forward[5].face[<[pos]>].forward[0.001]> save:bg
            newTextDisplay(
                loc - Vector3d(0.0, 0.0, 0.00095),
                coloredText("HP", "#e3e3e3"),
                data = FakeDisplayData(Transformation(
                    Vector3f(width * 2f + leftOffset + 1.55f, -2.1f, 0.00015f),
                    AxisAngle4f(),
                    Vector3f(1f, 1.1f, 1f),
                    AxisAngle4f()
                ))
            )

            //        - spawn text_display[teleport_duration=<[tp_dur]>;brightness=<map[block=15;sky=15]>;text=<element[<&color[#fc8403]><element[f].font[space:dbuttons]> <element[a].font[space:dbuttons]> <element[i].font[space:dbuttons]> <element[m].font[space:dbuttons]> <element[d].font[space:dbuttons]>]>;background_color=<color[#ffffff].with_alpha[0]>;scale=1,1,1;translation=<[n].font[space:smooth2].text_width.mul[0.025].mul[0.9].add[<[left_offset].add[1.9]>]>,-3.02,0.00021;force_no_persist=true] <[pos].forward[5].face[<[pos]>].forward[0.001]> save:bg
            val buttons =
                fontText("f", "#fe0001", "space:dbuttons").append(
                    Component.text(" ")
                ).append(
                    fontText("a", "#fe0001", "space:dbuttons").append(
                        Component.text(" ")
                    ).append(
                        fontText("i", "#fe0001", "space:dbuttons").append(
                            Component.text(" ")
                        ).append(
                            fontText("m", "#fe0001", "space:dbuttons").append(
                                Component.text(" ")
                            ).append(
                                fontText("d", "#fe0001", "space:dbuttons")
                            )
                        )
                    )
                )
            newTextDisplay(
                loc - Vector3d(0.0, 0.0, 0.00095),
                buttons,
                data = FakeDisplayData(Transformation(
                    Vector3f(width + leftOffset + 1.9f, -3.02f, 0.10021f),
                    AxisAngle4f(),
                    Vector3f(1f, 1f, 1f),
                    AxisAngle4f()
                ))
            )
        }
    }
}