package streams.kafka

import java.util.Properties

import org.apache.kafka.clients.producer._

object Producer {

  def apply(bootstrapServers: String, topic: String, message: String): Unit = {
    val props = new Properties()
    props.put("bootstrap.servers", bootstrapServers)

    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

    val producer = new KafkaProducer[String, String](props)

    val record = new ProducerRecord(topic, "key", message)
    producer.send(record)

    producer.close()
  }

}
