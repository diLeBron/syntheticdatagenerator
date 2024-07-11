package streams.kafka

import java.util
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecords, KafkaConsumer}

import java.util.Properties
import scala.collection.JavaConverters._
import scala.language.postfixOps

object Consumer {

  def apply(bootstrapServers: String, topic: String): List[String] = {
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
        if (!records.isEmpty) flag = false
      }
    }

    consumer.close()
    records.asScala.map(r => r.value()).toList
  }
}
