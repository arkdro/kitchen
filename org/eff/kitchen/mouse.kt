package org.eff.kitchen.mouse

import org.eff.kitchen.coordinates.Coord
import org.eff.kitchen.direction.Direction

abstract class Mouse() {
    abstract var coord: Coord
    abstract var direction: Direction
    abstract var speed: Int
}
