package org.eff.kitchen

import org.eff.kitchen.place.Place
import org.frice.Game
import org.frice.launch

class Kitchen : Game() {
}

// fun main(args: Array<String>) = launch(Kitchen::class.java)
fun main(args: Array<String>) {
    val place = Place()
    place.run()
}

