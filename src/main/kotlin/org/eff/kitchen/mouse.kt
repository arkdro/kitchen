package org.eff.kitchen.mouse

import mu.KotlinLogging
import org.eff.kitchen.Cleaner
import org.eff.kitchen.coordinates.Coord
import org.eff.kitchen.direction.Diagonal_direction
import org.eff.kitchen.direction.Direction
import org.eff.kitchen.direction.to_deltas
import org.eff.kitchen.field.Field
import org.eff.kitchen.food.Food
import org.eff.kitchen.move.Move

private val logger = KotlinLogging.logger {}

abstract class Mouse : Move {
    var tick = 0
    abstract val tick_limit: Int
    private var updated = true
    abstract var coord: Coord
    var direction = create_random_direction()
    abstract var speed: Int
    abstract val allowed_food: Food
    abstract fun to_char(): Char
    lateinit var old_coordinates: Coord // lateinit or real init somehow?

    override fun move(field: Field, cleaner: Cleaner) {
        tick++
        if (tick >= tick_limit) {
            tick = 0
            updated = true
            diagonal_move(field, cleaner)
        } else {
            updated = false
        }
    }

    fun diagonal_move(field: Field, cleaner: Cleaner) {
        if (speed == 0) {
            return
        }
        val old_direction = direction
        old_coordinates = coord
        if (can_walk_farther(coord, direction, allowed_food, field, cleaner)) {
            bite_cleaner_if_possible(coord, direction, allowed_food, field, cleaner)
            coord = calc_new_coordinates(coord, direction)
        } else {
            val new_direction = get_new_direction(coord, direction, allowed_food, field)
            if (can_walk_farther(coord, new_direction, allowed_food, field, cleaner)) {
                bite_cleaner_if_possible(coord, new_direction, allowed_food, field, cleaner)
                coord = calc_new_coordinates(coord, new_direction)
                direction = new_direction
            } else {
                val backward_direction = flip_both_directions(direction)
                if (can_walk_farther(coord, backward_direction, allowed_food, field, cleaner)) {
                    bite_cleaner_if_possible(coord, backward_direction, allowed_food, field, cleaner)
                    coord = calc_new_coordinates(coord, backward_direction)
                    direction = backward_direction
                } else {
                    logger.debug { "freeze food mouse, coord: $coord, dir: $direction, old dir: $old_direction" }
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

    fun is_updated(): Boolean {
        return updated
    }

    private fun freeze() {
        speed = 0
    }

    fun create_random_direction(): Direction {
        val rnd = java.util.Random()
        val directions = Diagonal_direction().directions
        val idx = rnd.nextInt(directions.size)
        return directions[idx]
    }

}

private fun is_cleaner_or_steps(coord: Coord, direction: Direction, allowed_food: Food, field: Field, cleaner: Cleaner): Boolean {
    val new_coord = calc_new_coordinates(coord, direction)
    if (disallowed_food(new_coord, field, allowed_food)) {
        return false
    } else {
        return new_coord == cleaner.coord || cleaner.marked_line.contains(new_coord)
    }
}

private fun disallowed_food(coord: Coord, field: Field, allowed_food: Food): Boolean {
    if (!valid_coordinates(coord, field.width, field.height)) {
        return true
    }
    val point = field.get_point(coord)
    return point != allowed_food
}

private fun bite_cleaner_if_possible(coord: Coord, direction: Direction, allowed_food: Food, field: Field, cleaner: Cleaner) {
    if (is_cleaner_or_steps(coord, direction, allowed_food, field, cleaner)) {
        logger.debug("bitten, ${cleaner.coord}, ${cleaner.marked_line}, ${coord + direction.to_deltas()}")
        cleaner.was_bitten(field)
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
    val coordinates_x = coord + Coord(deltas.x, 0)
    val coordinates_y = coord + Coord(0, deltas.y)
    val valid_x = valid_coordinates(coordinates_x, field.width, field.height)
    val valid_y = valid_coordinates(coordinates_y, field.width, field.height)
    if (valid_x && !valid_y) {
        return false
    }
    if (!valid_x && valid_y) {
        return true
    }
    val coordinates_diagonal = coord + deltas
    val food_diagonal = field.get_point(coordinates_diagonal)
    val food_x = field.get_point(coordinates_x)
    return food_diagonal != allowed_food
            && food_x != allowed_food
}

fun is_corner(coord: Coord, dir: Direction, allowed_food: Food, field: Field): Boolean {
    val deltas = dir.to_deltas()
    val coordinates_x = coord + Coord(deltas.x, 0)
    val coordinates_y = coord + Coord(0, deltas.y)
    val valid_x = valid_coordinates(coordinates_x, field.width, field.height)
    val valid_y = valid_coordinates(coordinates_y, field.width, field.height)
    if (!valid_x && !valid_y) {
        return true
    }
    if (valid_x != valid_y) {
        return false
    }
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

fun can_walk_farther(coord: Coord, direction: Direction, allowed_food: Food, field: Field, cleaner: Cleaner): Boolean {
    if (is_cleaner_or_steps(coord, direction, allowed_food, field, cleaner)) {
        return true
    }
    val new_coord = calc_new_coordinates(coord, direction)
    if (!valid_coordinates(new_coord, field.width, field.height)) {
        return false
    }
    val food = field.get_point(new_coord)
    if (food == Food.STEP) {
        logger.error("food == step, ${coord}, ${cleaner.coord}, ${cleaner.marked_line}")
    }
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

private fun valid_coordinates(coord: Coord, width: Int, height: Int): Boolean {
    return coord.x >= 0 && coord.x < width &&
            coord.y >= 0 && coord.y < height
}
