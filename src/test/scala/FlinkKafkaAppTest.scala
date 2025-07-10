import com.dimafeng.testcontainers.DockerComposeContainer
import com.dimafeng.testcontainers.ExposedService
import com.dimafeng.testcontainers.scalatest.TestContainerForAll
import org.apache.kafka.clients.admin.{AdminClient, AdminClientConfig, NewTopic}
import org.scalatest.flatspec._
import org.testcontainers.containers.wait.strategy.Wait
import ru.digitalleague.syntheticdatagenerator.streams.flink.FlinkKafkaAppRun
import ru.digitalleague.syntheticdatagenerator.streams.kafka.{Consumer, Producer}

import java.io.File
import java.util.Properties
import scala.collection.JavaConverters.seqAsJavaListConverter

class FlinkKafkaAppTest extends AnyFlatSpec with TestContainerForAll {

  override val containerDef: DockerComposeContainer.Def =
    DockerComposeContainer.Def(
      new File("src/test/resources/docker-compose.yaml"),
      tailChildContainers = true,
      exposedServices = Seq(
        ExposedService("zookeeper", 2181, Wait.defaultWaitStrategy()),
        ExposedService("kafka", 9092, Wait.defaultWaitStrategy())
      )
    )

  "FlinkKafkaApp" should "be sink to Kafka topic" in {
    withContainers { composedContainers =>
      assert(composedContainers.getServicePort("kafka", 9092) > 0)

      val brokers = "localhost:9092"
      val inTopicName1 = "test_input1"
      val inTopicName2 = "test_input2"
      val outTopicName = "test_output"
      val benchmark = List("Hello, Flink Kafka App!", "TEST MSG IN inTopicName2")

      val props = new Properties()
      props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, brokers)

      val adminClient = AdminClient.create(props)
      val inTopic1 = new NewTopic(inTopicName1, 1, 1.toShort)
      val inTopic2 = new NewTopic(inTopicName1, 1, 1.toShort)
      val outTopic = new NewTopic(outTopicName, 1, 1.toShort)
      adminClient.createTopics(Seq(inTopic1, inTopic2, outTopic).asJava).all().get()
      adminClient.close()

      Producer(brokers, inTopicName1, benchmark.head)
      Producer(brokers, inTopicName2, benchmark.last)

      FlinkKafkaAppRun(brokers, List(inTopicName1, inTopicName2), outTopicName)

      val received = Consumer(brokers, outTopicName, benchmark.length)

      assert(received.length == benchmark.length)

      assert(received.sorted == benchmark.sorted)
    }
  }
}