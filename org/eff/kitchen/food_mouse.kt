package org.eff.kitchen.mouse

import org.eff.kitchen.food.Food

class Food_mouse(fodder: Food, width: Int, height: Int) : Mouse() {
    override var x: Int = create_random_with_slack(width)
    override var y: Int = create_random_with_slack(height)
    override var speed = 1
}

fun create_random_with_slack(range: Int): Int {
    val rnd = java.util.Random()
    return 1 + rnd.nextInt(range - 2)
}