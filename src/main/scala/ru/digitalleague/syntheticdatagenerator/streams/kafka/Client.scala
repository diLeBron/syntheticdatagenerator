package ru.digitalleague.syntheticdatagenerator.streams.kafka

import org.apache.kafka.clients.admin.{AdminClient, AdminClientConfig, NewTopic}

import java.util.Properties
import scala.collection.JavaConverters._
import scala.language.postfixOps

object Client {
  def createTopic(brokers: String, topic: String, partitions: Int = 1, replication: Int = 1): Unit = {
    val props = new Properties()
    props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, brokers)

    val adminClient = AdminClient.create(props)
    adminClient.createTopics(Seq(new NewTopic(topic, partitions, replication.toShort)).asJava).all().get()
    adminClient.close()
  }
}
