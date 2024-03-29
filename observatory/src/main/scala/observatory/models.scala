package observatory

//import java.time.LocalDate

/**
 * Introduced in Week 1. Represents a location on the globe.
 *
 * @param lat Degrees of latitude, -90 ≤ lat ≤ 90
 * @param lon Degrees of longitude, -180 ≤ lon ≤ 180
 */
case class Location(lat: Double, lon: Double)

/**
 * Introduced in Week 3. Represents a tiled web map tile.
 * See https://en.wikipedia.org/wiki/Tiled_web_map
 * Based on http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames
 *
 * @param x    X coordinate of the tile
 * @param y    Y coordinate of the tile
 * @param zoom Zoom level, 0 ≤ zoom ≤ 19
 */
case class Tile(x: Int, y: Int, zoom: Int)

/**
 * Introduced in Week 4. Represents a point on a grid composed of
 * circles of latitudes and lines of longitude.
 *
 * @param lat Circle of latitude in degrees, -89 ≤ lat ≤ 90
 * @param lon Line of longitude in degrees, -180 ≤ lon ≤ 179
 */
case class GridLocation(lat: Int, lon: Int)

/**
 * Introduced in Week 5. Represents a point inside of a grid cell.
 *
 * @param x X coordinate inside the cell, 0 ≤ x ≤ 1
 * @param y Y coordinate inside the cell, 0 ≤ y ≤ 1
 */
case class CellPoint(x: Double, y: Double)

/**
 * Introduced in Week 2. Represents an RGB color.
 *
 * @param red   Level of red, 0 ≤ red ≤ 255
 * @param green Level of green, 0 ≤ green ≤ 255
 * @param blue  Level of blue, 0 ≤ blue ≤ 255
 */
case class Color(red: Int, green: Int, blue: Int)

case class StationID(stn: String, wban: String)

case class Station(stationId: StationID, latitude: Double, longitude: Double)

case class TemperatureRecord(stationId: StationID, month: Int, day: Int, fahrenheit: Double)
///**
// * Parser for temperature_colors.csv
// *
// * @param line
// */
//case class TemperatureColors(line: String) {
//  private val data = line.split(",", -1)
//
//  val temperature: Temperature = data(0).toDouble
//  val red: Int = data(1).toInt
//  val green: Int = data(2).toInt
//  val blue: Int = data(3).toInt
//}
//
//
///**
// * Parser for stations.csv
// *
// * @param line
// */
//case class Station(line: String) {
//  private val data = line.split(",", -1)
//
//  val stn_id: Int = if (data(0) == "") -1 else data(0).toInt
//  val wban_id: Int = if (data(1) == "") -1 else data(1).toInt
//  val lat: Option[Double] = if (data(2) == "") None else Some(data(2).toDouble)
//  val lon: Option[Double] = if (data(3) == "") None else Some(data(3).toDouble)
//}
//
///**
// * Parser for temperatures csv
// *
// * @param line
// * @param year
// */
//case class StationTemperature(line: String, year: Int) {
//  private val data = line.split(",", -1)
//
//  val stn_id: Int = if (data(0) == "") -1 else data(0).toInt
//  val wban_id: Int = if (data(1) == "") -1 else data(1).toInt
//  val date: LocalDate = LocalDate.parse(s"${year}-${data(2)}-${data(3)}")
//  val temperature: Temperature = (data(4).toDouble - 32) / 1.8
//}