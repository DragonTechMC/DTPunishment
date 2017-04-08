package me.morpheus.dtpunishment.configuration;

import java.util.HashMap;
import java.util.Map;

import com.google.common.reflect.TypeToken;
import com.google.inject.Singleton;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@Singleton
@ConfigSerializable
public class MainConfig {
    public static final TypeToken<MainConfig> TYPE = TypeToken.of(MainConfig.class);

    @Setting
    public Database database;

    @Setting
    public Punishment punishment;

    public MainConfig() {
        database = new MainConfig.Database();
        punishment = new MainConfig.Punishment();
    }

    @ConfigSerializable
    public static class Database {

        @Setting
        public Boolean enabled = false;

        @Setting
        public String name;

        @Setting
        public String host;

        @Setting
        public int port;

        @Setting
        public String username;

        @Setting
        public String password;
    }

    @ConfigSerializable
    public static class Punishment {

        public Punishment() {
            banpoints = new HashMap<String, String>();
            banpoints.put("10 banpoints", "1d");
            banpoints.put("20 banpoints", "2d");
            banpoints.put("30 banpoints", "3d");
            banpoints.put("40 banpoints", "4d");
            banpoints.put("50 banpoints", "5d");
            banpoints.put("60 banpoints", "6d");
            banpoints.put("70 banpoints", "7d");
            banpoints.put("80 banpoints", "14d");
            banpoints.put("90 banpoints", "28d");
            banpoints.put("100 banpoints", "168d");

            mutepoints = new HashMap<String, String>();
            mutepoints.put("5 mutepoints", "5m");
            mutepoints.put("10 mutepoints", "10m");
            mutepoints.put("20 mutepoints", "30m");
            mutepoints.put("30 mutepoints", "60m");
            mutepoints.put("40 mutepoints", "+1bp");
            mutepoints.put("50 mutepoints", "+2bp");
            mutepoints.put("60 mutepoints", "+3bp");
            mutepoints.put("70 mutepoints", "+4bp");
            mutepoints.put("80 mutepoints", "+5bp");
            mutepoints.put("90 mutepoints", "+10bp");
            mutepoints.put("100 mutepoints", "+20bp");
            mutepoints.put("110 mutepoints", "+30bp");
            mutepoints.put("120 mutepoints", "+50bp");
            mutepoints.put("130 mutepoints", "+100bp");
        }

        @Setting
        public Map<String, String> banpoints;

        @Setting
        public Map<String, String> mutepoints;
    }
}
