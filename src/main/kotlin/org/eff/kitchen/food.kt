package org.eff.kitchen.food

enum class Food {
    FULL,
    STEP,
    EMPTY;

    fun to_char(): Char =
            when (this) {
                FULL -> '*'
                STEP -> '+'
                EMPTY -> '.'
            }

}