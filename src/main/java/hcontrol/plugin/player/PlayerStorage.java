package hcontrol.plugin.player;

import org.bukkit.configuration.file.YamlConfiguration;

import hcontrol.plugin.model.CultivationRealm;
import hcontrol.plugin.model.SpiritualRoot;
import hcontrol.plugin.model.RootQuality;
import hcontrol.plugin.model.Title;
import hcontrol.plugin.stats.StatType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
        
        // load cultivation realm (tu tien)
        String realmName = yaml.getString("realm", "MORTAL");
        try {
            profile.setRealm(CultivationRealm.valueOf(realmName));
        } catch (IllegalArgumentException e) {
            profile.setRealm(CultivationRealm.MORTAL);
        }
        
        // load realm level & cultivation
        profile.setRealmLevel(yaml.getInt("realmLevel", 1));
        profile.setCultivation(yaml.getLong("cultivation", 0L));
        
        // load spiritual root
        String rootName = yaml.getString("spiritualRoot", "WOOD");
        try {
            profile.setSpiritualRoot(SpiritualRoot.valueOf(rootName));
        } catch (IllegalArgumentException e) {
            profile.setSpiritualRoot(SpiritualRoot.WOOD);
        }
        
        String qualityName = yaml.getString("rootQuality", "MORTAL");
        try {
            profile.setRootQuality(RootQuality.valueOf(qualityName));
        } catch (IllegalArgumentException e) {
            profile.setRootQuality(RootQuality.MORTAL);
        }
        
        // load dao heart & state
        profile.setDaoHeart(yaml.getDouble("daoHeart", 100.0));
        profile.setInnerInjury(yaml.getDouble("innerInjury", 0.0));
        profile.setTribulationPower(yaml.getDouble("tribulationPower", 0.0));
        profile.setKarmaPoints(yaml.getInt("karmaPoints", 0));
        
        // load cultivation time
        profile.setTotalCultivationTime(yaml.getLong("totalCultivationTime", 0L));
        profile.setInSeclusion(yaml.getBoolean("inSeclusion", false));
        profile.setSeclusionStartTime(yaml.getLong("seclusionStartTime", 0L));
        
        // load breakthrough cooldown
        profile.setBreakthroughCooldownEnd(yaml.getLong("breakthroughCooldownEnd", 0L));
        
        // load unlock system
        profile.setNextLevelUnlocked(yaml.getBoolean("nextLevelUnlocked", true));  // mac dinh true
        // Note: khong save/load quest/kill/achievement/pill counters - chi luu trang thai unlock
        
        // load breakthrough unlock system
        profile.setBreakthroughUnlocked(yaml.getBoolean("breakthroughUnlocked", false));  // mac dinh false
        // Note: khong save breakthrough counters - chi luu trang thai unlock
        
        // load title system
        String activeTitleName = yaml.getString("activeTitle", "NONE");
        try {
            profile.setActiveTitle(Title.valueOf(activeTitleName));
        } catch (IllegalArgumentException e) {
            profile.setActiveTitle(Title.NONE);
        }
        
        List<String> unlockedTitleNames = yaml.getStringList("unlockedTitles");
        if (!unlockedTitleNames.isEmpty()) {
            for (String titleName : unlockedTitleNames) {
                try {
                    profile.unlockTitle(Title.valueOf(titleName));
                } catch (IllegalArgumentException e) {
                    // skip invalid title
                }
            }
        }
        
        // load stats (tu tien primary stats)
        var stats = profile.getStats();
        var container = stats.getStatContainer();
        
        container.setBase(StatType.ROOT, yaml.getInt("stats.root", 2));
        container.setBase(StatType.SPIRIT, yaml.getInt("stats.spirit", 2));
        container.setBase(StatType.PHYSIQUE, yaml.getInt("stats.physique", 2));
        container.setBase(StatType.COMPREHENSION, yaml.getInt("stats.comprehension", 2));
        container.setBase(StatType.FORTUNE, yaml.getInt("stats.fortune", 2));
        
        // load HP/Linh Khi
        profile.setCurrentHP(yaml.getDouble("currentHP", stats.getMaxHP()));
        profile.setCurrentLingQi(yaml.getDouble("currentLingQi", stats.getMaxLingQi()));

        return profile;
    }

    public void save(PlayerProfile profile) {
        File file = new File(dataFolder, profile.getUuid().toString() + ".yml");
        YamlConfiguration yaml = new YamlConfiguration();

        // save co ban
        yaml.set("level", profile.getLevel());
        yaml.set("exp", profile.getExp());
        yaml.set("statPoints", profile.getStatPoints());
        
        // save cultivation realm (tu tien)
        yaml.set("realm", profile.getRealm().name());
        yaml.set("realmLevel", profile.getRealmLevel());
        yaml.set("cultivation", profile.getCultivation());
        
        // save spiritual root
        yaml.set("spiritualRoot", profile.getSpiritualRoot().name());
        yaml.set("rootQuality", profile.getRootQuality().name());
        
        // save dao heart & state
        yaml.set("daoHeart", profile.getDaoHeart());
        yaml.set("innerInjury", profile.getInnerInjury());
        yaml.set("tribulationPower", profile.getTribulationPower());
        yaml.set("karmaPoints", profile.getKarmaPoints());
        
        // save cultivation time
        yaml.set("totalCultivationTime", profile.getTotalCultivationTime());
        yaml.set("inSeclusion", profile.isInSeclusion());
        yaml.set("seclusionStartTime", profile.getSeclusionStartTime());
        
        // save breakthrough cooldown
        yaml.set("breakthroughCooldownEnd", profile.getBreakthroughCooldownEnd());
        
        // save unlock system
        yaml.set("nextLevelUnlocked", profile.isNextLevelUnlocked());
        // Note: khong save quest/kill/achievement/pill counters - chi luu trang thai unlock
        
        // save breakthrough unlock system
        yaml.set("breakthroughUnlocked", profile.isBreakthroughUnlocked());
        // Note: khong save breakthrough counters - chi luu trang thai unlock
        
        // save title system
        yaml.set("activeTitle", profile.getActiveTitle().name());
        
        List<String> unlockedTitleNames = new ArrayList<>();
        for (Title title : profile.getUnlockedTitles()) {
            unlockedTitleNames.add(title.name());
        }
        yaml.set("unlockedTitles", unlockedTitleNames);
        
        // save stats (tu tien primary stats)
        var stats = profile.getStats();
        var container = stats.getStatContainer();
        
        yaml.set("stats.root", container.getBase(StatType.ROOT));
        yaml.set("stats.spirit", container.getBase(StatType.SPIRIT));
        yaml.set("stats.physique", container.getBase(StatType.PHYSIQUE));
        yaml.set("stats.comprehension", container.getBase(StatType.COMPREHENSION));
        yaml.set("stats.fortune", container.getBase(StatType.FORTUNE));
        
        // save HP/Linh Khi
        yaml.set("currentHP", profile.getCurrentHP());
        yaml.set("currentLingQi", profile.getCurrentLingQi());

        try {
            yaml.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
       
}
