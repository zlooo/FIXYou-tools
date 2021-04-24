package io.github.zlooo.performance.tester.fixyou;

import io.github.zlooo.fixyou.parser.model.FixMessage;
import io.github.zlooo.fixyou.session.SessionID;
import io.github.zlooo.fixyou.utils.ArrayUtils;
import io.github.zlooo.performance.tester.Quote;
import io.github.zlooo.performance.tester.fix.FixConstants;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
public class QuoteStreamingMessageListener extends AbstractFIXYouTestMessageListener implements Runnable {

    private final Quote[] quotes = Quote.prepareQuotes(Quote.NUMBER_OF_PREPARED_QUOTES);
    private final Thread quoteStreamer = new Thread(this, "quote-streamer-fixyou");
    private volatile CharSequence quoteRequestId;

    public QuoteStreamingMessageListener() {
        quoteStreamer.start();
    }

    @Override
    public void onFixMessage(SessionID sessionID, FixMessage fixMessage) {
        final CharSequence messageType = fixMessage.getCharSequenceValue(FixConstants.MSG_TYPE_FIELD_NUMBER);
        if (ArrayUtils.equals(FixConstants.QUOTE_REQUEST, messageType)) {
            quoteRequestId = fixMessage.getCharSequenceValue(FixConstants.QUOTE_REQ_ID_FIELD_NUMBER).toString();
        } else if (ArrayUtils.equals(FixConstants.QUOTE_CANCEL, messageType)) {
            quoteRequestId = null;
        } else {
            log.error("Unrecognized message type {}", messageType);
        }
    }

    @Override
    public void run() {
        while (true) {
            final CharSequence requestIdCopy = quoteRequestId;
            if (requestIdCopy != null) {
                for (final Quote quote : quotes) {
                    final FixMessage fixMessage = getFixMessageFromPool();
                    fixMessage.setCharSequenceValue(FixConstants.MSG_TYPE_FIELD_NUMBER, FixConstants.QUOTE);
                    fixMessage.setCharSequenceValue(FixConstants.QUOTE_ID_FIELD_NUMBER, UUID.randomUUID().toString());
                    fixMessage.setCharSequenceValue(FixConstants.QUOTE_REQ_ID_FIELD_NUMBER, requestIdCopy);
                    fixMessage.setDoubleValue(FixConstants.BID_PX_FIELD_NUMBER, quote.getBidPrice(), (short) 2);
                    fixMessage.setDoubleValue(FixConstants.OFFER_PX_FIELD_NUMBER, quote.getOfferPrice(), (short) 2);
                    getChannel().writeAndFlush(fixMessage);
                }
            }
        }
    }
}
