package org.eff.kitchen.g_field

import com.uchuhimo.konf.Config
import org.eff.kitchen.config.Srv
import org.eff.kitchen.coordinates.Coord
import org.eff.kitchen.field.Field
import org.eff.kitchen.food.Food
import org.eff.kitchen.place.Place
import org.frice.obj.FObject
import org.frice.obj.sub.ShapeObject
import org.frice.resource.graphics.ColorResource
import org.frice.util.shape.FRectangle

class G_field(config: Config, place: Place) {
    var ground: MutableMap<Coord, FObject> = mutableMapOf()
    var food: MutableMap<Coord, FObject> = mutableMapOf()

    init {
        create_field_objects(config, place)
    }

    private fun create_field_objects(config: Config, place: Place) {
        for (y in 0 until config[Srv.vertical_cells]) {
            for (x in 0 until config[Srv.horizontal_cells]) {
                val ground_obj = create_one_ground_field_object(config, x, y)
                val coord = Coord(x, y)
                ground[coord] = ground_obj
                if (is_food(place.field, coord)) {
                    val obj = create_one_food_field_object(config, x, y)
                    food[coord] = obj
                }
            }
        }
    }

}

private fun is_food(field: Field, coord: Coord): Boolean =
        field.get_point(coord) == Food.FULL

fun create_one_ground_field_object(config: Config, x: Int, y: Int): FObject {
    val color = ColorResource.DARK_GRAY
    return create_one_field_object_with_color(config, x, y, color)
}

fun create_one_food_field_object(config: Config, x: Int, y: Int): FObject {
    val color = ColorResource.LIGHT_GRAY
    return create_one_field_object_with_color(config, x, y, color)
}

fun create_one_field_object_with_color(config: Config, x: Int, y: Int, color: ColorResource): FObject {
    val x_pos = x * config[Srv.step]
    val y_pos = y * config[Srv.step]
    val g_field = ShapeObject(color,
            FRectangle(config[Srv.step], config[Srv.step]),
            x_pos.toDouble(), y_pos.toDouble())
    return g_field
}
