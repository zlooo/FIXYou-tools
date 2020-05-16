package pl.zlooo.fixyou.performance.tester.quickfix;

import lombok.SneakyThrows;
import pl.zlooo.fixyou.performance.tester.fix.FixMessages;
import quickfix.*;
import quickfix.field.ClOrdID;
import quickfix.field.ExecType;
import quickfix.field.OrdStatus;

public class NewOrderSindleReceivingApplication implements Application {

    private int executionId = 1;
    private int orderId = 1;

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

    @SneakyThrows
    @Override
    public void fromApp(Message message, SessionID sessionId) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
        final String clordid = message.getString(ClOrdID.FIELD);
        Session.sendToTarget(FixMessages.createExecutionReport(clordid, executionId++, ExecType.PENDING_NEW, OrdStatus.PENDING_NEW, orderId++), sessionId);
        Session.sendToTarget(FixMessages.createExecutionReport(clordid, executionId++, ExecType.NEW, OrdStatus.NEW, orderId), sessionId);
        Session.sendToTarget(FixMessages.createExecutionReport(clordid, executionId++, ExecType.FILL, OrdStatus.FILLED, orderId), sessionId);
    }
}
