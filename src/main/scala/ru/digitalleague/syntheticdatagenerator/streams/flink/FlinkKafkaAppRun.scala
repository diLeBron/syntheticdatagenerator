package ru.digitalleague.syntheticdatagenerator.streams.flink

import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.api.scala._
import org.apache.flink.connector.kafka.sink.{KafkaRecordSerializationSchema, KafkaSink}
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.core.execution.JobClient
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment

object FlinkKafkaAppRun {
  def apply(bootstrapServers: String, inputTopic: String, outputTopic: String, groupId: String = "flinkTest"): JobClient  = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    val kafkaConsumer = KafkaSource.builder()
      .setBootstrapServers(bootstrapServers)
      .setTopics(inputTopic)
      .setGroupId(groupId)
      .setStartingOffsets(OffsetsInitializer.earliest())
      .setValueOnlyDeserializer(new SimpleStringSchema())
      .build()

    val serializer = KafkaRecordSerializationSchema.builder()
      .setValueSerializationSchema(new SimpleStringSchema())
      .setTopic(outputTopic)
      .build()

    val kafkaProducer = KafkaSink.builder()
      .setBootstrapServers(bootstrapServers)
      .setRecordSerializer(serializer)
      .build()

    val stream = env.fromSource(kafkaConsumer, WatermarkStrategy.noWatermarks(), "Kafka Source")
    stream.sinkTo(kafkaProducer)
    env.executeAsync()
  }
}
