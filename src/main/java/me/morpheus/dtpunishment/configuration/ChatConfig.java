package me.morpheus.dtpunishment.configuration;

import java.util.Collections;
import java.util.List;

import com.google.common.reflect.TypeToken;
import com.google.inject.Singleton;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@Singleton
@ConfigSerializable
public class ChatConfig {

    public static final TypeToken<ChatConfig> TYPE = TypeToken.of(ChatConfig.class);

    @Setting
    public Banned banned;

    @Setting
    public Caps caps;

    @Setting
    public Spam spam;

    public ChatConfig() {
        banned = new ChatConfig.Banned();
        caps = new ChatConfig.Caps();
        spam = new ChatConfig.Spam();
    }

    @ConfigSerializable
    public static class Banned {
        @Setting
        public int mutepoints = 4;

        @Setting
        public List<String> words = Collections.emptyList();
    }

    @ConfigSerializable
    public static class Caps {
        @Setting
        public int mutepoints = 4;

        @Setting
        public int minimum_length = 3;

        @Setting
        public int percentage = 50;
    }

    @ConfigSerializable
    public static class Spam {
        @Setting
        public int max_messages = 3;

        @Setting
        public int seconds = 10;

        @Setting
        public int mutepoints = 1;
    }
}
