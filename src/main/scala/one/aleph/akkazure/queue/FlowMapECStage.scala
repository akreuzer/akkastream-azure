package one.aleph.akkzure.queue

import akka.stream.stage.{ GraphStage, GraphStageLogic, InHandler, OutHandler }
import akka.stream.{ Attributes, Inlet, Outlet, FlowShape }
import scala.concurrent.{ Future, ExecutionContext }

// This flow does just a map
// with the exception that is also hands the ExecutionContext
// of the materializer to the function f
private[queue] class FlowMapECStage[In, Out](f: (In, ExecutionContext) => Out) extends GraphStage[FlowShape[In, Out]] {
  private val in = Inlet[In]("in")
  private val out = Outlet[Out]("out")
  override val shape = FlowShape(in, out)

  override def createLogic(inheritedAttributes: Attributes) = new GraphStageLogic(shape) {
    setHandler(out, new OutHandler {
      override def onPull() =
        tryPull(in)
    })

    setHandler(in, new InHandler {
      def onPush() = {
        implicit val executionContext = materializer.executionContext
        val input = grab(in)
        push(out, f(input, executionContext))
      }
    })
  }
}