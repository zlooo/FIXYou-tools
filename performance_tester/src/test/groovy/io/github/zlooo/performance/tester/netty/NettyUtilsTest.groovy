package io.github.zlooo.performance.tester.netty

import io.github.zlooo.performance.tester.netty.NettyUtils.AdminMessageHandler
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelHandlerContext
import org.assertj.core.api.Assertions
import quickfix.SessionID
import quickfix.field.TestReqID
import quickfix.fixt11.TestRequest
import spock.lang.Specification

import java.util.concurrent.atomic.AtomicInteger

class NettyUtilsTest extends Specification {

    private AtomicInteger sequencer = new AtomicInteger(0)
    private SessionID sessionID = new SessionID("FIXT11", "sender", "target")
    private AdminMessageHandler adminMessageHandler = new AdminMessageHandler(sessionID, sequencer)
    private ChannelHandlerContext channelHandlerContext = Mock()

    def "should respond to test request with heartbeat"() {
        setup:
        def testRequestId = "testRequestId"
        def testRequest = new TestRequest()
        testRequest.set(new TestReqID(testRequestId))
        def response

        when:
        adminMessageHandler.channelRead0(channelHandlerContext, testRequest.toString())

        then:
        1 * channelHandlerContext.writeAndFlush(_ as String) >> { String msg ->
            response = msg
            return Mock(ChannelFuture)
        }
        Assertions.assertThat(response as String).containsOnlyOnce("35=0").containsOnlyOnce("112=$testRequestId")
    }
}
