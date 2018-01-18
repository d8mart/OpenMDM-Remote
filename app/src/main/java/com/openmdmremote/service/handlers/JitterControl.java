package com.openmdmremote.service.handlers;


import com.openmdmremote.service.handlers.interfaces.IScreenCap;

import java.util.Collections;
import java.util.LinkedList;

public class JitterControl {

    private final int MINFREQUNECY = 40;
    private final int MAXFREQUENCY = 1000;

    private final double CORRECTIONTRASHOLD_DOWN = 0.1;
    private final double CORRECTIONTRASHOLD_UP = 0.2;

    // Less is more aggressive.
    private final int CORRECTION_SAMPLERATE_COUNTER = 4;

    public enum FreqMove {
        MARAD, GYORSIT, LASSIT
    }
    FreqMove CorrectionWay;

    // It count how long did not happen slow-down. So shows the quality.
    private int freqUnchange;

    int width=0;
    int height=0;

    // Congestion handling.
    private int WINDOW = 1000; // db. Max un ACKed packages
    private int sent = 0; // Current ACKed packages

    // Iteration
    private byte seqNumber = 0; // max 255;
    private byte lastReceived = 0;

    private int frequency;
    FrequencyQueue ackedFreq = new FrequencyQueue(5);

    // Todo: use SparseArray
    ImagePacket sentImages[] = new ImagePacket[256];

    private volatile static IScreenCap screenCap;

    public JitterControl(IScreenCap screenCap) {
        this.screenCap = screenCap;
        reset();
        screenCap.updateFrequency(frequency);
    }

    public void reset() {
        seqNumber = 0;
        lastReceived = 0;
        // It must be like in the C source code.
        frequency = 500;
        freqUnchange = CORRECTION_SAMPLERATE_COUNTER-1;
        ackedFreq.reset();

        // If came new user resend the image.
        screenCap.checkDiff(false);
    }

    public synchronized void receivedAck(byte receivedSeq, long timestamp) {
        // Filter the first packages.
        if(timestamp != 0) {
            ackedFreq.add((int) timestamp, (receivedSeq & 0xff));
        }

        int median = ackedFreq.getMedian();

        // If the network is bad it will slow-down
        if( median > frequency+getJitterTolerance()) {
            frequency = (int) (median+(median*CORRECTIONTRASHOLD_DOWN));

            // Todo: should put back?
            /* ha gyorsitunk akkor addig nem nezunk ujra mediant amig le nem ackelodott a legelso,
             ezzel a parameterekkel rendelkezo kep
             ackedFreq.block(getNextSeq());
             */
            ackedFreq.block(getNextSeq());
            CorrectionWay = FreqMove.LASSIT;
            freqUnchange = 0;

            screenCap.checkDiff(true);
        } else {
            CorrectionWay = FreqMove.MARAD;

            // Relevant only when receive with new arguments.
            if(median != -1) {
                freqUnchange++;
            }
        }

        // If it looks good for a while it try incrase the speed.
        if(freqUnchange == CORRECTION_SAMPLERATE_COUNTER) {
            frequency = (int) (frequency-(frequency*CORRECTIONTRASHOLD_UP));
            freqUnchange = CORRECTION_SAMPLERATE_COUNTER -3;

            /* ha gyorsitunk akkor addig nem nezunk ujra mediant amig le nem ackelodott a legelso,
             ezzel a parameterekkel rendelkezo kep*/
            ackedFreq.block(getNextSeq());
            CorrectionWay = FreqMove.GYORSIT;
        }

        frequency = Math.max(frequency, MINFREQUNECY);
        frequency = Math.min(frequency, MAXFREQUENCY);


        if(CorrectionWay != FreqMove.MARAD) {
            screenCap.updateFrequency(frequency);
            WINDOW = 1500 / frequency;
        }

        sent--;
        sent = Math.min(0, sent); // Error handling

        while(lastReceived != receivedSeq) {
            lastReceived++;
        }
    }

    private int getJitterTolerance(){
        return (int) (frequency*0.2);
    }

    public synchronized int sendt(long utime, int size) {
        int seq = seqNumber & 0xFF;
        sentImages[seq] = new ImagePacket(seq, utime, size);
        seqNumber++;
        sent++;
        return seq;
    }

    // Just for the marked package.
    private int getNextSeq(){
        int seq = (seqNumber+2 & 0xFF);
        return seq;
    }

    public boolean channelIsFree() {
        if(sent <= WINDOW) {
            return true;
        } else {
            return false;
        }

    }

    private class ImagePacket {
        int seq;
        long utime;
        int size;

        public ImagePacket(int seq, long utime, int size) {
            this.seq = seq;
            this.utime = utime;
            this.size = size;
        }
    }

    public class FrequencyQueue {
        private boolean blocked = false;
        private int unblocker = -1;

        private int limit;
        private LinkedList<Integer> list = new LinkedList<Integer>();

        public FrequencyQueue(int limit) {
            this.limit = limit;
        }

        public boolean add(Integer o, int seq) {
            if(blocked && seq == unblocker) {
                blocked = false;
            }

            if(blocked) {
                return false;
            }

            if(list.size() == limit) {
                list.remove();
            }
            list.add(o);
            return true;
        }

        // Until block the add function while the disabled is true.
        // Re enable by the seq number.
        public void block(int seqnum) {
            unblocker = seqnum;
            blocked = true;
            list.clear();
        }

        public int getMedian() {
            if(list.size()<5) {
                return -1;
            }

            LinkedList<Integer> m = new LinkedList<Integer>(list);
            Collections.sort(m);

            int middle = m.size() / 2;
            if (m.size()%2 == 1) {
                return m.get(middle);
            } else {
                return (m.get(middle-1)+m.get(middle)) / 2;
            }
        }

        @Override
        public String toString() {
            String r = "[ ";
            for(Integer i : list){
                r += i+", ";
            }
            return r+="]";
        }

        public void reset() {
            list.clear();
            blocked = false;
            unblocker = -1;
        }
    }

    public void setResolution(int screenX, int screenY) {
        width = screenX;
        height = screenY;
        // If the resolution has change the backend switch off the diff check and it send a new image.
        screenCap.updateScreenResolution(width, height);
    }
}
