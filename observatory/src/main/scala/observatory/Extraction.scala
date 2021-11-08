package observatory

import java.time.LocalDate


import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.rdd.RDD
import org.apache.spark.storage.StorageLevel
import org.apache.log4j.{Level, Logger}

import scala.io.Source


/**
 * 1st milestone: data extraction
 */
object Extraction {

  Logger.getLogger("org.apache.spark").setLevel(Level.WARN)
  val conf: SparkConf = new SparkConf().setMaster("local").setAppName("Capstone")
  val sc: SparkContext = new SparkContext(conf)

  /**
   * @param year             Year number
   * @param stationsFile     Path of the stations resource file to use (e.g. "/stations.csv")
   * @param temperaturesFile Path of the temperatures resource file to use (e.g. "/1975.csv")
   * @return A sequence containing triplets (date, location, temperature)
   */
  def locateTemperatures(year: Int, stationsFile: String, temperaturesFile: String): Iterable[(LocalDate, Location, Temperature)] = {


    def loadFile(path: String): Seq[String] = Source.fromInputStream(getClass.getResourceAsStream(path), "utf-8").getLines.toSeq

    val statRdd = stations(sc.parallelize(loadFile(stationsFile)))
    val tempRdd = temperatures(sc.parallelize(loadFile(temperaturesFile)))

    val resultsRdd =
      statRdd.join(tempRdd)
        .mapValues(value => {
          val station: Station = value._1
          val tempRecords: Iterable[TemperatureRecord] = value._2

          val location = Location(station.latitude, station.longitude)
          tempRecords.map(tempRecord =>
            (LocalDate.of(year, tempRecord.month, tempRecord.day), location, celsius(tempRecord.fahrenheit))
          )
        })
        .values

    if (resultsRdd.isEmpty())
      Iterable.empty[(LocalDate, Location, Double)]
    else
      resultsRdd.reduce((v1, v2) => v1 ++ v2)
  }

  /**
   * @param records A sequence containing triplets (date, location, temperature)
   * @return A sequence containing, for each location, the average temperature over the year.
   */
  def locationYearlyAverageRecords(records: Iterable[(LocalDate, Location, Temperature)]): Iterable[(Location, Temperature)] = {
    val recordsRdd = sc.parallelize(records.toSeq)

    recordsRdd
      .groupBy(_._2)
      .mapValues(records => records.foldRight(0.0)((rec, sum) => sum + rec._3) / records.size)
      .aggregate(Iterable.empty[(Location, Temperature)])(
        (acc, ld) => acc ++ Iterable.apply(ld),
        (a1, a2) => a1 ++ a2
      )
  }

  def celsius(fahrenheit: Double): Double = (fahrenheit - 32d) / 1.8

  def stations(rawStations: RDD[String]): RDD[(StationID, Station)] = {

    def invalidStation(record: Array[String]) =
      if (record.length != 4) true
      else (record(0).isEmpty && record(1).isEmpty) || record(2).isEmpty || record(3).isEmpty


    rawStations
      .map(line => line.split(","))
      .filter(stationRecord => !invalidStation(stationRecord))
      .map(s => Station(StationID(s(0), s(1)), s(2).toDouble, s(3).toDouble))
      .groupBy(_.stationId)
      .mapValues(_.head)
      .persist(StorageLevel.MEMORY_AND_DISK)
  }

  def temperatures(rawTemperatures: RDD[String]): RDD[(StationID, Iterable[TemperatureRecord])] = {

    def invalidTemperatureRecord(record: Array[String]) = record.length != 5 || record(4) == "9999.9"

    rawTemperatures
      .map(line => line.split(","))
      .filter(tempRecord => !invalidTemperatureRecord(tempRecord))
      .map(t => TemperatureRecord(StationID(t(0), t(1)), t(2).toInt, t(3).toInt, t(4).toDouble))
      .groupBy(_.stationId)
      .persist(StorageLevel.MEMORY_AND_DISK)
  }
}