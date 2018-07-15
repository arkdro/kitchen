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
    private val place = Place()
    private var direction = KeyEvent.VK_SPACE // stop
    override fun onInit() {
        super.onInit()
        val window_width = 300
        val window_height = 200
        setSize(window_width, window_height)
        autoGC = false
        isResizable = false
        title = "The kitchen"
        addKeyListener(object : KeyListener {
            override fun keyTyped(e: KeyEvent) = Unit
            override fun keyPressed(e: KeyEvent) {
                val code = e.keyCode
                if (is_allowed_key(code)) {
                    logger.debug {"pressed: $e, ${e.keyChar}, ${e.keyCode}"}
                    direction = code
                }
            }
            override fun keyReleased(e: KeyEvent) = Unit
        })

    }
}

// fun main(args: Array<String>) = launch(Kitchen::class.java)
fun main(args: Array<String>) {
    val place = Place()
    place.run()
}

