package ae.masdar.datamining;


import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Crawler {

    public static JSONObject getJson(String url) throws LimitExceededException {
        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpGet request = new HttpGet(url);
        HttpResponse response;
        try {
            response = httpclient.execute(request);
            switch(response.getStatusLine().getStatusCode()) {
                case 200:
                    BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                    String line = "";
                    StringBuilder resp = new StringBuilder();
                    while ((line = br.readLine()) != null) {
                        resp.append(line);
                    }
                    return new JSONObject(resp.toString());

                case 303 :
                    // alternatives available
                break;
                case 404 :
                    // word not found
                break;
                case 500 :
                    throw new LimitExceededException();

            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

}
