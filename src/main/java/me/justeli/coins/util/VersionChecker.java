package me.justeli.coins.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.time.Instant;
import java.util.Optional;

/* Eli @ February 04, 2022 (creation) */
public final class VersionChecker
{
    private Version latestVersion;

    public Optional<Version> latestVersion ()
    {
        return Optional.of(this.latestVersion);
    }

    public VersionChecker (String repository)
    {
        try
        {
            URL url = new URL("https://api.github.com/repos/" + repository + "/releases/latest");
            URLConnection request = url.openConnection();

            request.setReadTimeout(1000);
            request.setConnectTimeout(1000);
            request.connect();

            JsonParser jsonParser = new JsonParser();
            try (InputStream inputStream = (InputStream) request.getContent();
                 InputStreamReader reader = new InputStreamReader(inputStream))
            {
                JsonElement root = jsonParser.parse(reader);
                JsonObject jsonObject = root.getAsJsonObject();
                this.latestVersion = new Version(
                    jsonObject.get("tag_name").getAsString(),
                    jsonObject.get("prerelease").getAsBoolean(),
                    jsonObject.get("name").getAsString(),
                    jsonObject.get("published_at").getAsString()
                );
            }
        }
        catch (Exception ignored) {}
    }

    public static class Version
    {
        private final String tag;
        private final boolean preRelease;
        private final String name;
        private final long time;

        public Version (String tag, boolean preRelease, String name, String time)
        {
            this.tag = tag;
            this.preRelease = preRelease;
            this.name = name;
            this.time = Instant.parse(time).toEpochMilli();
        }

        public String tag ()
        {
            return this.tag;
        }

        public boolean preRelease ()
        {
            return this.preRelease;
        }

        public String name ()
        {
            return this.name;
        }

        public long time ()
        {
            return this.time;
        }

        @Override
        public boolean equals (Object version)
        {
            return this.tag == null || !(version instanceof Version)
                ? super.equals(version)
                : this.tag.equals(((Version) version).tag);
        }
    }
}
