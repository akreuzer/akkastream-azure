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

class AzureQueueSpec extends TestKit(ActorSystem()) with FlatSpecLike with BeforeAndAfterEach with BeforeAndAfterAll {
  import one.aleph.akkzure.queue.scaladsl._

  implicit val materializer = ActorMaterializer()
  val storageConnectionStringOpt = Properties.envOrNone("AZURE_CONNECTION_STRING")
  val timeout = 10.seconds

  assume(storageConnectionStringOpt.isDefined, "Please set AZURE_CONNECTION_STRING for the live test.")

  val storageAccount = CloudStorageAccount.parse(storageConnectionStringOpt.get)
  val queueClient = storageAccount.createCloudQueueClient
  val queue = queueClient.getQueueReference("testqueue")
  queue.createIfNotExists

  override def beforeEach: Unit = {
    queue.clear
    super.beforeEach
  }
  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
    super.afterAll
  }

  private var testMsgCount = 0
  def queueTestMsg: CloudQueueMessage = {
    val message = new CloudQueueMessage(s"Test message no. ${testMsgCount}")
    testMsgCount += 1
    message
  }

  def assertCannotGetMessageFromQueue = {
    assertThrows[TimeoutException] {
      Await.result(AzureQueueSource(queue).take(1).runWith(Sink.seq), timeout)
    }
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
      Await.result(future, timeout).map(_.getMessageContentAsString)
    }
  }

  "AzureQueueSink" should "be able to queue messages" in {
    val msgs = (1 to 10).map(_ => queueTestMsg)
    Await.result(Source(msgs).runWith(AzureQueueSink(queue)), timeout)

    assertResult(10) {
      val future = AzureQueueSource(queue).take(10).runWith(Sink.seq)
      Await.result(future, timeout).size
    }
  }

  "AzureQueueDeleteSink" should "be able to delete messages" in {
    // When queuing 10 messages
    val msgs = (1 to 10).map(_ => queueTestMsg)
    Await.result(Source(msgs).runWith(AzureQueueSink(queue)), timeout)
    // and deleting 10 message 
    Await.result(AzureQueueSource(queue).take(10).runWith(AzureQueueDeleteSink(queue)), timeout)

    // then there should be no messages on the queue anymore
    assertCannotGetMessageFromQueue
  }

  it should "fail for messages not on the queue" in {
    val msgs = (1 to 10).map(_ => queueTestMsg)
    assertThrows[java.lang.IllegalArgumentException] {
      Await.result(Source(msgs).runWith(AzureQueueDeleteSink(queue)), timeout)
    }
  }

  "AzureQueueDeleteOrUpdateSink" should "be able to update visibility timeout" in {
    // Queue 10 messages
    val msgs = (1 to 10).map(_ => queueTestMsg)
    Await.result(Source(msgs).runWith(AzureQueueSink(queue)), timeout)
    // Update the visibility to much later for 10 messages
    Await.result(
      AzureQueueSource(queue).take(10).map(msg => (msg, UpdateVisibility(120)))
        .runWith(AzureQueueDeleteOrUpdateSink(queue)),
      timeout
    )
    // Now we should not be able to get another one
    assertCannotGetMessageFromQueue
  }

  it should "be able to delete messages" in {
    // Queue 10 messages
    val msgs = (1 to 10).map(_ => queueTestMsg)
    Await.result(Source(msgs).runWith(AzureQueueSink(queue)), timeout)
    // Delete 10 messages
    Await.result(
      AzureQueueSource(queue).take(10).map(msg => (msg, Delete))
        .runWith(AzureQueueDeleteOrUpdateSink(queue)),
      timeout
    )

    // Now we should not be able to get another one
    assertCannotGetMessageFromQueue
  }

  "AzureQueueSink from javadsl" should "be able to queue messages" in {
    AzureQueueSpecJava.azureQueueJavaDSL(queue, materializer)
  }
}