package me.justeli.coins.main;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.zip.GZIPOutputStream;

/*
 * bStats collects some data for plugin authors.
 *
 * Check out https://bStats.org/ to learn more about bStats!
 */
public class Metrics
{
    // The version of this bStats class
    public static final int B_STATS_VERSION = 1;

    // The url to which the data is sent
    private static final String URL = "https://bStats.org/submitData/bukkit";

    // Should failed requests be logged?
    private static boolean logFailedRequests;

    // The uuid of the server
    private static String serverUUID;

    // The plugin
    private final JavaPlugin plugin;

    // A list with all custom charts
    private final List<CustomChart> charts = new ArrayList<>();

    /*
     * Class constructor.
     *
     * @param plugin The plugin which stats should be submitted.
     */
    public Metrics(JavaPlugin plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null!");
        }
        this.plugin = plugin;

        // Get the config file
        File bStatsFolder = new File(plugin.getDataFolder().getParentFile(), "bStats");
        File configFile = new File(bStatsFolder, "config.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        // Check if the config file exists
        if (!config.isSet("serverUuid")) {
            // Every server gets it's unique random id.
            config.addDefault("serverUuid", UUID.randomUUID().toString());
            // Should failed request be logged?
            try {
                config.save(configFile);
            } catch (IOException ignored) { }
        }

        // Load the data
        serverUUID = config.getString("serverUuid");
        logFailedRequests = config.getBoolean("logFailedRequests", false);

        boolean found = false;
        // Search for all other bStats Metrics classes to see if we are the first one
        for (Class<?> service : Bukkit.getServicesManager().getKnownServices()) {
            try {
                service.getField("B_STATS_VERSION"); // Our identifier :)
                found = true; // We aren't the first
                break;
            } catch (NoSuchFieldException ignored) { }
        }
        // Register our service
        Bukkit.getServicesManager().register(Metrics.class, this, plugin, ServicePriority.Normal);
        if (!found) {
            // We are the first!
            startSubmitting();
        }
    }

    public void add (String value, String data)
    {
        addCustomChart(new Metrics.SimplePie(value)
        {
            @Override public String getValue()
            {
                return data;
            }
        });
    }

    /*
     * Adds a custom chart.
     *
     * @param chart The chart to add.
     */
    public void addCustomChart(CustomChart chart) {
        if (chart == null) {
            throw new IllegalArgumentException("Chart cannot be null!");
        }
        charts.add(chart);
    }

    /*
     * Starts the Scheduler which submits our data every 30 minutes.
     */
    private void startSubmitting() {
        final Timer timer = new Timer(true); // We use a timer cause the Bukkit scheduler is affected by server lags
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!plugin.isEnabled()) { // Plugin was disabled
                    timer.cancel();
                    return;
                }
                // Nevertheless we want our code to run in the Bukkit main thread, so we have to use the Bukkit scheduler
                // Don't be afraid! The connection to the bStats server is still async, only the stats collection is sync ;)
                Bukkit.getScheduler().runTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        submitData();
                    }
                });
            }
        }, 1000*60*5, 1000*60*30);
        // Submit the data every 30 minutes, first time after 5 minutes to give other plugins enough time to start
        // WARNING: Changing the frequency has no effect but your plugin WILL be blocked/deleted!
        // WARNING: Just don't do it!
    }

    /*
     * Gets the plugin specific data.
     * This method is called using Reflection.
     *
     * @return The plugin specific data.
     */
    public JSONObject getPluginData() {
        JSONObject data = new JSONObject();

        String pluginName = plugin.getDescription().getName();
        String pluginVersion = plugin.getDescription().getVersion();

        data.put("pluginName", pluginName); // Append the name of the plugin
        data.put("pluginVersion", pluginVersion); // Append the version of the plugin
        JSONArray customCharts = new JSONArray();
        for (CustomChart customChart : charts) {
            // Add the data of the custom charts
            JSONObject chart = customChart.getRequestJsonObject();
            if (chart == null) { // If the chart is null, we skip it
                continue;
            }
            customCharts.add(chart);
        }
        data.put("customCharts", customCharts);

        return data;
    }

    /*
     * Gets the server specific data.
     *
     * @return The server specific data.
     */
    private JSONObject getServerData() {
        // Minecraft specific data
        int playerAmount = Bukkit.getOnlinePlayers().size();
        int onlineMode = Bukkit.getOnlineMode() ? 1 : 0;
        String bukkitVersion = org.bukkit.Bukkit.getVersion();
        bukkitVersion = bukkitVersion.substring(bukkitVersion.indexOf("MC: ") + 4, bukkitVersion.length() - 1);

        // OS/Java specific data
        String javaVersion = System.getProperty("java.version");
        String osName = System.getProperty("os.name");
        String osArch = System.getProperty("os.arch");
        String osVersion = System.getProperty("os.version");
        int coreCount = Runtime.getRuntime().availableProcessors();

        JSONObject data = new JSONObject();

        data.put("serverUUID", serverUUID);

        data.put("playerAmount", playerAmount);
        data.put("onlineMode", onlineMode);
        data.put("bukkitVersion", bukkitVersion);

        data.put("javaVersion", javaVersion);
        data.put("osName", osName);
        data.put("osArch", osArch);
        data.put("osVersion", osVersion);
        data.put("coreCount", coreCount);

        return data;
    }

    /*
     * Collects the data and sends it afterwards.
     */
    private void submitData() {
        final JSONObject data = getServerData();

        JSONArray pluginData = new JSONArray();
        // Search for all other bStats Metrics classes to get their plugin data
        for (Class<?> service : Bukkit.getServicesManager().getKnownServices()) {
            try {
                service.getField("B_STATS_VERSION"); // Our identifier :)
            } catch (NoSuchFieldException ignored) {
                continue; // Continue "searching"
            }
            // Found one!
            try {
                pluginData.add(service.getMethod("getPluginData").invoke(Bukkit.getServicesManager().load(service)));
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) { }
        }

        data.put("plugins", pluginData);

        // Create a new thread for the connection to the bStats server
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Send the data
                    sendData(data);
                } catch (Exception e) {
                    // Something went wrong! :(
                    if (logFailedRequests) {
                        plugin.getLogger().log(Level.WARNING, "Could not submit plugin stats of " + plugin.getName(), e);
                    }
                }
            }
        }).start();
    }

    /*
     * Sends the data to the bStats server.
     *
     * @param data The data to send.
     * @throws Exception If the request failed.
     */
    private static void sendData(JSONObject data) throws Exception {
        try
        {
            if (data == null) {
                throw new IllegalArgumentException("Data cannot be null!");
            }
            if (Bukkit.isPrimaryThread()) {
                throw new IllegalAccessException("This method must not be called from the main thread!");
            }
            HttpsURLConnection connection = (HttpsURLConnection) new URL(URL).openConnection();

            // Compress the data to save bandwidth
            byte[] compressedData = compress(data.toString());

            // Add headers
            connection.setRequestMethod("POST");
            connection.addRequestProperty("Accept", "application/json");
            connection.addRequestProperty("Connection", "close");
            connection.addRequestProperty("Content-Encoding", "gzip"); // We gzip our request
            connection.addRequestProperty("Content-Length", String.valueOf(compressedData.length));
            connection.setRequestProperty("Content-Type", "application/json"); // We send our data in JSON format
            connection.setRequestProperty("User-Agent", "MC-Server/" + B_STATS_VERSION);

            // Send data
            connection.setDoOutput(true);
            DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
            outputStream.write(compressedData);
            outputStream.flush();
            outputStream.close();

            connection.getInputStream().close(); // We don't care about the response - Just send our data :)
        }
        catch (IOException ignored) {}
    }

    /*
     * Gzips the given String.
     *
     * @param str The string to gzip.
     * @return The gzipped String.
     * @throws IOException If the compression failed.
     */
    private static byte[] compress(final String str) throws IOException {
        if (str == null) {
            return null;
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(outputStream);
        gzip.write(str.getBytes("UTF-8"));
        gzip.close();
        return outputStream.toByteArray();
    }

    /*
     * Represents a custom chart.
     */
    public static abstract class CustomChart {

        // The id of the chart
        protected final String chartId;

        /*
         * Class constructor.
         *
         * @param chartId The id of the chart.
         */
        public CustomChart(String chartId) {
            if (chartId == null || chartId.isEmpty()) {
                throw new IllegalArgumentException("ChartId cannot be null or empty!");
            }
            this.chartId = chartId;
        }

        protected JSONObject getRequestJsonObject() {
            JSONObject chart = new JSONObject();
            chart.put("chartId", chartId);
            try {
                JSONObject data = getChartData();
                if (data == null) {
                    // If the data is null we don't send the chart.
                    return null;
                }
                chart.put("data", data);
            } catch (Throwable t) {
                if (logFailedRequests) {
                    Bukkit.getLogger().log(Level.WARNING, "Failed to get data for custom chart with id " + chartId, t);
                }
                return null;
            }
            return chart;
        }

        protected abstract JSONObject getChartData();

    }

    /*
     * Represents a custom simple pie.
     */
    public static abstract class SimplePie extends CustomChart {

        /*
         * Class constructor.
         *
         * @param chartId The id of the chart.
         */
        public SimplePie(String chartId) {
            super(chartId);
        }

        /*
         * Gets the value of the pie.
         *
         * @return The value of the pie.
         */
        public abstract String getValue();

        @Override
        protected JSONObject getChartData() {
            JSONObject data = new JSONObject();
            String value = getValue();
            if (value == null || value.isEmpty()) {
                // Null = skip the chart
                return null;
            }
            data.put("value", value);
            return data;
        }
    }

}