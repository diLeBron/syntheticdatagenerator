package ru.digitalleague.syntheticdatagenerator.streams.flink

import org.apache.flink.api.common.JobExecutionResult
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.api.connector.source.lib.NumberSequenceSource
import org.apache.flink.api.scala._
import org.apache.flink.connector.kafka.sink.{KafkaRecordSerializationSchema, KafkaSink}
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment

object FlinkSinkToKafkaAppRun {
  def apply(bootstrapServers: String, data: List[String], outputTopic: String): JobExecutionResult = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    val source = new NumberSequenceSource(0, data.length - 1)

    val serializer = KafkaRecordSerializationSchema.builder()
      .setValueSerializationSchema(new SimpleStringSchema())
      .setTopic(outputTopic)
      .build()

    val kafkaProducer = KafkaSink.builder()
      .setBootstrapServers(bootstrapServers)
      .setRecordSerializer(serializer)
      .build()

    val stream = env
      .fromSource(source, WatermarkStrategy.noWatermarks(), "Kafka Source")
      .map(index => data(index.toInt))
    stream.sinkTo(kafkaProducer)
    env.execute()
  }
}
