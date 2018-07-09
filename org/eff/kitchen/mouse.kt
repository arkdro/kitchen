package org.eff.kitchen.mouse

import org.eff.kitchen.coordinates.Coord
import org.eff.kitchen.direction.Direction
import org.eff.kitchen.food.Food

abstract class Mouse() {
    abstract var coord: Coord
    abstract var direction: Direction
    abstract var speed: Int
    abstract val allowed_food: Food
}
fun flip_horizontal_direction(d: Direction): Direction =
        when (d) {
            Direction.W -> Direction.E
            Direction.E -> Direction.W
            Direction.NW -> Direction.NE
            Direction.NE -> Direction.NW
            Direction.SW -> Direction.SE
            Direction.SE -> Direction.SW
            Direction.N -> Direction.N
            Direction.S -> Direction.S
        }

fun flip_vertical_direction(d: Direction): Direction =
        when (d) {
            Direction.W -> Direction.W
            Direction.E -> Direction.E
            Direction.NW -> Direction.SW
            Direction.NE -> Direction.SE
            Direction.SW -> Direction.NW
            Direction.SE -> Direction.NE
            Direction.N -> Direction.S
            Direction.S -> Direction.N
        }

