package org.eff.kitchen.place


import org.eff.kitchen.coordinates.Coord
import org.eff.kitchen.field.Field
import org.eff.kitchen.mouse.Food_mouse

const val width = 30
const val height = 30
const val v_gap = 3
const val h_gap = 3

class Place {
    var field = Field(width, height, h_gap, v_gap)
    //val catcher = Catcher()
    val food_mice = create_food_mice()
    //val ground_mice = MutableList<Ground_mouse>(0, {})
    fun run() {
        display()
        update_mice()
    }

    private fun display() {

    }

    private fun update_mice() {
        food_mice.forEach { it.move(field) }
    }
}

fun create_food_mice(): MutableList<Food_mouse> {
    val num = 1
    val list = mutableListOf<Food_mouse>()
    for (x in 1..num) {
        val initial_corner = Coord(h_gap, v_gap)
        val finish_corner = Coord(width - h_gap - 1, height - v_gap - 1)
        val mouse = Food_mouse(initial_corner, finish_corner)
        list.add(mouse)
    }
    return list
}