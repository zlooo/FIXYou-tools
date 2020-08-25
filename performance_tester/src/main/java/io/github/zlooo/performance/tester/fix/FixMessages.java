package io.github.zlooo.performance.tester.fix;

import io.github.zlooo.fixyou.parser.model.CharField;
import io.github.zlooo.fixyou.parser.model.CharSequenceField;
import io.github.zlooo.fixyou.parser.model.DoubleField;
import io.github.zlooo.fixyou.parser.model.FixMessage;
import lombok.experimental.UtilityClass;
import quickfix.Message;
import quickfix.SessionID;
import quickfix.field.*;
import quickfix.fix50sp2.ExecutionReport;
import quickfix.fix50sp2.NewOrderSingle;
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

    public static FixMessage  toExecutionReport(FixMessage fixMessage, char[] executionId, char execType, char orderStatus, char[] orderId) {
        final CharSequenceField clordIdField = fixMessage.getField(FixConstants.CLORD_ID_FIELD_NUMBER);
        final int clordidLength = clordIdField.getValue().length();
        final char[] clordIdAsCharArr = clordIdField.getUnderlyingValue();
        fixMessage.resetAllDataFieldsAndReleaseByteSource();
        fixMessage.<CharSequenceField>getField(FixConstants.CLORD_ID_FIELD_NUMBER).setValue(clordIdAsCharArr, clordidLength);
        fixMessage.<CharSequenceField>getField(io.github.zlooo.fixyou.FixConstants.MESSAGE_TYPE_FIELD_NUMBER).setValue(io.github.zlooo.performance.tester.fix.FixConstants.EXECUTION_REPORT);
        fixMessage.<CharSequenceField>getField(io.github.zlooo.performance.tester.fix.FixConstants.ORDER_ID_FIELD_NUMBER).setValue(orderId);
        fixMessage.<CharSequenceField>getField(io.github.zlooo.performance.tester.fix.FixConstants.EXEC_ID_FIELD_NUMBER).setValue(executionId);
        fixMessage.<CharField>getField(io.github.zlooo.performance.tester.fix.FixConstants.EXEC_TYPE_FIELD_NUMBER).setValue(execType);
        fixMessage.<CharField>getField(io.github.zlooo.performance.tester.fix.FixConstants.ORD_STATUS_FIELD_NUMBER).setValue(orderStatus);
        fixMessage.<CharSequenceField>getField(io.github.zlooo.performance.tester.fix.FixConstants.SYMBOL_FIELD_NUMBER).setValue(VODL);
        fixMessage.<CharField>getField(io.github.zlooo.performance.tester.fix.FixConstants.SIDE_FIELD_NUMBER).setValue('1');
        fixMessage.<DoubleField>getField(io.github.zlooo.performance.tester.fix.FixConstants.LEAVES_QTY_FIELD_NUMBER).setValue(LEAVES_QTY_LONG, (short) 0);
        fixMessage.<DoubleField>getField(io.github.zlooo.performance.tester.fix.FixConstants.CUM_QTY_FIELD_NUMBER).setValue(CUM_QTY_LONG, (short) 0);
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
}
