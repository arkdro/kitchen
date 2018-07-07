package org.eff.kitchen.mouse

import org.eff.kitchen.food.Food

class Food_mouse(fodder: Food, width: Int, height: Int) : Mouse() {
    override var x: Int = create_random(width)
    override var y: Int = create_random(height)
    override var speed = 1
}

fun create_random(range: Int): Int {
    val rnd = java.util.Random()
    return rnd.nextInt(range)
}