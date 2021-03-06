package org.wikipedia.translation;

import android.os.AsyncTask;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Created by aman_ on 3/29/2018.
 * Purpose: The class makes a call to the Yandex API to receieve translations for highlighted text within articles
 *
 */

// The class needs to extend Asynctask as it runs in a new thread
public class Translator extends AsyncTask <String, Void, String> {

    private final String KEY;
    private final String PATH;
    private final String CHARSET;

    public Translator() {
        KEY = "trnsl.1.1.20180329T182936Z.7a2ea55bab627000.b0c9cc1bb2ba24da663ab45d7311ac374dfa3a41";
        PATH = "https://translate.yandex.net/api/v1.5/tr.json/translate";
        CHARSET = StandardCharsets.UTF_8.name();
    }

    // calls to the Yandex API are made within this method
    @Override
    protected String doInBackground(String... strings) {
        String lang = strings[1] +"-"+strings[2];
        String text = strings[0];
        String urlParameters = "key="+KEY+"&text="+text+"&lang="+lang;
        BufferedReader reader = null;
        String response;
        String result = "";

        try {
            byte[] postData = urlParameters.getBytes(CHARSET);
            int postDataLength = postData.length;
            String request = PATH;
            URL url = new URL(request);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setInstanceFollowRedirects(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("charset", "utf-8");
            conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
            conn.setUseCaches(false);
            try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
                wr.write(postData);

            }

            int statusCode = conn.getResponseCode();

            if (statusCode ==  200) {
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line = null;

                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                response = sb.toString();
                JSONObject jsonObject = new JSONObject(response);

                result = jsonObject.getJSONArray("text").getString(0);

            }

        } catch (Exception e) {
            return "-1";
        }

        return result;
    }

    // Grabs the result from doInBackground and returns it to the caller
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
    }
}
