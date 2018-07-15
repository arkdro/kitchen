package org.eff.kitchen

import mu.KotlinLogging
import org.eff.kitchen.place.Place
import org.frice.Game
import org.frice.launch

private val logger = KotlinLogging.logger {}

class Kitchen : Game() {
    init {
        logger.info("kitchen started")
    }
}

// fun main(args: Array<String>) = launch(Kitchen::class.java)
fun main(args: Array<String>) {
    val place = Place()
    place.run()
}

