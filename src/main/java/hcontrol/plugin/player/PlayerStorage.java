package hcontrol.plugin.player;

import org.bukkit.configuration.file.YamlConfiguration;

import hcontrol.plugin.stats.StatType;

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
            return null;
        }

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        PlayerProfile profile = new PlayerProfile(uuid);
        
        // load co ban
        profile.setLevel(yaml.getInt("level", 1));
        profile.setExp(yaml.getLong("exp", 0L));
        profile.setStatPoints(yaml.getInt("statPoints", 0));
        
        // load stats (primary)
        var stats = profile.getStats();
        var container = stats.getStatContainer();
        
        container.setBase(StatType.STRENGTH, yaml.getInt("stats.str", 2));
        container.setBase(StatType.AGILITY, yaml.getInt("stats.agi", 2));
        container.setBase(StatType.INTELLIGENCE, yaml.getInt("stats.int", 2));
        container.setBase(StatType.VITALITY, yaml.getInt("stats.vit", 2));
        container.setBase(StatType.LUCK, yaml.getInt("stats.lck", 2));
        
        // load HP/Mana
        profile.setCurrentHP(yaml.getDouble("currentHP", stats.getMaxHP()));
        profile.setCurrentMana(yaml.getDouble("currentMana", stats.getMaxMana()));

        return profile;
    }

    public void save(PlayerProfile profile) {
        File file = new File(dataFolder, profile.getUuid().toString() + ".yml");
        YamlConfiguration yaml = new YamlConfiguration();

        // save co ban
        yaml.set("level", profile.getLevel());
        yaml.set("exp", profile.getExp());
        yaml.set("statPoints", profile.getStatPoints());
        
        // save stats (primary)
        var stats = profile.getStats();
        var container = stats.getStatContainer();
        
        yaml.set("stats.str", container.getBase(StatType.STRENGTH));
        yaml.set("stats.agi", container.getBase(StatType.AGILITY));
        yaml.set("stats.int", container.getBase(StatType.INTELLIGENCE));
        yaml.set("stats.vit", container.getBase(StatType.VITALITY));
        yaml.set("stats.lck", container.getBase(StatType.LUCK));
        
        // save HP/Mana
        yaml.set("currentHP", profile.getCurrentHP());
        yaml.set("currentMana", profile.getCurrentMana());

        try {
            yaml.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
       
}
