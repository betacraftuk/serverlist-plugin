/**
 * BukkitVersion v1.0.0 (2023-05-14) by Moresteck
 * <br /><br />
 * BukkitVersion is a plugin addon to provide help
 * with compatibility issues. It tells the plugin
 * what Bukkit version it's running on, and it
 * can do some operations for the plugin.
 * <br />
 * You can use this tool for your own, but don't
 * delete this disclaimer from here.
 *
 * @author Moresteck
 */
package uk.betacraft.bukkitversion;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import uk.betacraft.serverlist.BCPing;

public class BukkitVersion {
    private Logger log = Logger.getLogger("Minecraft");
    private static String addon_version = "1.0.0";

    private String version;
    private JavaPlugin plugin;
    private Server server;

    public BukkitVersion(JavaPlugin plugin) {
        this.plugin = plugin;
        this.server = plugin.getServer();

        this.setupVersion(server);
    }

    /**
     * Gets the version of the addon.
     *
     * @return BukkitVersion's version
     */
    public static String getBVVersion() {
        return addon_version;
    }

    private void setupVersion(final Server server) {
        try {
            this.version = this.parseVersion(server);
            this.log.info(this.plugin.getDescription().getName() + " uses BukkitVersion v" + addon_version);
            this.log.info("Version detected: " + this.version + " (" + this.getVersionId() + ")");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets now running version as a string.
     *
     * @return Version now running
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * Gets the build of Bukkit.
     *
     * @return Bukkit's build running now
     */
    public String getBukkitVersion() {
        return plugin.getServer().getVersion();
    }

    /**
     * Says if the Bukkit's version is higher than 1.1-R4.
     *
     * @return Version higher than 1.1-R4
     */
    public boolean isBukkitNewSystem() {
        this.log.info("Version id: " + this.getVersionId());
        return getVersionId() > 6 ? true : 
            getBukkitVersion().startsWith("git-Bukkit-1.1-R4") || 
            getBukkitVersion().startsWith("git-Bukkit-1.1-R5") || 
            getBukkitVersion().startsWith("git-Bukkit-1.1-R6");
    }

    /**
     * Gets the version ID. <br />
     * Detects versions from b1.7.3 to 1.1. <br />
     * Versions b1.1 - b1.7.2 were dropped because Uberbukkit exists
     *
     * @return Version id starting from 0
     */
    public int getVersionId() {
        if (version.equals("b1.7.3")) {
            return 0;
        } else if (version.equals("b1.8")) {
            return 1;
        } else if (version.equals("b1.8.1")) {
            return 2;
        } else if (version.equals("b1.9")) {
            return 3;
        } else if (version.startsWith("1.0.0")) {
            return 4;
        } else if (version.startsWith("1.0.1")) {
            return 5;
        } else if (version.equals("1.1")) {
            return 6;
        } else if (version.startsWith("1.2.")) {
            int thirddigit = Integer.parseInt(version.split("\\.")[2]);
            if (thirddigit < 4) {
                // 1.2.1
                // 1.2.2
                // 1.2.3
                return 7;
            } else {
                // 1.2.4
                // 1.2.5
                return 8;
            }
        } else if (version.startsWith("1.3")) {
            // 1.3.1
            // 1.3.2
            return 9;
        } else if (version.startsWith("1.4.")) {
            int thirddigit = Integer.parseInt(version.split("\\.")[2]);
            if (thirddigit <= 2) {
                // 1.4.2
                return 10;
            } else if (thirddigit <= 5) {
                // 1.4.5
                return 11;
            } else {
                // 1.4.6
                // 1.4.7
                return 12;
            }
        } else if (version.equals("1.5")) {
            // 1.5
            return 13;
        } else if (version.startsWith("1.5.")) {
            int thirddigit = Integer.parseInt(version.split("\\.")[2]);
            if (thirddigit == 1) {
                // 1.5.1
                return 13;
            } else {
                // 1.5.2
                return 14;
            }
        } else if (version.startsWith("1.6.")) {
            int thirddigit = Integer.parseInt(version.split("\\.")[2]);
            if (thirddigit == 1) {
                // 1.6.1
                return 15;
            } else if (thirddigit == 2) {
                // 1.6.2
                return 16;
            } else if (thirddigit <= 4) {
                // 1.6.4
                return 17;
            }
        } else if (version.startsWith("1.7.")) {
            int thirddigit = Integer.parseInt(version.split("\\.")[2]);
            if (thirddigit <= 5) {
                // 1.7.2
                // 1.7.5
                return 18;
            } else {
                // 1.7.6
                // 1.7.7
                // 1.7.8
                // 1.7.9
                // 1.7.10
                return 19;
            }
        } else if (version.startsWith("1.8")) {
            // 1.8
            // 1.8.3
            // 1.8.4
            // 1.8.5
            // 1.8.6
            // 1.8.7
            // 1.8.8
            return 20;
        } else if (version.equals("1.9")) {
            return 21;
        } else if (version.startsWith("1.9.")) {
            int thirddigit = Integer.parseInt(version.split("\\.")[2]);
            if (thirddigit == 2) {
                // 1.9.2
                return 22;
            } else {
                // 1.9.4
                return 23;
            }
        } else if (version.startsWith("1.10")) {
            // 1.10
            // 1.10.2
            return 24;
        } else if (version.equals("1.11")) {
            // 1.11
            return 25;
        } else if (version.startsWith("1.11.")) {
            // 1.11.1
            // 1.11.2
            return 26;
        } else if (version.equals("1.12")) {
            return 27;
        } else if (version.equals("1.12.1")) {
            return 28;
        } else if (version.equals("1.12.2")) {
            return 29;
        } else if (version.equals("1.13")) {
            return 30;
        } else if (version.equals("1.13.1")) {
            return 31;
        } else if (version.equals("1.13.2")) {
            return 32;
        } else if (version.equals("1.14")) {
            return 33;
        } else if (version.equals("1.14.1")) {
            return 34;
        } else if (version.equals("1.14.2")) {
            return 35;
        } else if (version.equals("1.14.3")) {
            return 36;
        } else if (version.equals("1.14.4")) {
            return 37;
        } else if (version.equals("1.15")) {
            return 38;
        } else if (version.equals("1.15.1")) {
            return 39;
        } else if (version.equals("1.15.2")) {
            return 40;
        } else if (version.equals("1.16.1")) {
            return 41;
        } else if (version.equals("1.16.2")) {
            return 42;
        } else if (version.equals("1.16.3")) {
            return 43;
        } else if (version.equals("1.16.4")) {
            return 44;
        } else if (version.equals("1.16.5")) {
            return 44;
        } else if (version.equals("1.17")) {
            return 45;
        } else if (version.equals("1.17.1")) {
            return 46;
        } else if (version.equals("1.18")) {
            return 47;
        } else if (version.equals("1.18.1")) {
            return 47;
        } else if (version.equals("1.18.2")) {
            return 48;
        } else if (version.equals("1.19")) {
            return 49;
        } else if (version.startsWith("1.19.")) {
            int thirddigit = Integer.parseInt(version.split("\\.")[2]);
            if (thirddigit <= 2) {
                // 1.19.1
                // 1.19.2
                return 50;
            } else if (thirddigit == 3) {
                // 1.19.3
                return 51;
            } else {
                // 1.19.4
                return 52;
            }
        } else if (version.startsWith("1.20")) {
            return 53;
        } else if (version.startsWith("1.21")) {
            return 54;
        } else if (version.startsWith("1.22")) {
            return 55;
        }

        // 1.22.7 is the last JE version.
        return 56;
    }

    /**
     * Creates a world using given arguments.
     *
     * @param name World's name
     * @param env World's environment
     * @param seed World's seed
     * @param gen World's generator
     * @param useSeed Shall the method use specified seed? If false, it will use a random seed
     *
     * @return World
     */
    public World createWorld(String name, Environment env, long seed, ChunkGenerator gen, boolean useSeed) {
        World bworld = null;
        if (this.isBukkitNewSystem()) {
            org.bukkit.WorldCreator creator = new org.bukkit.WorldCreator(name);
            creator.environment(env);
            creator.generator(gen);
            if (useSeed) {
                creator.seed(seed);
            }
            bworld = server.getWorld(name) == null ? server.createWorld(creator) : server.getWorld(name);
        } else {
            if (useSeed) {
                bworld = server.getWorld(name) == null ? server.createWorld(name, env,
                        seed, gen) : server.getWorld(name);
            } else {
                bworld = server.getWorld(name) == null ? server.createWorld(name, env,
                        gen) : server.getWorld(name);
            }
        }
        return bworld;
    }

    /**
     * Registers an event, but may result in an NPE.
     * <br />
     * You don't have to specify the priority and type if your version is 1.1-R4 or higher.
     *
     * @param type Type enum to register (e.g. "PLAYER_ITEM", "PLAYER_INTERACT")
     * @param priority Priority to register (Lowest, Low, Normal, Monitor, High, Highest)
     */
    public void registerEvent(String type, Listener listener, String priority) {
        if (isBukkitNewSystem()) {
            PluginManager pluginManager = this.plugin.getServer().getPluginManager();

            try {
                Method registerEventsMethod = pluginManager.getClass()
                        .getDeclaredMethod("registerEvents", listener.getClass(), plugin.getClass());
                
                registerEventsMethod.invoke(pluginManager, listener, plugin);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        } else {
            this.plugin.getServer().getPluginManager().registerEvent(org.bukkit.event.Event.Type.valueOf(type), 
                    listener, org.bukkit.event.Event.Priority.valueOf(priority), plugin);
        }
    }

    /**
     * You still have to do self-check for the Bukkit's version for no warnings.
     * <br />
     * Safely registers an event with specified Priority. <br />
     * You don't have to specify the priority and type if your version is 1.1-R4 or higher.
     *
     * @param type Type enum to register (e.g. "PLAYER_ITEM", "PLAYER_INTERACT")
     * @param priority Priority to register (Lowest, Low, Normal, Monitor, High, Highest)
     */
    public void registerEventSafely(String type, Listener listener, String priority) {
        try {
            this.registerEvent(type, listener, priority);
        } catch (Exception ex) {
            this.log.info("BV: Could not register \"" + type + "\" for " + this.plugin.getDescription().getName());
            this.log.info("BV: Perhaps you should use the newest Craftbukkit build?");
        }
    }

    /**
     * You still have to do self-check for the Bukkit's version for no warnings.
     * <br />
     * Safely registers an event.
     *
     * @param type Type enum to register (e.g. "PLAYER_ITEM", "PLAYER_INTERACT")
     */
    public void registerEventSafely(String type, Listener listener) {
        registerEventSafely(type, listener, "Normal");
    }

    /**
     * Lists all online players.
     * 
     * @return online players
     */
    public ArrayList<Player> getOnlinePlayers() {
        ArrayList<Player> arlp = new ArrayList<>();
        try {
            if (version.equals("1.7.10") || this.getVersionId() > 19) {
                try {
                    Class<?> bukkitclass = this.getClass().getClassLoader().loadClass("org.bukkit.Bukkit");
                    Method newmethod = bukkitclass.getDeclaredMethod("getOnlinePlayers");
                    Object t = newmethod.invoke(null);

                    if (t instanceof Collection) {
                        @SuppressWarnings("unchecked")
                        Collection<? extends Player> cs = (Collection<? extends Player>) t;
                        for (Player p: cs) {
                            if (p != null) {
                                arlp.add(p);
                            }
                        }
                    } else {
                        for (Player p: BCPing.server.getOnlinePlayers()) {
                            if (p != null) {
                                arlp.add(p);
                            }
                        }
                    }
                } catch (Throwable t) {
                    BCPing.log.warning("BukkitVersion: Fatal error whilst getting online players.");
                    t.printStackTrace();
                }
            } else {
                for (Player p: BCPing.server.getOnlinePlayers()) {
                    if (p != null) {
                        arlp.add(p);
                    }
                }
            }
        } catch (Throwable t) {
            BCPing.log.warning("BukkitVersion: Unknown error whilst getting online players.");
            t.printStackTrace();
        }
        return arlp;
    }

    /**
     * Gives the version number from b1.7.3 to 1.1.
     * <br />
     * For higher versions use {@link BukkitVersion.getVersionId()}
     * or {@link BukkitVersion.isBukkitNewSystem()}
     *
     * @param server Server instance
     * @return Formatted version
     */
    public String parseVersion(Server server) {
        String bver = server.getVersion();
        String version = bver.substring(bver.indexOf("MC: ") + 4).replace(")", "");
        // = part[2].replace(")", "");
        // Detects b1.9-pre5.
        if (bver.equals("Beta 1.9 Prerelease 5")) {
            return "b1.9";
        }

        // Detects supported Beta versions.
        if (version.equals("1.7.3") || version.equals("1.8.1")) {
            return "b" + version;
        }

        // Detects b1.8 and other release versions.
        try {
            World w = server.getWorlds().get(0);
            server.createWorld(w.getName(), w.getEnvironment());
            return version.startsWith("1.0.1") || version.startsWith("1.0.0") ? version : "b" + version;
        } catch (Throwable ex) {
            return version;
        }
    }
}
