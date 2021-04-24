package io.github.zlooo.performance.tester.fixyou;

import io.github.zlooo.fixyou.parser.model.FixMessage;
import io.github.zlooo.fixyou.session.SessionID;
import io.github.zlooo.performance.tester.fix.FixConstants;
import io.github.zlooo.performance.tester.fix.FixMessages;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NewOrderSingleReceivingMessageListener extends AbstractFIXYouTestMessageListener {

    private final char[] execID = new char[]{'0', '0', '0', '0', '0'};
    private final char[] orderID = new char[]{'0', '0', '0', '0', '0'};

    @Override
    public void onFixMessage(SessionID sessionID, FixMessage fixMessage) {
        final CharSequence clordId = fixMessage.getCharSequenceValue(FixConstants.CLORD_ID_FIELD_NUMBER);
        getChannel().write(FixMessages.toExecutionReport(getFixMessageFromPool(), clordId, increment(execID), 'A', 'A', increment(orderID))).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
        getChannel().write(FixMessages.toExecutionReport(getFixMessageFromPool(), clordId, increment(execID), '0', '0', orderID)).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
        getChannel().writeAndFlush(FixMessages.toExecutionReport(getFixMessageFromPool(), clordId, increment(execID), '2', '2', orderID)).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
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
