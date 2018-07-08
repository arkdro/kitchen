package org.eff.kitchen.place


import org.eff.kitchen.food.Food

const val width = 30
const val height = 30
const val v_gap = 3
const val h_gap = 3

class Place {
    val catcher = Catcher()
    val food_mice = MutableList<Food_mouse>(0, {})
    val ground_mice = MutableList<Ground_mouse>(0, {})
    var stock = fill_stock()
    init {
        fill_stock()
        create_mice()
    }

fun fill_stock(): Array<Array<Food>> {
    val stock = Array(height, {Array(width, { Food.FULL })})
    for (y in 0..height) {
        for (x in 0..width) {
            stock[y][x] = food_at_coordinates(x, y)
        }
    }
}

fun Place.create_mice() {
    create_food_mice()
    create_ground_mice()
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