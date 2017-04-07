package me.morpheus.dtpunishment.configuration;


import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.Collections;
import java.util.List;

@ConfigSerializable
public class ChatConfig {

    public static final TypeToken<ChatConfig> TYPE = TypeToken.of(ChatConfig.class);

    @Setting public Banned banned = new ChatConfig.Banned();
    @Setting public Caps caps = new ChatConfig.Caps();
    @Setting public Spam spam = new ChatConfig.Spam();

    @ConfigSerializable
    public static class Banned {
        @Setting public int mutepoints;
        @Setting public List<String> words = Collections.emptyList();
    }

    @ConfigSerializable
    public static class Caps {
        @Setting public int mutepoints;
        @Setting public int minimum_length;
        @Setting public int percentage;
    }

    @ConfigSerializable
    public static class Spam {
        @Setting public int max_messages;
        @Setting public int seconds;
        @Setting public int mutepoints;
    }
}
