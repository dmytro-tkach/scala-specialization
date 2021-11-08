package observatory

import com.sksamuel.scrimage.writer

import java.io.File


object Main extends App {
  val stationsPath: String = "/stations.csv"

  Interaction2.availableLayers.foreach(layer => {
    val layerName = layer.layerName
    println(s"Performing $layerName")
    val colors = layer.colorScale
    val layerNameID = layer.layerName.id
    val normalYears = 1975 to 1990


    def generateImage(year: Year, tile: Tile, grid: GridLocation => Temperature): Unit = {
      val image = Visualization2.visualizeGridNoGC(grid, colors, tile)
      val file = new File(s"target/$layerNameID/$year/${tile.zoom}/${tile.x}-${tile.y}.png")
      if (!file.getParentFile.exists) file.getParentFile.mkdirs
      image.output(file)
    }


    def temperatureAverages(year: Year): Iterable[(Location, Temperature)] = {
      val temperaturesFile: String = s"/$year.csv"
      val locations = Extraction.locateTemperatures(year, stationsPath, temperaturesFile)
      Extraction.locationYearlyAverageRecords(locations).seq
    }


    if (layerNameID == "Deviations") {
      val normalTemperatures: Map[Year, Iterable[(Location, Temperature)]] = (
        for {
          year <- normalYears
        } yield (year, temperatureAverages(year))).toMap

      val normalsGrid = Manipulation.getAverageGrid(normalTemperatures.values)

      for {year <- layer.bounds} {

        val temperatures = normalTemperatures.get(year) match {
          case Some(temperatures) => temperatures
          case None => temperatureAverages(year)
        }

        val yearlyData = Seq((year, Manipulation.getDeviationGrid(temperatures, normalsGrid)))
        Interaction.generateTiles(0 to 3)(yearlyData, generateImage)
      }
    }

    else {
      for {year <- layer.bounds} {

        val temperatures = temperatureAverages(year)
        val grid: GridLocation => Temperature = Manipulation.getGrid(temperatures)
        val yearlyData = Seq((year, grid))
        Interaction.generateTiles(0 to 3)(yearlyData, generateImage)
      }
    }
    }
  )

}