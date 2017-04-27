package one.aleph.akkzure.queue.scaladsl

import com.microsoft.azure.storage.queue.{ CloudQueue, CloudQueueMessage }
import one.aleph.akkzure.queue.{ AzureQueueSinkFunctions, FlowMapECStage, DeleteOrUpdateMessage }
import akka.stream.scaladsl.{ Flow, Sink, Keep }
import akka.Done
import scala.concurrent.{ Future, ExecutionContext }

object AzureQueueSink {
  def apply(cloudQueue: CloudQueue, maxInFlight: Int = 4): Sink[CloudQueueMessage, Future[Done]] = {
    fromFunction(AzureQueueSinkFunctions.addMessage(cloudQueue)(_)(_), maxInFlight)
  }

  def fromFunction[T](f: (T, ExecutionContext) => Future[Done], maxInFlight: Int): Sink[T, Future[Done]] = {
    val flowStage = new FlowMapECStage[T, Future[Done]](f)
    Flow.fromGraph(flowStage)
      .mapAsync(maxInFlight)(identity)
      .toMat(Sink.ignore)(Keep.right)
  }
}

object AzureQueueDeleteSink {
  def apply(cloudQueue: CloudQueue, maxInFlight: Int = 4): Sink[CloudQueueMessage, Future[Done]] = {
    AzureQueueSink.fromFunction(AzureQueueSinkFunctions.deleteMessage(cloudQueue)(_)(_), maxInFlight)
  }
}

object AzureQueueDeleteOrUpdateSink {
  def apply(cloudQueue: CloudQueue, maxInFlight: Int = 4): Sink[(CloudQueueMessage, DeleteOrUpdateMessage), Future[Done]] = {
    AzureQueueSink.fromFunction((input, ec) =>
      AzureQueueSinkFunctions.deleteOrUpdateMessage(cloudQueue)(input._1, input._2)(ec), maxInFlight)
  }
}