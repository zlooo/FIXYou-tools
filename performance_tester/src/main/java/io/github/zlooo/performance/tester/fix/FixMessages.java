package io.github.zlooo.performance.tester.fix;

import fixio.fixprotocol.FieldType;
import fixio.fixprotocol.FixMessageBuilder;
import fixio.fixprotocol.FixMessageBuilderImpl;
import fixio.fixprotocol.MessageTypes;
import fixio.fixprotocol.fields.FixedPointNumber;
import io.github.zlooo.fixyou.parser.model.FixMessage;
import lombok.experimental.UtilityClass;
import quickfix.Message;
import quickfix.SessionID;
import quickfix.field.*;
import quickfix.fix50sp2.ExecutionReport;
import quickfix.fix50sp2.NewOrderSingle;
import quickfix.fix50sp2.QuoteCancel;
import quickfix.fix50sp2.QuoteRequest;
import quickfix.fix50sp2.component.QuotReqGrp;
import quickfix.fixt11.Heartbeat;
import quickfix.fixt11.Logon;
import quickfix.fixt11.Logout;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@UtilityClass
public class FixMessages {

    private static final double LEAVES_QTY = 10.0;
    private static final long LEAVES_QTY_LONG = 10;
    private static final double CUM_QTY = 0.0;
    private static final long CUM_QTY_LONG = 0;
    private static final char[] VODL = new char[]{'V', 'O', 'D', '.', 'L'};

    public static String logon(SessionID sessionID, int sequenceNumber, int heartbeatInterval, boolean resetSequenceNumber) {
        final Logon logon = new Logon();
        putSessionIdInfo(sessionID, logon.getHeader(), false);
        putStandardHeaderFields(logon.getHeader(), sequenceNumber);
        logon.set(new EncryptMethod(EncryptMethod.NONE_OTHER));
        logon.set(new HeartBtInt(heartbeatInterval));
        logon.set(new DefaultApplVerID("7"));
        logon.set(new ResetSeqNumFlag(resetSequenceNumber));
        return logon.toString();
    }

    public static String logout(SessionID sessionID, int sequenceNumber, String text) {
        final Logout logout = new Logout();
        putSessionIdInfo(sessionID, logout.getHeader(), false);
        putStandardHeaderFields(logout.getHeader(), sequenceNumber);
        if (text != null) {
            logout.set(new Text());
        }
        return logout.toString();
    }

    public static String newOrderSingle(SessionID sessionID, int sequenceNumber, String clordid) {
        final NewOrderSingle newOrderSingle = createNewOrderSingle(clordid);
        putSessionIdInfo(sessionID, newOrderSingle.getHeader(), false);
        putStandardHeaderFields(newOrderSingle.getHeader(), sequenceNumber);
        return newOrderSingle.toString();
    }

    public static String executionReport(SessionID sessionID, int sequenceNumber, String clordid, int executionId, char execType, char orderStatus, int orderId) {
        final ExecutionReport executionReport = createExecutionReport(clordid, executionId, execType, orderStatus, orderId);
        putSessionIdInfo(sessionID, executionReport.getHeader(), false);
        putStandardHeaderFields(executionReport.getHeader(), sequenceNumber);
        return executionReport.toString();
    }

    public static String quoteRequest(SessionID sessionID, int sequenceNumber, String quoteRequestId) {
        final QuoteRequest quoteRequest = new QuoteRequest();
        quoteRequest.set(new QuoteReqID(quoteRequestId));
        final QuotReqGrp.NoRelatedSym noRelatedSym = new QuotReqGrp.NoRelatedSym();
        noRelatedSym.set(new Symbol("VOD.L"));
        quoteRequest.addGroup(noRelatedSym);
        putSessionIdInfo(sessionID, quoteRequest.getHeader(), false);
        putStandardHeaderFields(quoteRequest.getHeader(), sequenceNumber);
        return quoteRequest.toString();
    }

    public static String quoteCancel(SessionID sessionID, int sequenceNumber, String quoteRequestId) {
        final QuoteCancel quoteCancel = new QuoteCancel();
        quoteCancel.set(new QuoteReqID(quoteRequestId));
        quoteCancel.set(new QuoteCancelType(QuoteCancelType.CANCEL_ALL_QUOTES));
        putSessionIdInfo(sessionID, quoteCancel.getHeader(), false);
        putStandardHeaderFields(quoteCancel.getHeader(), sequenceNumber);
        return quoteCancel.toString();
    }

    public static String heartbeat(SessionID sessionID, int sequenceNumber) {
        final Heartbeat heartbeat = new Heartbeat();
        putSessionIdInfo(sessionID, heartbeat.getHeader(), false);
        putStandardHeaderFields(heartbeat.getHeader(), sequenceNumber);
        return heartbeat.toString();
    }

    public static String heartbeat(SessionID sessionID, int sequenceNumber, String testRequestId) {
        final Heartbeat heartbeat = new Heartbeat();
        heartbeat.set(new TestReqID(testRequestId));
        putSessionIdInfo(sessionID, heartbeat.getHeader(), false);
        putStandardHeaderFields(heartbeat.getHeader(), sequenceNumber);
        return heartbeat.toString();
    }

    public static FixMessage toExecutionReport(FixMessage fixMessage, CharSequence clordId, char[] executionId, char execType, char orderStatus, char[] orderId) {
        fixMessage.reset();
        fixMessage.setCharSequenceValue(FixConstants.CLORD_ID_FIELD_NUMBER, clordId);
        fixMessage.setCharSequenceValue(io.github.zlooo.fixyou.FixConstants.MESSAGE_TYPE_FIELD_NUMBER, io.github.zlooo.performance.tester.fix.FixConstants.EXECUTION_REPORT);
        fixMessage.setCharSequenceValue(io.github.zlooo.performance.tester.fix.FixConstants.ORDER_ID_FIELD_NUMBER, orderId);
        fixMessage.setCharSequenceValue(io.github.zlooo.performance.tester.fix.FixConstants.EXEC_ID_FIELD_NUMBER, executionId);
        fixMessage.setCharValue(io.github.zlooo.performance.tester.fix.FixConstants.EXEC_TYPE_FIELD_NUMBER, execType);
        fixMessage.setCharValue(io.github.zlooo.performance.tester.fix.FixConstants.ORD_STATUS_FIELD_NUMBER, orderStatus);
        fixMessage.setCharSequenceValue(io.github.zlooo.performance.tester.fix.FixConstants.SYMBOL_FIELD_NUMBER, VODL);
        fixMessage.setCharValue(io.github.zlooo.performance.tester.fix.FixConstants.SIDE_FIELD_NUMBER, '1');
        fixMessage.setDoubleValue(io.github.zlooo.performance.tester.fix.FixConstants.LEAVES_QTY_FIELD_NUMBER, LEAVES_QTY_LONG, (short) 0);
        fixMessage.setDoubleValue(io.github.zlooo.performance.tester.fix.FixConstants.CUM_QTY_FIELD_NUMBER, CUM_QTY_LONG, (short) 0);
        return fixMessage;
    }

    private static void putSessionIdInfo(SessionID sessionID, Message.Header header, boolean flipIDs) {
        if (flipIDs) {
            header.setString(io.github.zlooo.performance.tester.fix.FixConstants.SENDER_COMP_ID_FIELD_NUMBER, sessionID.getTargetCompID());
            header.setString(io.github.zlooo.performance.tester.fix.FixConstants.TARGET_COMP_ID_FIELD_NUMBER, sessionID.getSenderCompID());
        } else {
            header.setString(io.github.zlooo.performance.tester.fix.FixConstants.SENDER_COMP_ID_FIELD_NUMBER, sessionID.getSenderCompID());
            header.setString(io.github.zlooo.performance.tester.fix.FixConstants.TARGET_COMP_ID_FIELD_NUMBER, sessionID.getTargetCompID());
        }
        header.setString(io.github.zlooo.performance.tester.fix.FixConstants.BEGIN_STRING_FIELD_NUMBER, sessionID.getBeginString());
    }

    private static void putStandardHeaderFields(Message.Header header, int sequenceNumber) {
        header.setInt(io.github.zlooo.performance.tester.fix.FixConstants.MSG_SEQ_NUM_FIELD_NUMBER, sequenceNumber);
        header.setUtcTimeStamp(io.github.zlooo.performance.tester.fix.FixConstants.SENDING_TIME_FIELD_NUMBER, LocalDateTime.now(ZoneOffset.UTC));
    }

    private static NewOrderSingle createNewOrderSingle(String clordid) {
        final NewOrderSingle newOrderSingle = new NewOrderSingle();
        newOrderSingle.set(new ClOrdID(clordid));
        newOrderSingle.set(new Side(Side.BUY));
        newOrderSingle.set(new TransactTime(LocalDateTime.now()));
        newOrderSingle.set(new OrdType(OrdType.MARKET));
        return newOrderSingle;
    }

    public static ExecutionReport createExecutionReport(String clordid, int executionId, char execType, char orderStatus, int orderId) {
        final ExecutionReport executionReport = new ExecutionReport();
        executionReport.set(new OrderID(String.valueOf(orderId)));
        executionReport.set(new ClOrdID(clordid));
        executionReport.set(new ExecID(orderId + "_" + executionId));
        executionReport.set(new ExecType(execType));
        executionReport.set(new OrdStatus(orderStatus));
        executionReport.set(new Symbol("VOD.L"));
        executionReport.set(new Side(Side.BUY));
        executionReport.set(new LeavesQty(LEAVES_QTY));
        executionReport.set(new CumQty(CUM_QTY));
        return executionReport;
    }

    public static FixMessageBuilder createFixioExecutionReport(String clordid, int executionId, char execType, char orderStatus, int orderId) {
        return new FixMessageBuilderImpl(MessageTypes.EXECUTION_REPORT).add(FieldType.OrderID, String.valueOf(orderId))
                                                                       .add(FieldType.ClOrdID, clordid)
                                                                       .add(FieldType.ExecID, orderId + "_" + executionId)
                                                                       .add(FieldType.ExecType, execType)
                                                                       .add(FieldType.OrdStatus, orderStatus)
                                                                       .add(FieldType.Symbol, "VOD.L")
                                                                       .add(FieldType.Side, Side.BUY).add(FieldType.LeavesQty, new FixedPointNumber(LEAVES_QTY_LONG))
                                                                       .add(FieldType.CumQty, new FixedPointNumber(CUM_QTY_LONG));
    }
}
