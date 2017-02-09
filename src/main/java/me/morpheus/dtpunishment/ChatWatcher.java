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

    ConfigurationNode rootNode = null;


    public boolean containBannedWords(String message){

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

        try {
            rootNode = main.getDefaultConfigLoader().load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int minimum = rootNode.getNode("chat", "caps", "minimum_lenght").getInt();
        int percentage = rootNode.getNode("chat", "caps", "percentage").getInt();

        String[] words = message.split("\\s+");
        for (String word : words) {
            if (word.length() > minimum) {
                if (StringUtils.isAllUpperCase(word.replaceAll("[^\\w]", ""))) {
                    return true;
                } else if (!StringUtils.isAllLowerCase(word.replaceAll("[^\\w]", ""))) {
                    int count = 0;
                    for (int i = 0; i < word.replaceAll("[^\\w]", "").length(); i++) {
                        if (Character.isUpperCase(word.replaceAll("[^\\w]", "").charAt(i))) count++;
                    }
                    int max = word.replaceAll("[^\\w]", "").length() / (100/percentage);
                    if (count > max) return true;
                }
            }
        }
        return false;
    }







}
