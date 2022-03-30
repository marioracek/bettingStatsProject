package org.example;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
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
    public static int SOUTHAMPTON = 41;

    public static int PREMIER_LEAGUE = 39;
    public static int CHAMPIONS_LEAGUE = 39;

    public static int SEASON_2021 = 2021;


    public static void main(String[] args) throws IOException {


        //TODO skusit inkorporovat pocet a mena zranenych hracov
        //TODO skusit vypisovat kolko zapasov vyhral/prehral dany team
        //TODO cisleniky aby som zadal teamId a hodi mi to nazov teamu a opacne zadam team a hodi mi to teamId
        getAverageCornerKicksAgainstTeam(SOUTHAMPTON, SEASON_2021, PREMIER_LEAGUE);
        getAverageStatisticsByTeam(LEEDS, SEASON_2021, PREMIER_LEAGUE);
    }

    public static void getAverageStatisticsByTeam(int teamId, int season, int league) throws IOException {
        double cornerKicks = 0;
        double shots = 0;
        DecimalFormat df = new DecimalFormat("###.##");

        JSONArray teamFixtures = getTeamFixtures(teamId, season, league);
        List<Integer> fixtureIds = getFixtureIds(teamFixtures, teamId);
        List<JSONArray> statistics = new ArrayList<>();
        System.out.println("Team id " + teamId + " fixtures " + fixtureIds);

        ArrayList<Integer> opponents = getTeamsIds(teamFixtures);
        opponents.removeAll(Collections.singletonList(teamId));

        for (Integer integer : fixtureIds) {
            statistics.add(getFixtureStatistics(teamId, integer));
        }

        for (int i = 0; i < LAST_3; i++) {
            if (statistics.get(i).getJSONObject(7).get("value") != null) {
                System.out.println("Strely celkovo: " + statistics.get(i).getJSONObject(2).getDouble("value"));
                System.out.println("Rohy: " + statistics.get(i).getJSONObject(7).getDouble("value"));
                System.out.println("Pomer strely na branu vs rohy: " + Double.parseDouble(df.format(statistics.get(i).getJSONObject(2).getDouble("value") / statistics.get(i).getJSONObject(7).getDouble("value"))));
                System.out.println("\n");
                cornerKicks += statistics.get(i).getJSONObject(7).getDouble("value");
                shots += statistics.get(i).getJSONObject(2).getDouble("value");
            }
        }


        double averageCorners = Double.parseDouble(df.format(cornerKicks / statistics.size()));
        double averageShots = Double.parseDouble(df.format(shots / statistics.size()));

        System.out.println("Priemer striel na branu: " + averageShots);
        System.out.println("Priemer rohov: " + averageCorners);
        System.out.println("Priemer pomeru striel na branu vs rohy: " + Double.parseDouble(df.format(averageShots / averageCorners)));
    }

    public static void getAverageCornerKicksAgainstTeam(int teamId, int season, int league) throws IOException {
        double cornerKicks = 0;
        double shots = 0;
        DecimalFormat df = new DecimalFormat("###.##");

        //////////////////// teams ids of arsenal last 5 opponents
        JSONArray teamFixtures = getTeamFixtures(teamId, season, league);
        ArrayList<Integer> opponents = getTeamsIds(teamFixtures);
        List<Integer> fixtureIds = getFixtureIds(teamFixtures, teamId);
        List<JSONArray> statistics = new ArrayList<>();

        opponents.removeAll(Collections.singletonList(teamId));

        System.out.println("Team id " + teamId + " fixtures " + fixtureIds);

        for (int i = 0; i < fixtureIds.size(); i++) {
            statistics.add(getFixtureStatistics(opponents.get(i), fixtureIds.get(i)));
        }

        for (int i = 0; i < LAST_3; i++) {
            System.out.println("Proti teamu: " + opponents.get(i));
            if (statistics.get(i).getJSONObject(7).get("value") != null) {
                System.out.println("Strely celkovo: " + statistics.get(i).getJSONObject(2).getDouble("value"));
                System.out.println("Rohy: " + statistics.get(i).getJSONObject(7).getDouble("value"));
                System.out.println("Pomer strely na branu vs rohy: " + Double.parseDouble(df.format(statistics.get(i).getJSONObject(2).getDouble("value") / statistics.get(i).getJSONObject(7).getDouble("value"))));
                System.out.println("\n");
                cornerKicks += statistics.get(i).getJSONObject(7).getDouble("value");
                shots += statistics.get(i).getJSONObject(2).getDouble("value");
            }
        }
        double averageCorners = Double.parseDouble(df.format(cornerKicks / statistics.size()));
        double averageShots = Double.parseDouble(df.format(shots / statistics.size()));

        System.out.println("Priemer striel na branu proti teamu: " + averageShots);
        System.out.println("Priemer rohov proti teamu: " + averageCorners);
        System.out.println("Priemer pomeru striel na branu vs rohy proti teamu: " + Double.parseDouble(df.format(averageShots / averageCorners)));
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
    public static JSONArray getTeamFixtures(int teamId, int season, int league) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("https://v3.football.api-sports.io/fixtures?league=" + league + "&season=" + season + "&team=" + teamId + "&last=" + LAST_3 + "").get().addHeader("x-apisports-key", "86d7d994c59c862221c9118242fc4808").build();
        Response response = client.newCall(request).execute();
        String responseBody = Objects.requireNonNull(response.body()).string();
        JSONObject responseObj = new JSONObject(responseBody);

        return responseObj.getJSONArray("response");
    }

}
