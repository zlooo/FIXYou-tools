package io.github.zlooo.performance.tester.fixio;

import fixio.fixprotocol.FixMessage;
import fixio.fixprotocol.FixMessageBuilderImpl;
import fixio.fixprotocol.MessageTypes;
import fixio.handlers.FixApplicationAdapter;
import fixio.validator.BusinessRejectException;
import io.github.zlooo.performance.tester.fix.FixMessages;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import quickfix.field.ClOrdID;
import quickfix.field.ExecType;
import quickfix.field.OrdStatus;

import java.util.List;

@Slf4j
public class NewOrderSingleReceivingApplication extends FixApplicationAdapter {

    private int executionId = 1;
    private int orderId = 1;

    @Override
    public void onMessage(ChannelHandlerContext ctx, FixMessage msg, List<Object> out) throws BusinessRejectException, InterruptedException {
        final String messageType = msg.getHeader().getMessageType();
        switch (messageType) {
            case MessageTypes.NEW_ORDER_SINGLE:
                final String clordid = msg.getString(ClOrdID.FIELD);
                ctx.write(FixMessages.createFixioExecutionReport(clordid, executionId++, ExecType.PENDING_NEW, OrdStatus.PENDING_NEW, orderId++));
                ctx.write(FixMessages.createFixioExecutionReport(clordid, executionId++, ExecType.NEW, OrdStatus.NEW, orderId));
                ctx.writeAndFlush(FixMessages.createFixioExecutionReport(clordid, executionId++, ExecType.FILL, OrdStatus.FILLED, orderId));
                break;
            case MessageTypes.LOGOUT:
                ctx.writeAndFlush(new FixMessageBuilderImpl(MessageTypes.LOGOUT));
                break;
            default:
                log.error("Unrecognized message type {}", messageType);
        }
    }
}
