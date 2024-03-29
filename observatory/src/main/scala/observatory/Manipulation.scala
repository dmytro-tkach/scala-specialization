package observatory

import observatory.Visualization.predictTemperature

import scala.collection.concurrent.TrieMap
import scala.collection.mutable

/**
 * 4th milestone: value-added information
 */
object Manipulation extends ManipulationInterface {
  /**
   * @param temperatures Known temperatures
   * @return A function that, given a latitude in [-89, 90] and a longitude in [-180, 179],
   *         returns the predicted temperature at this location
   */
  def makeGrid(temperatures: Iterable[(Location, Temperature)]): GridLocation => Temperature = {
    System.gc()
    getGrid(temperatures)
  }

  def getGrid(temperatures: Iterable[(Location, Temperature)]): GridLocation => Temperature = {
    memorizeGrid {
      gridLoc: GridLocation =>
        predictTemperature(temperatures, Location(gridLoc.lat, gridLoc.lon))
    }
  }

  /**
   * @param temperaturess Sequence of known temperatures over the years (each element of the collection
   *                      is a collection of pairs of location and temperature)
   * @return A function that, given a latitude and a longitude, returns the average temperature at this location
   */
  def average(temperaturess: Iterable[Iterable[(Location, Temperature)]]): GridLocation => Temperature = {
    System.gc()
    getAverageGrid(temperaturess)
  }

  def getAverageGrid(temperaturess: Iterable[Iterable[(Location, Temperature)]]): GridLocation => Temperature = {
    val numberYears: Int = temperaturess.size

    memorizeGrid {
      gridLoc: GridLocation =>
        val doubleLocation = Location(gridLoc.lat, gridLoc.lon)
        temperaturess
          .map(predictTemperature(_, doubleLocation))
          .sum / numberYears
    }
  }

  /**
   * @param temperatures Known temperatures
   * @param normals      A grid containing the “normal” temperatures
   * @return A grid containing the deviations compared to the normal temperatures
   */
  def deviation(temperatures: Iterable[(Location, Temperature)], normals: GridLocation => Temperature): GridLocation => Temperature = {
    System.gc()
    getDeviationGrid(temperatures, normals)
  }

  def getDeviationGrid(temperatures: Iterable[(Location, Temperature)], normals: GridLocation => Temperature): GridLocation => Temperature = {
    memorizeGrid {
      gridLoc: GridLocation =>
        getGrid(temperatures)(gridLoc) - normals(gridLoc)
    }
  }

  /**
   * @param grid grid function
   * @return The same grid function byt memorized using a TrieMap
   */
  def memorizeGrid(grid: GridLocation => Temperature): GridLocation => Temperature = {
    val locationTemperature: mutable.Map[GridLocation, Temperature] = new TrieMap[GridLocation, Temperature]()

    gridLoc: GridLocation =>
      locationTemperature.get(gridLoc) match {
        case Some(temperature) => temperature
        case None =>
          val temperature: Temperature = grid(gridLoc)
          locationTemperature.put(gridLoc, temperature)
          temperature
      }
  }
}