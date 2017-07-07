package vw.be.server.verticle;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;

public class ManageUserApplicationLoadTest implements Runnable {

    public static void main(String[] args) {
        int i = 0;
        while (i < 5) {
            Thread t = new Thread(new ManageUserApplicationLoadTest());
            t.start();
            i++;
        }
    }

    private void sendGet() throws Exception {

        String url = "http://localhost:23002/api/users/";

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        int responseCode = con.getResponseCode();

        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result
        System.out.println(response.toString());
    }

    @Override
    public void run() {
        LocalDateTime after30seconds = LocalDateTime.now().plusSeconds(30);
        while (LocalDateTime.now().isBefore(after30seconds)) {
            try {
                sendGet();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
