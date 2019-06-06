package edu.duke.oit.vw.queue

import com.github.nscala_time.time.Imports._
import edu.duke.oit.vw.scalatra.WidgetsConfig
import java.util.Properties
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

object MetricsRecorder {
  val props = new Properties()
  val retries: java.lang.Integer = 0
  props.put("bootstrap.servers",WidgetsConfig.properties("WidgetsMetrics.kafka.bootstrapServers"))
  props.put("client.id",WidgetsConfig.properties("WidgetsMetrics.kafka.clientId"))
  props.put("acks",WidgetsConfig.properties("WidgetsMetrics.kafka.acks"))
  props.put("retries",retries)
  props.put("security.protocol",WidgetsConfig.properties("WidgetsMetrics.kafka.securityProtocol"))
  props.put("ssl.client_auth",WidgetsConfig.properties("WidgetsMetrics.kafka.sslClientAuth"))
  props.put("ssl.keystore.location",WidgetsConfig.properties("WidgetsMetrics.kafka.sslKeystoreLocation"))
  props.put("ssl.keystore.password",WidgetsConfig.properties("WidgetsMetrics.kafka.sslKeystorePassword"))
  props.put("ssl.truststore.location",WidgetsConfig.properties("WidgetsMetrics.kafka.sslTruststoreLocation"))
  props.put("ssl.truststore.password",WidgetsConfig.properties("WidgetsMetrics.kafka.sslTruststorePassword"))
  props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
  props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
  val producer = new KafkaProducer[String, String](props)

  def lineProtocol(measurement:String,tags:Map[String,String],fields:Map[String,Any],timestamp:DateTime = DateTime.now()) = {
    val metadata = tags.map(kv => kv._1 + "=" + kv._2).mkString(",")
    val data = fields.map(kv => kv._1 + "=" + kv._2).mkString(",")
    val nano = timestamp.millis
    s"$measurement,$metadata $data $nano"
  }

  def recordBatch(json:String) = {
    val message = BatchUpdateMessage(json)
    val metric = lineProtocol("incoming.uris",Map(),Map("count" -> message.uris.length))
    val data = new ProducerRecord[String, String](WidgetsConfig.properties("WidgetsMetrics.kafka.batchTopic"),"0",metric)
    producer.send(data)
  }

}
