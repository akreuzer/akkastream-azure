package one.aleph.akkzure.queue.scaladsl

import com.microsoft.azure.storage.queue.{ CloudQueue, CloudQueueMessage }
import one.aleph.akkzure.queue.{ AzureQueueSourceSettings, AzureQueueSourceStage }
import akka.stream.scaladsl.Source
import akka.NotUsed

object AzureQueueSource {

  /**
   * Scala API: creates a [[AzureQueueSource]] for a Azure CloudQueue.
   */
  def apply(
    cloudQueue: CloudQueue,
    settings: AzureQueueSourceSettings = AzureQueueSourceSettings.default
  ): Source[CloudQueueMessage, NotUsed] =
    Source.fromGraph(new AzureQueueSourceStage(cloudQueue, settings))
}