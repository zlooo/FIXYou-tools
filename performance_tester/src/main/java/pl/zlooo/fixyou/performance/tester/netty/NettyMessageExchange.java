package pl.zlooo.fixyou.performance.tester.netty;

import com.github.benmanes.caffeine.SingleConsumerQueue;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import pl.zlooo.fixyou.performance.tester.MessageExchange;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Queue;

@Slf4j
public class NettyMessageExchange implements MessageExchange<String> {

    private final Channel channel;
    private final Queue<String> receivedMessages;

    public NettyMessageExchange(Channel channel) {
        this.channel = channel;
        receivedMessages = SingleConsumerQueue.optimistic();
        channel.pipeline().addLast(new SimpleChannelInboundHandler<String>() {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
                if (!receivedMessages.offer(msg)) {
                    log.error("Could not add message {} to internal queue. Is it too small?", msg);
                }
            }
        });
    }

    @Override
    public void sendMessage(@Nonnull String message) {
        channel.write(message).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    @Nullable
    @Override
    public String getSingleMessage() {
        return receivedMessages.poll();
    }

    @Override
    public void endOfBatch() {
        channel.flush();
    }
}
