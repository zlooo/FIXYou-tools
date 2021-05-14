package io.github.zlooo.performance.tester.netty;

import io.github.zlooo.performance.tester.fix.FixMessages;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.AttributeKey;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import quickfix.SessionID;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@UtilityClass
public class NettyUtils {

    public static final AttributeKey<AtomicInteger> SEQUENCER_KEY = AttributeKey.valueOf("sequencer");
    private static final int MAX_FRAME_LENGTH = 5000;
    private static final ByteBuf DELIMITER = Unpooled.wrappedBuffer("\u000110=".getBytes(StandardCharsets.US_ASCII));
    private static final String TEST_REQUEST_ID = "112=";
    private static final int TEST_REQUEST_ID_LEN = TEST_REQUEST_ID.length();
    private static final int HEARTBEAT_INTERVAL = 25;

    public static ChannelInitializer channelInitializer(SessionID sessionID) {
        return new ChannelInitializer() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                final AtomicInteger sequencer = new AtomicInteger(0);
                ch.attr(SEQUENCER_KEY).set(sequencer);
                ch.pipeline()
                  .addLast(new DelimiterBasedFrameDecoder(MAX_FRAME_LENGTH, false, true, DELIMITER))
                  .addLast(new StringDecoder(StandardCharsets.US_ASCII))
                  .addLast(new StringEncoder(StandardCharsets.US_ASCII))
                  .addLast(new AdminMessageHandler(sessionID, sequencer));
                ch.eventLoop().scheduleAtFixedRate(() -> ch.writeAndFlush(FixMessages.heartbeat(sessionID, sequencer.incrementAndGet())), HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL, TimeUnit.SECONDS);
            }
        };
    }

    @RequiredArgsConstructor
    private static class AdminMessageHandler extends SimpleChannelInboundHandler<String> {

        private final SessionID sessionID;
        private final AtomicInteger sequencer;

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
            if (msg.contains("35=0")) {
                ctx.writeAndFlush(FixMessages.heartbeat(sessionID, sequencer.incrementAndGet()));
            } else if (msg.contains("35=1")) {
                final int beginIndex = msg.indexOf(TEST_REQUEST_ID);
                ctx.writeAndFlush(FixMessages.heartbeat(sessionID, sequencer.incrementAndGet(), msg.substring(beginIndex + TEST_REQUEST_ID_LEN, msg.indexOf("\u0001", beginIndex))));
            } else {
                ctx.fireChannelRead(msg);
            }
        }
    }
}
