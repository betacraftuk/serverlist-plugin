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

import com.google.gson.JsonObject;


public class PingThread extends Thread {

	@Override
	public void run() {
		HttpURLConnection con = null;
		try {
			URL url = new URL(BCPing.HOST + "/server_update");
			int failsInARow = 0;
			while (BCPing.running) {
				try {
					con = (HttpURLConnection) url.openConnection();
					con.setRequestMethod("POST");
					con.addRequestProperty("Content-Type", "application/json");
					con.setUseCaches(false);
					con.setDoOutput(true);
					con.setDoInput(true);

					OutputStream os = con.getOutputStream();

					ArrayList<Player> online = BCPing.bukkitversion.getOnlinePlayers();

					JsonObject jobj = (JsonObject) BCPing.gson.toJsonTree(BCPing.config);

					jobj.addProperty("max_players", Bukkit.getServer().getMaxPlayers());
					jobj.addProperty("online_players", online.size());
					jobj.addProperty("software_name", Bukkit.getServer().getName());
					jobj.addProperty("software_version", Bukkit.getServer().getVersion());
					jobj.addProperty("connect_online_mode", Bukkit.getServer().getOnlineMode());

					if (BCPing.config.send_players) {
						String listString = online.stream().map(Player::getName)
								.collect(Collectors.joining(","));
						jobj.addProperty("player_names", listString);
					} else {
						jobj.addProperty("player_names", "");
					}


					String data = BCPing.gson.toJson(jobj);
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
								//BCPing.log.info("[BetaCraftPing] You can customize your server's appearance on the list by going to: 'https://api.betacraft.uk/edit_server.jsp?id=" + privatekey + "'");
							}
							failsInARow = 0;
						} else {
							failsInARow++;
							if (failsInARow <= 5) {
								BCPing.log.info("[BetacraftPing] Failed to ping the server list");
								BCPing.log.info("[BetacraftPing] Error: \"" + response.msg + "\"");
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
					//t.printStackTrace();
					failsInARow++;
					if (failsInARow <= 5) {
						BCPing.log.warning("[BetacraftPing] Failed to ping server list. (" + t.getMessage() + ")");
						BCPing.log.warning("[BetacraftPing] Perhaps ping_details.json is not configured properly?");


						String result = new BufferedReader(new InputStreamReader(con.getErrorStream()))
								.lines().collect(Collectors.joining("\n"));
						BCPing.log.info("[BetacraftPing] Error: \"" + result + "\"");
					}
					Thread.sleep(60000);
				}

			}
		} catch (Throwable t) {
			// Prevent fail messages at server shutdown
			if (!BCPing.running)
				return;
			//t.printStackTrace();
			BCPing.log.warning("[BetacraftPing] The heartbeat was permanently interrupted (" + t.getMessage() + ")");
		}
	}

	public static ErrResponse readResponse(InputStream is) {
		try {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();

			int nRead;
			byte[] readData = new byte[is.available()];

			while ((nRead = is.read(readData, 0, readData.length)) != -1) {
				buffer.write(readData, 0, nRead);
			}

			buffer.flush();
			String responString = new String(buffer.toByteArray());

			//System.out.println(responString);

			return BCPing.gson.fromJson(responString, ErrResponse.class);
		} catch (Throwable t) {
			BCPing.log.warning("Failed to read response: " + t.getMessage());
			return null;
		}
	}

	public static class ErrResponse {
		public boolean error;
		public String msg;
	}
}
