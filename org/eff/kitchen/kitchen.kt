package org.eff.kitchen

import mu.KotlinLogging
import org.eff.kitchen.place.Place
import org.frice.Game
import org.frice.launch
import org.frice.platform.FriceDrawer
import java.awt.event.KeyEvent
import java.awt.event.KeyListener

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

    override fun onExit() {
        System.exit(0)
    }

    override fun onRefresh() {
        super.onRefresh()
        place.set_new_direction(key_event_to_direction(direction))
        place.one_iteration()
    }

}

// fun main(args: Array<String>) = launch(Kitchen::class.java)
fun main(args: Array<String>) {
    val place = Place()
    place.run()
}

private val allowed_keys =
        setOf(
                KeyEvent.VK_UP,
                KeyEvent.VK_DOWN,
                KeyEvent.VK_LEFT,
                KeyEvent.VK_RIGHT,
                // following num pad keys are for debug only
                KeyEvent.VK_NUMPAD1,
                KeyEvent.VK_NUMPAD3,
                KeyEvent.VK_NUMPAD7,
                KeyEvent.VK_NUMPAD9
        )

