package hcontrol.plugin.player;

import hcontrol.plugin.event.PlayerLevelUpEvent;
import org.bukkit.Bukkit;

public class LevelService {

    private static final double BASE_EXP = 100.0;
    private static final double POWER = 1.5;

    public long getExpToNext(int level) {
        return (long) (BASE_EXP * Math.pow(level, POWER));
    }

    public void addExp(PlayerProfile profile, long amount) {
        if (amount <= 0) return;

        profile.setExp(profile.getExp() + amount);

        while (profile.getExp() >= getExpToNext(profile.getLevel())) {
            long need = getExpToNext(profile.getLevel());
            profile.setExp(profile.getExp() - need);

            int oldLevel = profile.getLevel();
            profile.setLevel(oldLevel + 1);

            Bukkit.getPluginManager().callEvent(
                new PlayerLevelUpEvent(profile, oldLevel, profile.getLevel())
            );
        }
    }
    public long getRequiredExp(int level) {
        return getExpToNext(level);
    }
}
