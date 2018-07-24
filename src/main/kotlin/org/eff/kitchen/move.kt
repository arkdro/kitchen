package org.eff.kitchen.move

import org.eff.kitchen.Cleaner
import org.eff.kitchen.field.Field

interface Move {
    fun move(field: Field, cleaner: Cleaner)
}