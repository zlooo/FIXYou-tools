package pl.zlooo.fixyou.performance.tester.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.experimental.UtilityClass;

import java.nio.charset.StandardCharsets;

@UtilityClass
public class NettyUtils {

    private static final int MAX_FRAME_LENGTH = 5000;
    private static final ByteBuf DELIMITER = Unpooled.wrappedBuffer("\u000110=".getBytes(StandardCharsets.US_ASCII));

    public static ChannelInitializer channelInitializer() {
        return new ChannelInitializer() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ch.pipeline().addLast(new DelimiterBasedFrameDecoder(MAX_FRAME_LENGTH, false, true, DELIMITER)).addLast(new StringDecoder(StandardCharsets.US_ASCII)).addLast(new StringEncoder(StandardCharsets.US_ASCII));
            }
        };
    }
}
