package me.morpheus.dtpunishment;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import me.morpheus.dtpunishment.configuration.ChatConfig;

@Singleton
public class WordChecker {

	private ChatConfig chatConfig;

	private Map<UUID, List<String>> map = new HashMap<UUID, List<String>>();

	private Instant previous;

	private Map<UUID, String> lastBadSentence = new HashMap<UUID, String>();

	private Pattern bannedWordsRegexPattern;

	@Inject
	public WordChecker(ChatConfig chatConfig) {
		this.chatConfig = chatConfig;

		buildWordList();
	}

	public void buildWordList() {
		// Builds the regex that we will check against the incoming user
		// messages - should be run each time a word is added or
		// the config is refreshed

		StringBuilder sb = new StringBuilder();
		sb.append("(");

		for (String s : chatConfig.banned.words) {

			if (sb.length() > 1)
				sb.append("|");

			// Is partial?
			if (s.startsWith("*")) {
				sb.append(s.substring(1));
			} else {
				sb.append("\\b" + s + "\\b");
			}
		}

		sb.append(")");

		bannedWordsRegexPattern = Pattern.compile(sb.toString(), Pattern.CASE_INSENSITIVE);
	}

	public boolean containsBannedWords(String message) {
		return bannedWordsRegexPattern.matcher(message).find();
	}

	public String getBannedWord(String message) {
		Matcher matcher = bannedWordsRegexPattern.matcher(message);
		if (matcher.find()) {
			return matcher.group(1);
		}

		return null;
	}

	public boolean containsUppercase(String message) {

		if (message.replaceAll("[\\W]", "").length() <= chatConfig.caps.minimum_length)
			return false;

		String[] words = message.split("\\s+");
		int upper = 0;
		int total = 0;

		for (String word : words) {
			String cleaned = word.replaceAll("[\\W]", "");

			total += cleaned.length();

			if (StringUtils.isAllLowerCase(cleaned))
				continue;

			for (int i = 0; i < cleaned.length(); i++) {
				if (Character.isUpperCase(cleaned.charAt(i)))
					upper++;
			}

		}
		int max = (chatConfig.caps.percentage * total) / 100;
		return upper > max;

	}

	public boolean isSpam(String message, UUID author) {

		Instant now = Instant.now();
		message = StringUtils.lowerCase(message);

		if (previous == null || now.isAfter(previous.plusSeconds(chatConfig.spam.seconds)))
			map.clear();

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

		return count > chatConfig.spam.max_messages;
	}

}
