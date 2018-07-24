package org.eff.kitchen.mouse

import org.eff.kitchen.Cleaner
import org.eff.kitchen.coordinates.Coord
import org.eff.kitchen.direction.Diagonal_direction
import org.eff.kitchen.direction.Direction
import org.eff.kitchen.field.Field
import org.eff.kitchen.food.Food

class Food_mouse(initial_corner: Coord, finish_corner: Coord) : Mouse() {
    override var coord: Coord = create_random_coordinates(initial_corner, finish_corner)
    override var direction: Direction = create_random_direction()
    override var speed = 1
    override val allowed_food = Food.FULL
    override fun to_char(): Char = 'O'
    override fun move(field: Field, cleaner: Cleaner) {
        diagonal_move(field, cleaner)
    }
}

fun create_random_coordinates(initial_corner: Coord, finish_corner: Coord): Coord {
    val x: Int = create_random_with_slack(initial_corner.x, finish_corner.x)
    val y: Int = create_random_with_slack(initial_corner.y, finish_corner.y)
    return Coord(x, y)
}

fun create_random_with_slack(begin: Int, end: Int): Int {
    val len = end - begin + 1
    val rnd = java.util.Random()
    val slack = 1
    val random_val = rnd.nextInt(len - 2 * slack)
    return begin + random_val + slack
}

fun create_random_direction(): Direction {
    val rnd = java.util.Random()
    val directions = Diagonal_direction().directions
    val idx = rnd.nextInt(directions.size)
    return directions[idx]
}