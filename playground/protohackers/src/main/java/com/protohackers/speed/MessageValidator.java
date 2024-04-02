package com.protohackers.speed;

public class MessageValidator {
    public static boolean isClientMessageValid(Message msg, ServerThread.ThreadMode threadMode, boolean wantHeartbeat) {
        if (msg == null) return false;
        // TICKET or HEARTBEAT messages can only be sent by the server
        if (msg.getType() == Message.MessageType.TICKET || msg.getType() == Message.MessageType.HEARTBEAT) return false;
        if (msg.getType() == Message.MessageType.I_AM_CAMERA && threadMode != ServerThread.ThreadMode.UNKNOWN)
            return false;
        if (wantHeartbeat && msg.getType() == Message.MessageType.WANT_HEARTBEAT) return false;
        if (msg.getType() == Message.MessageType.TICKET &&
                (msg.getTimestamp1() > msg.getTimestamp2() || msg.getMile1() > msg.getMile2()) )
            return false;
        if (msg.getType() == Message.MessageType.PLATE && threadMode != ServerThread.ThreadMode.CAMERA) return false;

        return true;
    }
}
