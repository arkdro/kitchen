package org.eff.kitchen.mouse

import org.eff.kitchen.coordinates.Coord
import org.eff.kitchen.direction.Direction
import org.eff.kitchen.direction.to_deltas
import org.eff.kitchen.food.Food

abstract class Mouse() {
    abstract var coord: Coord
    abstract var direction: Direction
    abstract var speed: Int
    abstract val allowed_food: Food

    fun diagonal_move(area) {
        if (can_walk_farther(coord, direction, allowed_food, area)) {
            coord = calc_new_coordinates(coord, direction)
        } else {
            var new_direction = get_new_direction()
            var new_coordinates = calc_new_coordinates(coord, new_direction)
            if (can_walk_farther(new_coordinates, allowed_food, area)) {
                coord = calc_new_coordinates(coord, direction)
                direction = new_direction
            } else {
                new_direction = flip_both_directions(direction)
                new_coordinates = calc_new_coordinates(coord, new_direction)
                if (can_walk_farther(new_coordinates, allowed_food, area)) {
                    coord = calc_new_coordinates(coord, direction)
                    direction = new_direction
                } else {
                    freeze()
                }
            }
        }
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

fun calc_new_coordinates(coord: Coord, direction: Direction): Coord {
    val deltas = direction.to_deltas()
    return Coord(coord.x + deltas.x, coord.y + deltas.y)
}

fun can_walk_farther(coord: Coord, direction: Direction, allowed_food: Food, area): Boolean {
    val new_coord = calc_new_coordinates(coord, direction)
    val food = area[new_coord.y][new_coord.x]
    return food == allowed_food
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
