package one.aleph.akkzure.queue

import org.scalatest._
import com.microsoft.azure.storage._
import com.microsoft.azure.storage.queue._
import scala.concurrent._
import scala.concurrent.duration._
import akka.stream.scaladsl._
import akka.stream._
import akka.testkit._
import akka.actor.ActorSystem
import scala.util.Properties

class AzureQueueSpec extends TestKit(ActorSystem()) with FlatSpecLike with BeforeAndAfterEach {
  import one.aleph.akkzure.queue.scaladsl._

  implicit val materializer = ActorMaterializer()
  val storageConnectionStringOpt = Properties.envOrNone("AZURE_CONNECTION_STRING")

  assume(storageConnectionStringOpt.isDefined, "Please set AZURE_CONNECTION_STRING for the live test.")

  //val storageConnectionString = RoleEnvironment.getConfigurationSettings.get("StorageConnectionString")
  val storageAccount = CloudStorageAccount.parse(storageConnectionStringOpt.get)
  val queueClient = storageAccount.createCloudQueueClient
  val queue = queueClient.getQueueReference("testqueue")
  queue.createIfNotExists

  override def beforeEach() {
    queue.clear
    super.beforeEach()
  }

  private var testMsgCount = 0
  def queueTestMsg: CloudQueueMessage = {
    val message = new CloudQueueMessage(s"Test message no. ${testMsgCount}")
    testMsgCount += 1
    message
  }

  "One" should "be able to queue and retrive messages" in {
    val msgSent = queueTestMsg
    queue.addMessage(msgSent)
    val msgRes = queue.retrieveMessage
    assert(msgSent.getMessageContentAsString == msgRes.getMessageContentAsString)
  }

  "AzureQueueSource" should "be able to retrieve messages" in {
    val msgs = (1 to 10).map(_ => queueTestMsg)
    msgs.foreach(m => queue.addMessage(m))

    assertResult(msgs.map(_.getMessageContentAsString)) {
      val future = AzureQueueSource(queue).take(10).runWith(Sink.seq)
      Await.result(future, 3.seconds).map(_.getMessageContentAsString)
    }
  }

  "AzureQueueSink" should "be able to queue messages" in {
    val msgs = (1 to 10).map(_ => queueTestMsg)
    Await.result(Source(msgs).runWith(AzureQueueSink(queue)), 3.seconds)

    assertResult(10) {
      val future = AzureQueueSource(queue).take(10).runWith(Sink.seq)
      Await.result(future, 3.seconds).size
    }
  }

  "AzureQueueDeleteSink" should "be able to delete messages" in {
    // Queue 2 messages
    val msgs = (1 to 2).map(_ => queueTestMsg)
    Await.result(Source(msgs).runWith(AzureQueueSink(queue)), 3.seconds)
    // Delete 2 messages
    Await.result(AzureQueueSource(queue).take(2).runWith(AzureQueueDeleteSink(queue)), 3.seconds)

    // Now we should not be able to get another one
    assertThrows[TimeoutException] {
      Await.result(AzureQueueSource(queue).take(1).runWith(Sink.seq), 3.seconds)
    }
  }
}