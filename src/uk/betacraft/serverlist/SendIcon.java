package uk.betacraft.serverlist;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;

import uk.betacraft.serverlist.PingThread.ErrResponse;

public class SendIcon {

    public static void sendIcon() {

        String icon = Config.getIcon();

        HttpURLConnection con = null;
        try {
            URL url = new URL(BCPing.HOST + "/server_update_icon");
            try {
                con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.addRequestProperty("Content-Type", "application/json");
                con.setUseCaches(false);
                con.setDoOutput(true);
                con.setDoInput(true);

                OutputStream os = con.getOutputStream();

                JsonObject jobj = new JsonObject();
                jobj.addProperty("socket", BCPing.config.socket);
                if (icon == null) {
                    jobj.addProperty("icon", "");
                } else {
                    jobj.addProperty("icon", icon);
                }

                if (BCPing.config.private_key != null) {
                    jobj.addProperty("private_key", BCPing.config.private_key);
                }

                String data = BCPing.gson.toJson(jobj);

                byte[] json = data.getBytes("UTF-8");

                os.write(json);
                os.flush();
                os.close();

                // process response
                ErrResponse response = PingThread.readResponse(con.getInputStream());
                if (response != null) {
                    if (!response.error) {
                        BCPing.log.info("[BetacraftPing] Server icon updated successfully.");
                    } else {
                        BCPing.log.info("[BetacraftPing] Failed to update server icon");
                        BCPing.log.info("[BetacraftPing] Error: \"" + response.message + "\"");
                    }
                } else {
                    BCPing.log.info("[BetacraftPing] Failed to read ping response (is null)");
                }
            } catch (Throwable t) {
                BCPing.log.warning("[BetacraftPing] Failed to update server icon (" + t.getMessage() + ")");
                String result = new BufferedReader(new InputStreamReader(con.getErrorStream()))
                        .lines().collect(Collectors.joining("\n"));
                BCPing.log.info("[BetacraftPing] Error: \"" + result + "\"");
            }
        } catch (Throwable t) {
            BCPing.log.warning("[BetacraftPing] Failed to update server icon (" + t.getMessage() + ")");
        }
    }
}
