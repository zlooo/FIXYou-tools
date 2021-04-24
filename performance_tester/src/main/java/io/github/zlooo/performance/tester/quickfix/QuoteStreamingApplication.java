package io.github.zlooo.performance.tester.quickfix;

import io.github.zlooo.performance.tester.Quote;
import io.github.zlooo.performance.tester.fix.FixConstants;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import quickfix.*;
import quickfix.field.BidPx;
import quickfix.field.OfferPx;
import quickfix.field.QuoteID;
import quickfix.field.QuoteReqID;

import java.util.UUID;

@Slf4j
public class QuoteStreamingApplication implements Application, Runnable {

    private final Quote[] quotes = Quote.prepareQuotes(Quote.NUMBER_OF_PREPARED_QUOTES);
    private final Thread quoteStreamer = new Thread(this, "quote-streamer-quickfix");
    private volatile String quoteRequestId;
    private SessionID sessionID;

    public QuoteStreamingApplication() {
        quoteStreamer.start();
    }

    @Override
    public void onCreate(SessionID sessionId) {

    }

    @Override
    public void onLogon(SessionID sessionId) {

    }

    @Override
    public void onLogout(SessionID sessionId) {

    }

    @Override
    public void toAdmin(Message message, SessionID sessionId) {

    }

    @Override
    public void fromAdmin(Message message, SessionID sessionId) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {

    }

    @Override
    public void toApp(Message message, SessionID sessionId) throws DoNotSend {

    }

    @Override
    public void fromApp(Message message, SessionID sessionId) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
        final String messageType = message.getHeader().getString(FixConstants.MSG_TYPE_FIELD_NUMBER);
        switch (messageType) {
            case FixConstants.QUOTE_REQUEST_AS_STRING:
                this.sessionID = sessionId;
                quoteRequestId = message.getString(FixConstants.QUOTE_REQ_ID_FIELD_NUMBER);
                break;
            case FixConstants.QUOTE_CANCEL_AS_STRING:
                quoteRequestId = null;
                break;
            default:
                log.error("Unrecognized message type {}", messageType);
        }
    }

    @Override
    @SneakyThrows
    public void run() {
        while (true) {
            final String requestIdCopy = quoteRequestId;
            if (requestIdCopy != null) {
                for (final Quote quote : quotes) {
                    final quickfix.fix50sp2.Quote fixMessage = new quickfix.fix50sp2.Quote();
                    fixMessage.set(new QuoteID(UUID.randomUUID().toString()));
                    fixMessage.set(new QuoteReqID(requestIdCopy));
                    fixMessage.set(new BidPx(quote.getBidPriceAsDouble()));
                    fixMessage.set(new OfferPx(quote.getOfferPriceAsDouble()));
                    Session.sendToTarget(fixMessage, sessionID);
                }
            }
        }
    }
}
