package me.morpheus.dtpunishment.configuration;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.Map;

@ConfigSerializable
public class DTPunishmentConfig {


    public static final TypeToken<DTPunishmentConfig> TYPE = TypeToken.of(DTPunishmentConfig.class);

    @Setting public Database database = new Database();
    @Setting public punishment punishment = new punishment();

    @ConfigSerializable
    public static class Database {
        @Setting public Boolean enabled = false;
        @Setting public String name;
        @Setting public String host;
        @Setting public int port;
        @Setting public String username;
        @Setting public String password;
    }

    @ConfigSerializable
    public static class punishment {
        @Setting public Map<String, String> banpoints;
        @Setting public Map<String, String> mutepoints;
    }






}
