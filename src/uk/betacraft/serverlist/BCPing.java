package uk.betacraft.serverlist;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import pl.moresteck.uberbukkit.Uberbukkit;
import uk.betacraft.bukkitversion.BukkitVersion;

/**
 * 
 * Uberbukkit-compliant only for Beta.
 *
 */
public class BCPing extends JavaPlugin {
	public static Server server;
	public static Logger log;
	public static BukkitVersion bukkitversion;
	public static Gson gson = new GsonBuilder().setPrettyPrinting().create();
	
	PingThread thread;
	public static Config config;
	public static boolean running = true;
	public static boolean uberbukkit = false;
	
	// "http://localhost:2137"
	protected static final String HOST = "https://api.betacraft.uk/v2";

	public void onEnable() {
		server = this.getServer();
		log = this.getServer().getLogger();
		bukkitversion = new BukkitVersion(this);

		log.info("[BetacraftPing] BetacraftPing v" + this.getDescription().getVersion() + " enabled.");
		
		try {
			Uberbukkit.getPVN();
			uberbukkit = true;
			log.info("[BetacraftPing] Uberbukkit detected");
		} catch (Throwable t) {}
		
		
		File configfile = new File("plugins/BetacraftPing/ping_details.json");
		String pingdetails = null;
		try {
			pingdetails = new String(Files.readAllBytes(configfile.toPath()), "UTF-8");
		} catch (Throwable t) {
			if (!(t instanceof NoSuchFileException)) {
				t.printStackTrace();
			}
			configfile.getParentFile().mkdirs();
		}
		if (pingdetails != null) {
			config = gson.fromJson(pingdetails, Config.class);
			// TODO validation?
		} else {
			config = new Config();
			String serverip = Bukkit.getServer().getIp();
			
			if (serverip.equals("")) {
				serverip = getIPFromAmazon();
			}
			config.connect_socket = serverip + ":" + Bukkit.getServer().getPort();
			config.name = "A Minecraft server";
			config.description = "";
			config.version_category = Config.getCategory();
			config.connect_protocol = Config.getPVN();
			config.send_players = true;
			
			if (uberbukkit) {
				config.connect_version = Config.getLatestForPVN(config.connect_protocol);
			} else {
				config.connect_version = bukkitversion.getVersion();
			}
			
			try {
				Files.write(configfile.toPath(), gson.toJson(config).getBytes("UTF-8"));
			} catch (Throwable t) {
				log.warning("[BetacraftPing] Failed to write default configuration! Disabling...");
				this.getServer().getPluginManager().disablePlugin(this);
				running = false;
				return;
			}
			
			log.warning("[BetacraftPing] Failed to load configuration!");
			log.warning("[BetacraftPing] Wrote default configuration --- see plugins/BetacraftPing/ping_details.json");
		}
		
		SendIcon.sendIcon();
		
		thread = new PingThread();
		thread.start();
	}
	
	public void onDisable() {
		log.info("[BetacraftPing] Disabling...");
		running = false;
		if (thread != null) thread.interrupt();
	}
	
	public static String getIPFromAmazon() {
        try {
            URL myIP = new URL("http://checkip.amazonaws.com");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(myIP.openStream()));
            return bufferedReader.readLine();

        } catch (Exception e) {
            log.warning("[BetacraftPing] Failed to get IP from Amazon! Are you offline?");
            e.printStackTrace();
        }
        return null;
    }
}
