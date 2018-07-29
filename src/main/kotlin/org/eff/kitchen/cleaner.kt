package org.eff.kitchen

import mu.KotlinLogging
import org.eff.kitchen.coordinates.Coord
import org.eff.kitchen.direction.Direction
import org.eff.kitchen.direction.to_deltas
import org.eff.kitchen.field.Field
import org.eff.kitchen.food.Food
import org.eff.kitchen.mouse.Food_mouse

private val logger = KotlinLogging.logger {}

class Cleaner {
    var coord = Coord(0, 0)
    var old_coord = coord
    var direction = Direction.N
    var speed = 0
    var marked_line = mutableSetOf<Coord>()
    var just_finished_line = false
    var cleaned_points = mutableSetOf<Coord>()

    fun move(field: Field, food_mice: List<Food_mouse>) {
        old_coord = coord
        just_finished_line = false
        cleaned_points = mutableSetOf()
        if (speed == 0) {
            return
        }
        when (next_step_result(coord, direction, field, food_mice)) {
            Next_step_result.FOOD -> go_over_food(field)
            Next_step_result.GROUND -> go_over_ground(field)
            Next_step_result.MOUSE -> run_into_mouse(field)
            Next_step_result.STEP -> run_into_step(field)
            Next_step_result.WALL -> run_into_wall()
        }
    }

    private fun go_over_food(field: Field) {
        put_step_at_current_food(field)
        move_farther()
    }

    private fun put_step_at_current_food(field: Field) {
        val current_point = field.get_point(coord)
        if (current_point == Food.FULL) {
            field.set_point(coord, Food.STEP)
            marked_line.add(coord)
        }
    }

    private fun move_farther() {
        coord += direction.to_deltas()
    }

    private fun go_over_ground(field: Field) {
        put_step_at_current_food(field)
        if (field.get_point(coord) == Food.STEP) {
            move_farther()
            freeze()
            finish_marked_line(field)
        } else {
            move_farther()
        }
    }

    private fun run_into_step(field: Field) {
        step_exploded(field)
    }

    private fun run_into_mouse(field: Field) {
        step_exploded(field)
    }

    private fun step_exploded(field: Field) {
        pay_fine()
        clean_marked_line(field)
        set_initial_coordinates()
        freeze()
    }

    private fun run_into_wall() {
        freeze()
    }

    private fun set_initial_coordinates() {
        coord = Coord(0, 0)
    }

    private fun clean_marked_line(field: Field) {
        for (c in marked_line) {
            field.set_point(c, Food.FULL)
        }
        marked_line = mutableSetOf<Coord>()
    }

    private fun finish_marked_line(field: Field) {
        // not implemented
        for (point in marked_line) {
            field.set_point(point, Food.EMPTY)
        }
        cleaned_points = marked_line.toMutableSet()
        marked_line = mutableSetOf()
        logger.debug("finish line, points: $cleaned_points, line: $marked_line")
        just_finished_line = true
    }

    fun pay_fine() {
        // not implemented
    }

    fun freeze() {
        speed = 0
    }

    fun to_char(): Char = '@'

    fun start_moving(d: Direction) {
        direction = d
        speed = 1
        logger.debug("start moving cleaner, d: $direction, spd: $speed")
    }

    fun was_bitten(field: Field) {
        run_into_mouse(field)
    }
}

private fun next_step_result(coord: Coord, direction: Direction, field: Field, food_mice: List<Food_mouse>): Next_step_result {
    val next_coordinates = coord + direction.to_deltas()
    if (hit_wall(next_coordinates, field)) {
        return Next_step_result.WALL
    } else if (hit_mouse(next_coordinates, food_mice)) {
        return Next_step_result.MOUSE
    }
    val next_point = field.get_point(next_coordinates)
    return when (next_point) {
        Food.FULL -> Next_step_result.FOOD
        Food.STEP -> Next_step_result.STEP
        Food.EMPTY -> Next_step_result.GROUND
    }
}

private fun hit_wall(coordinates: Coord, field: Field): Boolean {
    return !field.is_inside(coordinates)
}

private fun hit_mouse(coordinates: Coord, food_mice: List<Food_mouse>): Boolean {
    return food_mice.any { coordinates == it.coord }
}
