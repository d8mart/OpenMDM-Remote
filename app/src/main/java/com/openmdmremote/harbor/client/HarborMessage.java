package com.openmdmremote.harbor.client;

import com.google.protobuf.ByteString;

public class HarborMessage {

    public enum MsgType { BYTE, STRING };

    private final String conntrackID;
    private byte[] data;
    private String msg;

    private MsgType msgType;

    private ByteString byteString;
    private byte[] datas2;
    String str1;
    String str2;

    public HarborMessage(String conntrackID, byte[] data) {
        this.conntrackID = conntrackID;
        this.data = data;
        msgType = MsgType.BYTE;
    }

    public HarborMessage(String conntrackID, String msg) {
        this.conntrackID = conntrackID;
        this.msg = msg;
        msgType = MsgType.STRING;
    }

    public String getConntrackID() {
        return conntrackID;
    }

    public MsgType getMsgType() {
        return msgType;
    }

    public String getStringMsg() {
        return msg;
    }

    public ByteString getByteMsg() {
        byteString = ByteString.copyFrom(data);
        datas2 = byteString.toByteArray(); // convierto el bytestring en array de bytes otra vez
        str1 = byteString.toString();
        str2 = byteString.toStringUtf8();
        return ByteString.copyFrom(data);
    }
}
