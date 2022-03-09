package org.example;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws IOException {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://v3.football.api-sports.io/fixtures?season=2021&team=40&last=2")
                .get()
                .addHeader("x-apisports-key", "86d7d994c59c862221c9118242fc4808")
                .build();

        //zavolam API call
        Response response = client.newCall(request).execute();

        //dostanem response ako string
        String responseBody = Objects.requireNonNull(response.body()).string();

        //string si prevediem do JSON objektu
        JSONObject responseObj = new JSONObject(responseBody);

        //kedze su tie fixtures ulozene este v poli s nazvom response, musim este vyextrahovat ten response ako JSONArray kde sa uz nachadzaju jednotlive fixtures
        JSONArray arrayOfFixtures = responseObj.getJSONArray("response");

        //takto z toho JSON dostanem cez indexy uz jednotlive fixtures s ktorymi mozem dalej pracovat (aktualne ich tam mam dve, tzn index 0 a 1)
        //toto je ale len JSONObject celej jednej fixture, ale ta vnutri obsahuje dalsie JSONObjects ako su goals, teams atd...

        for (int i = 0; i < arrayOfFixtures.length(); i++) {
            JSONObject fixture = arrayOfFixtures.getJSONObject(i);

            System.out.println("fixture info " + fixture.getJSONObject("fixture"));
        }

    }
}
