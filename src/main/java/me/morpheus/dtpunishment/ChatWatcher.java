package me.morpheus.dtpunishment;

import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.entity.living.player.Player;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatWatcher {

    private DTPunishment main;

    public ChatWatcher (DTPunishment main) {
        this.main = main;
    }

    public boolean containBannedWords(String message){

        List<String> list = main.getChatConfig().banned.words;

        for (String s : list) {
            if (message.replaceAll("(?ix)[\\W]", "").contains(s)) return true;
        }
        return false;
    }

    public boolean containUppercase(String message) {

        int minimum = main.getChatConfig().caps.minimum_lenght;
        int percentage = main.getChatConfig().caps.percentage;

        String[] words = message.split("\\s+");
        for (String word : words) {
            String cleaned = word.replaceAll("[\\W]", "");

            if (word.length() <= minimum || StringUtils.isAllLowerCase(cleaned)) continue;
            if (StringUtils.isAllUpperCase(cleaned)) return true;

            int count = 0;
            for (int i = 0; i < cleaned.length(); i++) {
                if (Character.isUpperCase(cleaned.charAt(i))) count++;
            }
            int max = cleaned.length() / (100/percentage);
            if (count > max) return true;
        }

        return false;
    }

    //God, forgive me, I will refactor this as soon as I can.
    static Map<Player, ArrayList<String>> map = new HashMap<>();
    static Instant previous;

    public boolean isSpam(String message, Player author, Instant last) {



        ArrayList<String> me = new ArrayList<>();

        int seconds = main.getChatConfig().spam.seconds;

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

        int max = main.getChatConfig().spam.max_messages + 1;

        return count >= max;
    }







}
