package org.example;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Helper {

    public static List<Integer> getHeadToHeadFixtures(int teamOne, int teamTwo, int numberOfMatches) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("https://v3.football.api-sports.io/fixtures/headtohead?h2h=" + teamOne + "-" + teamTwo + "&last=" + numberOfMatches + "").get().addHeader("x-apisports-key", "86d7d994c59c862221c9118242fc4808").build();
        Response response = client.newCall(request).execute();
        String responseBody = Objects.requireNonNull(response.body()).string();
        JSONObject responseObj = new JSONObject(responseBody);

        return getFixtureIds(responseObj.getJSONArray("response"), numberOfMatches);

    }

    public static int getNumberOfTeamsMatches(int teamId, int season, int leagueId) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("https://v3.football.api-sports.io/teams/statistics?league=" + leagueId + "&season=" + season + "&team=" + teamId + "").get().addHeader("x-apisports-key", "86d7d994c59c862221c9118242fc4808").build();
        Response response = client.newCall(request).execute();
        String responseBody = Objects.requireNonNull(response.body()).string();
        JSONObject responseObj = new JSONObject(responseBody);
        return responseObj.getJSONObject("response").getJSONObject("fixtures").getJSONObject("played").getInt("total");
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

    //vrati pole statistik pre dany team a dany zapas
    public static JSONArray  getFixtureStatistics(int teamId, int fixtureId) throws IOException {
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
    public static List<Integer> getFixtureIds(JSONArray fixtures, int numberOfMatches) {
        ArrayList<Integer> fixtureIds = new ArrayList<>();

        for (int i = 0; i < numberOfMatches; i++) {
            JSONObject fixtureInfo = fixtures.getJSONObject(i);
            fixtureIds.add(fixtureInfo.getJSONObject("fixture").getInt("id"));
        }

        return fixtureIds;
    }

    //get teams ids from particular fixture
    public static List<Integer> getTeamsIds(JSONArray fixtures, int numberOfMatches) {
        ArrayList<Integer> teamsIds = new ArrayList<>();

        for (int i = 0; i < numberOfMatches; i++) {
            JSONObject fixtureInfo = fixtures.getJSONObject(i);

            teamsIds.add(fixtureInfo.getJSONObject("teams").getJSONObject("away").getInt("id"));
            teamsIds.add(fixtureInfo.getJSONObject("teams").getJSONObject("home").getInt("id"));
        }

        return teamsIds;
    }

    //get teams ids from particular fixture
    public static List<String> getHomeOrAwayStats(JSONArray fixtures, int teamId, int numberOfMatches) {
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

    //
    public static JSONArray getTeamFixturesByTablePosition(int teamId, JSONArray fixtures, List<Integer> listOfTeams) {
        JSONArray opponentsIds = new JSONArray();
        int opponent = 0;

        for (int i = 0; i < fixtures.length(); i++) {
            if (fixtures.getJSONObject(i).getJSONObject("teams").getJSONObject("home").getInt("id") == teamId) {
                opponent = fixtures.getJSONObject(i).getJSONObject("teams").getJSONObject("away").getInt("id");
            } else {
                opponent = fixtures.getJSONObject(i).getJSONObject("teams").getJSONObject("home").getInt("id");
            }

            if (listOfTeams.contains(opponent)) {
                opponentsIds.put(fixtures.getJSONObject(i));
            }
        }

        return opponentsIds;
    }
}
