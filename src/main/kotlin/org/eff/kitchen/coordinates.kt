package org.eff.kitchen.coordinates

data class Coord(var x: Int, var y: Int) {
    operator fun plus(delta: Coord): Coord {
        return Coord(x + delta.x, y + delta.y)
    }
    operator fun minus(delta: Coord): Coord {
        return Coord(x - delta.x, y - delta.y)
    }
}
