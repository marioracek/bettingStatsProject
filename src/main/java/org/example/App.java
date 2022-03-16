package org.example;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;

public class App {

    public static int LAST_1 = 1;
    public static int LAST_2 = 2;
    public static int LAST_3 = 3;
    public static int LAST_5 = 5;

    public static int LIVERPOOOL = 40;
    public static int ARSENAL = 42;

    //TODO refactor method + create better printout (more structured)
    public static void main(String[] args) throws IOException {
        getAverageCornerKicksAgainstTeam(LIVERPOOOL);
    }

    public static double getAverageCornerKicksByTeam(int teamId) throws IOException {
        double average = 0;

        JSONArray teamFixtures = getTeamFixtures(teamId);
        List<Integer> fixtureIds = getFixtureIds(teamFixtures);
        List<JSONArray> statistics = new ArrayList<>();
        System.out.println("Team id " + teamId + " fixtures " + fixtureIds);

        for (Integer integer : fixtureIds) {
            statistics.add(getFixtureStatistics(teamId, integer));
            System.out.println(statistics);
        }

        for (JSONArray statistic : statistics) {
            if (statistic.getJSONObject(7).get("value") != null) {
                average += statistic.getJSONObject(7).getDouble("value");
                System.out.println(average);
            }
        }
        System.out.println("Average corner kicks from team id " + teamId + " in last 3 games: " + average / statistics.size());
        return average / statistics.size();
    }

    public static double getAverageCornerKicksAgainstTeam(int teamId) throws IOException {
        double average = 0;

        //////////////////// teams ids of arsenal last 5 opponents
        JSONArray teamFixtures = getTeamFixtures(teamId);
        ArrayList<Integer> opponents = getTeamsIds(teamFixtures);
        List<Integer> fixtureIds = getFixtureIds(teamFixtures);
        List<JSONArray> statistics = new ArrayList<>();

        opponents.removeAll(Collections.singletonList(teamId));

        System.out.println("Team id " + teamId + " opponents " + opponents);
        System.out.println("Team id " + teamId + " fixtures " + fixtureIds);

        for (int i = 0; i < fixtureIds.size(); i++) {
            statistics.add(getFixtureStatistics(opponents.get(i), fixtureIds.get(i)));
            System.out.println(statistics);
        }

        for (JSONArray statistic : statistics) {
            if (statistic.getJSONObject(7).get("value") != null) {
                average += statistic.getJSONObject(7).getDouble("value");
                System.out.println(average);
            }
        }
        System.out.println("Average corner kicks against team id " + teamId + " in last 3 games: " + average / statistics.size());
        return average / statistics.size();
    }

    //vrati pole statistik pre dany team a dany zapas
    public static JSONArray getFixtureStatistics(int teamId, int fixtureId) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("https://v3.football.api-sports.io/fixtures/statistics?fixture=" + fixtureId + "&team=" + teamId + "").get().addHeader("x-apisports-key", "86d7d994c59c862221c9118242fc4808").build();
        Response response = client.newCall(request).execute();
        String responseBody = Objects.requireNonNull(response.body()).string();
        JSONObject responseObj = new JSONObject(responseBody);

        if (responseObj.getInt("results") == 0) {
            return new JSONArray();
        } else {
            return responseObj.getJSONArray("response").getJSONObject(0).getJSONArray("statistics");
        }
    }

    //2. call
    //metoda mi vravi vsetky fixture ids pre dany team za poslednych x zapasov
    public static ArrayList<Integer> getFixtureIds(JSONArray fixtures) throws IOException {
        ArrayList<Integer> fixtureIds = new ArrayList<>();

        for (int i = 0; i < LAST_3; i++) {
            JSONObject fixtureInfo = fixtures.getJSONObject(i);

            fixtureIds.add(fixtureInfo.getJSONObject("fixture").getInt("id"));
        }

        return fixtureIds;
    }

    //get teams ids from particular fixture
    public static ArrayList<Integer> getTeamsIds(JSONArray fixtures) throws IOException {
        ArrayList<Integer> teamsIds = new ArrayList<>();

        for (int i = 0; i < LAST_3; i++) {
            JSONObject fixtureInfo = fixtures.getJSONObject(i);

            teamsIds.add(fixtureInfo.getJSONObject("teams").getJSONObject("away").getInt("id"));
            teamsIds.add(fixtureInfo.getJSONObject("teams").getJSONObject("home").getInt("id"));
        }

        return teamsIds;
    }

    //1. call
    //metoda berie na vstupe teamId a kolko poslednych zapasov chcem brat v uvahu a vracia JSONArray konkretnych fixtures
    public static JSONArray getTeamFixtures(int teamId) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("https://v3.football.api-sports.io/fixtures?season=2021&team=" + teamId + "&last=" + LAST_3 + "").get().addHeader("x-apisports-key", "86d7d994c59c862221c9118242fc4808").build();
        Response response = client.newCall(request).execute();
        String responseBody = Objects.requireNonNull(response.body()).string();
        JSONObject responseObj = new JSONObject(responseBody);

        return responseObj.getJSONArray("response");
    }

}
