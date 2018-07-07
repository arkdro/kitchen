package org.eff.kitchen.mouse

import org.eff.kitchen.direction.Direction

abstract class Mouse() {
    abstract var x: Int
    abstract var y: Int
    abstract var direction: Direction
    abstract var speed: Int
}
