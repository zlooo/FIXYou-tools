package io.github.zlooo.performance.tester.fixio;

import fixio.fixprotocol.FieldType;
import fixio.fixprotocol.FixMessage;
import fixio.fixprotocol.FixMessageBuilderImpl;
import fixio.fixprotocol.fields.FixedPointNumber;
import fixio.handlers.FixApplicationAdapter;
import fixio.validator.BusinessRejectException;
import io.github.zlooo.performance.tester.Quote;
import io.github.zlooo.performance.tester.fix.FixConstants;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

@Slf4j
public class QuoteStreamingApplication extends FixApplicationAdapter implements Runnable {

    private final Quote[] quotes = Quote.prepareQuotes(Quote.NUMBER_OF_PREPARED_QUOTES);
    private final Thread quoteStreamer = new Thread(this, "quote-streamer-fixio");
    private volatile String quoteRequestId;
    private ChannelHandlerContext channelHandlerContext;

    public QuoteStreamingApplication() {
        quoteStreamer.start();
    }

    @Override
    public void onMessage(ChannelHandlerContext ctx, FixMessage msg, List<Object> out) throws BusinessRejectException, InterruptedException {
        final String messageType = msg.getHeader().getMessageType();
        switch (messageType) {
            case FixConstants.QUOTE_REQUEST_AS_STRING:
                this.channelHandlerContext = ctx;
                quoteRequestId = msg.getString(FixConstants.QUOTE_REQ_ID_FIELD_NUMBER);
                break;
            case FixConstants.QUOTE_CANCEL_AS_STRING:
                quoteRequestId = null;
                break;
            default:
                log.error("Unrecognized message type {}", messageType);
        }
    }

    @Override
    public void run() {
        while (true) {
            final String requestIdCopy = quoteRequestId;
            if (requestIdCopy != null) {
                for (final Quote quote : quotes) {
                    channelHandlerContext.writeAndFlush(new FixMessageBuilderImpl(FixConstants.QUOTE_AS_STRING).add(FieldType.QuoteID, UUID.randomUUID().toString())
                                                                                                               .add(FieldType.QuoteReqID, requestIdCopy)
                                                                                                               .add(FieldType.BidPx, new FixedPointNumber(quote.getBidPrice(), 2))
                                                                                                               .add(FieldType.OfferPx, new FixedPointNumber(quote.getOfferPrice(), 2)));
                }
            }
        }
    }
}
