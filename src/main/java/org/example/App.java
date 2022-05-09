package org.example;

import org.json.JSONArray;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

import static org.example.Helper.*;

public class App {

    public static final String LAST_5_CORNERS = "Priemer rohov za poslednych 5 zapasov: ";
    public static final String LAST_5_SHOTS = "Priemer striel za poslednych 5 zapasov: ";

    public static final String LAST_10_CORNERS = "Priemer rohov za poslednych 10 zapasov: ";
    public static final String LAST_10_SHOTS = "Priemer striel za poslednych 10 zapasov: ";

    public static final int PREMIER_LEAGUE = 39;
    public static final int CHAMPIONS_LEAGUE = 2;
    public static final int LEAGUE_CUP = 48;
    public static final int FA_CUP = 45;


    public static final int SEASON_2021 = 2021;

    static List<Integer> bigSix = Arrays.asList(40, 42, 33, 47, 49, 50);

    public static void main(String[] args) throws IOException {
        //TODO prekladac na ligy podobne ako mam teamy
        //TODO skusit inkorporovat pocet a mena zranenych hracov
        //TODO skusit vypisovat kolko zapasov vyhral/prehral dany team
        //TODO urobit filter proti roznym kategoriam teamu (big 4, last 5, middle table)
        //TODO vypisovat kolko rohov dava dany team doma a kolko vonku

        // if league is 0 then it means that all leagues are counted in

        JSONArray teamFixtures = getTeamFixtures(39, 2021, PREMIER_LEAGUE, getNumberOfTeamsMatches(39, 2021, PREMIER_LEAGUE));
        //JSONArray teamFixturesBigSix = getTeamFixturesByTablePosition(40, getTeamFixtures(40, 2021, 39, getNumberOfTeamsMatches(40, 2021, 39)), bigSix);

        getAverageStatisticsByTeam(39, 2021, PREMIER_LEAGUE, teamFixtures.length(), teamFixtures);
        getAverageStatisticsAgainstTeam(44, SEASON_2021, PREMIER_LEAGUE, getNumberOfTeamsMatches(44, SEASON_2021, PREMIER_LEAGUE));

        getHeadToHeadStatistics(39, 44, 4);

    }

    public static void getHeadToHeadStatistics(int teamOne, int teamTwo, int numberOfMatches) throws IOException {
        int cornerKicksTeamOne = 0;
        int shotsTeamOne = 0;

        int cornerKicksTeamTwo = 0;
        int shotsTeamTwo = 0;
        DecimalFormat df = new DecimalFormat("###.##");
        List<Integer> fixtureIds = getHeadToHeadFixtures(teamOne, teamTwo, numberOfMatches);
        List<JSONArray> statisticsTeamOne = new ArrayList<>();
        List<JSONArray> statisticsTeamTwo = new ArrayList<>();

        for (Integer integer : fixtureIds) {
            statisticsTeamOne.add(getFixtureStatistics(teamOne, integer));
        }

        for (Integer integer : fixtureIds) {
            statisticsTeamTwo.add(getFixtureStatistics(teamTwo, integer));
        }

        System.out.println(statisticsTeamOne);

        System.out.println("Statistiky teamu: " + getTeamName(String.valueOf(teamOne)));

        for (int i = 0; i < numberOfMatches; i++) {
            System.out.println("Strely: " + statisticsTeamOne.get(i).getJSONObject(2).getDouble("value"));
            if (statisticsTeamOne.get(i).getJSONObject(7).isNull("value")) {
                System.out.println("Rohy: 0");
                System.out.println("\n");
                cornerKicksTeamOne += 0;
            } else {
                System.out.println("Rohy: " + statisticsTeamOne.get(i).getJSONObject(7).getDouble("value"));
                System.out.println("\n");
                cornerKicksTeamOne += statisticsTeamOne.get(i).getJSONObject(7).getDouble("value");
            }
            shotsTeamOne += statisticsTeamOne.get(i).getJSONObject(2).getDouble("value");
        }

        double averageCornersTeamOne = Double.parseDouble(df.format(cornerKicksTeamOne / statisticsTeamOne.size()));
        double averageShotsTeamOne = Double.parseDouble(df.format(shotsTeamOne / statisticsTeamOne.size()));

        System.out.println("Priemer striel na branu: " + averageShotsTeamOne);
        System.out.println("Priemer rohov: " + averageCornersTeamOne);
        System.out.println("\n");

        System.out.println("Statistiky teamu: " + getTeamName(String.valueOf(teamTwo)));
        for (int i = 0; i < numberOfMatches; i++) {

            System.out.println("Strely: " + statisticsTeamTwo.get(i).getJSONObject(2).getDouble("value"));
            if (statisticsTeamTwo.get(i).getJSONObject(7).isNull("value")) {
                System.out.println("Rohy: 0");
                System.out.println("\n");
                cornerKicksTeamTwo += 0;
            } else {
                System.out.println("Rohy: " + statisticsTeamTwo.get(i).getJSONObject(7).getDouble("value"));
                System.out.println("\n");
                cornerKicksTeamTwo += statisticsTeamTwo.get(i).getJSONObject(7).getDouble("value");
            }
            shotsTeamTwo += statisticsTeamTwo.get(i).getJSONObject(2).getDouble("value");
        }

        double averageCornersTeamTwo = Double.parseDouble(df.format(cornerKicksTeamTwo / statisticsTeamTwo.size()));
        double averageShotsTeamTwo = Double.parseDouble(df.format(shotsTeamTwo / statisticsTeamTwo.size()));

        System.out.println("Priemer striel na branu: " + averageShotsTeamTwo);
        System.out.println("Priemer rohov: " + averageCornersTeamTwo);
        System.out.println("\n");
    }

    public static void getAverageStatisticsByTeam(int teamId, int season, int league, int numberOfMatches, JSONArray teamFixtures) throws IOException {
        int counter = 0;
        double cornerKicks = 0;
        double shots = 0;
        DecimalFormat df = new DecimalFormat("###.##");

        List<Integer> opponents = getTeamsIds(teamFixtures, numberOfMatches);
        List<Integer> fixtureIds = getFixtureIds(teamFixtures, numberOfMatches);
        List<JSONArray> statistics = new ArrayList<>();
        List<String> homeOrAway = getHomeOrAwayStats(teamFixtures, teamId, numberOfMatches);

        opponents.removeAll(Collections.singletonList(teamId));

        for (Integer integer : fixtureIds) {
            statistics.add(getFixtureStatistics(teamId, integer));
        }

        System.out.println("Team " + getTeamName(String.valueOf(teamId)) + " fixtures " + fixtureIds);
        System.out.println("\n");

        System.out.println("Statistiky rohov a striel teamu: " + getTeamName(String.valueOf(teamId)));
        System.out.println("\n");


        for (int i = 0; i < numberOfMatches; i++) {
            counter++;
            System.out.println("Home or away: " + homeOrAway.get(i));
            System.out.println("Proti teamu: " + getTeamName(String.valueOf(opponents.get(i))));

            if (statistics.get(i).getJSONObject(2).isNull("value")) {
                System.out.println("Strely: 0");
                shots += 0;
            } else {
                System.out.println("Strely: " + statistics.get(i).getJSONObject(2).getDouble("value"));
                shots += statistics.get(i).getJSONObject(2).getDouble("value");
            }

            if (statistics.get(i).getJSONObject(7).isNull("value")) {
                System.out.println("Rohy: 0");
                System.out.println("\n");
                cornerKicks += 0;
            } else {
                System.out.println("Rohy: " + statistics.get(i).getJSONObject(7).getDouble("value"));
                System.out.println("\n");
                cornerKicks += statistics.get(i).getJSONObject(7).getDouble("value");
            }


            if (counter == 5) {
                double averageCornersAfterFive = Double.parseDouble(df.format(cornerKicks / 5));
                double averageShotsAfterFive = Double.parseDouble(df.format(shots / 5));

                System.out.println(LAST_5_SHOTS + averageShotsAfterFive);
                System.out.println(LAST_5_CORNERS + averageCornersAfterFive);

                System.out.println("\n");
            }

            if (counter == 10) {
                double averageCornersAfterTen = Double.parseDouble(df.format(cornerKicks / 10));
                double averageShotsAfterTen = Double.parseDouble(df.format(shots / 10));

                System.out.println(LAST_10_SHOTS + averageShotsAfterTen);
                System.out.println(LAST_10_CORNERS + averageCornersAfterTen);

                System.out.println("\n");
            }

        }

        double averageCorners = Double.parseDouble(df.format(cornerKicks / statistics.size()));
        double averageShots = Double.parseDouble(df.format(shots / statistics.size()));

        System.out.println("Priemer striel na branu: " + averageShots);
        System.out.println("Priemer rohov: " + averageCorners);
        System.out.println("\n");
    }

    public static void getAverageStatisticsAgainstTeam(int teamId, int season, int league, int numberOfMatches) throws IOException {
        int counter = 0;
        double cornerKicks = 0;
        double shots = 0;
        DecimalFormat df = new DecimalFormat("###.##");

        JSONArray teamFixtures = getTeamFixtures(teamId, season, league, numberOfMatches);
        List<Integer> opponents = getTeamsIds(teamFixtures, numberOfMatches);
        List<Integer> fixtureIds = getFixtureIds(teamFixtures, numberOfMatches);
        List<JSONArray> statistics = new ArrayList<>();
        List<String> homeOrAway = getHomeOrAwayStats(teamFixtures, teamId, numberOfMatches);

        opponents.removeAll(Collections.singletonList(teamId));

        System.out.println("Team " + getTeamName(String.valueOf(teamId)) + " fixtures " + fixtureIds);
        System.out.println("\n");

        System.out.println("Statistiky rohov a striel proti teamu: " + getTeamName(String.valueOf(teamId)));
        System.out.println("\n");

        for (int i = 0; i < fixtureIds.size(); i++) {
            statistics.add(getFixtureStatistics(opponents.get(i), fixtureIds.get(i)));
        }

        for (int i = 0; i < numberOfMatches; i++) {
            counter++;
            System.out.println("Home or away: " + homeOrAway.get(i));
            System.out.println("Team: " + getTeamName(String.valueOf(opponents.get(i))));

            System.out.println("Strely: " + statistics.get(i).getJSONObject(2).getDouble("value"));
            if (statistics.get(i).getJSONObject(7).isNull("value")) {
                System.out.println("Rohy: 0");
                System.out.println("\n");
                cornerKicks += 0;
            } else {
                System.out.println("Rohy: " + statistics.get(i).getJSONObject(7).getDouble("value"));
                System.out.println("\n");
                cornerKicks += statistics.get(i).getJSONObject(7).getDouble("value");
            }
            shots += statistics.get(i).getJSONObject(2).getDouble("value");

            if (counter == 5) {
                double averageCornersAfterFive = Double.parseDouble(df.format(cornerKicks / 5));
                double averageShotsAfterFive = Double.parseDouble(df.format(shots / 5));

                System.out.println(LAST_5_SHOTS + averageShotsAfterFive);
                System.out.println(LAST_5_CORNERS + averageCornersAfterFive);

                System.out.println("\n");
            }

            if (counter == 10) {
                double averageCornersAfterTen = Double.parseDouble(df.format(cornerKicks / 10));
                double averageShotsAfterTen = Double.parseDouble(df.format(shots / 10));

                System.out.println(LAST_10_SHOTS + averageShotsAfterTen);
                System.out.println(LAST_10_CORNERS + averageCornersAfterTen);

                System.out.println("\n");
            }

        }

        double averageCorners = Double.parseDouble(df.format(cornerKicks / statistics.size()));
        double averageShots = Double.parseDouble(df.format(shots / statistics.size()));

        System.out.println("Priemer striel na branu proti teamu " + getTeamName(String.valueOf(teamId)) + ": " + averageShots);
        System.out.println("Priemer rohov proti teamu " + getTeamName(String.valueOf(teamId)) + ": " + averageCorners);
    }
}
