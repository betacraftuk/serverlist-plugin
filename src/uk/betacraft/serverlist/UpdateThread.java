package uk.betacraft.serverlist;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Collectors;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class UpdateThread extends Thread {
    public static JsonObject newestRelease = null;
    public static boolean update = false;

    public void run() {
        HttpURLConnection con = null;
        try {
            URL url = new URL("https://api.github.com/repos/betacraftuk/serverlist-plugin/releases?per_page=1");
            while (BCPing.running) {
                try {
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    con.setUseCaches(false);
                    con.setDoOutput(true);
                    con.setDoInput(true);

                    // process response
                    try {
                        JsonArray releases = BCPing.gson.fromJson(PingThread.readStringResponse(con.getInputStream()), JsonArray.class);
                        newestRelease = releases.get(0).getAsJsonObject();
                        String newestReleaseTag = newestRelease.get("tag_name").getAsString();

                        if (!BCPing.PLUGIN_VERSION.equals(newestReleaseTag)) {
                            update = true;

                            BCPing.log.info("[BetacraftPing] New release available! " + newestReleaseTag);
                            BCPing.log.info("[BetacraftPing] Download it at " + newestRelease.get("html_url").getAsString());
                        } else {
                            update = false;
                        }
                    } catch (Throwable t) {
                        if (con.getResponseCode() != 404) {
                            BCPing.log.warning("[BetacraftPing] Failed to get updates. (" + t.getMessage() + ")");

                            try {
                                String result = new BufferedReader(new InputStreamReader(con.getErrorStream()))
                                        .lines().collect(Collectors.joining("\n"));

                                BCPing.log.info("[BetacraftPing] Error: \"" + result + "\"");
                            } catch (Throwable t2) {
                                t2.printStackTrace();
                            }
                        }

                        update = false;
                    }

                    Thread.sleep(1000 * 60 * 60 * 3); // 3 hours
                } catch (Throwable t) {
                    // Prevent fail messages at server shutdown
                    if (!BCPing.running)
                        return;

                    update = false;

                    BCPing.log.warning("[BetacraftPing] Failed to get updates. (" + t.getMessage() + ")");

                    try {
                        String result = new BufferedReader(new InputStreamReader(con.getErrorStream()))
                                .lines().collect(Collectors.joining("\n"));

                        BCPing.log.info("[BetacraftPing] Error: \"" + result + "\"");
                    } catch (Throwable t2) {
                        t2.printStackTrace();
                    }

                    try {
                        Thread.sleep(1000 * 60 * 60 * 3); // 3 hours
                    } catch (Throwable t2) {
                        if (!BCPing.running)
                            return;

                        update = false;

                        t2.printStackTrace();
                    }
                }
            }

        } catch (Throwable t) {
            update = false;

            t.printStackTrace();
        }
    }
}
