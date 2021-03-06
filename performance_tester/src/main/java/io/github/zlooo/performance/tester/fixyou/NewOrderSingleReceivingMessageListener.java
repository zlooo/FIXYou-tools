package io.github.zlooo.performance.tester.fixyou;

import io.github.zlooo.fixyou.commons.pool.ObjectPool;
import io.github.zlooo.fixyou.netty.AbstractNettyAwareFixMessageListener;
import io.github.zlooo.fixyou.parser.model.Field;
import io.github.zlooo.fixyou.parser.model.FixMessage;
import io.github.zlooo.fixyou.session.SessionID;
import io.github.zlooo.performance.tester.fix.FixConstants;
import io.github.zlooo.performance.tester.fix.FixMessages;
import io.netty.channel.ChannelFutureListener;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NewOrderSingleReceivingMessageListener extends AbstractNettyAwareFixMessageListener {

    private final char[] execID = new char[]{'0', '0', '0', '0', '0'};
    private final char[] orderID = new char[]{'0', '0', '0', '0', '0'};
    @Setter
    private ObjectPool<FixMessage> fixMessageObjectPool;

    @Override
    public void onFixMessage(SessionID sessionID, FixMessage fixMessage) {
        final Field clordIdField = fixMessage.getField(FixConstants.CLORD_ID_FIELD_NUMBER);
        clordIdField.getCharSequenceValue(); //just to trigger parsing
        getChannel().write(FixMessages.toExecutionReport(getFixMessageFromPool(), clordIdField, increment(execID), 'A', 'A', increment(orderID))).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
        getChannel().write(FixMessages.toExecutionReport(getFixMessageFromPool(), clordIdField, increment(execID), '0', '0', orderID)).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
        getChannel().writeAndFlush(FixMessages.toExecutionReport(getFixMessageFromPool(), clordIdField, increment(execID), '2', '2', orderID)).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    private FixMessage getFixMessageFromPool() {
        FixMessage fixMessage;
        while ((fixMessage = fixMessageObjectPool.tryGetAndRetain()) == null) {
            Thread.yield();
        }
        return fixMessage;
    }

    private static char[] increment(char[] counter) {
        for (int i = counter.length - 1; i >= 0; i--) {
            final char digit = ++counter[i];
            if (digit > '9') {
                counter[i] = '0';
                continue;
            } else {
                break;
            }
        }
        return counter;
    }
}
