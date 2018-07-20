package org.eff.kitchen

import com.uchuhimo.konf.Config
import mu.KotlinLogging
import org.eff.kitchen.config.Srv
import org.eff.kitchen.config.build_config
import org.eff.kitchen.coordinates.Coord
import org.eff.kitchen.direction.Direction
import org.eff.kitchen.field.Field
import org.eff.kitchen.food.Food
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
    private lateinit var g_mice: Map<Food_mouse, FObject>

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
                logger.debug { "pressed: $e, ${e.keyChar}, ${e.keyCode}" }
                key_pressed = e.keyCode
            }

            override fun keyReleased(e: KeyEvent) = Unit
        })
        g_field = ShapeObject(ColorResource.CYAN,
                FRectangle(config[Srv.width], config[Srv.height]))
        addObject(g_field)
        g_mice = create_mouse_objects(place.food_mice)
        g_mice.forEach { _, mouse -> addObject(mouse) }
    }

    override fun onExit() {
        System.exit(0)
    }

    override fun onRefresh() {
        super.onRefresh()
        if (is_allowed_key(key_pressed)) {
            logger.debug { "pressed, onRefresh: $key_pressed" }
            place.set_new_direction(key_event_to_direction(key_pressed))
        }
        place.one_iteration()
        redraw_mice()
    }

    private fun redraw_mice() {
        for (mouse in place.food_mice) {
            val delta = mouse.coord - mouse.old_coordinates
            val g_mouse = g_mice[mouse]!!
            g_mouse.move(delta.x * config[Srv.step].toDouble(),
                    delta.y * config[Srv.step].toDouble())
        }
    }

}

private fun create_field_objects(place: Place): ArrayList<ArrayList<FObject>> {
    val field = mutableListOf<ArrayList<FObject>>()
    for (y in 0 until config[Srv.vertical_cells]) {
        val row = mutableListOf<FObject>()
        for (x in 0 until config[Srv.horizontal_cells]) {
            val obj = create_one_field_object(
                    place.field,
                    x, y)
            row.add(obj)
        }
        val row2 = ArrayList(row)
        field.add(row2)
    }
    val field2 = ArrayList(field)
    return field2
}

private fun create_one_field_object(field: Field, x: Int, y: Int): FObject {
    val color = get_field_point_color(field, x, y)
    val x_pos = x * config[Srv.step]
    val y_pos = y * config[Srv.step]
    val g_field = ShapeObject(color,
            FRectangle(config[Srv.step], config[Srv.step]),
            x_pos.toDouble(), y_pos.toDouble())
    return g_field
}

private fun get_field_point_color(field: Field, x: Int, y: Int): ColorResource {
    if (field.get_point(Coord(x, y)) == Food.FULL) {
        logger.debug { "get color, light, $x, $y" }
        return ColorResource.LIGHT_GRAY
    } else {
        logger.debug { "get color, dark, $x, $y" }
        return ColorResource.DARK_GRAY
    }
}

private fun create_mouse_objects(mice: List<Food_mouse>): Map<Food_mouse, FObject> {
    val result = mutableMapOf<Food_mouse, FObject>()
    for (mouse in mice) {
        val g_mouse = create_one_mouse_object(mouse.coord)
        result[mouse] = g_mouse
    }
    return result
}

private fun create_one_mouse_object(coord: Coord): FObject {
    val g_step = config[Srv.step]
    val g_width = config[Srv.cell_size] * config[Srv.scale]
    val g_height = config[Srv.cell_size] * config[Srv.scale]
    val g_mouse = ShapeObject(ColorResource.BLACK,
            FRectangle(g_width, g_height),
            coord.x.toDouble() * g_step,
            coord.y.toDouble() * g_step)
    return g_mouse
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
