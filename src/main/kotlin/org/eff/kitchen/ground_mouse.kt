package org.eff.kitchen.mouse

import org.eff.kitchen.Cleaner
import org.eff.kitchen.coordinates.Coord
import org.eff.kitchen.field.Field
import org.eff.kitchen.food.Food

class Ground_mouse(width: Int, height: Int, h_gap: Int, v_gap: Int) : Mouse() {
    override val tick_limit = 10
    override var coord: Coord = create_random_coordinates(width, height, h_gap, v_gap)
    override var speed = 1
    override val allowed_food = Food.EMPTY
    override fun to_char(): Char = '#'

    init {
        old_coordinates = coord
    }

}

private fun create_random_coordinates(width: Int, height: Int, h_gap: Int, v_gap: Int): Coord {
    val x = create_random(width)
    var y = create_random(height)
    if (y > v_gap || y < height - v_gap) {
        y = 0
    }
    return Coord(x, y)
}

private fun create_random(size: Int): Int {
    val rnd = java.util.Random()
    return rnd.nextInt(size)
}
