import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.api.scala._
import org.apache.flink.connector.kafka.sink.{KafkaRecordSerializationSchema, KafkaSink}
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.functions.source.SourceFunction
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment

object FlinkKafkaTest extends App {
  val env = StreamExecutionEnvironment.getExecutionEnvironment
  val source = new SourceFunction[String] {
    override def run(ctx: SourceFunction.SourceContext[String]): Unit = {
      ctx.collect("Hello world")
    }
    override def cancel(): Unit = {}
  }

  val kafkaConsumer = KafkaSource.builder()
    .setBootstrapServers("localhost:9092")
    .setTopics("test_gen2")
    .setGroupId("flink-consumer-group")
    .setStartingOffsets(OffsetsInitializer.earliest())
    .setValueOnlyDeserializer(new SimpleStringSchema())
    .build()

  val serializer = KafkaRecordSerializationSchema.builder()
    .setValueSerializationSchema(new SimpleStringSchema())
    .setTopic("test_gen2")
    .build()

  val kafkaProducer = KafkaSink.builder()
    .setBootstrapServers("localhost:9092")
    .setRecordSerializer(serializer)
    .build()

  val stream = env.fromSource(kafkaConsumer, WatermarkStrategy.noWatermarks(), "Kafka Source")
  stream.print()
  env.fromElements("Add producer Test").sinkTo(kafkaProducer)
  env.execute()
}
