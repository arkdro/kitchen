package org.eff.kitchen.mouse

import mu.KotlinLogging
import org.eff.kitchen.coordinates.Coord
import org.eff.kitchen.direction.Direction
import org.eff.kitchen.direction.to_deltas
import org.eff.kitchen.field.Field
import org.eff.kitchen.food.Food
import org.eff.kitchen.move.Move

private val logger = KotlinLogging.logger {}

abstract class Mouse : Move {
    abstract var coord: Coord
    abstract var direction: Direction
    abstract var speed: Int
    abstract val allowed_food: Food
    abstract fun to_char(): Char
    lateinit var old_coordinates: Coord // lateinit or real init somehow?

    fun diagonal_move(field: Field) {
        val old_direction = direction
        old_coordinates = coord
        if (can_walk_farther(coord, direction, allowed_food, field)) {
            coord = calc_new_coordinates(coord, direction)
        } else {
            val new_direction = get_new_direction(coord, direction, allowed_food, field)
            if (can_walk_farther(coord, new_direction, allowed_food, field)) {
                coord = calc_new_coordinates(coord, new_direction)
                direction = new_direction
            } else {
                val backward_direction = flip_both_directions(direction)
                if (can_walk_farther(coord, backward_direction, allowed_food, field)) {
                    coord = calc_new_coordinates(coord, backward_direction)
                    direction = backward_direction
                } else {
                    freeze()
                }
            }
        }
        logger.debug {
            """
            food at coordinates: ${field.get_point(coord)}
            allowed_food: $allowed_food
            old coordinates: $old_coordinates
            new coordinates: $coord
            old direction: $old_direction
            new direction: $direction"""
        }
        if (wrong_background(field, coord, allowed_food)) {
            log_error(field, coord, direction, old_coordinates, old_direction, allowed_food)
        }
    }

    private fun freeze() {
        speed = 0
    }

}

private fun get_new_direction(coord: Coord, dir: Direction, allowed_food: Food, field: Field): Direction =
        when {
            is_corner(coord, dir, allowed_food, field) -> flip_both_directions(dir)
            is_vertical_wall(coord, dir, allowed_food, field) -> flip_horizontal_direction(dir)
            else -> flip_vertical_direction(dir)
        }

fun is_vertical_wall(coord: Coord, dir: Direction, allowed_food: Food, field: Field): Boolean {
    val deltas = dir.to_deltas()
    val coordinates_diagonal = coord + deltas
    val food_diagonal = field.get_point(coordinates_diagonal)
    val coordinates_x = coord + Coord(deltas.x, 0)
    val food_x = field.get_point(coordinates_x)
    return food_diagonal != allowed_food
            && food_x != allowed_food
}

fun is_corner(coord: Coord, dir: Direction, allowed_food: Food, field: Field): Boolean {
    return is_open_corner(coord, dir, allowed_food, field)
            || is_closed_corner(coord, dir, allowed_food, field)
}

fun is_open_corner(coord: Coord, dir: Direction, allowed_food: Food, field: Field): Boolean {
    val deltas = dir.to_deltas()
    val coordinates_diagonal = coord + deltas
    val food_diagonal = field.get_point(coordinates_diagonal)
    val coordinates_x = coord + Coord(deltas.x, 0)
    val food_x = field.get_point(coordinates_x)
    val coordinates_y = coord + Coord(0, deltas.y)
    val food_y = field.get_point(coordinates_y)
    return food_diagonal != allowed_food
            && food_x == allowed_food
            && food_y == allowed_food
}

fun is_closed_corner(coord: Coord, dir: Direction, allowed_food: Food, field: Field): Boolean {
    val deltas = dir.to_deltas()
    val coordinates_diagonal = coord + deltas
    val food_diagonal = field.get_point(coordinates_diagonal)
    val coordinates_x = coord + Coord(deltas.x, 0)
    val food_x = field.get_point(coordinates_x)
    val coordinates_y = coord + Coord(0, deltas.y)
    val food_y = field.get_point(coordinates_y)
    return food_diagonal != allowed_food
            && food_x != allowed_food
            && food_y != allowed_food
}

fun calc_new_coordinates(coord: Coord, direction: Direction): Coord {
    val deltas = direction.to_deltas()
    return Coord(coord.x + deltas.x, coord.y + deltas.y)
}

fun can_walk_farther(coord: Coord, direction: Direction, allowed_food: Food, field: Field): Boolean {
    val new_coord = calc_new_coordinates(coord, direction)
    val food = field.get_point(new_coord)
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

private fun wrong_background(field: Field, coord: Coord, allowed_food: Food): Boolean {
    return field.get_point(coord) != allowed_food
}

private fun log_error(field: Field, new_coord: Coord, new_direction: Direction,
                      old_coordinates: Coord, old_direction: Direction,
                      allowed_food: Food) {
    logger.error(
            "wrong background:\n"
                    + "field:\n$field\n"
                    + "food at coordinates: ${field.get_point(new_coord)}\n"
                    + "allowed_food: $allowed_food\n"
                    + "old coordinates: $old_coordinates\n"
                    + "new coordinates: $new_coord\n"
                    + "old direction: $old_direction\n"
                    + "new direction: $new_direction"
    )
}