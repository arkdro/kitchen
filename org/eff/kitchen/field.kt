package org.eff.kitchen.field

import org.eff.kitchen.coordinates.Coord
import org.eff.kitchen.food.Food

class Field(width: Int, height: Int, h_gap: Int, v_gap: Int) {
    private val field = fill_stock(width, height, h_gap, v_gap)

    fun get_point(coordinates: Coord): Food {
        return field[coordinates.y][coordinates.x]
    }
    fun set_point(coordinates: Coord, value: Food) {
        field[coordinates.y][coordinates.x] = value
    }
}

fun fill_stock(width: Int, height: Int, h_gap: Int, v_gap: Int): Array<Array<Food>> {
    val stock = Array(height, {Array(width, { Food.FULL })})
    for (y in 0..height) {
        for (x in 0..width) {
            stock[y][x] = food_at_coordinates(width, height, h_gap, v_gap, x, y)
        }
    }
    return stock
}

fun food_at_coordinates(width: Int, height: Int, h_gap: Int, v_gap: Int, x: Int, y: Int): Food {
    if (x < h_gap || x >= width - h_gap) {
        return Food.EMPTY
    }
    if (y < v_gap || y >= height - v_gap) {
        return Food.EMPTY
    }
    return Food.FULL
}

