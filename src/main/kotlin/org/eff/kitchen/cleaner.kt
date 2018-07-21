package org.eff.kitchen

import org.eff.kitchen.coordinates.Coord
import org.eff.kitchen.direction.Direction
import org.eff.kitchen.direction.to_deltas
import org.eff.kitchen.field.Field
import org.eff.kitchen.food.Food
import org.eff.kitchen.move.Move

class Cleaner : Move {
    var coord = Coord(0, 0)
    var direction = Direction.N
    var speed = 0
    var marked_line = mutableSetOf<Coord>()

    override fun move(field: Field) {
        if (speed == 0) {
            return
        }
        when (next_step_result(coord, direction, field)) {
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
        field.set_point(coord, Food.STEP)
    }

    private fun move_farther() {
        coord += direction.to_deltas()
    }

    private fun go_over_ground(field: Field) {
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

    private fun pay_fine() {
        // not implemented
    }

    private fun run_into_wall() {
        freeze()
    }

    private fun freeze() {
        speed = 0
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
        clean_marked_line(field)
    }

    fun to_char(): Char = '@'
}

private fun next_step_result(coord: Coord, direction: Direction, field: Field): Next_step_result {
    return Next_step_result.FOOD
}
