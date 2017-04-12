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
		chatConfig.banned.words.add("idiot");
		chatConfig.banned.words.add("noob");

		chatConfig.caps.percentage = 50;
		chatConfig.caps.minimum_length = 15;
	}

	@Test
	public void testBannedWords() {
		WordChecker subject = new WordChecker(chatConfig);

		// We match bad words
		assertEquals(true, subject.containsBannedWords("iDIoT"));
		assertEquals(true, subject.containsBannedWords("idiot"));

		// We don't match ok words
		assertEquals(false, subject.containsBannedWords("nooob"));
		assertEquals(false, subject.containsBannedWords("newb"));
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
}
