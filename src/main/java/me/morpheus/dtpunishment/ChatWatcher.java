package me.morpheus.dtpunishment;

import ninja.leaping.configurate.ConfigurationNode;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ChatWatcher {

    private DTPunishment main;

    public ChatWatcher (DTPunishment main) {
        this.main = main;
    }

    public boolean containBannedWords(String message){

        ConfigurationNode rootNode = null;
        try {
            rootNode = main.getDefaultConfigLoader().load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String str = rootNode.getNode("chat", "banned", "words").getString();
        List<String> list  = Arrays.asList(str.split(","));

        for (String l : list) {
            if (message.replaceAll("[^\\w]", "").toLowerCase().trim().contains(l)){
                return true;
            }
        }
        return false;
    }

    public boolean containUppercase(String message) {
        String[] words = message.split("\\s+");
        for (String word : words) {
            if (word.length() > 3 && StringUtils.isAllUpperCase(word.replaceAll("[^\\w]", ""))) {
                return true;
            }
        }
        return false;
    }







}
