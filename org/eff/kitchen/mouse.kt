package org.eff.kitchen.mouse

import org.eff.kitchen.coordinates.Coord
import org.eff.kitchen.direction.Direction
import org.eff.kitchen.food.Food

abstract class Mouse() {
    abstract var coord: Coord
    abstract var direction: Direction
    abstract var speed: Int
    abstract val allowed_food: Food

    fun diagonal_move(area) {
        var new_direction: Direction
        if (can_walk(coord, direction, allowed_food, area)) {
            new_direction = direction
        } else {
            new_direction = get_new_direction(area)
            if (!can_walk(coord, new_direction, allowed_food, area)) {
                new_direction = flip_both_directions(direction)
            }
        }
        calc_new_coordinates(coord, new_direction)
    }

    fun get_new_direction(area): Direction =
            when {
                can_walk(coord, direction, allowed_food, area) -> direction
                is_corner(coord, direction, allowed_food, area) -> flip_both_directions(direction)
                is_vertical_wall(coord, direction, allowed_food, area) -> flip_horizontal_direction(direction)
                else -> flip_vertical_direction(direction)
            }
    private fun freeze() {
        speed = 0
    }

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

fun flip_both_directions(d: Direction): Direction =
        flip_horizontal_direction(flip_vertical_direction(d))
