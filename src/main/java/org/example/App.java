package org.example;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.SQLOutput;
import java.text.DecimalFormat;
import java.util.*;

public class App {

    public static int LAST_1 = 1;
    public static int LAST_2 = 2;
    public static int LAST_3 = 3;
    public static int LAST_5 = 5;

    public static int LIVERPOOL = 40;
    public static int ARSENAL = 42;
    public static int EVERTON = 45;
    public static int NEWCASTLE = 34;
    public static int LEEDS = 63;
    public static int WOLVES = 39;

    //TODO refactor method + create better printout (more structured)
    public static void main(String[] args) throws IOException {

        //TODO prerobit metodu getAverageCornerKicksAgainstTeam po vzore getAverageStatisticsByTeam - tzn aby mi to ukazalo, kolko striel na branu bolo priemerne proti tomu teamu a kolko rohov sa kopalo priemerne proti tomu teamu
        //System.out.println("Average corner kicks against team " + WOLVES + " in last 3 games: " + getAverageCornerKicksAgainstTeam(WOLVES));
        getAverageStatisticsByTeam(LEEDS);
    }

    public static void getAverageStatisticsByTeam(int teamId) throws IOException {
        double cornerKicks = 0;
        double shots = 0;
        DecimalFormat df = new DecimalFormat("###.##");

        JSONArray teamFixtures = getTeamFixtures(teamId);
        List<Integer> fixtureIds = getFixtureIds(teamFixtures, teamId);
        List<JSONArray> statistics = new ArrayList<>();
        System.out.println("Team id " + teamId + " fixtures " + fixtureIds);

        for (Integer integer : fixtureIds) {
            statistics.add(getFixtureStatistics(teamId, integer));
        }

        for (JSONArray statistic : statistics) {
            if (statistic.getJSONObject(7).get("value") != null) {
                System.out.println("Strely celkovo: " + statistic.getJSONObject(2).getDouble("value"));
                System.out.println("Rohy: " + statistic.getJSONObject(7).getDouble("value"));
                System.out.println("Pomer strely na branu vs rohy: " + Double.parseDouble(df.format(statistic.getJSONObject(2).getDouble("value") / statistic.getJSONObject(7).getDouble("value"))));
                System.out.println("\n");
                cornerKicks += statistic.getJSONObject(7).getDouble("value");
                shots += statistic.getJSONObject(2).getDouble("value");
            }
        }


        double averageCorners = Double.parseDouble(df.format(cornerKicks / statistics.size()));
        double averageShots = Double.parseDouble(df.format(shots / statistics.size()));

        System.out.println("Priemer striel na branu: " + averageShots);
        System.out.println("Priemer rohov: " + averageCorners);
        System.out.println("Priemer pomeru striel na branu vs rohy: " + Double.parseDouble(df.format(averageShots / averageCorners)));
    }

    public static double getAverageCornerKicksAgainstTeam(int teamId) throws IOException {
        double average = 0;

        //////////////////// teams ids of arsenal last 5 opponents
        JSONArray teamFixtures = getTeamFixtures(teamId);
        ArrayList<Integer> opponents = getTeamsIds(teamFixtures);
        List<Integer> fixtureIds = getFixtureIds(teamFixtures, teamId);
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
        DecimalFormat df = new DecimalFormat("###.##");
        return Double.parseDouble(df.format(average / statistics.size()));
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
    public static ArrayList<Integer> getFixtureIds(JSONArray fixtures, int teamId) throws IOException {
        ArrayList<Integer> fixtureIds = new ArrayList<>();

        for (int i = 0; i < LAST_3; i++) {
            JSONObject fixtureInfo = fixtures.getJSONObject(i);
            if (fixtureInfo.getJSONObject("teams").getJSONObject("away").getInt("id") == teamId) {
                System.out.println("Proti teamu: " + fixtureInfo.getJSONObject("teams").getJSONObject("home").getInt("id"));
            } else {
                System.out.println("Proti teamu: " + fixtureInfo.getJSONObject("teams").getJSONObject("away").getInt("id"));
            }
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
