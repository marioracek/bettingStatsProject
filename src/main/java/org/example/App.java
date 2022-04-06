package org.example;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.*;

public class App {

    public static int PREMIER_LEAGUE = 39;
    public static int CHAMPIONS_LEAGUE = 2;

    public static int SEASON_2021 = 2021;


    public static void main(String[] args) throws IOException {
        //TODO prekladac na ligy podobne ako mam teamy
        //TODO skusit inkorporovat pocet a mena zranenych hracov
        //TODO skusit vypisovat kolko zapasov vyhral/prehral dany team
        //TODO urobit filter aby bolo mozne podla ligy (lokalna liga, liga majstrov, pohare)
        //TODO urobit filter proti konkretnemu teamu
        //TODO urobit filter proti roznym kategoriam teamu

        //getAverageCornerKicksAgainstTeam(BENFICA, SEASON_2021, 0);
        // if league is 0 then it means that all leagues are counted in
        getAverageStatisticsByTeam(40, SEASON_2021, 0, 3);
    }

    public static String getTeamName(String teamId) throws IOException {
        String file = "/Users/marioracek/IdeaProjects/bettingStatsProject/src/main/java/org/example/teams.json";
        String contents = new String(Files.readAllBytes(Paths.get(file)));

        JSONObject obj = new JSONObject(contents);
        if (obj.has(teamId)) {
            return obj.getJSONObject(teamId).get("name").toString();
        } else {
            return "No team found";
        }
    }

    public static void getAverageStatisticsByTeam(int teamId, int season, int league, int numberOfMatches) throws IOException {
        double cornerKicks = 0;
        double shots = 0;
        DecimalFormat df = new DecimalFormat("###.##");

        JSONArray teamFixtures = getTeamFixtures(teamId, season, league, numberOfMatches);
        List<Integer> fixtureIds = getFixtureIds(teamFixtures, numberOfMatches);
        ArrayList<Integer> opponents = getTeamsIds(teamFixtures, numberOfMatches);
        List<JSONArray> statistics = new ArrayList<>();
        ArrayList<String> homeOrAway = getHomeOrAwayStats(teamFixtures, teamId, numberOfMatches);

        opponents.removeAll(Collections.singletonList(teamId));

        System.out.println("Team id " + teamId + " fixtures " + fixtureIds);
        System.out.println("\n");

        for (Integer integer : fixtureIds) {
            statistics.add(getFixtureStatistics(teamId, integer));
        }

        for (int i = 0; i < numberOfMatches; i++) {
            System.out.println("Proti teamu: " + getTeamName(String.valueOf(opponents.get(i))));
            System.out.println(homeOrAway.get(i));
            if (statistics.get(i).getJSONObject(7).get("value") != null) {
                System.out.println("Strely: " + statistics.get(i).getJSONObject(2).getDouble("value"));
                System.out.println("Rohy: " + statistics.get(i).getJSONObject(7).getDouble("value"));
                System.out.println("\n");
                cornerKicks += statistics.get(i).getJSONObject(7).getDouble("value");
                shots += statistics.get(i).getJSONObject(2).getDouble("value");
            }
        }


        double averageCorners = Double.parseDouble(df.format(cornerKicks / statistics.size()));
        double averageShots = Double.parseDouble(df.format(shots / statistics.size()));

        System.out.println("Priemer striel na branu: " + averageShots);
        System.out.println("Priemer rohov: " + averageCorners);
    }

    public static void getAverageCornerKicksAgainstTeam(int teamId, int season, int league, int numberOfMatches) throws IOException {
        double cornerKicks = 0;
        double shots = 0;
        DecimalFormat df = new DecimalFormat("###.##");

        //////////////////// teams ids of arsenal last 5 opponents
        JSONArray teamFixtures = getTeamFixtures(teamId, season, league, numberOfMatches);
        ArrayList<Integer> opponents = getTeamsIds(teamFixtures, numberOfMatches);
        List<Integer> fixtureIds = getFixtureIds(teamFixtures, numberOfMatches);
        List<JSONArray> statistics = new ArrayList<>();

        opponents.removeAll(Collections.singletonList(teamId));

        System.out.println("Team id " + teamId + " fixtures " + fixtureIds);

        for (int i = 0; i < fixtureIds.size(); i++) {
            statistics.add(getFixtureStatistics(opponents.get(i), fixtureIds.get(i)));
        }

        for (int i = 0; i < numberOfMatches; i++) {
            System.out.println("Proti teamu: " + getTeamName(String.valueOf(opponents.get(i))));
            if (statistics.get(i).getJSONObject(7).get("value") != null) {
                System.out.println("Strely proti teamu: " + statistics.get(i).getJSONObject(2).getDouble("value"));
                System.out.println("Rohy proti teamu: " + statistics.get(i).getJSONObject(7).getDouble("value"));
                System.out.println("\n");
                cornerKicks += statistics.get(i).getJSONObject(7).getDouble("value");
                shots += statistics.get(i).getJSONObject(2).getDouble("value");
            }
        }
        double averageCorners = Double.parseDouble(df.format(cornerKicks / statistics.size()));
        double averageShots = Double.parseDouble(df.format(shots / statistics.size()));

        System.out.println("Priemer striel na branu proti teamu: " + averageShots);
        System.out.println("Priemer rohov proti teamu: " + averageCorners);
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
    public static ArrayList<Integer> getFixtureIds(JSONArray fixtures, int numberOfMatches) throws IOException {
        ArrayList<Integer> fixtureIds = new ArrayList<>();

        for (int i = 0; i < numberOfMatches; i++) {
            JSONObject fixtureInfo = fixtures.getJSONObject(i);
            fixtureIds.add(fixtureInfo.getJSONObject("fixture").getInt("id"));
        }

        return fixtureIds;
    }

    //get teams ids from particular fixture
    public static ArrayList<Integer> getTeamsIds(JSONArray fixtures, int numberOfMatches) throws IOException {
        ArrayList<Integer> teamsIds = new ArrayList<>();

        for (int i = 0; i < numberOfMatches; i++) {
            JSONObject fixtureInfo = fixtures.getJSONObject(i);

            teamsIds.add(fixtureInfo.getJSONObject("teams").getJSONObject("away").getInt("id"));
            teamsIds.add(fixtureInfo.getJSONObject("teams").getJSONObject("home").getInt("id"));
        }

        return teamsIds;
    }

    //get teams ids from particular fixture
    public static ArrayList<String> getHomeOrAwayStats(JSONArray fixtures, int teamId, int numberOfMatches) throws IOException {
        ArrayList<String> homeOrAway = new ArrayList<>();

        for (int i = 0; i < numberOfMatches; i++) {
            JSONObject fixtureInfo = fixtures.getJSONObject(i);

            if (fixtureInfo.getJSONObject("teams").getJSONObject("away").getInt("id") == teamId) {
                homeOrAway.add("away");
            }

            if (fixtureInfo.getJSONObject("teams").getJSONObject("home").getInt("id") == teamId) {
                homeOrAway.add("home");
            }
        }

        return homeOrAway;
    }

    //1. call
    //metoda berie na vstupe teamId a kolko poslednych zapasov chcem brat v uvahu a vracia JSONArray konkretnych fixtures
    public static JSONArray getTeamFixtures(int teamId, int season, int league, int numberOfMatches) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request;
        if (league == 0) {
            request = new Request.Builder().url("https://v3.football.api-sports.io/fixtures?season=" + season + "&team=" + teamId + "&status=FT&last=" + numberOfMatches + "").get().addHeader("x-apisports-key", "86d7d994c59c862221c9118242fc4808").build();
        } else {
            request = new Request.Builder().url("https://v3.football.api-sports.io/fixtures?league=" + league + "&season=" + season + "&team=" + teamId + "&status=FT&last=" + numberOfMatches + "").get().addHeader("x-apisports-key", "86d7d994c59c862221c9118242fc4808").build();
        }
        Response response = client.newCall(request).execute();
        String responseBody = Objects.requireNonNull(response.body()).string();
        JSONObject responseObj = new JSONObject(responseBody);

        return responseObj.getJSONArray("response");
    }

}
