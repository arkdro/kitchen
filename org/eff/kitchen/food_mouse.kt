package org.eff.kitchen.mouse

import org.eff.kitchen.direction.Diagonal_direction
import org.eff.kitchen.direction.Direction
import org.eff.kitchen.food.Food

class Food_mouse(fodder: Food, width: Int, height: Int) : Mouse() {
    override var x: Int = create_random_with_slack(width)
    override var y: Int = create_random_with_slack(height)
    override var direction: Direction = create_random_direction()
    override var speed = 1
}

fun create_random_with_slack(range: Int): Int {
    val rnd = java.util.Random()
    return 1 + rnd.nextInt(range - 2)
}

fun create_random_direction(): Direction {
    val rnd = java.util.Random()
    val directions = Diagonal_direction().directions
    val idx = rnd.nextInt(directions.size)
    return directions[idx]
}