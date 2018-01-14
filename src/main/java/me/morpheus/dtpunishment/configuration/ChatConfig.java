package me.morpheus.dtpunishment.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import com.google.inject.Singleton;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class ChatConfig {

	@Setting(comment = "settings governing banned words and their punishments")
	public Banned banned = new Banned();

	@Setting(comment = "settings governing SHOUTING IN CAPS and its punishments")
	public Caps caps = new Caps();

	@Setting(comment = "settings governing spamming spamming spamming and its punishments")
	public Spam spam = new Spam();

	@Setting(comment = "settings governing spamming the same character oooooooooover and oooooooooooooover")
	public CharacterSpam characterspam = new CharacterSpam();

	@ConfigSerializable
	public static class Banned {

	    public Banned() {
            replacer.put("bitch", "cute");
        }

		@Setting(comment = "number of mutepoints a user receives for saying a banned word")
		public int mutepoints = 4;

		@Setting(comment = "list of words that are banned on the server - e.g. [naughty, words, go, here]")
		public List<String> words = new ArrayList<String>();

		@Setting(comment = "if we should replace the banned words with ****. if true, the message will be sent but with" +
                "stars (****) instead of the word. if false, the messagr will be cancelled at all")
        public boolean starBadWords = false;

        @Setting(comment = "the replacer for words. Words that are here (key) will be replaced with its respective value\n" +
                "when someone sends a message with them. Words here MUST be in the words list above too. This takes priority over starring bad words.")
        public Map<String, String> replacer = Maps.newHashMap();
	}

	@ConfigSerializable
	public static class Caps {
		@Setting(comment = "number of mutepoints a user receives for using too many caps")
		public int mutepoints = 4;

		@Setting(comment = "the minimum length of a message to qualify for too many caps")
		public int minimum_length = 4;

		@Setting(comment = "the percentage of caps required in the message to trigger a punishment")
		public int percentage = 50;
	}

	@ConfigSerializable
	public static class Spam {
		@Setting(comment = "the maximum number of identical messages a user can say in the spam period")
		public int max_messages = 3;

		@Setting(comment = "how many seconds to watch for spam over")
		public int seconds = 10;

		@Setting(comment = "number of mutepoints a user receives each time they spam")
		public int mutepoints = 1;
	}

	@ConfigSerializable
	public static class CharacterSpam {
		@Setting(comment = "the number of repeated characters to consider character spam")
		public int repeated_characters = 10;

		@Setting(comment = "number of mutepoints a user receives each time they spam")
		public int mutepoints = 1;
	}
}
