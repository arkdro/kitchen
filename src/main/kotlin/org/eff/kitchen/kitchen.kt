package org.eff.kitchen

import com.uchuhimo.konf.Config
import mu.KotlinLogging
import org.eff.kitchen.config.Srv
import org.eff.kitchen.config.build_config
import org.eff.kitchen.direction.Direction
import org.eff.kitchen.mouse.Food_mouse
import org.eff.kitchen.place.Place
import org.eff.kitchen.place.build_food_mouse_coordinates
import org.frice.Game
import org.frice.launch
import org.frice.obj.FObject
import org.frice.obj.sub.ShapeObject
import org.frice.resource.graphics.ColorResource
import org.frice.util.shape.FRectangle
import java.awt.event.KeyEvent
import java.awt.event.KeyListener

private val logger = KotlinLogging.logger {}
private val config = build_config()

class Kitchen : Game() {
    private lateinit var g_field: FObject

    init {
        logger.info("kitchen started")
        logger.info("config: $config")
    }

    private lateinit var place: Place
    private var key_pressed = KeyEvent.VK_SPACE // stop
    override fun onInit() {
        super.onInit()
        place = Place(config[Srv.horizontal_cells],
                config[Srv.vertical_cells],
                config[Srv.horizontal_gap],
                config[Srv.vertical_gap])
        val window_width = config[Srv.width]
        val window_height = config[Srv.height]
        setSize(window_width, window_height)
        autoGC = false
        isResizable = true
        title = "The kitchen"
        addKeyListener(object : KeyListener {
            override fun keyTyped(e: KeyEvent) = Unit
            override fun keyPressed(e: KeyEvent) {
                logger.debug {"pressed: $e, ${e.keyChar}, ${e.keyCode}"}
                key_pressed = e.keyCode
            }
            override fun keyReleased(e: KeyEvent) = Unit
        })
        addObject(g_field)
    }

    override fun onExit() {
        System.exit(0)
    }

    override fun onRefresh() {
        super.onRefresh()
        if (is_allowed_key(key_pressed)) {
            logger.debug {"pressed, onRefresh: $key_pressed"}
            place.set_new_direction(key_event_to_direction(key_pressed))
        }
        place.one_iteration()
        redraw_mice()
    }

    private fun redraw_mice() {
        val g_step = config[Srv.step]
        val g_width = config[Srv.cell_size] * config[Srv.scale]
        val g_height = config[Srv.cell_size] * config[Srv.scale]
        for (mouse in place.food_mice) {
            val old_coordinates = mouse.old_coordinates
            var g_mouse_local = ShapeObject(ColorResource.CYAN,
                    FRectangle(g_width,
                            g_height),
                    old_coordinates.x.toDouble() * g_step,
                    old_coordinates.y.toDouble() * g_step)
            removeObject(g_mouse_local)
            val new_coordinates = mouse.coord
            g_mouse_local = ShapeObject(ColorResource.BLACK,
                    FRectangle(g_width,
                            g_height),
                    new_coordinates.x.toDouble() * g_step,
                    new_coordinates.y.toDouble() * g_step)
            addObject(g_mouse_local)
        }
    }

}

private fun create_mouse_objects(mice: List<Food_mouse>): List<FObject> {
    val coordinates = build_food_mouse_coordinates(mice)
    val g_step = config[Srv.step]
    val g_width = config[Srv.cell_size] * config[Srv.scale]
    val g_height = config[Srv.cell_size] * config[Srv.scale]
    val result = mutableListOf<FObject>()
    for ((coord, _) in coordinates) {
        val g_mouse = ShapeObject(ColorResource.BLACK,
                FRectangle(g_width, g_height),
                coord.x.toDouble() * g_step,
                coord.y.toDouble() * g_step)
        result.add(g_mouse)
    }
    return result
}

private fun is_allowed_key(key: Int): Boolean {
    return allowed_keys.contains(key)
}

private val allowed_keys =
        setOf(
                KeyEvent.VK_UP,
                KeyEvent.VK_DOWN,
                KeyEvent.VK_LEFT,
                KeyEvent.VK_RIGHT,
                // following num pad keys are for debug only
                KeyEvent.VK_NUMPAD1,
                KeyEvent.VK_NUMPAD3,
                KeyEvent.VK_NUMPAD7,
                KeyEvent.VK_NUMPAD9
        )

// for debug only
private fun key_event_to_direction(d: Int): Direction =
        when (d) {
            KeyEvent.VK_UP -> Direction.N
            KeyEvent.VK_DOWN -> Direction.S
            KeyEvent.VK_LEFT -> Direction.W
            KeyEvent.VK_RIGHT -> Direction.E
            KeyEvent.VK_NUMPAD1 -> Direction.SW
            KeyEvent.VK_NUMPAD3 -> Direction.SE
            KeyEvent.VK_NUMPAD7 -> Direction.NW
            KeyEvent.VK_NUMPAD9 -> Direction.NE
            else -> Direction.N
        }

fun main(args: Array<String>) = launch(Kitchen::class.java)
