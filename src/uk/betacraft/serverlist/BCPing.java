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

import uk.betacraft.bukkitversion.BukkitVersion;
import uk.betacraft.uberbukkit.Uberbukkit;

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

    PingThread pingThread;
    UpdateThread updateThread;
    public static Config config;
    public static boolean running = true;
    public static boolean uberbukkit = false;

    public static String PLUGIN_VERSION = "";

    //protected static final String HOST = "http://localhost:2137/api/v2";
    protected static final String HOST = "https://api.betacraft.uk/v2";

    public void onEnable() {
        server = this.getServer();
        log = this.getServer().getLogger();
        bukkitversion = new BukkitVersion(this);

        PLUGIN_VERSION = this.getDescription().getVersion();

        log.info("[BetacraftPing] BetacraftPing v" + this.getDescription().getVersion() + " enabled.");

        try {
            Uberbukkit.getTargetPVN();
            uberbukkit = true;
            log.info("[BetacraftPing] Uberbukkit detected");
        } catch (Throwable t) {}


        File pingDetailsFile = new File("plugins/BetacraftPing/ping_details.json");
        String pingDetails = null;

        try {
            pingDetails = new String(Files.readAllBytes(pingDetailsFile.toPath()), "UTF-8");
        } catch (Throwable t) {
            if (!(t instanceof NoSuchFileException)) {
                t.printStackTrace();
            }
            pingDetailsFile.getParentFile().mkdirs();
        }

        if (pingDetails != null) {
            config = gson.fromJson(pingDetails, Config.class);
            // TODO validation?
        } else {
            config = new Config();
            String serverip = Bukkit.getServer().getIp();

            if (serverip.equals("")) {
                serverip = getIPFromAmazon();
            }

            config.socket = serverip + ":" + Bukkit.getServer().getPort();
            config.name = "A Minecraft server";
            config.description = "";
            config.category = Config.getCategory();
            config.protocol = Config.getPVN();
            config.send_players = true;

            if (uberbukkit) {
                config.game_version = config.v1_version = Config.getLatestForPVN(config.protocol);
            } else {
                config.game_version = config.v1_version = bukkitversion.getVersion();
            }

            try {
                Files.write(pingDetailsFile.toPath(), gson.toJson(config).getBytes("UTF-8"));
            } catch (Throwable t) {
                log.warning("[BetacraftPing] Failed to write default configuration! Disabling...");
                this.getServer().getPluginManager().disablePlugin(this);
                running = false;
                return;
            }

            log.warning("[BetacraftPing] Failed to load configuration!");
            log.warning("[BetacraftPing] Wrote default configuration --- see plugins/BetacraftPing/ping_details.json");
        }

        pingThread = new PingThread();
        pingThread.start();

        updateThread = new UpdateThread();
        updateThread.start();
    }

    public void onDisable() {
        if (UpdateThread.update)
            log.info("[BetacraftPing] Download latest plugin update from " + UpdateThread.newestRelease.get("html_url").getAsString());

        log.info("[BetacraftPing] Disabling...");
        running = false;

        if (pingThread != null)
            pingThread.interrupt();

        if (updateThread != null)
            updateThread.interrupt();
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
