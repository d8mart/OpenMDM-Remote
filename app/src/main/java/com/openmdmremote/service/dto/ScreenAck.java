package com.openmdmremote.service.dto;

public class ScreenAck extends Payload{
    public int sequenceNumber;
    public long timestamp;

    // set defaults
	public ScreenAck() {
        sequenceNumber = 0;
        timestamp = 0L;
	}

    public boolean isValid(){
		return sequenceNumber <= 255 && sequenceNumber >= 0;
	}
	
	@Override
	public String toString() {
		return "sequenceNumber: " + sequenceNumber + ", timestamp: " + timestamp;
	}
}
