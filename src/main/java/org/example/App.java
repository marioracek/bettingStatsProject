package org.example;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class App {

    public static int LAST_5 = 5;

    //TODO refactor method + create better printout (more structured)
    public static void main(String[] args) throws IOException {

        //Liverpool
        int teamId = 40;
        List<Integer> fixturesArray = getFixtureIds(teamId);

        for (int i = 0; i < fixturesArray.size(); i++) {
            System.out.println(getFixtureStatistics(teamId, fixturesArray.get(i)));
        }

    }

    //vrati pole statistik pre dany team a dany zapas
    //TODO treba toto dorobit tak, aby sa mi sem vlozil zakazdym aj ten druhy team a dostal som statistiky oboch teamov z danej fixture
    public static JSONArray getFixtureStatistics(int teamId, int fixtureId) throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder().url("https://v3.football.api-sports.io/fixtures/statistics?fixture=" + fixtureId + "&team=" + teamId + "").get().addHeader("x-apisports-key", "86d7d994c59c862221c9118242fc4808").build();

        //zavolam API call
        Response response = client.newCall(request).execute();

        //dostanem response ako string
        String responseBody = Objects.requireNonNull(response.body()).string();

        //string si prevediem do JSON objektu
        JSONObject responseObj = new JSONObject(responseBody);

        return responseObj.getJSONArray("response").getJSONObject(0).getJSONArray("statistics");
    }

    //metoda mi vravi vsetky fixture ids pre dany team za poslednych x zapasov
    public static List<Integer> getFixtureIds(int teamId) throws IOException {
        List<Integer> fixtureIds = new ArrayList<>();


        //toto je ale len JSONObject celej jednej fixture, ale ta vnutri obsahuje dalsie JSONObjects ako su goals, teams atd...
        for (int i = 0; i < LAST_5; i++) {
            JSONObject fixtureInfo = getTeamFixtures(teamId).getJSONObject(i);

            fixtureIds.add(fixtureInfo.getJSONObject("fixture").getInt("id"));
        }

        return fixtureIds;
    }

    //metoda berie na vstupe teamId a kolko poslednych zapasov chcem brat v uvahu a vracia JSONArray konkretnych fixtures
    public static JSONArray getTeamFixtures(int teamId) throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder().url("https://v3.football.api-sports.io/fixtures?season=2021&team=" + teamId + "&last=" + LAST_5 + "").get().addHeader("x-apisports-key", "86d7d994c59c862221c9118242fc4808").build();

        //zavolam API call
        Response response = client.newCall(request).execute();

        //dostanem response ako string
        String responseBody = Objects.requireNonNull(response.body()).string();

        //string si prevediem do JSON objektu
        JSONObject responseObj = new JSONObject(responseBody);

        //kedze su tie fixtures ulozene este v poli s nazvom response, musim este vyextrahovat ten response ako JSONArray kde sa uz nachadzaju jednotlive fixtures
        return responseObj.getJSONArray("response");
    }

}
