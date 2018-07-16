package org.eff.kitchen

import mu.KotlinLogging
import org.eff.kitchen.config.build_config
import org.eff.kitchen.direction.Direction
import org.eff.kitchen.place.Place
import org.frice.Game
import org.frice.launch
import org.frice.obj.FObject
import org.frice.obj.sub.ShapeObject
import org.frice.resource.graphics.ColorResource
import org.frice.util.shape.FRectangle
import java.awt.event.KeyEvent
import java.awt.event.KeyListener

private val logger = KotlinLogging.logger {}

class Kitchen : Game() {
    private lateinit var g_field: FObject
    init {
        g_field = ShapeObject(ColorResource.LIGHT_GRAY,
                FRectangle(190, 100),
                100.0, 75.0)
        logger.info("kitchen started")
        logger.info("cfg: ${build_config()}")
    }
    private lateinit var g_mouse: FObject
    private val place = Place()
    private var key_pressed = KeyEvent.VK_SPACE // stop
    override fun onInit() {
        super.onInit()
        val window_width = 600
        val window_height = 400
        setSize(window_width, window_height)
        autoGC = false
        isResizable = true
        title = "The kitchen"
        addKeyListener(object : KeyListener {
            override fun keyTyped(e: KeyEvent) = Unit
            override fun keyPressed(e: KeyEvent) {
                logger.debug {"pressed: $e, ${e.keyChar}, ${e.keyCode}"}
                key_pressed = e.keyCode
            }
            override fun keyReleased(e: KeyEvent) = Unit
        })
    }

    override fun onExit() {
        System.exit(0)
    }

    override fun onRefresh() {
        super.onRefresh()
        if (is_allowed_key(key_pressed)) {
            logger.debug {"pressed, onRefresh: $key_pressed"}
            place.set_new_direction(key_event_to_direction(key_pressed))
            if(key_pressed == KeyEvent.VK_NUMPAD1) {
                addObject(g_field)
            } else {
                removeObject(g_field)
            }
        }
        place.one_iteration()
    }

}

private fun is_allowed_key(key: Int): Boolean {
    return allowed_keys.contains(key)
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

// for debug only
private fun key_event_to_direction(d: Int): Direction =
        when (d) {
            KeyEvent.VK_UP -> Direction.N
            KeyEvent.VK_DOWN -> Direction.S
            KeyEvent.VK_LEFT -> Direction.W
            KeyEvent.VK_RIGHT -> Direction.E
            KeyEvent.VK_NUMPAD1 -> Direction.SW
            KeyEvent.VK_NUMPAD3 -> Direction.SE
            KeyEvent.VK_NUMPAD7 -> Direction.NW
            KeyEvent.VK_NUMPAD9 -> Direction.NE
            else -> Direction.N
        }

fun main(args: Array<String>) = launch(Kitchen::class.java)
