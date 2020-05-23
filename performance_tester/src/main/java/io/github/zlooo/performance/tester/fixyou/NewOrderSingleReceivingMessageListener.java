package io.github.zlooo.performance.tester.fixyou;

import io.github.zlooo.fixyou.netty.AbstractNettyAwareFixMessageListener;
import io.github.zlooo.fixyou.netty.NettyHandlerAwareSessionState;
import io.github.zlooo.fixyou.parser.model.FixMessage;
import io.github.zlooo.fixyou.session.SessionID;
import io.github.zlooo.performance.tester.fix.FixConstants;
import io.github.zlooo.performance.tester.fix.FixMessages;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;

public class NewOrderSingleReceivingMessageListener extends AbstractNettyAwareFixMessageListener {

    private final char[] execID = new char[]{'0', '0', '0', '0', '0'};
    private final char[] orderID = new char[]{'0', '0', '0', '0', '0'};

    @Override
    public void onFixMessage(SessionID sessionID, FixMessage fixMessage) {
        fixMessage.retain();
        getChannel().write(FixMessages.toExecutionReport(fixMessage, increment(execID), 'A', 'A', increment(orderID))).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
        final NettyHandlerAwareSessionState sessionState = NettyHandlerAwareSessionState.getForChannel(getChannel());
        final FixMessage newOrderExecutionReport = sessionState.getFixMessageObjectPool().getAndRetain();
        final ByteBuf clordIdFieldData = fixMessage.getField(FixConstants.CLORD_ID_FIELD_NUMBER).getFieldData();
        newOrderExecutionReport.getField(FixConstants.CLORD_ID_FIELD_NUMBER).setFieldData(clordIdFieldData);
        getChannel().write(FixMessages.toExecutionReport(newOrderExecutionReport, increment(execID), '0', '0', orderID)).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
        final FixMessage filledOrderExecutionReport = sessionState.getFixMessageObjectPool().getAndRetain();
        filledOrderExecutionReport.getField(FixConstants.CLORD_ID_FIELD_NUMBER).setFieldData(clordIdFieldData);
        getChannel().writeAndFlush(FixMessages.toExecutionReport(filledOrderExecutionReport, increment(execID), '2', '2', orderID)).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    private static char[] increment(char[] counter) {
        for (int i = counter.length - 1; i >= 0; i--) {
            final char digit = counter[i]++;
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
