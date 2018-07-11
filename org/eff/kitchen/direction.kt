package org.eff.kitchen.direction

import org.eff.kitchen.coordinates.Coord

abstract class Any_direction {
    abstract val directions: Array<Direction>
}

enum class Direction {
    NW, N, NE,
    W,     E,
    SW, S, SE
}

class Normal_direction : Any_direction() {
    override val directions = arrayOf(
            Direction.N,
            Direction.W,
            Direction.E,
            Direction.S)
}

class Diagonal_direction : Any_direction() {
    override val directions = arrayOf(
            Direction.NW,
            Direction.NE,
            Direction.SW,
            Direction.SE)
}

fun Direction.to_deltas(): Coord =
        when (this) {
            Direction.W -> Coord(-1, 0)
            Direction.E -> Coord(1, 0)
            Direction.NW -> Coord(-1, -1)
            Direction.NE -> Coord(1, -1)
            Direction.SW -> Coord(-1, 1)
            Direction.SE -> Coord(1, 1)
            Direction.N -> Coord(0, -1)
            Direction.S -> Coord(0, 1)
        }

