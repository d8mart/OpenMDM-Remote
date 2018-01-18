package com.openmdmremote.harbor.client;

import com.openmdmremote.BuildConfig;
import com.openmdmremote.harbor.HRPCProto;

import static com.openmdmremote.harbor.HRPCProto.Message.Type.AUTHREQUEST;
import static com.openmdmremote.harbor.HRPCProto.Message.Type.DEVICEINFOS;
import static com.openmdmremote.harbor.HRPCProto.Message.Type.REGISTRATION;
import static com.openmdmremote.harbor.HRPCProto.Message.Type.REMOTEAUTH;
import static com.openmdmremote.harbor.HRPCProto.Message.Type.TRANSPORT;
import static com.openmdmremote.harbor.HRPCProto.Message.newBuilder;

public class MessageBuilder {

    public static HRPCProto.Message getAuthRequestMessage(AuthCredentials authCredentials) {
        HRPCProto.AuthRequest.Builder builder = HRPCProto.AuthRequest.newBuilder()
                .setDeviceid(authCredentials.getDeviceToken())
                .setVersion(Integer.toString(BuildConfig.VERSION_CODE));

        HRPCProto.AuthRequest authRequest = builder.build();

        return newBuilder()
                .setType(AUTHREQUEST)
                .setAuthrequest(authRequest)
                .build();
    }

    public static HRPCProto.Message getRegistrationRequest(AuthCredentials authCredentials) {
        HRPCProto.Registration.Builder builder = HRPCProto.Registration.newBuilder()
                .setWkversion(Integer.toString(BuildConfig.VERSION_CODE))
                .setAccountid(authCredentials.getAccountID())
                .setSerialnumber(authCredentials.getSerial());

        HRPCProto.Registration registration = builder.build();

        return newBuilder()
                .setType(REGISTRATION)
                .setRegistration(registration)
                .build();
    }

    public static HRPCProto.Message getRemoteAuthRequestMessage(String session, int visitorID) {
        HRPCProto.RemoteAuth.AuthRequest.Builder request = HRPCProto.RemoteAuth.AuthRequest.newBuilder()
                .setToken(session)
                .setVisitorid(visitorID);

        HRPCProto.RemoteAuth.Builder remoteAuthMsg = HRPCProto.RemoteAuth.newBuilder()
                .setType(HRPCProto.RemoteAuth.Type.AUTHREQUEST)
                .setAuthrequest(request);

        return newBuilder()
                .setType(REMOTEAUTH)
                .setRemoteauth(remoteAuthMsg)
                .build();
    }

    public static HRPCProto.Message getTransportMessage(HarborMessage harborMessage) {
        HRPCProto.TransportPKG.Builder authRequest = HRPCProto.TransportPKG.newBuilder()
                .setConntrackid(harborMessage.getConntrackID());

        switch (harborMessage.getMsgType()) {
            case STRING:
                authRequest.setMsgjson(harborMessage.getStringMsg());
                break;
            case BYTE:
                authRequest.setMsgbyte(harborMessage.getByteMsg()); //convierte los datos tipo byte de harbormessage en ByteString de protobuf
                break;
        }

        return newBuilder()
                .setType(TRANSPORT)
                .setTransportpkg(authRequest.build())
                .build();
    }

    public static HRPCProto.Message getDeviceInfosMessage(DeviceInfos deviceInfos) {
        HRPCProto.DeviceInfos.Builder builder = HRPCProto.DeviceInfos.newBuilder()
                .setAndroidversion(deviceInfos.getAndroidVersion())
                .setAndroidID(deviceInfos.getAndroidID())
                .setSdklevel(deviceInfos.getSdklevel())
                .setRooted(deviceInfos.isRooted())
                .setDevicemodel(deviceInfos.getModel())
                .setDevicebrand(deviceInfos.getBrand());

        if(deviceInfos.hasLocation()) {
            builder.setLocation(deviceInfos.getLocation());
        }

        HRPCProto.DeviceInfos hDeviceInfos = builder.build();

        return newBuilder()
                .setType(DEVICEINFOS)
                .setDeviceinfos(hDeviceInfos)
                .build();
    }
}
