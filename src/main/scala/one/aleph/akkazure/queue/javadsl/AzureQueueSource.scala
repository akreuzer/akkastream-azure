package one.aleph.akkzure.queue.javadsl

import com.microsoft.azure.storage.queue.{ CloudQueue, CloudQueueMessage }
import one.aleph.akkzure.queue.{ AzureQueueSourceSettings, AzureQueueSourceStage }
import akka.stream.javadsl.Source
import akka.NotUsed

object AzureQueueSource {

  /**
   * Java API: creates a [[AzureQueueSource]] for a Azure CloudQueue.
   */
  def create(cloudQueue: CloudQueue, settings: AzureQueueSourceSettings): Source[CloudQueueMessage, NotUsed] =
    Source.fromGraph(new AzureQueueSourceStage(cloudQueue, settings))

  def create(cloudQueue: CloudQueue): Source[CloudQueueMessage, NotUsed] =
    create(cloudQueue, AzureQueueSourceSettings.default)
}