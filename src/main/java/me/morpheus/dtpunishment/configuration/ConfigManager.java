package me.morpheus.dtpunishment.configuration;

import com.google.common.reflect.TypeToken;
import me.morpheus.dtpunishment.DTPunishment;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.GuiceObjectMapperFactory;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;

import java.io.File;
import java.nio.file.Path;

/**
 * Created by Frani on 12/01/2018.
 */
public class ConfigManager<T> {

    private ConfigurationLoader<CommentedConfigurationNode> loader;
    private CommentedConfigurationNode config;
    private T node;
    private Class<T> configInstance;
    private String file;
    private File configDir;
    private GuiceObjectMapperFactory mapper;

    public ConfigManager(Class<T> configInstance, String file, File configDir, GuiceObjectMapperFactory mapper) {
        this.configInstance = configInstance;
        this.file = file;
        this.configDir = configDir;
        this.mapper = mapper;
        load();
    }

    @SuppressWarnings("unchecked")
    public void load() {
        try {
            if (!configDir.exists()) {
                configDir.mkdirs();
            }
            File c = new File(configDir, file);
            if (!c.exists()) c.createNewFile();
            this.loader = HoconConfigurationLoader.builder().setFile(c).build();
            config = loader.load(ConfigurationOptions.defaults()
                    .setSerializers(TypeSerializers.newCollection().registerType(TypeToken.of(PunishmentLength.class), new PunishmentLengthSerializer()))
                    .setShouldCopyDefaults(true));
            node = config.getValue(TypeToken.of(configInstance), configInstance.newInstance());
            loader.save(config);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public T getConfig() {
        return this.node;
    }

}
