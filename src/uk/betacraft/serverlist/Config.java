package uk.betacraft.serverlist;

import java.io.File;
import java.nio.file.Files;
import java.util.Base64;

import uk.betacraft.uberbukkit.Uberbukkit;

public class Config {
    public static uk.betacraft.bukkitversion.Config config;

    public String name = "";
    public String game_version = "";
    public String protocol = "";
    public String socket = "";
    public String category = "";
    public String description = "";
    public boolean send_players = true;
    public String private_key = null; // don't include by default, so `null`

    public static String getPVN() {
        if (BCPing.uberbukkit) {
            return "beta_" + Integer.toString(Uberbukkit.getTargetPVN());
        }

        switch(BCPing.bukkitversion.getVersionId()) {
        case 0:
            return "beta_14";
        case 1:
            return "beta_17";
        case 2:
            return "beta_17";
        case 3:
            return "beta_21";
        case 4:
            return "release_22";
        case 5:
            return "release_22";
        case 6:
            return "release_22";
        case 7:
            return "release_28";
        case 8:
            return "release_29";
        case 9:
            return "release_39";
        case 10:
            return "release_47";
        case 11:
            return "release_49";
        case 12:
            return "release_51";
        case 13:
            return "release_60";
        case 14:
            return "release_61";
        case 15:
            return "release_73";
        case 16:
            return "release_74";
        case 17:
            return "release_78";
        case 18:
            return "release_4";
        case 19:
            return "release_5";
        default:
            return "unknown";
        }
    }

    public static String getLatestForPVN(String pvn) {
        switch(pvn) {
        case "beta_14":
            return "b1.7.3";
        case "beta_13":
            return "b1.6.6";
        case "beta_12":
            return "b1.6-tb3";
        case "beta_11":
            return "b1.5_01";
        case "beta_10":
            return "b1.4_01";
        case "beta_9":
            return "b1.3_01";
        case "beta_8":
            return "b1.2_02";
        case "beta_7":
            return "b1.1_02";
        default:
            return "unknown";
        }
    }

    public static String getCategory() {
        return getPVN().startsWith("beta_") ? "beta" : "release";
    }

    private static String icon = null;
    public static String getIcon() {
        if (icon == null) {
            File iconfile = new File("plugins/BetacraftPing/server_icon.png");
            if (!iconfile.exists()) {
                BCPing.log.warning("[BetacraftPing] No server icon found!");
                return icon;
            }

            if (iconfile.length() > 64000) {
                BCPing.log.severe("[BetacraftPing] Server icon size is too big! (64 kB max, recommended res: 128x128)");
            } else {
                try {
                    byte[] filebytes = Files.readAllBytes(iconfile.toPath());
                    byte[] b64str = Base64.getEncoder().encode(filebytes);
                    icon = new String(b64str, "UTF-8");

                } catch (Throwable t) {
                    BCPing.log.severe("[BetacraftPing] Failed to read server icon:");
                    t.printStackTrace();
                }
            }
            return icon;
        } else {
            return icon;
        }
    }
}
