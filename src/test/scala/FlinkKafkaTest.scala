import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer
import org.apache.flink.streaming.util.serialization.SimpleStringSchema
import java.util.Properties

class FlinkKafkaTest extends App {
  val env = StreamExecutionEnvironment.getExecutionEnvironment

  val properties = new Properties()
  properties.setProperty("bootstrap.servers", "localhost:9092")
  properties.setProperty("group.id", "flink-consumer-group")

  val kafkaConsumer = new FlinkKafkaConsumer[String](
    "input-topic",
    new SimpleStringSchema(),
    properties
  )

  val kafkaProducer = new FlinkKafkaProducer[String](
    "output-topic",              // Имя выходного топика
    new SimpleStringSchema(),    // Схема сериализации
    properties
  )

  val inputStream = env.addSource(kafkaConsumer)
  val processedStream = inputStream.map(value => s"Processed: $value")

  //processedStream.addSink(kafkaProducer)

  env.execute("Flink Kafka Example")
}
