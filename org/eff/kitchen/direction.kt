package org.eff.kitchen.direction

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
