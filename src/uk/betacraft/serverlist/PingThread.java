package uk.betacraft.serverlist;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class PingThread extends Thread {

    @Override
    public void run() {
        HttpURLConnection con = null;
        try {
            URL url = new URL(BCPing.HOST + "/server_update");
            int failsInARow = -1;
            while (BCPing.running) {
                try {
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("POST");
                    con.addRequestProperty("Content-Type", "application/json");
                    con.setUseCaches(false);
                    con.setDoOutput(true);
                    con.setDoInput(true);

                    OutputStream os = con.getOutputStream();

                    ArrayList<org.bukkit.entity.Player> online = BCPing.bukkitversion.getOnlinePlayers();

                    JsonObject payload = (JsonObject) BCPing.gson.toJsonTree(BCPing.config);

                    payload.addProperty("max_players", Bukkit.getServer().getMaxPlayers());
                    payload.addProperty("online_players", online.size());

                    JsonObject software = new JsonObject();
                    software.addProperty("name", Bukkit.getServer().getName());
                    software.addProperty("version", Bukkit.getServer().getVersion());

                    payload.add("software", software);
                    payload.addProperty("online_mode", Bukkit.getServer().getOnlineMode());

                    if (BCPing.config.send_players) {
                        JsonArray playersArray = new JsonArray();

                        for (Player bukkitPlayer : online) {
                            JsonObject playerObject = new JsonObject();
                            playerObject.addProperty("username", bukkitPlayer.getName());

                            playersArray.add(playerObject);
                        }

                        payload.add("players", playersArray);
                    } else {
                        payload.add("players", new JsonArray());
                    }

                    String data = BCPing.gson.toJson(payload);
                    //BCPing.log.info(data);

                    byte[] json = data.getBytes("UTF-8");

                    os.write(json);
                    os.flush();
                    os.close();

                    // process response
                    ErrResponse response = readResponse(con.getInputStream());

                    if (response != null) {
                        if (!response.error) {
                            if (failsInARow != 0) {
                                BCPing.log.info("[BetacraftPing] Server list ping was successful");

                                SendIcon.sendIcon();
                            }

                            failsInARow = 0;
                        } else {
                            failsInARow++;

                            if (failsInARow <= 5) {
                                BCPing.log.info("[BetacraftPing] Failed to ping the server list");
                                BCPing.log.info("[BetacraftPing] Error: \"" + response.message + "\"");
                            }
                        }
                    } else {
                        failsInARow++;

                        if (failsInARow <= 5) {
                            BCPing.log.info("[BetacraftPing] Failed to read ping response (is null)");
                        }
                    }

                    Thread.sleep(60000);
                } catch (Throwable t) {
                    // Prevent fail messages at server shutdown
                    if (!BCPing.running)
                        return;

                    failsInARow++;

                    if (failsInARow <= 5) {
                        BCPing.log.warning("[BetacraftPing] Failed to ping server list. (" + t.getMessage() + ")");
                        BCPing.log.warning("[BetacraftPing] Perhaps ping_details.json is not configured properly?");


                        try {
                            String result = new BufferedReader(new InputStreamReader(con.getErrorStream()))
                                    .lines().collect(Collectors.joining("\n"));
                            BCPing.log.info("[BetacraftPing] Error: \"" + result + "\"");
                        } catch (Throwable t2) {
                            t2.printStackTrace();
                        }
                    }

                    try {
                        Thread.sleep(60000);
                    } catch (Throwable t2) {
                        if (!BCPing.running)
                            return;

                        t2.printStackTrace();
                    }
                }

            }
        } catch (Throwable t) {
            // Prevent fail messages at server shutdown
            if (!BCPing.running)
                return;

            BCPing.log.warning("[BetacraftPing] The heartbeat was permanently interrupted (" + t.getMessage() + ")");
        }
    }

    public static String readStringResponse(InputStream is) {
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            int nRead;
            byte[] readData = new byte[is.available()];

            while ((nRead = is.read(readData, 0, readData.length)) != -1) {
                buffer.write(readData, 0, nRead);
            }

            buffer.flush();

            return new String(buffer.toByteArray());
        } catch (Throwable t) {
            BCPing.log.warning("Failed to read response: " + t.getMessage());
            return null;
        }
    }

    public static ErrResponse readResponse(InputStream is) {
        try {
            return BCPing.gson.fromJson(readStringResponse(is), ErrResponse.class);
        } catch (Throwable t) {
            BCPing.log.warning("Failed to read response: " + t.getMessage());
            return null;
        }
    }

    public static class ErrResponse {
        public boolean error;
        public String message;
    }
}
