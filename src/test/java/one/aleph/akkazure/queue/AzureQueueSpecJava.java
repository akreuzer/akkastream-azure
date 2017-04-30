package one.aleph.akkzure.queue;

import org.scalatest.*;
import com.microsoft.azure.storage.*;
import com.microsoft.azure.storage.queue.*;

import akka.stream.javadsl.*;
import akka.stream.*;
import akka.*;
import java.util.concurrent.*;
import one.aleph.akkzure.queue.javadsl.*;

class AzureQueueSpecJava  {
    static public void azureQueueJavaDSL(final CloudQueue queue, final ActorMaterializer materializer) throws InterruptedException, ExecutionException, TimeoutException {
        final Source<Integer, NotUsed> sourceInt = Source.range(1, 10);
        final Source<CloudQueueMessage, NotUsed> source = 
            sourceInt.map(i -> new CloudQueueMessage("Java Azure Cloud Test " + i.toString()));

        final Sink<CloudQueueMessage, CompletionStage<Done>> sink = AzureQueueSink.create(queue);

        source.runWith(sink, materializer).toCompletableFuture().get(10, TimeUnit.SECONDS);
    }
}