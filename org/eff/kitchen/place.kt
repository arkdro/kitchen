package org.eff.kitchen.place


import org.eff.kitchen.coordinates.Coord
import org.eff.kitchen.food.Food
import org.eff.kitchen.mouse.Food_mouse

const val width = 30
const val height = 30
const val v_gap = 3
const val h_gap = 3

class Place {
    var stock = fill_stock()
    //val catcher = Catcher()
    val food_mice = create_food_mice()
    //val ground_mice = MutableList<Ground_mouse>(0, {})
}

fun fill_stock(): Array<Array<Food>> {
    val stock = Array(height, {Array(width, { Food.FULL })})
    for (y in 0..height) {
        for (x in 0..width) {
            stock[y][x] = food_at_coordinates(x, y)
        }
    }
    return stock
}

fun food_at_coordinates(x: Int, y: Int): Food {
    if (x < h_gap || x >= width - h_gap) {
        return Food.EMPTY
    }
    if (y < v_gap || y >= height - v_gap) {
        return Food.EMPTY
    }
    return Food.FULL
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