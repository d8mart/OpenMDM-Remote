package com.openmdmremote.logfender.logger;

import android.util.Log;

import com.openmdmremote.logfender.misc.Utils;
import com.openmdmremote.logfender.net.LogentriesClient;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class AsyncLoggingWorker {

	/*
	 * Constants
	 */

    private static final String TAG = "Webkey-logfender";

    private static final int RECONNECT_WAIT = 100; // milliseconds.
    private static final int MAX_QUEUE_POLL_TIME = 1000; // milliseconds.
    /** Size of the internal event queue. */
    private static final int QUEUE_SIZE = 32768;
    /** Limit on individual log length ie. 2^16*/
    public static final int LOG_LENGTH_LIMIT = 65536;

    private static final int MAX_NETWORK_FAILURES_ALLOWED = 3;
    private static final int MAX_RECONNECT_ATTEMPTS = 3;

    /** Error message displayed when queue overflow occurs */
    private static final String QUEUE_OVERFLOW = "Logentries Buffer Queue Overflow. Message Dropped!";

    /** Indicator if the socket appender has been started. */
    private boolean started = false;

    /** Asynchronous socket appender. */
    private SocketAppender appender;

    /** Message queue. */
    private ArrayBlockingQueue<String> queue;

    public AsyncLoggingWorker(String token, String host, int port, boolean secure) throws IOException {

        queue = new ArrayBlockingQueue<String>(QUEUE_SIZE);
        appender = new SocketAppender(token, host, port, secure);
        appender.start();
        started = true;
    }

    public void addLineToQueue(String line) {

        // Check that we have all parameters set and socket appender running.
        if (!this.started) {

            appender.start();
            started = true;
        }

        if (line.length() > LOG_LENGTH_LIMIT) {
            for(String logChunk: Utils.splitStringToChunks(line, LOG_LENGTH_LIMIT)) {
                tryOfferToQueue(logChunk);
            }

        } else {
            tryOfferToQueue(line);
        }
    }

    /**
     * Stops the socket appender. queueFlushTimeout (if greater than 0) sets the maximum timeout in milliseconds for
     * the message queue to be flushed by the socket appender, before it is stopped. If queueFlushTimeout
     * is equal to zero - the method will wait until the queue is empty (which may be dangerous if the
     * queue is constantly populated by another thread mantime.
     *
     * @param queueFlushTimeout - max. wait time in milliseconds for the message queue to be flushed.
     */
    public void close(long queueFlushTimeout) {
        if(queueFlushTimeout < 0) {
            throw new IllegalArgumentException("queueFlushTimeout must be greater or equal to zero");
        }

        long now = System.currentTimeMillis();

        while(!queue.isEmpty())
        {
            if(queueFlushTimeout != 0) {
                if(System.currentTimeMillis() - now >= queueFlushTimeout) {
                    // The timeout expired - need to stop the appender.
                    break;
                }
            }
        }
        appender.interrupt();
        started = false;
    }

    public void close() {
        close(0);
    }

    private void tryOfferToQueue(String line) throws RuntimeException {
        if(!queue.offer(line)) {
            Log.e(TAG, "The queue is full - will try to drop the oldest message in it.");
            queue.poll();
            /*
            FIXME: This code migrated from LE Java Library; currently, there is no a simple
            way to backup the queue in case of overflow due to requirements to max.
            memory consumption and max. possible size of the local logs storage. If use
            the local storage - the we have three problems: 1) Correct joining of logs from
            the queue and from the local storage (and we need some right event to trigger this joining);
            2) Correct order of logs after joining; 3) Data consistence problem, because we're
            accessing the storage from different threads, so sync. logic will increase overall
            complexity of the code. So, for now this logic is left AS IS, due to relatively
            rareness of the case with queue overflow.
             */

            if(!queue.offer(line)) {
                throw new RuntimeException(QUEUE_OVERFLOW);
            }
        }
    }

    private class SocketAppender extends Thread {

        // Formatting constants
        private static final String LINE_SEP_REPLACER = "\u2028";

        private final String host;
        private final int port;
        private final boolean secure;

        private LogentriesClient leClient;

        private String token;

        public SocketAppender(String token, String host, int port, boolean secure) {
            super("Logentries Socket appender");

            // Don't block shut down
            setDaemon(true);
            this.token = token;
            this.host = host;
            this.port = port;
            this.secure = secure;
        }

        private void openConnection() throws IOException, InstantiationException {
            if(leClient == null){
                leClient = new LogentriesClient(token, host, port, secure);
            }

            leClient.connect();
        }

        private boolean reopenConnection(int maxReConnectAttempts) throws InterruptedException, InstantiationException {
            if(maxReConnectAttempts < 0) {
                throw new IllegalArgumentException("maxReConnectAttempts value must be greater or equal to zero");
            }

            for(int attempt = 0; attempt < maxReConnectAttempts; ++attempt) {
                try {

                    openConnection();
                    return true;

                } catch (IOException e) {
                    // Ignore the exception and go for the next
                    // iteration.
                }

                Thread.sleep(RECONNECT_WAIT);
            }

            return false;
        }

        @Override
        public void run() {
            try {

                // Open connection
                reopenConnection(MAX_RECONNECT_ATTEMPTS);

                int numFailures = 0;
                boolean connectionIsBroken = false;
                String message = null;

                // Send data in queue
                while(true) {

                    // Try to take data from the queue if there are no logs from
                    // the local storage left to transportMessage.
                    message = queue.poll(MAX_QUEUE_POLL_TIME, TimeUnit.MILLISECONDS);
                    // Send data, reconnect if needed.
                    while (true) {

                        try {
                            if(message != null) {
                                this.leClient.write(Utils.formatMessage(message.replace("\n", LINE_SEP_REPLACER)));
                                message = null;
                            }

                        } catch (IOException e) {

                            if(numFailures >= MAX_NETWORK_FAILURES_ALLOWED) {
                                connectionIsBroken = true; // Have tried to reconnect for MAX_NETWORK_FAILURES_ALLOWED
                                                           // times and failed, so assume, that we have no link to the
                                                           // server at all...
                                message = null;
                            } else {
                                ++numFailures;

                                // Try to re-open the lost connection.
                                reopenConnection(MAX_RECONNECT_ATTEMPTS);
                            }

                            continue;
                        }

                        break;
                    }
                }
            } catch (InterruptedException e) {
                // We got interrupted, stop.

            } catch (InstantiationException e) {
                Log.e(TAG, "Cannot instantiate LogentriesClient due to improper configuration. Error: " + e.getMessage());
            }
        }
    }

}
