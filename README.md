# akkazure
[![Build Status](https://travis-ci.org/akreuzer/akkastream-azure.svg?branch=master)](https://travis-ci.org/akreuzer/akkastream-azure)

akkazure is an [Akka Stream](http://akka.io/) connector for the [Azure Queue Storage](https://azure.microsoft.com/en-us/services/storage/queues/).

## Example usage

#### Init Azure Storage API

```scala
import com.microsoft.azure.storage._
import com.microsoft.azure.storage.queue._
val storageConnectionString = "DefaultEndpointsProtocol=http;AccountName=<YourAccountName>;AccountKey=<YourKey>"
val storageAccount = CloudStorageAccount.parse(storageConnectionString)
val queueClient = storageAccount.createCloudQueueClient
val queue = queueClient.getQueueReference("myQueue")
```
For more details, see [Microsoft Azure Storage Docs](https://docs.microsoft.com/en-us/azure/storage/storage-java-how-to-use-queue-storage).

#### Queuing a message
```scala
import one.aleph.akkzure.queue._
import one.aleph.akkzure.queue.scaladsl._

// Create an example message
val message = new CloudQueueMessage("Hello Azure")

Source.single(message).runWith(AzureQueueSink(queue)
```

#### Processing and deleting messages
```scala
AzureQueueSource(queue).take(10)
.map({ msg: CloudQueueMessage =>  
  println(msg.getMessageContentAsString) // Print the messages content
  msg                                    // Return message to the flow for deletion
}).runWith(AzureQueueDeleteSink(queue))
```
