/*
Usage:
spark-submit --packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.4.0 --class KafkaExample <jar location> <kafka topic name> <search string>
 */
import java.util

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.Seconds
import org.apache.spark.streaming.dstream.DStream
import twitter4j.conf.ConfigurationBuilder
import twitter4j.auth.OAuthAuthorization
import twitter4j.Status
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.{SparkConf, SparkContext}
import java.util.Properties



 object  KafkaExample {
  def main(args: Array[String]): Unit = {
    val kafkaBrokers = "localhost:9092,localhost:9093"
    val sparkConfiguration = new SparkConf()
      .setAppName("kafka-tutorials")
      .setMaster(sys.env.get("spark.master").getOrElse("local[*]"))
    val sparkContext = new SparkContext(sparkConfiguration)
    if (args.length < 2) {
      println("Correct usage: Program_Name inputTopic outputTopic")
      System.exit(1)
    }
    val outTopic = args(0).toString
    val keys = Array("u4ffZeRplaEGpZ94Pk9I6CbPp", "SUCMOFhcGWfyIklPAqlPyzLqQuz8aanEANzDm6Mek4o591mitC", "1092853592644571136-8MKPInQrqrMFed1qPAHwaHUV0ASKp7", "teVEchbsYrwpke6BSWQg1Vmoi6zDb8GxFMXFDi6H7Eikz")
    val appName = "TwitterData"
    val ssc = new StreamingContext(sparkContext, Seconds(1))
    val Array(consumerKey, consumerSecret, accessToken, accessTokenSecret) = keys.take(4)
    val filters = args.takeRight(keys.length - 1)
    val cb = new ConfigurationBuilder
    cb.setDebugEnabled(true).setOAuthConsumerKey(consumerKey)
      .setOAuthConsumerSecret(consumerSecret)
      .setOAuthAccessToken(accessToken)
      .setOAuthAccessTokenSecret(accessTokenSecret)
    val auth = new OAuthAuthorization(cb.build)
    val tweets = TwitterUtils.createStream(ssc, Some(auth),filters)
    val englishTweets: DStream[Status] = tweets.filter(_.getLang() == "en")
    val textAndSentences: DStream[String] =
      tweets.filter(x => x.getLang == "en").
        map(_.getText)
    textAndSentences.print()

    textAndSentences.foreachRDD( rdd => {

      rdd.foreachPartition( partition => {
        val props = new util.HashMap[String, Object]()
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBrokers)
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
          "org.apache.kafka.common.serialization.StringSerializer")
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
          "org.apache.kafka.common.serialization.IntegerSerializer")
        val producer = new KafkaProducer[Int, String](props)
        partition.foreach( record => {
          val data = record.toString
          val sentiment = SentimentAnalyzer.mainSentiment(data)
          var sent = 0;
          if(sentiment.toString == "POSITIVE"){
            sent = 2;
          }else if(sentiment.toString == "NEUTRAL" ){
            sent = 1;
          }else{
            sent =0;
          }
          val message = new ProducerRecord[Int, String](outTopic, sent,sent.toString)
          //data+"=========>"+sentiment.toString
          producer.send(message)
        } )
        producer.close()
      })

    })
    ssc.start()
    ssc.awaitTermination()

  }
}
