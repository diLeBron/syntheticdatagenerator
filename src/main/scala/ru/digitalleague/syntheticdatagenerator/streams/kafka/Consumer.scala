package ru.digitalleague.syntheticdatagenerator.streams.kafka

import org.apache.kafka.clients.admin.{AdminClient, AdminClientConfig, NewTopic}

import java.util
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecords, KafkaConsumer}

import java.util.Properties
import scala.collection.JavaConverters._
import scala.language.postfixOps

object Consumer {

  def apply(bootstrapServers: String, topic: String, countWaitMsg: Int = 1): List[String] = {
    val props = new Properties()
    props.put("bootstrap.servers", bootstrapServers)

    props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props.put(ConsumerConfig.GROUP_ID_CONFIG, java.util.UUID.randomUUID.toString)
    props.put(ConsumerConfig.CLIENT_ID_CONFIG, "your_client_id")
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    val consumer = new KafkaConsumer[String, String](props)

    consumer.subscribe(util.Collections.singletonList(topic))

    var flag = true
    var records: ConsumerRecords[String, String] = consumer.poll(100)

    if (records.isEmpty) {
      while (flag) {
        records = consumer.poll(1000)
        if (records.count() == countWaitMsg) flag = false
      }
    }

    consumer.close()
    records.asScala.map(r => r.value()).toList
  }
}

object KafkaConsumerApp extends App {
  val brokers = "localhost:9092"
  val props = new Properties()
  props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, brokers)

//  val adminClient = AdminClient.create(props)
  val topicName = "test_output"
//  val topic = new NewTopic(topicName, 1, 1.toShort)
//  adminClient.createTopics(Seq(topic).asJava).all().get()
//  adminClient.close()

  props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
  props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
  props.put("group.id", "something")

  val consumer = new KafkaConsumer[String, String](props)

  consumer.subscribe(util.Collections.singletonList(topicName))

  while (true) {
    val records = consumer.poll(100)
    for (record <- records.asScala) {
      println(record)
    }
  }
}
