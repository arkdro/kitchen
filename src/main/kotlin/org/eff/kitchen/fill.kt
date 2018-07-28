package org.eff.kitchen.fill

import org.eff.kitchen.coordinates.Coord
import org.eff.kitchen.field.Field
import org.eff.kitchen.food.Food
import org.eff.kitchen.mouse.Food_mouse
import java.util.*

fun fill(field: Field, mice: Set<Food_mouse>): Set<Coord> {
    val queue = prepare_queue(mice)
    var temp_field = prepare_temp_field(field)
    while (queue.size > 0) {
        val coord = queue.remove()
        val left = find_left_point(temp_field, coord)
        val right = find_right_point(temp_field, field.width, coord)
        for (x in left.x..right.x) {
            val c = Coord(x, coord.y)
            make_point_busy(temp_field, c)
            val upper_point = get_upper_coord(c)
            if (is_free(temp_field, upper_point, field.width, field.height)) {
                queue.add(upper_point)
            }
            val lower_point = get_lower_coord(c)
            if (is_free(temp_field, lower_point, field.width, field.height)) {
                queue.add(lower_point)
            }
        }
    }
    return get_free_points_of_field(temp_field, field)
}

enum class Status {
    FREE,
    BUSY
}

fun prepare_temp_field(field: Field): Array<Array<Status>> {
    val data = Array(field.height,
            {
                Array(field.width,
                        { Status.FREE })
            })
    for (y in 0 until field.height) {
        for (x in 0 until field.width) {
            val coord = Coord(x, y)
            val point = field.get_point(coord)
            if (point == Food.FULL) {
                data[y][x] = Status.FREE
            } else {
                data[y][x] = Status.BUSY
            }
        }
    }
    return data
}

fun prepare_queue(mice: Set<Food_mouse>): Queue<Coord> {
    val queue = ArrayDeque<Coord>()
    for (mouse in mice) {
        queue.add(mouse.coord)
    }
    return queue
}

fun find_left_point(data: Array<Array<Status>>, coord: Coord): Coord {
    var x = coord.x
    while (true) {
        var prev_x = x
        if (x < 0) {
            return Coord(0, coord.y)
        }
        if (data[coord.y][x] != Status.FREE) {
            return Coord(prev_x, coord.y)
        } else {
            x--
        }
    }
}

fun find_right_point(data: Array<Array<Status>>, width: Int, coord: Coord): Coord {
    var x = coord.x
    while (true) {
        val prev_x = x
        if (x > width - 1) {
            return Coord(width - 1, coord.y)
        }
        if (data[coord.y][x] != Status.FREE) {
            return Coord(prev_x, coord.y)
        } else {
            x++
        }
    }
}

fun make_point_busy(data: Array<Array<Status>>, coord: Coord) {
    data[coord.y][coord.x] = Status.BUSY
}

fun get_upper_coord(coord: Coord): Coord {
    return Coord(coord.x, coord.y - 1)
}

fun get_lower_coord(coord: Coord): Coord {
    return Coord(coord.x, coord.y + 1)
}

fun is_free(data: Array<Array<Status>>, coord: Coord, width: Int, height: Int): Boolean {
    if (coord.x < 0 || coord.x >= width || coord.y < 0 || coord.y >= height) {
        return false
    }
    val point = data[coord.y][coord.x]
    return point == Status.FREE
}

fun get_free_points_of_field(data: Array<Array<Status>>, field: Field): Set<Coord> {
    var points = mutableSetOf<Coord>()
    for (y in 0 until field.height) {
        for (x in 0 until field.width) {
            val point = data[y][x]
            if (point == Status.FREE) {
                val coord = Coord(x, y)
                points.add(coord)
            }
        }
    }
    return points
}