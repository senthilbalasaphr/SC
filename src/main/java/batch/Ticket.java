package batch;

import java.io.*;
import java.net.*;
import java.util.*;
import org.json.*;

public class Ticket {
    private static final String API_KEY_FILE = "/Users/baps/Documents/Twillo/SC-SK/src/main/java/batch/apikey.txt";  // <-- File path
    private static final String BASE_URL = "https://api.polygon.io/v3/reference/tickers";
    private static final String OUTPUT_FILE = "/Users/baps/Documents/Twillo/SC-SK/src/main/java/batch/tickers.csv";

    public static void main(String[] args) {
        List<String> tickers = new ArrayList<>();
        String apiKey = readApiKey();
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("API key not found in " + API_KEY_FILE);
            return;
        }else{
            System.err.println("API key not found in " + apiKey);
        }

        String url = BASE_URL + "?market=stocks&active=true&limit=1000&apiKey=" + apiKey;

        try {
            while (url != null) {
                String jsonResponse = getHttpResponse(url);
                JSONObject json = new JSONObject(jsonResponse);

                JSONArray results = json.optJSONArray("results");
                if (results != null) {
                    for (int i = 0; i < results.length(); i++) {
                        JSONObject tickerObj = results.getJSONObject(i);
                        String ticker = tickerObj.optString("ticker");
                        String name = tickerObj.optString("name");
                        String safeName = name.replace(",", "\\,");
                        tickers.add(ticker + "," + safeName);
                    }
                }

                String nextUrl = json.optString("next_url", null);
                url = (nextUrl != null && !nextUrl.isEmpty())
                        ? nextUrl + "&apiKey=" + apiKey
                        : null;

                System.out.println("Fetched " + tickers.size() + " tickers so far...");
            }

            writeToFile(tickers);
            System.out.println("Saved " + tickers.size() + " tickers to " + OUTPUT_FILE);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String readApiKey() {
        try (BufferedReader reader = new BufferedReader(new FileReader(API_KEY_FILE))) {
            return reader.readLine().trim();
        } catch (IOException e) {
            return null;
        }
    }

    private static String getHttpResponse(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream())
        );

        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }

        reader.close();
        conn.disconnect();
        return response.toString();
    }

    private static void writeToFile(List<String> data) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(OUTPUT_FILE))) {
            writer.write("Symbol,Name\n");
            for (String line : data) {
                writer.write(line);
                writer.newLine();
            }
        }
    }

}
