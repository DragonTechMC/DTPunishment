package me.morpheus.dtpunishment.test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.spongepowered.api.Server;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.text.Text;
import junit.framework.TestCase;
import me.morpheus.dtpunishment.WordChecker;
import me.morpheus.dtpunishment.configuration.ChatConfig;
import me.morpheus.dtpunishment.data.DataStore;
import me.morpheus.dtpunishment.listeners.PlayerListener;
import me.morpheus.dtpunishment.penalty.MutepointsPunishment;

@RunWith(PowerMockRunner.class)
public class PlayerChatEventsTest extends TestCase {

    private Logger mockLogger;
    private DataStore mockDataStore;
    private WordChecker mockChatWatcher;
    private ChatConfig mockChatConfig;
    private MutepointsPunishment mockMutePunish;
    private Player mockPlayer;
    private Player mockOtherPlayer;
    private MessageChannelEvent.Chat mockChatEvent;
    private Text text;
    private Server mockServer;

    public void setUp() throws Exception {

        // Set the serializer
        TestPlainTextSerializer.inject();
        text = Text.of("Hello world!");

        // Mock the logger
        mockLogger = mock(Logger.class);

        // Mock the data store
        mockDataStore = mock(DataStore.class);
        // Setup some base level values
        when(mockDataStore.getBanpoints(any(UUID.class))).thenReturn(0);
        when(mockDataStore.getMutepoints(any(UUID.class))).thenReturn(0);

        mockPlayer = mock(Player.class);
        when(mockPlayer.getName()).thenReturn("SomePlayer");
        when(mockPlayer.getUniqueId()).thenReturn(UUID.randomUUID());
        when(mockPlayer.hasPermission(anyString())).thenReturn(true);

        mockOtherPlayer = mock(Player.class);
        when(mockOtherPlayer.getName()).thenReturn("OtherPlayer");
        when(mockOtherPlayer.getUniqueId()).thenReturn(UUID.randomUUID());

        mockChatEvent = mock(MessageChannelEvent.Chat.class);
        when(mockChatEvent.getRawMessage()).thenReturn(text);
        when(mockChatEvent.getMessage()).thenReturn(text);

        mockChatWatcher = mock(WordChecker.class);

        mockChatConfig = spy(ChatConfig.class);

        ArrayList<Player> onlinePlayers = new ArrayList<Player>();
        onlinePlayers.add(mockPlayer);
        onlinePlayers.add(mockOtherPlayer);

        mockMutePunish = mock(MutepointsPunishment.class);

        // Mock the server
        mockServer = mock(Server.class);
        when(mockServer.getOnlinePlayers()).thenReturn(onlinePlayers);
    }

    @Test
    public void testOnPlayerChatMutesMutedPlayerAndLogsToLogger() {
        PlayerListener subject = new PlayerListener(mockLogger, mockDataStore, mockChatWatcher, mockChatConfig,
                mockMutePunish, mockServer);

        when(mockDataStore.isMuted(mockPlayer.getUniqueId())).thenReturn(true);
        when(mockDataStore.getExpiration(mockPlayer.getUniqueId())).thenReturn(Instant.now().plusSeconds(3000));

        subject.onPlayerChat(mockChatEvent, mockPlayer);

        // Verify message cancelled
        verify(mockChatEvent).setMessageCancelled(true);
        // The player got notified
        verify(mockPlayer).sendMessage(any(Text.class));
        // The message got logged
        verify(mockLogger).info("[Message cancelled] - " + text.toPlain());
    }

    @Test
    public void testOnPlayerChatUnmutesExpiredMute() {
        PlayerListener subject = new PlayerListener(mockLogger, mockDataStore, mockChatWatcher, mockChatConfig,
                mockMutePunish, mockServer);

        when(mockDataStore.isMuted(mockPlayer.getUniqueId())).thenReturn(true);
        when(mockDataStore.getExpiration(mockPlayer.getUniqueId())).thenReturn(Instant.now().minusSeconds(3000));

        subject.onPlayerChat(mockChatEvent, mockPlayer);

        // The player got unmuted
        verify(mockDataStore).unmute(mockPlayer.getUniqueId());
    }

    @Test
    public void testOnPlayerSpam() {
        PlayerListener subject = new PlayerListener(mockLogger, mockDataStore, mockChatWatcher, mockChatConfig,
                mockMutePunish, mockServer);

        when(mockChatWatcher.isSpam(anyString(), any(UUID.class))).thenReturn(true);

        subject.onPlayerChat(mockChatEvent, mockPlayer);

        // The player got points
        verify(mockDataStore).addMutepoints(any(UUID.class), anyInt());
        // The message is cancelled
        verify(mockChatEvent).setMessageCancelled(true);
        // The message got logged
        verify(mockLogger).info("[Message cancelled (spam)] - " + text.toPlain());
        // Mutepoint punishment was checked
        verify(mockMutePunish).check(any(UUID.class), anyInt());
    }

    @Test
    public void testOnPlayerUppercase() {
        PlayerListener subject = new PlayerListener(mockLogger, mockDataStore, mockChatWatcher, mockChatConfig,
                mockMutePunish, mockServer);

        when(mockChatWatcher.containsUppercase(anyString())).thenReturn(true);

        subject.onPlayerChat(mockChatEvent, mockPlayer);

        // The player got points
        verify(mockDataStore).addMutepoints(any(UUID.class), anyInt());
        // The message is cancelled
        verify(mockChatEvent).setMessageCancelled(true);
        // The message got logged
        verify(mockLogger).info("[Message cancelled (uppercase)] - " + text.toPlain());
        // Mutepoint punishment was checked
        verify(mockMutePunish).check(any(UUID.class), anyInt());
    }

    @Test
    public void testOnPlayerBannedWords() {
        PlayerListener subject = new PlayerListener(mockLogger, mockDataStore, mockChatWatcher, mockChatConfig,
                mockMutePunish, mockServer);

        when(mockChatWatcher.containsBannedWords(anyString())).thenReturn(true);

        subject.onPlayerChat(mockChatEvent, mockPlayer);

        // The player got points
        verify(mockDataStore).addMutepoints(any(UUID.class), anyInt());
        // The message is cancelled
        verify(mockChatEvent).setMessageCancelled(true);
        // The message got logged
        verify(mockLogger).info("[Message cancelled (banned words)] - " + text.toPlain());
        // Mutepoint punishment was checked
        verify(mockMutePunish).check(any(UUID.class), anyInt());
    }

    @Test
    public void testOnPlayerIsGoodConsoleDoesntGetMutepointsMessage() {
        PlayerListener subject = new PlayerListener(mockLogger, mockDataStore, mockChatWatcher, mockChatConfig,
                mockMutePunish, mockServer);

        subject.onPlayerChat(mockChatEvent, mockPlayer);

        verifyZeroInteractions(mockLogger);
    }
}
