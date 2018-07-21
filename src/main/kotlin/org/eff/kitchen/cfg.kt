package org.eff.kitchen.config

import com.uchuhimo.konf.Config
import com.uchuhimo.konf.ConfigSpec

fun build_config(): Config {
    val config = Config { addSpec(Srv) }
            .withSourceFrom.hocon.resource("server.conf")
            .withSourceFrom.env()
            .withSourceFrom.systemProperties()
    return config
}

class Srv {
    companion object : ConfigSpec("srv") {
        val horizontal_cells by optional<Int>(10, "amount.horizontal")
        val vertical_cells by optional<Int>(10, "amount.vertical")
        val horizontal_gap by optional<Int>(2, "gap.horizontal")
        val vertical_gap by optional<Int>(2, "gap.vertical")
        val cell_size by optional<Int>(20, "cell.size")
        val scale by optional<Int>(1)
        val width by lazy { config ->
            config[horizontal_cells] * config[cell_size] * config[scale]
        }
        val height by lazy { config ->
            config[vertical_cells] * config[cell_size] * config[scale]
        }
        val step by lazy { config ->
            config[cell_size] * config[scale]
        }
        val add_window_horizontal by optional<Int>(0, "window.add.horizontal")
        val add_window_vertical by optional<Int>(0, "window.add.vertical")
    }
}
