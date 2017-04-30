package one.aleph.akkzure.queue.javadsl

import com.microsoft.azure.storage.queue.{ CloudQueue, CloudQueueMessage }
import one.aleph.akkzure.queue.{ AzureQueueSinkFunctions, FlowMapECStage, DeleteOrUpdateMessage }
import akka.stream.javadsl.Sink
import akka.Done
import scala.concurrent.{ Future, ExecutionContext }
import java.util.concurrent.CompletionStage

object AzureQueueSink {
  def create(cloudQueue: CloudQueue, maxInFlight: Int): Sink[CloudQueueMessage, CompletionStage[Done]] = {
    fromFunction(AzureQueueSinkFunctions.addMessage(cloudQueue)(_)(_), maxInFlight)
  }

  def create(cloudQueue: CloudQueue): Sink[CloudQueueMessage, CompletionStage[Done]] = {
    create(cloudQueue, 4)
  }

  private[javadsl] def fromFunction[T](f: (T, ExecutionContext) => Future[Done], maxInFlight: Int): Sink[T, CompletionStage[Done]] = {
    import one.aleph.akkzure.queue.scaladsl.{ AzureQueueSink => AzureQueueSinkScalaDSL }
    import scala.compat.java8.FutureConverters._
    AzureQueueSinkScalaDSL.fromFunction(f, maxInFlight).mapMaterializedValue(_.toJava).asJava
  }
}

object AzureQueueDeleteSink {
  def create(cloudQueue: CloudQueue, maxInFlight: Int): Sink[CloudQueueMessage, CompletionStage[Done]] = {
    AzureQueueSink.fromFunction(AzureQueueSinkFunctions.deleteMessage(cloudQueue)(_)(_), maxInFlight)
  }

  def create(cloudQueue: CloudQueue): Sink[CloudQueueMessage, CompletionStage[Done]] = {
    create(cloudQueue, 4)
  }
}

object AzureQueueDeleteOrUpdateSink {
  def create(cloudQueue: CloudQueue, maxInFlight: Int): Sink[(CloudQueueMessage, DeleteOrUpdateMessage), CompletionStage[Done]] = {
    AzureQueueSink.fromFunction((input, ec) =>
      AzureQueueSinkFunctions.deleteOrUpdateMessage(cloudQueue)(input._1, input._2)(ec), maxInFlight)
  }
  def create(cloudQueue: CloudQueue): Sink[(CloudQueueMessage, DeleteOrUpdateMessage), CompletionStage[Done]] = {
    create(cloudQueue, 4)
  }
}