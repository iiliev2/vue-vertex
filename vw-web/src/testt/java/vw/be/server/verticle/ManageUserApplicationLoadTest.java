package testt.java.vw.be.server.verticle;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicLong;

public class ManageUserApplicationLoadTest implements Runnable {
    private static final String url = "http://localhost:23002/api/users/";
    private static final String reportFormat = "[Total %s/%ss] [Average %s/s] [Recent throughput %s/s]";
    private static final int threadsCount = Runtime.getRuntime().availableProcessors() * 2 - 1;
    private static final long reportDelay = 500;
    private static final long updateDelay = Math.round(Math.max(reportDelay * 0.75, 1.0));
    // each thread will log here how many requests were successfully
    // completed without errors
    private static AtomicLong totalRequestsProcessed = new AtomicLong(0);

    public static void main(String[] args) throws IOException {
        System.out.println("Kill to quit...");
        long i = 0;

        while (i++ < threadsCount) new Thread(new ManageUserApplicationLoadTest()).start();

        new Thread(() -> {
            long requests,
                    previousRequests = 0,
                    startTime = System.currentTimeMillis(),
                    reportedAt = startTime,
                    lastReportedAt = reportedAt,
                    average, recentThroughput;
            double elapsedTime;
            while (true) {
                try {
                    Thread.sleep(reportDelay);
                } catch (InterruptedException e) {
                }
                synchronized (totalRequestsProcessed) {
                    requests = totalRequestsProcessed.get();
                    reportedAt = System.currentTimeMillis();

                    recentThroughput = (long) ((requests - previousRequests) * 1000.0) / (reportedAt - lastReportedAt);
                    previousRequests = requests;
                    lastReportedAt = reportedAt;

                    elapsedTime = (reportedAt - startTime) / 1000.0;

                    average = Math.round(requests / elapsedTime);

                    report(String.format(reportFormat, requests, elapsedTime, average, recentThroughput));
                }
            }
        }).start();
    }

    private static void report(String msg) {
        StringBuilder report =
                new StringBuilder()
                        .append('\r')
                        .append(msg);

        System.out.print(report);
        System.out.flush();
    }

    private int sendGet() throws Exception {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");
        int result = con.getResponseCode();

        // closes the underlying socket, else we get resource leak and
        // are unable to properly fire next requests
        con.getInputStream().close();

        con.disconnect();
        return result;
    }

    @Override
    public void run() {
        long atStart = System.currentTimeMillis();
        long requestsProcessed = 0;

        while (true) {
            try {
                if (sendGet() == 200) requestsProcessed++;
            } catch (Exception e) {
            }
            if (System.currentTimeMillis() > atStart + updateDelay) {
                atStart += updateDelay;
                totalRequestsProcessed.addAndGet(requestsProcessed);
                requestsProcessed = 0;
            }
        }
    }
}
