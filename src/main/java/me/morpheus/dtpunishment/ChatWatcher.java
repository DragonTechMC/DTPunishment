package me.morpheus.dtpunishment;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.entity.living.player.Player;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatWatcher {

    private DTPunishment main;

    public ChatWatcher (DTPunishment main) {
        this.main = main;
    }

    ConfigurationNode chatNode = null;

    public boolean containBannedWords(String message){

        Path potentialFile = Paths.get(main.getConfigPath() + "\\chat.conf");
        ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder().setPath(potentialFile).build();
        try {
            this.chatNode = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String str = chatNode.getNode("banned", "words").getString();
        List<String> list  = Arrays.asList(str.split(","));

        for (String s : list) {
            if (message.replaceAll("[^\\w]", "").toLowerCase().trim().contains(s)){
                return true;
            }
        }
        return false;
    }

    public boolean containUppercase(String message) {

        Path potentialFile = Paths.get(main.getConfigPath() + "\\chat.conf");
        ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder().setPath(potentialFile).build();
        try {
            this.chatNode = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int minimum = chatNode.getNode("caps", "minimum_lenght").getInt();
        int percentage = chatNode.getNode("caps", "percentage").getInt();

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

    //God, forgive me, I will refactor this as soon as I can.
    static Map<Player, ArrayList<String>> map = new HashMap<>();
    static Instant previous;

    public boolean isSpam(String message, Player author, Instant last) {

        Path potentialFile = Paths.get(main.getConfigPath() + "\\chat.conf");
        ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder().setPath(potentialFile).build();
        try {
            this.chatNode = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ArrayList<String> me = new ArrayList<>();

        int seconds = chatNode.getNode("spam", "seconds").getInt();

        if (previous == null || last.isAfter(previous.plusSeconds(seconds))) {
            map.clear();
        }

        previous = last;

        if (map.get(author) != null) {
            map.get(author).add(message);
        } else {
            me.add(message);
            map.put(author, me);
            return false;
        }


        int count = 0;
        for (String str : map.get(author)) {
            if (str.equalsIgnoreCase(message)) {
                count++;
            }
        }

        int max = chatNode.getNode("spam", "max_messages").getInt() + 1;

        return count >= max;
    }







}
