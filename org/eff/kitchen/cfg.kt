package org.eff.kitchen.config

import com.uchuhimo.konf.Config
import com.uchuhimo.konf.ConfigSpec

fun build_config() {
    val config = Config { addSpec(Srv) }
            .withSourceFrom.hocon.resource("server.conf")
            .withSourceFrom.env()
            .withSourceFrom.systemProperties()
    val h = config[Srv.host]
    val p = config[Srv.port]
    println("host: $h")
    println("port: $p")
    println("conf: $config")
}

class Srv {
    companion object : ConfigSpec("srv") {
        val host by optional("0.0.0.0")
        val port by required<Int>()
    }
}
