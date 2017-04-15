package me.morpheus.dtpunishment.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import junit.framework.TestCase;
import me.morpheus.dtpunishment.WordChecker;
import me.morpheus.dtpunishment.configuration.ChatConfig;

@RunWith(PowerMockRunner.class)
public class WordCheckerTests extends TestCase {

	private ChatConfig chatConfig;

	public void setUp() {
		chatConfig = new ChatConfig();
		// Partial - cannot be in any word
		chatConfig.banned.words.add("*twat");
		// Non-partial - cannot stand alone
		chatConfig.banned.words.add("shit");

		chatConfig.caps.percentage = 50;
		chatConfig.caps.minimum_length = 15;
	}

	@Test
	public void testEmptyWordListDoesntMatchAnything() {
		WordChecker subject = new WordChecker(new ChatConfig());

		// We match bad words
		assertEquals(false, subject.containsBannedWords("some innocent sentence"));
	}

	@Test
	public void testBannedWordsRespectCase() {
		WordChecker subject = new WordChecker(chatConfig);

		// We match bad words
		assertEquals(true, subject.containsBannedWords("shit"));
		assertEquals(true, subject.containsBannedWords("ShIt"));
	}

	@Test
	public void testNonBannedWords() {
		WordChecker subject = new WordChecker(chatConfig);

		// We don't match ok words
		assertEquals(false, subject.containsBannedWords("hello"));
		assertEquals(false, subject.containsBannedWords("world"));
	}

	@Test
	public void testOkSentencesDontTriggerBans() {
		WordChecker subject = new WordChecker(chatConfig);

		// We don't match ok words
		assertEquals(false, subject.containsBannedWords("I just watch people"));
		assertEquals(false, subject.containsBannedWords("Push it, push it real good"));
	}

	public void testPartialsTriggerBans() {
		WordChecker subject = new WordChecker(chatConfig);

		// We match partials
		assertEquals(true, subject.containsBannedWords("I justwatch people"));
		assertEquals(true, subject.containsBannedWords("You megatwat"));
	}

	public void testNonPartialsDontTriggerBans() {
		WordChecker subject = new WordChecker(chatConfig);

		// We don't match non partials inside words
		assertEquals(false, subject.containsBannedWords("Pushit, pushit real good"));
		assertEquals(false, subject.containsBannedWords("Oh dear, you missed, you could say it's a mishit"));
	}

	@Test
	public void testCaps() {
		WordChecker subject = new WordChecker(chatConfig);

		assertEquals(true, subject.containsUppercase("TEST SOME CAPS OMGGGG!"));
		assertEquals(true, subject.containsUppercase("message with more THAN FIFTY PERCENT CAPS OMG!"));
		assertEquals(false, subject.containsUppercase("message with more less than fifty PERCENT CAPS OMG!"));

		// too short to trigger filter
		assertEquals(false, subject.containsUppercase("TOO SHORT!"));
		assertEquals(true, subject.containsUppercase("LONGER THAN TOO SHORT!"));
	}

	@Test
	public void testCharacterSpam() {
		WordChecker subject = new WordChecker(chatConfig);

		assertEquals(true, subject.isCharacterSpam("Noooooooooooooooo!"));
		assertEquals(false, subject.isCharacterSpam("I once saw a huge cow go 'moo moo' over the moon"));
	}
}
