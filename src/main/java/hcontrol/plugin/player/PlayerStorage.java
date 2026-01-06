package hcontrol.plugin.player;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class PlayerStorage {

    private final File dataFolder;

    public PlayerStorage(File dataFolder) {
        this.dataFolder = new File(dataFolder, "players");
        if (!this.dataFolder.exists()) {
            this.dataFolder.mkdirs();
        }
    }

    public PlayerProfile load(UUID uuid) {
        File file = new File(dataFolder, uuid.toString() + ".yml");

        if (!file.exists()) {
            return new PlayerProfile(uuid);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        PlayerProfile profile = new PlayerProfile(uuid);
        profile.setLevel(config.getInt("level", 1));
        profile.setExp(config.getLong("exp", 0));

        return profile;
    }

    public void save(PlayerProfile profile) {
        File file = new File(dataFolder, profile.getUuid().toString() + ".yml");
        FileConfiguration config = new YamlConfiguration();

        config.set("level", profile.getLevel());
        config.set("exp", profile.getExp());

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
