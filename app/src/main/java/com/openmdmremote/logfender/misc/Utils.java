package com.openmdmremote.logfender.misc;

import java.util.ArrayList;

public class Utils {

    /** Formats given message to make it suitable for ingestion by Logentris endpoint.
     *  If isUsingHttp == true, the method produces such structure:
        {"Timestamp": 12345, "Message": "MESSAGE"}

     * @param message Message to be sent to Logentries
     *
     * @return
     */
    public static String formatMessage(String message) {
        StringBuilder sb = new StringBuilder();

        sb.append("{");

        long timestamp = System.currentTimeMillis(); // Current time in UTC in milliseconds.
        sb.append("\"Timestamp\": ").append(Long.toString(timestamp)).append(", ");

        sb.append("\"Message\": \"").append(message);
        sb.append("\"}");

        return sb.toString();
    }

    public static String[] splitStringToChunks(String source, int chunkLength) {
        if(chunkLength < 0) {
            throw new IllegalArgumentException("Chunk length must be greater or equal to zero!");
        }

        int srcLength = source.length();
        if(chunkLength == 0 || srcLength <= chunkLength) {
            return new String[] { source };
        }

        ArrayList<String> chunkBuffer = new ArrayList<String>();
        int splitSteps = srcLength / chunkLength + (srcLength % chunkLength > 0 ? 1 : 0);

        int lastCutPosition = 0;
        for(int i = 0; i < splitSteps; ++i) {

            if(i < splitSteps - 1) {
                // Cut out the chunk of the requested size.
                chunkBuffer.add(source.substring(lastCutPosition, lastCutPosition + chunkLength));
            }
            else
            {
                // Cut out all that left to the end of the string.
                chunkBuffer.add(source.substring(lastCutPosition));
            }

            lastCutPosition += chunkLength;
        }

        return chunkBuffer.toArray(new String[chunkBuffer.size()]);
    }
}
