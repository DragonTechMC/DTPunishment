package me.morpheus.dtpunishment.listener;

import me.morpheus.dtpunishment.DTPunishment;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class PlayerListener {

    private DTPunishment main;

    public PlayerListener(DTPunishment main){
        this.main = main;
    }

    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join event) {
        Optional<Player> player = event.getCause().first(Player.class);
        if (player.isPresent()) {
            Player p = event.getTargetEntity();
            p.sendMessage(Text.of(TextColors.AQUA, TextStyles.BOLD, "Hi " + p.getName()));

            Path playerData = Paths.get(main.getConfigPath() + "/data/" + p.getName() + ".conf");

            if(!playerData.toFile().exists()){
                main.getLogger().info("No data file has been found for " + p.getName());
                main.getLogger().info("Creating player file");
                try {
                    Files.createFile(playerData);
                    main.getLogger().info("Success");
                    ConfigurationLoader<CommentedConfigurationNode> loader =
                            HoconConfigurationLoader.builder().setPath(playerData).build();
                    ConfigurationNode rootNode = loader.createEmptyNode(ConfigurationOptions.defaults());
                    rootNode.getNode("points", "banpoints").setValue(0);
                    rootNode.getNode("points", "mutepoints").setValue(0);
                    loader.save(rootNode);
                } catch (IOException e) {
                    main.getLogger().info("Error while creating player file");
                    e.printStackTrace();
                }
            }
        }
    }
}
