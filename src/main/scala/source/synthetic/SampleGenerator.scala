package source.synthetic

import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit.{DAYS, SECONDS}
import java.time.{LocalDate, LocalDateTime, ZoneOffset}
import scala.util.Random
import ujson.Value

object SampleGenerator {
  private val random = new Random(System.nanoTime)
  private val timestampFmt = "yyyy-MM-dd HH:mm:ss"

  def between(from: Int, to: Int): Int = {
    from + Random.nextInt(to - from + 1)
  }

  def int_generator(
      count: Int,
      lower: Int,
      upper: Int,
      start: Int,
      repeat: Int = 1,
      step: Int = 1
  ): List[Int] = {
    if (start != 0) {
      List
        .range(start, start + (((count / repeat) + 1) * step), step)
        .flatMap(x => List.fill(repeat)(x))
        .take(count)
    } else List.fill(count)(between(lower, upper))
  }

  def between(from: LocalDate, to: LocalDate): LocalDate = {
    from.plusDays(random.nextInt(DAYS.between(from, to).toInt))
  }

  def betweenDT(from: LocalDateTime, to: LocalDateTime): LocalDateTime = {
    from.plusSeconds(random.nextInt(SECONDS.between(from, to).toInt))
  }

  def between(from: LocalDateTime, to: LocalDateTime): Long = {
    toTimestampSeconds(betweenDT(from, to))
  }

  def betweenStrFormat(
      from: LocalDateTime,
      to: LocalDateTime,
      fmt: String = timestampFmt
  ): String = {
    toDateTimeFormat(betweenDT(from, to), fmt)
  }

  def toTimestampSeconds(dt: LocalDateTime): Long = {
    dt.atZone(ZoneOffset.UTC).toInstant().toEpochMilli() / 1000
  }

  def datetime(from: LocalDateTime, step_by_minute: Int, count: Int): List[Long] = {
    List.range(0, count).map(x => toTimestampSeconds(from.plusMinutes(x * step_by_minute)))
  }

  def datetimeStrFormat(
      from: LocalDateTime,
      step_by_minute: Int,
      count: Int,
      fmt: String = timestampFmt
  ): List[String] = {
    List.range(0, count).map(x => toDateTimeFormat(from.plusMinutes(x * step_by_minute), fmt))
  }

  def toDateTimeFormat(dt: LocalDateTime, fmt: String): String = {
    val formatter = DateTimeFormatter.ofPattern(fmt)
    dt.format(formatter)
  }

  def random(count: Int, lenght: Int = 4): List[String] =
    List.fill(count)(Random.alphanumeric.filter(_.isLetter).take(lenght).mkString)

  def random[A](count: Int, values: List[A]): List[A] =
    List.fill(count)(Random.shuffle(values).head)

  def random_bool(count: Int): List[Boolean] = List.fill(count)(Random.nextBoolean)

  def generate(
      dataType: String,
      action: String,
      rule: Map[String, Value],
      count: Int
  ): List[Any] = {
    val dtype = dataType.split('(').head
    if (action.toLowerCase == "insert") {
      val data = rule("values").arr.toList
      if (List("string", "varchar", "timestamp").contains(dtype)) {
        val a = data.map(x => x.toString().replace("\"", "'"))
        a
      } else data
    } else {
      (dtype, rule) match {
        case ("int", r) if r.isEmpty => List.fill(count)(between(0, 2147483646))
        case ("int", r) if r.contains("acceptable_values") =>
          random(count, r("acceptable_values").arr.toList.map(x => x.num.toInt))
        case ("int", _) =>
          int_generator(
            count,
            rule.getOrElse("lower_threshold", 0).toString.replaceAll("\"", "").toInt,
            rule.getOrElse("upper_threshold", 2147483646).toString.replaceAll("\"", "").toInt,
            rule.getOrElse("initial_value", 0).toString.replaceAll("\"", "").toInt,
            rule.getOrElse("repeat_number", 1).toString.replaceAll("\"", "").toInt,
            rule.getOrElse("step", 1).toString.replaceAll("\"", "").toInt
          )
        case ("string", r) if r.isEmpty => random(count).map(x => s"'${x}'")
        case ("string", r) if r.contains("acceptable_values") =>
          random(
            count,
            rule("acceptable_values").arr.toList.map(x =>
              x.toString()
                .replaceAll("\"", "")
                .replaceAll("\\[", "")
                .replaceAll("]", "")
            )
          ).map(x => s"'${x}'")
        case ("string", _) => random(count).map(x => s"'${x}'")
        case ("varchar", r) if r.isEmpty =>
          random(count, dataType.split('(').last.split(')').head.toInt).map(x => s"'${x}'")
        case ("varchar", r) if r.contains("acceptable_values") =>
          random(
            count,
            rule("acceptable_values").arr.toList.map(x =>
              x.toString()
                .replaceAll("\"", "")
                .replaceAll("\\[", "")
                .replaceAll("]", "")
            )
          )
            .map(x => s"'${x}'")
        case ("varchar", _) => random(count).map(x => s"'${x}'")
        case ("boolean", r) if r.contains("acceptable_values") =>
          random(count, r("acceptable_values").arr.toList.map(x => x.bool))
        case ("boolean", _) => random_bool(count)
        case ("timestamp", r) if r.isEmpty =>
          List
            .fill(count)(betweenStrFormat(LocalDateTime.now().minusYears(1), LocalDateTime.now()))
            .map(x => s"'${x}'")
        case ("timestamp", r) if r.contains("acceptable_values") =>
          random(
            count,
            r("acceptable_values").arr.toList.map(x =>
              x.toString()
                .replaceAll("\"", "")
                .replaceAll("\\[", "")
                .replaceAll("]", "")
            )
          )
            .map(x => s"'${x}'")
        case ("timestamp", r) if r.contains("lower_threshold") =>
          List
            .fill(count)(
              betweenStrFormat(
                LocalDateTime.parse(rule("lower_threshold").toString().replaceAll("\"", "")),
                LocalDateTime.parse(rule("upper_threshold").toString().replaceAll("\"", ""))
              )
            )
            .map(x => s"'${x}'")
        case ("timestamp", _) =>
          datetimeStrFormat(
            LocalDateTime.parse(rule("start").toString().replaceAll("\"", "")),
            rule("step_by_minutes").num.toInt,
            count
          ).map(x => s"'${x}'")
        case (_, _) =>
          println(s"Unsupported data type = `${dataType}` and rule = `${rule}`")
          List.empty
      }
    }
  }

  def generator(
      rules: List[(String, String, Map[String, Value])],
      count: Int
  ): List[List[Any]] = {
    val a = rules.map(x => generate(x._1, x._2, x._3, count))
    a
  }

}
