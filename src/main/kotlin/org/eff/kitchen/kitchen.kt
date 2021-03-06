package org.eff.kitchen

import mu.KotlinLogging
import org.eff.kitchen.config.Srv
import org.eff.kitchen.config.build_config
import org.eff.kitchen.coordinates.Coord
import org.eff.kitchen.direction.Direction
import org.eff.kitchen.fill.fill
import org.eff.kitchen.food.Food
import org.eff.kitchen.g_field.G_field
import org.eff.kitchen.mouse.Food_mouse
import org.eff.kitchen.mouse.Ground_mouse
import org.eff.kitchen.place.Place
import org.frice.Game
import org.frice.launch
import org.frice.obj.FObject
import org.frice.obj.button.SimpleText
import org.frice.obj.sub.ShapeObject
import org.frice.resource.graphics.ColorResource
import org.frice.util.shape.FRectangle
import org.frice.util.time.FTimer
import java.awt.event.KeyEvent
import java.awt.event.KeyListener

private const val ground_mouse_time = 60000
private val logger = KotlinLogging.logger {}
private val config = build_config()

class Kitchen : Game() {
    private lateinit var g_field: G_field
    private lateinit var g_food_mice: Map<Food_mouse, FObject>
    private lateinit var g_ground_mice: MutableMap<Ground_mouse, FObject>
    private lateinit var g_cleaner: FObject
    private lateinit var g_cleaner_steps: MutableMap<Coord, FObject>
    private lateinit var g_total_score: SimpleText
    private lateinit var g_level: SimpleText
    private lateinit var g_shots: SimpleText
    private var score = 0
    private var total_score = 0
    private var score_at_level_begin = 0
    private var level = 1
    private var level_cleaned = false
    private var ground_mouse_timer = FTimer(ground_mouse_time)
    private var showing_end = false
    private lateinit var end_timer: FTimer
    private lateinit var g_end_text: SimpleText

    init {
        logger.info("kitchen started")
        logger.info("config: $config")
    }

    private lateinit var place: Place
    private var key_pressed = KeyEvent.VK_SPACE // stop
    override fun onInit() {
        super.onInit()
        level = 1 // necessary, otherwise it becomes 0
        place = build_place()
        val window_width = config[Srv.width] + config[Srv.add_window_horizontal]
        val window_height = config[Srv.height] + config[Srv.add_window_vertical] +
                config[Srv.step] * 3
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
        add_all_objects(place)
    }

    override fun onExit() {
        System.exit(0)
    }

    override fun onRefresh() {
        super.onRefresh()
        if (showing_end && end_timer.ended()) {
            logger.debug("end timer ended")
            removeObject(g_end_text)
            init_all_for_start()
            showing_end = false
            return
        } else if (showing_end) {
            return
        }
        if (is_allowed_key(key_pressed)) {
            logger.debug { "pressed, onRefresh: $key_pressed" }
            val dir = key_event_to_direction(key_pressed)
            if (is_key_for_cleaner(key_pressed)) {
                place.set_cleaner_new_direction(dir)
            } else {
                place.set_new_direction(dir)
            }
            key_pressed = KeyEvent.VK_UNDO
        } else {
            if (key_pressed != KeyEvent.VK_UNDO) {
                place.cleaner.freeze()
            }
        }
        if (ground_mouse_timer.ended()) {
            val need_redraw_cleaner = add_ground_mouse()
            if (need_redraw_cleaner) {
                redraw_cleaner_and_steps()
            }
        }
        place.one_iteration(level)
        redraw_field_if_needed()
        redraw_cleaner_and_steps()
        redraw_mice()
        if (place.cleaner.shots <= 0) {
            remove_all_objects()
            display_end_message()
            showing_end = true
            end_timer = FTimer(3000)
            return
        }
        if (level_cleaned) {
            remove_all_objects()
            level++
            score_at_level_begin = total_score
            place = build_place()
            add_all_objects(place)
            level_cleaned = false
            init_ground_mouse_timer()
        }
    }

    private fun init_all_for_start() {
        level = 1
        score_at_level_begin = 0
        total_score = 0
        place = build_place()
        add_all_objects(place)
        level_cleaned = false
        init_ground_mouse_timer()
    }

    private fun init_ground_mouse_timer() {
        ground_mouse_timer = FTimer(ground_mouse_time)
    }

    private fun build_place(): Place {
        return Place(config[Srv.horizontal_cells],
                config[Srv.vertical_cells],
                config[Srv.horizontal_gap],
                config[Srv.vertical_gap],
                level)
    }

    private fun redraw_field_if_needed() {
        if (place.cleaner.just_finished_line) {
            val points_to_ground = fill(place.field, place.food_mice.toSet())
            update_cleaned_field(points_to_ground)
            update_field(points_to_ground)
            update_total_score_text()
            init_ground_mouse_timer()
            if (most_of_level_cleaned()) {
                level_cleaned = true
            }
        }
    }

    private fun most_of_level_cleaned(): Boolean {
        var total_food = (place.width - place.h_gap * 2) * (place.height - place.v_gap * 2)
        var not_cleaned = 0
        for (y in 0 until place.height) {
            for (x in 0 until place.width) {
                val point = place.field.get_point(Coord(x, y))
                if (point == Food.FULL) {
                    not_cleaned++
                }
            }
        }
        return not_cleaned < total_food * 0.33
    }

    private fun update_cleaned_field(points_to_ground: Set<Coord>) {
        // remove food objects, so only ground objects
        // are shown at these coordinates
        for (coord in place.cleaner.cleaned_points) {
            g_field.food[coord]?.also { removeObject(it) }
        }
        for (coord in points_to_ground) {
            g_field.food[coord]?.also { removeObject(it) }
        }
    }

    private fun update_field(points_to_ground: Set<Coord>) {
        for (coord in points_to_ground) {
            place.field.set_point(coord, Food.EMPTY)
        }
    }

    private fun update_total_score_text() {
        score = place.field.get_current_food_count()
        total_score = score_at_level_begin + score
        val text = "Score: $total_score"
        g_total_score.text = text
    }

    private fun update_level_text() {
        val text = "Level: $level"
        g_level.text = text
    }

    private fun update_shots_text() {
        val text = "Shots: ${place.cleaner.shots}"
        g_shots.text = text
    }

    private fun redraw_cleaner_and_steps() {
        redraw_cleaner()
        update_cleaner_step_objects()
        redraw_cleaner_shots()
    }

    private fun redraw_cleaner() {
        if (!place.cleaner.is_updated()) {
            return
        }
        if (place.cleaner.is_at_start()) {
            removeObject(g_cleaner)
            g_cleaner = create_cleaner_object(place.cleaner)
            addObject(g_cleaner)
        } else {
            val delta = place.cleaner.coord - place.cleaner.old_coord
            g_cleaner.move(delta.x * config[Srv.step].toDouble(),
                    delta.y * config[Srv.step].toDouble())
        }
    }

    private fun update_cleaner_step_objects() {
        val steps = place.cleaner.marked_line.toSet()
        for (step in steps) {
            if (!g_cleaner_steps.containsKey(step)) {
                val obj = create_one_cleaner_step_object(step)
                g_cleaner_steps[step] = obj
                addObject(obj)
            }
        }
        val deleted = mutableSetOf<Coord>()
        for ((g_step, obj) in g_cleaner_steps) {
            if (!steps.contains(g_step)) {
                removeObject(obj)
                deleted.add(g_step)
            }
        }
        deleted.forEach { g_cleaner_steps.remove(it) }
    }

    private fun create_one_cleaner_step_object(coord: Coord): FObject {
        val g_step = config[Srv.step]
        val g_width = config[Srv.cell_size] * config[Srv.scale] / 4
        val g_height = config[Srv.cell_size] * config[Srv.scale] / 4
        val offset = config[Srv.cell_size] * config[Srv.scale] / 4 * 1.5
        val obj = ShapeObject(ColorResource.WHITE,
                FRectangle(g_width, g_height),
                coord.x.toDouble() * g_step + offset,
                coord.y.toDouble() * g_step + offset)
        return obj
    }

    private fun create_cleaner_step_objects(): MutableMap<Coord, FObject> {
        return mutableMapOf<Coord, FObject>()
    }

    private fun redraw_cleaner_shots() {
        if (place.cleaner.is_at_start()) {
            update_shots_text()
        }
    }

    private fun redraw_mice() {
        for (mouse in place.food_mice) {
            if (!mouse.is_updated()) {
                continue
            }
            val delta = mouse.coord - mouse.old_coordinates
            val g_mouse = g_food_mice[mouse]!!
            g_mouse.move(delta.x * config[Srv.step].toDouble(),
                    delta.y * config[Srv.step].toDouble())
        }
        for (mouse in place.ground_mice) {
            if (!mouse.is_updated()) {
                continue
            }
            val delta = mouse.coord - mouse.old_coordinates
            val g_mouse = g_ground_mice[mouse]!!
            g_mouse.move(delta.x * config[Srv.step].toDouble(),
                    delta.y * config[Srv.step].toDouble())
        }
    }

    private fun add_total_score() {
        val text = ""
        val y = config[Srv.height] + config[Srv.add_window_vertical] +
                config[Srv.step]
        val x = 2 * config[Srv.step]
        g_total_score = SimpleText(ColorResource.MAGENTA, text, x.toDouble(), y.toDouble())
        g_total_score.textSize = config[Srv.step].toDouble() * 1.25
        update_total_score_text()
        addObject(g_total_score)
    }

    private fun remove_total_score() {
        removeObject(g_total_score)
    }

    private fun add_level_object() {
        val text = ""
        val y = config[Srv.height] + config[Srv.add_window_vertical] +
                config[Srv.step]
        val x = 12 * config[Srv.step]
        g_level = SimpleText(ColorResource.MAGENTA, text, x.toDouble(), y.toDouble())
        g_level.textSize = config[Srv.step].toDouble() * 1.25
        update_level_text()
        addObject(g_level)
    }

    private fun remove_level_object() {
        removeObject(g_level)
    }

    private fun add_shots_object() {
        val text = ""
        val y = config[Srv.height] + config[Srv.add_window_vertical] +
                config[Srv.step]
        val x = 22 * config[Srv.step]
        g_shots = SimpleText(ColorResource.MAGENTA, text, x.toDouble(), y.toDouble())
        g_shots.textSize = config[Srv.step].toDouble() * 1.25
        update_shots_text()
        addObject(g_shots)
    }

    private fun remove_shots_object() {
        removeObject(g_shots)
    }

    private fun add_ground_mouse(): Boolean {
        var need_redraw_cleaner = false
        if (place.ground_mice.size >= place.ground_mouse_limit) {
            place.cleaner.collapsed_by_ground_mice(place.field)
            remove_ground_mice()
            need_redraw_cleaner = true
        }
        add_one_ground_mouse()
        return need_redraw_cleaner
    }

    private fun remove_ground_mice() {
        place.remove_ground_mice()
        remove_ground_mouse_objects()
    }

    private fun add_one_ground_mouse() {
        val mouse = place.add_ground_mouse()
        val g_mouse = create_one_mouse_object(mouse.coord)
        g_ground_mice[mouse] = g_mouse
        addObject(g_mouse)
    }

    private fun add_field_object_to_graphics() {
        // lower layer is ground
        for ((_, obj) in g_field.ground) {
            addObject(obj)
        }
        // upper layer is food
        for ((_, obj) in g_field.food) {
            addObject(obj)
        }
    }

    private fun remove_field_object_from_graphics() {
        for ((_, obj) in g_field.ground) {
            removeObject(obj)
        }
        for ((_, obj) in g_field.food) {
            removeObject(obj)
        }
    }

    private fun add_all_objects(place: Place) {
        add_total_score()
        add_level_object()
        add_shots_object()
        g_field = G_field(config, place)
        add_field_object_to_graphics()
        g_cleaner = create_cleaner_object(place.cleaner)
        addObject(g_cleaner)
        g_cleaner_steps = create_cleaner_step_objects()
        create_mouse_objects()
    }

    private fun remove_all_objects() {
        remove_field_object_from_graphics()
        removeObject(g_cleaner)
        remove_mouse_objects()
        remove_total_score()
        remove_level_object()
        remove_shots_object()
    }

    private fun remove_mouse_objects() {
        g_food_mice.forEach { _, mouse -> removeObject(mouse) }
        remove_ground_mouse_objects()
    }

    private fun remove_ground_mouse_objects() {
        g_ground_mice.forEach { _, mouse -> removeObject(mouse) }
    }

    private fun create_mouse_objects() {
        g_food_mice = create_food_mouse_objects(place.food_mice)
        g_food_mice.forEach { _, mouse -> addObject(mouse) }
        create_ground_mouse_objects()
    }

    private fun create_ground_mouse_objects() {
        g_ground_mice = create_ground_mouse_objects(place.ground_mice)
        g_ground_mice.forEach { _, mouse -> addObject(mouse) }
    }

    private fun display_end_message() {
        g_end_text = create_end_message_object()
        addObject(g_end_text)
    }

    private fun create_end_message_object(): SimpleText {
        val text = "GAME OVER"
        val y = config[Srv.height] / 2
        val x = config[Srv.width] / 4
        val obj = SimpleText(ColorResource.RED, text, x.toDouble(), y.toDouble())
        obj.textSize = config[Srv.step].toDouble() * 2
        obj.text = text
        return obj
    }

}

private fun create_cleaner_object(cleaner: Cleaner): FObject {
    val g_step = config[Srv.step]
    val g_width = config[Srv.cell_size] * config[Srv.scale]
    val g_height = config[Srv.cell_size] * config[Srv.scale]
    val coord = cleaner.coord
    val obj = ShapeObject(ColorResource.GREEN,
            FRectangle(g_width, g_height),
            coord.x.toDouble() * g_step,
            coord.y.toDouble() * g_step)
    return obj
}

private fun create_food_mouse_objects(mice: List<Food_mouse>): Map<Food_mouse, FObject> {
    val result = mutableMapOf<Food_mouse, FObject>()
    for (mouse in mice) {
        val g_mouse = create_one_mouse_object(mouse.coord)
        result[mouse] = g_mouse
    }
    return result
}

private fun create_ground_mouse_objects(mice: List<Ground_mouse>): MutableMap<Ground_mouse, FObject> {
    val result = mutableMapOf<Ground_mouse, FObject>()
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

private fun is_key_for_cleaner(key: Int): Boolean {
    return keys_for_cleaner.contains(key)
}

private val keys_for_cleaner =
        setOf(
                KeyEvent.VK_UP,
                KeyEvent.VK_DOWN,
                KeyEvent.VK_LEFT,
                KeyEvent.VK_RIGHT)

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
