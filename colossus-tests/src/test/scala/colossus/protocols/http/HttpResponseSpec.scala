package colossus.protocols.http

import akka.util.ByteString
import colossus.controller.Source
import colossus.core.DataBuffer
import colossus.testkit.CallbackMatchers
import org.scalatest.{OptionValues, TryValues, MustMatchers, WordSpec}

class HttpResponseSpec extends WordSpec with MustMatchers with TryValues with OptionValues with CallbackMatchers {

  "An HttpResponse" must {

    "be constuctable from a ByteStringLike" in {

      val response = HttpResponse(HttpVersion.`1.1`, HttpCodes.ACCEPTED, Vector(), "test conversion")

      val expected = HttpResponse(HttpVersion.`1.1`, HttpCodes.ACCEPTED, Vector(), ByteString("test conversion"))

      response must equal(expected)

    }
  }

  "StreamingHttpResponse" must {

    "be constructable from a StaticHttpResponse" in {

      val payload = "look ma, no hands!"
      val res = HttpResponse(HttpVersion.`1.1`, HttpCodes.OK, Vector(), ByteString(payload))

      val streamed = StreamingHttpResponse.fromStatic(res)

      streamed.body.get.pullCB() must evaluateTo{x : Option[DataBuffer] =>
        ByteString(x.value.takeAll) must equal(res.body.get)
      }
    }

    "be constuctable from a ByteStringLike" in {

      val payload = "look ma, no hands!"

      val expected = StreamingHttpResponse(
        HttpResponseHead(HttpVersion.`1.1`, HttpCodes.OK, Vector("content-length"->"18")), 
        Some(Source.one(DataBuffer(ByteString("test conversion"))))
      )

      val response = StreamingHttpResponse(HttpVersion.`1.1`, HttpCodes.OK, Vector(), payload)

      response.head must equal(expected.head)

      response.body.get.pullCB() must evaluateTo{x : Option[DataBuffer] =>
        ByteString(x.value.takeAll) must equal(ByteString(payload))
      }

    }
  }

}
