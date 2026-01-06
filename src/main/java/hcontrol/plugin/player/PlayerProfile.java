package hcontrol.plugin.player;

import java.util.UUID;

public class PlayerProfile {

    private final UUID uuid;

    private int level;
    private long exp;

    public PlayerProfile(UUID uuid) {
        this.uuid = uuid;
        this.level = 1;
        this.exp = 0;
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getLevel() {
        return level;
    }

    public long getExp() {
        return exp;
    }

    /* ===== PHASE 1.1: setter co kiem soat ===== */

    public void setLevel(int level) {
        if (level < 1) {
            throw new IllegalArgumentException("Level khong duoc < 1");
        }
        this.level = level;
    }

    public void setExp(long exp) {
        if (exp < 0) {
            this.exp = 0;
            return;
        }
        this.exp = exp;
    }
}
