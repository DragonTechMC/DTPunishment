package me.morpheus.dtpunishment;

import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ChatWatcher {

    private DTPunishment main;

    public ChatWatcher (DTPunishment main) {
        this.main = main;
    }

    private static Map<UUID, List<String>> map = new HashMap<>();
    private static Instant previous;

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

    public boolean isSpam(String message, UUID author) {

        Instant now = Instant.now();
        message = StringUtils.lowerCase(message);

        int seconds = main.getChatConfig().spam.seconds;

        if (previous == null || now.isAfter(previous.plusSeconds(seconds))) map.clear();

        previous = now;

        if (map.get(author) != null) {
            map.get(author).add(message);
        } else {
            List<String> messages = new ArrayList<>();
            messages.add(message);
            map.put(author, messages);
            return false;
        }

        int count = Collections.frequency(map.get(author), message);

        int max = main.getChatConfig().spam.max_messages + 1;

        return count >= max;
    }







}
