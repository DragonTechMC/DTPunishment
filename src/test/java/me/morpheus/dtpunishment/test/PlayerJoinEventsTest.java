package me.morpheus.dtpunishment.test;

import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.spongepowered.api.Server;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import junit.framework.TestCase;
import me.morpheus.dtpunishment.WordChecker;
import me.morpheus.dtpunishment.configuration.ChatConfig;
import me.morpheus.dtpunishment.data.ChatOffenceData;
import me.morpheus.dtpunishment.data.DataStore;
import me.morpheus.dtpunishment.listeners.PlayerListener;
import me.morpheus.dtpunishment.penalty.MutepointsPunishment;

@RunWith(PowerMockRunner.class)
public class PlayerJoinEventsTest extends TestCase {

	private Logger mockLogger;
	private DataStore mockDataStore;
	private WordChecker mockWordChecker;
	private ChatConfig mockChatConfig;
	private MutepointsPunishment mockMutePunish;
	private Player mockPlayer;
	private ClientConnectionEvent.Join mockJoinEvent;
	private Server mockServer;
	private ChatOffenceData mockChatOffenceData;

	public void setUp() throws Exception {

		// Mock the logger
		mockLogger = mock(Logger.class);

		// Mock the data store
		mockDataStore = mock(DataStore.class);
		// All points were 6 months in the past
		when(mockDataStore.getBanpointsUpdatedAt(any(UUID.class))).thenReturn(LocalDate.now().minusMonths(6));
		when(mockDataStore.getMutepointsUpdatedAt(any(UUID.class))).thenReturn(LocalDate.now().minusMonths(6));
		// Setup some base level values
		when(mockDataStore.getBanpoints(any(UUID.class))).thenReturn(0);
		when(mockDataStore.getMutepoints(any(UUID.class))).thenReturn(0);

		mockPlayer = mock(Player.class);
		when(mockPlayer.getName()).thenReturn("SomePlayer");
		when(mockPlayer.getUniqueId()).thenReturn(UUID.randomUUID());

		// Make sure the connection event returns the mocked player
		mockJoinEvent = mock(ClientConnectionEvent.Join.class);
		when(mockJoinEvent.getTargetEntity()).thenReturn(mockPlayer);

		mockChatOffenceData = mock(ChatOffenceData.class);
		mockServer = mock(Server.class);

		mockWordChecker = mock(WordChecker.class);
	}

	@Test
	public void testOnPlayerPreJoinKicksPlayersWithBadNames() {
		// Player doesn't exist
		User mockUser = mock(User.class);
		when(mockUser.getName()).thenReturn("Youtwatface");

		ClientConnectionEvent.Login mockPreJoinEvent = mock(ClientConnectionEvent.Login.class);
		when(mockPreJoinEvent.getTargetUser()).thenReturn(mockUser);

		ChatConfig config = new ChatConfig();
		config.banned.words.add("*twat");

		WordChecker wordChecker = new WordChecker(config);

		// Create the listener
		PlayerListener subject = new PlayerListener(mockLogger, mockDataStore, wordChecker, config, mockMutePunish,
				mockServer, mockChatOffenceData);

		// Check if they joined they got created
		subject.onPlayerPreJoin(mockPreJoinEvent);

		verify(mockPreJoinEvent).setMessage(any());
		verify(mockPreJoinEvent).setCancelled(true);
	}

	@Test
	public void testOnPlayerPreJoinDoesntKickPlayersWithFullBadWordMatchesInGoodNames() {
		// Player doesn't exist
		User mockUser = mock(User.class);
		when(mockUser.getName()).thenReturn("Pushit");

		ClientConnectionEvent.Login mockPreJoinEvent = mock(ClientConnectionEvent.Login.class);
		when(mockPreJoinEvent.getTargetUser()).thenReturn(mockUser);

		ChatConfig config = new ChatConfig();
		config.banned.words.add("shit");

		WordChecker wordChecker = new WordChecker(config);

		// Create the listener
		PlayerListener subject = new PlayerListener(mockLogger, mockDataStore, wordChecker, config, mockMutePunish,
				mockServer, mockChatOffenceData);

		// Check if they joined they got created
		subject.onPlayerPreJoin(mockPreJoinEvent);

		verify(mockPreJoinEvent, never()).setCancelled(true);
	}

	@Test
	public void testOnPlayerJoinCreatesPlayerData() {
		// Player doesn't exist
		when(mockDataStore.userExists(any(UUID.class))).thenReturn(false);

		// Create the listener
		PlayerListener subject = new PlayerListener(mockLogger, mockDataStore, mockWordChecker, mockChatConfig,
				mockMutePunish, mockServer, mockChatOffenceData);

		// Check if they joined they got created
		subject.onPlayerJoin(mockJoinEvent);

		verify(mockDataStore).createUser(any(UUID.class));
	}

	@Test
	public void testOnExistingPlayerJoinRemovesFiveMutepoints() {
		// Player exists
		when(mockDataStore.userExists(any(UUID.class))).thenReturn(true);

		// Create the listener
		PlayerListener subject = new PlayerListener(mockLogger, mockDataStore, mockWordChecker, mockChatConfig,
				mockMutePunish, mockServer, mockChatOffenceData);

		// player has more than 5 mutepoints, 5 should be removed
		when(mockDataStore.getMutepoints(any(UUID.class))).thenReturn(10);

		subject.onPlayerJoin(mockJoinEvent);
		verify(mockDataStore).removeMutepoints(mockPlayer.getUniqueId(), 5);
	}

	@Test
	public void testOnExistingPlayerJoinRemovesRemainingMutepoints() {
		// Player exists
		when(mockDataStore.userExists(any(UUID.class))).thenReturn(true);

		// Create the listener
		PlayerListener subject = new PlayerListener(mockLogger, mockDataStore, mockWordChecker, mockChatConfig,
				mockMutePunish, mockServer, mockChatOffenceData);

		// player has less than 5 mutepoints, that amount should be removed
		when(mockDataStore.getMutepoints(any(UUID.class))).thenReturn(4);

		subject.onPlayerJoin(mockJoinEvent);
		verify(mockDataStore).removeMutepoints(mockPlayer.getUniqueId(), 4);
	}

	@Test
	public void testOnExistingPlayerJoinRemovesOneBanpoint() {
		// Player exists
		when(mockDataStore.userExists(any(UUID.class))).thenReturn(true);

		// Create the listener
		PlayerListener subject = new PlayerListener(mockLogger, mockDataStore, mockWordChecker, mockChatConfig,
				mockMutePunish, mockServer, mockChatOffenceData);

		// player has more than 5 mutepoints, that amount should be removed
		when(mockDataStore.getBanpoints(any(UUID.class))).thenReturn(5);

		subject.onPlayerJoin(mockJoinEvent);
		verify(mockDataStore).removeBanpoints(mockPlayer.getUniqueId(), 1);
	}

}
