package io.github.zlooo.performance.tester.fixyou;

import io.github.zlooo.fixyou.netty.AbstractNettyAwareFixMessageListener;
import io.github.zlooo.fixyou.netty.NettyHandlerAwareSessionState;
import io.github.zlooo.fixyou.parser.model.CharSequenceField;
import io.github.zlooo.fixyou.parser.model.FixMessage;
import io.github.zlooo.fixyou.session.SessionID;
import io.github.zlooo.performance.tester.fix.FixConstants;
import io.github.zlooo.performance.tester.fix.FixMessages;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class NewOrderSingleReceivingMessageListener extends AbstractNettyAwareFixMessageListener {

    private final char[] execID = new char[]{'0', '0', '0', '0', '0'};
    private final char[] orderID = new char[]{'0', '0', '0', '0', '0'};

    @Override
    public void onFixMessage(SessionID sessionID, FixMessage fixMessage) {
        fixMessage.retain();
        final CharSequenceField clordIdField = fixMessage.getField(FixConstants.CLORD_ID_FIELD_NUMBER);
        clordIdField.getValue(); //just to trigger parsing
        final NettyHandlerAwareSessionState sessionState = NettyHandlerAwareSessionState.getForChannel(getChannel());
        final FixMessage newOrderExecutionReport = getFixMessageFromPool(sessionState);
        newOrderExecutionReport.<CharSequenceField>getField(FixConstants.CLORD_ID_FIELD_NUMBER).setValue(clordIdField);
        final FixMessage filledOrderExecutionReport = getFixMessageFromPool(sessionState);
        filledOrderExecutionReport.<CharSequenceField>getField(FixConstants.CLORD_ID_FIELD_NUMBER).setValue(clordIdField);
        getChannel().write(FixMessages.toExecutionReport(fixMessage, increment(execID), 'A', 'A', increment(orderID))).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
        getChannel().write(FixMessages.toExecutionReport(newOrderExecutionReport, increment(execID), '0', '0', orderID)).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
        getChannel().writeAndFlush(FixMessages.toExecutionReport(filledOrderExecutionReport, increment(execID), '2', '2', orderID)).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    private FixMessage getFixMessageFromPool(NettyHandlerAwareSessionState sessionState) {
        FixMessage fixMessage;
        while ((fixMessage = sessionState.getFixMessageWritePool().tryGetAndRetain()) == null) {
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
