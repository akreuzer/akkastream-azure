package one.aleph.akkzure.queue

import com.microsoft.azure.storage.queue.{ CloudQueue, CloudQueueMessage }
import scala.concurrent.{ Future, ExecutionContext }
import akka.Done

sealed trait DeleteOrUpdateMessage
case object Delete extends DeleteOrUpdateMessage
case class UpdateVisibility(timeout: Int) extends DeleteOrUpdateMessage

object AzureQueueSinkFunctions {
  def addMessage(cloudQueue: CloudQueue)(msg: CloudQueueMessage)(implicit executionContext: ExecutionContext): Future[Done] = {
    Future {
      cloudQueue.addMessage(msg)
      Done
    }
  }

  def deleteMessage(cloudQueue: CloudQueue)(msg: CloudQueueMessage)(implicit executionContext: ExecutionContext): Future[Done] = {
    Future {
      cloudQueue.deleteMessage(msg)
      Done
    }
  }

  def updateMessage(cloudQueue: CloudQueue)(msg: CloudQueueMessage, timeout: Int)(implicit executionContext: ExecutionContext): Future[Done] = {
    Future {
      cloudQueue.updateMessage(msg, timeout)
      Done
    }
  }

  def deleteOrUpdateMessage(cloudQueue: CloudQueue)(msg: CloudQueueMessage, op: DeleteOrUpdateMessage)(implicit executionContext: ExecutionContext): Future[Done] = {
    op match {
      case Delete => deleteMessage(cloudQueue)(msg)
      case UpdateVisibility(timeout) => updateMessage(cloudQueue)(msg, timeout)
    }
  }
}

