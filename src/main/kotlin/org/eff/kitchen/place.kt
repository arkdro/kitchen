package org.eff.kitchen.place


import org.eff.kitchen.Cleaner
import org.eff.kitchen.coordinates.Coord
import org.eff.kitchen.direction.Direction // for debug only
import org.eff.kitchen.field.Field
import org.eff.kitchen.mouse.Food_mouse
import org.eff.kitchen.mouse.Ground_mouse
import org.eff.kitchen.mouse.Mouse

class Place(val width: Int, val height: Int, val h_gap: Int, val v_gap: Int, level: Int) {
    var field = Field(width, height, h_gap, v_gap)
    val cleaner = Cleaner()
    val food_mice = create_food_mice(width, height, h_gap, v_gap, level)
    var ground_mice = create_ground_mice(width, height, h_gap, v_gap)
    val ground_mouse_limit = 3

    fun run() {
        while (true) {
            one_iteration()
        }
    }

    fun one_iteration() {
        display()
        update_cleaner()
        update_mice()
        Thread.sleep(500)
    }

    // for debug only
    fun set_new_direction(d: Direction) {
        val mouse = food_mice[0]
        mouse.direction = d
    }

    fun set_cleaner_new_direction(d: Direction) {
        cleaner.start_moving(d)
    }

    private fun display() {
        val food_mouse_coordinates = build_mouse_coordinates(food_mice)
        val ground_mouse_coordinates = build_mouse_coordinates(ground_mice)
        for (y in 0 until height) {
            for (x in 0 until width) {
                val display_point: Char
                if (food_mouse_coordinates.contains(Coord(x, y))) {
                    val mouse = food_mouse_coordinates[Coord(x, y)]
                    display_point = mouse!!.to_char()
                } else if (ground_mouse_coordinates.contains(Coord(x, y))) {
                    val mouse = ground_mouse_coordinates[Coord(x, y)]
                    display_point = mouse!!.to_char()
                } else if (cleaner.coord == Coord(x, y)) {
                    display_point = cleaner.to_char()
                } else {
                    display_point = field.get_point(Coord(x, y)).to_char()
                }
                print(display_point)
            }
            println()
        }
        println()
        println()
    }

    private fun update_mice() {
        food_mice.forEach { it.move(field, cleaner) }
        ground_mice.forEach { it.move(field, cleaner) }
    }

    private fun update_cleaner() {
        cleaner.move(field, food_mice)
    }

    fun add_ground_mouse(): Ground_mouse {
        val mouse = Ground_mouse(width, height, h_gap, v_gap)
        ground_mice.add(mouse)
        return mouse
    }

    fun remove_ground_mice() {
        ground_mice = mutableListOf<Ground_mouse>()
    }
}

fun create_food_mice(width: Int, height: Int, h_gap: Int, v_gap: Int, level: Int): MutableList<Food_mouse> {
    val num = level
    val list = mutableListOf<Food_mouse>()
    for (x in 1..num) {
        val initial_corner = Coord(h_gap, v_gap)
        val finish_corner = Coord(width - h_gap - 1, height - v_gap - 1)
        val mouse = Food_mouse(initial_corner, finish_corner)
        list.add(mouse)
    }
    return list
}

fun build_mouse_coordinates(mice: List<Mouse>): Map<Coord, Mouse> {
    val coords = mice.map { Pair(it.coord, it) }
    return coords.toMap()
}

fun create_ground_mice(width: Int, height: Int, h_gap: Int, v_gap: Int): MutableList<Ground_mouse> {
    val mice = mutableListOf<Ground_mouse>()
    val mouse = Ground_mouse(width, height, h_gap, v_gap)
    mice.add(mouse)
    return mice
}
