# PHASE 6 — PLAYER SKILLS SYSTEM

> **Ngày bắt đầu:** 2026-01-16  
> **Mục tiêu:** Build hệ thống skills cho player, data-driven, balance với mob skills

---

## 🎯 **MỤC TIÊU CHÍNH**

### **Nguyên tắc vàng:**
```
Skill = Request → CombatService
Không damage trực tiếp
Không hard-code effect
Cost system (LingQi)
Cooldown tracking
```

### **Không làm:**
- ❌ Hard-code skill trong code
- ❌ Skill tự deal damage (phải qua CombatService)
- ❌ Duplicate code với MobSkill
- ❌ Allow cast khi không đủ LingQi

### **Phải làm:**
- ✅ PlayerSkill = data object (YAML)
- ✅ LingQi cost system
- ✅ Cooldown tracking (reuse SkillCooldownManager)
- ✅ Skill learning progression
- ✅ Hotbar binding (1-9 slots)
- ✅ Hook vào CombatService

---

## 📐 **KIẾN TRÚC TỔNG QUAN**

```
┌─────────────────────────────────────────────────┐
│          CULTIVATION CONTEXT (mở rộng)          │
│  (Thêm skill system vào context có sẵn)        │
├─────────────────────────────────────────────────┤
│                                                 │
│  ┌──────────────────┐    ┌──────────────────┐ │
│  │PlayerSkillRegistry│◄───│ skills.yml       │ │
│  │   (templates)    │    │  (YAML config)   │ │
│  └──────────────────┘    └──────────────────┘ │
│         │                                       │
│         ▼                                       │
│  ┌──────────────────┐                          │
│  │PlayerSkillService│                          │
│  │  - castSkill()   │                          │
│  │  - checkCost()   │                          │
│  │  - learnSkill()  │                          │
│  └──────────────────┘                          │
│         │                                       │
│         ▼                                       │
│  ┌──────────────────┐    ┌──────────────────┐ │
│  │SkillCooldown     │    │SkillExecutor     │ │
│  │Manager (reuse)   │    │  (player ver)    │ │
│  └──────────────────┘    └──────────────────┘ │
│         │                         │             │
└─────────┼─────────────────────────┼─────────────┘
          │                         │
          ▼                         ▼
    ┌────────────┐          ┌──────────────┐
    │PlayerProfile│         │CombatService │
    │- learnedSkills       └──────────────┘
    │- skillHotbar
    └────────────┘
```

---

## 🗂️ **CẤU TRÚC FILES MỚI**

```
src/main/java/hcontrol/plugin/
│
├── playerskill/                    # PACKAGE MỚI
│   ├── PlayerSkill.java            # Model: skill definition
│   ├── PlayerSkillRegistry.java   # Templates (load YAML)
│   ├── PlayerSkillService.java    # Business logic
│   ├── PlayerSkillExecutor.java   # Execute skill effects
│   ├── SkillCost.java              # Model: cost (LingQi, items)
│   └── SkillType.java              # Enum (có thể reuse từ Phase 7.2)
│
├── model/
│   └── PlayerProfile.java          # MODIFY: add skill data
│
├── core/
│   └── CultivationContext.java     # MODIFY: add skill services
│
├── command/
│   └── SkillCommand.java           # /skill commands
│
└── listener/
    └── PlayerSkillListener.java    # Listen hotbar interact
```

---

## 📦 **DATA MODELS**

### **1. PlayerSkill (Definition)**
```java
/**
 * PLAYER SKILL DEFINITION - Template cho skill
 * Tương tự MobSkill nhưng có thêm cost & learning requirements
 */
public class PlayerSkill {
    private final String skillId;              // "fireball"
    private final String displayName;          // "§cHỏa Cầu"
    private final SkillType type;              // MELEE, RANGED, AOE, BUFF, HEAL
    private final SkillCost cost;              // LingQi cost, item cost
    private final int cooldown;                // Cooldown (seconds)
    private final double damageMultiplier;     // Damage multiplier
    private final double range;                // Cast range
    private final CultivationRealm minRealm;   // Yêu cầu realm tối thiểu
    private final int minLevel;                // Yêu cầu level tối thiểu
    private final List<SkillEffect> effects;   // Potion effects
    private final List<String> description;    // Mô tả skill
    
    // Special properties
    private final int projectileCount;         // For ranged skills
    private final double areaRadius;           // For AOE skills
    
    // Builder pattern
    public static class Builder { }
    
    /**
     * Skill effect (giống MobSkill.SkillEffect)
     */
    public static class SkillEffect {
        private final PotionEffectType effectType;
        private final int duration;  // ticks
        private final int amplifier;
        
        public SkillEffect(PotionEffectType type, int duration, int amplifier) {
            this.effectType = type;
            this.duration = duration;
            this.amplifier = amplifier;
        }
        
        // Getters
    }
}
```

### **2. SkillCost (Model)**
```java
/**
 * SKILL COST - Chi phí để cast skill
 */
public class SkillCost {
    private final double lingQi;               // LingQi cost
    private final Map<String, Integer> items;  // Item cost (optional, Phase 8)
    
    public SkillCost(double lingQi) {
        this.lingQi = lingQi;
        this.items = new HashMap<>();
    }
    
    public SkillCost(double lingQi, Map<String, Integer> items) {
        this.lingQi = lingQi;
        this.items = items;
    }
    
    public boolean canAfford(PlayerProfile profile) {
        return profile.getCurrentLingQi() >= lingQi;
    }
    
    public void deduct(PlayerProfile profile) {
        double newLingQi = profile.getCurrentLingQi() - lingQi;
        profile.setCurrentLingQi(Math.max(0, newLingQi));
    }
    
    // Getters
}
```

### **3. SkillType (Enum - có thể reuse)**
```java
/**
 * SKILL TYPE - Reuse từ Phase 7.2 hoặc tạo mới
 */
public enum SkillType {
    MELEE,      // Cận chiến
    RANGED,     // Tầm xa (projectile)
    AOE,        // Phạm vi (area effect)
    BUFF,       // Tăng buff bản thân
    DEBUFF,     // Giảm buff địch (tương lai)
    HEAL,       // Hồi máu/Linh Khí
    TELEPORT,   // Dịch chuyển
    SUMMON      // Triệu hồi (tương lai)
}
```

---

## 🔧 **SERVICES & LOGIC**

### **PlayerSkillRegistry**
```java
/**
 * PLAYER SKILL REGISTRY - Quản lý skill templates
 * Load từ YAML, cache trong RAM
 */
public class PlayerSkillRegistry {
    private final Map<String, PlayerSkill> skills = new HashMap<>();
    
    public void registerSkill(PlayerSkill skill) {
        skills.put(skill.getSkillId(), skill);
    }
    
    public PlayerSkill getSkill(String skillId) {
        return skills.get(skillId);
    }
    
    public List<PlayerSkill> getAllSkills() {
        return new ArrayList<>(skills.values());
    }
    
    public List<PlayerSkill> getSkillsByRealm(CultivationRealm realm) {
        return skills.values().stream()
            .filter(s -> s.getMinRealm().ordinal() <= realm.ordinal())
            .toList();
    }
    
    /**
     * Load skills từ YAML config
     */
    public void loadFromConfig(FileConfiguration config) {
        ConfigurationSection skillsSection = config.getConfigurationSection("skills");
        if (skillsSection == null) return;
        
        for (String skillId : skillsSection.getKeys(false)) {
            ConfigurationSection skillSection = skillsSection.getConfigurationSection(skillId);
            
            PlayerSkill skill = new PlayerSkill.Builder(skillId)
                .displayName(skillSection.getString("display_name"))
                .type(SkillType.valueOf(skillSection.getString("type")))
                .cost(new SkillCost(skillSection.getDouble("cost")))
                .cooldown(skillSection.getInt("cooldown"))
                .damageMultiplier(skillSection.getDouble("damage_multiplier", 1.0))
                .range(skillSection.getDouble("range", 5.0))
                .minRealm(CultivationRealm.valueOf(skillSection.getString("min_realm", "PHAM_NHAN")))
                .minLevel(skillSection.getInt("min_level", 1))
                .build();
            
            registerSkill(skill);
        }
    }
}
```

### **PlayerSkillService**
```java
/**
 * PLAYER SKILL SERVICE - Business logic
 */
public class PlayerSkillService {
    private final PlayerSkillRegistry registry;
    private final SkillCooldownManager cooldownManager;
    private final PlayerSkillExecutor executor;
    
    public PlayerSkillService(PlayerSkillRegistry registry, 
                              SkillCooldownManager cooldownManager,
                              PlayerSkillExecutor executor) {
        this.registry = registry;
        this.cooldownManager = cooldownManager;
        this.executor = executor;
    }
    
    /**
     * Cast skill
     */
    public boolean castSkill(Player player, PlayerProfile profile, String skillId) {
        PlayerSkill skill = registry.getSkill(skillId);
        if (skill == null) {
            player.sendMessage("§cSkill không tồn tại!");
            return false;
        }
        
        // Check: player đã học skill chưa?
        if (!profile.hasLearnedSkill(skillId)) {
            player.sendMessage("§cBạn chưa học skill này!");
            return false;
        }
        
        // Check: realm/level requirement
        if (!canUseSkill(profile, skill)) {
            player.sendMessage("§cBạn chưa đủ realm/level để dùng skill này!");
            return false;
        }
        
        // Check: cooldown
        if (cooldownManager.isOnCooldown(player.getUniqueId(), skillId)) {
            long remainingSeconds = cooldownManager.getRemainingTime(player.getUniqueId(), skillId) / 1000;
            player.sendMessage("§cSkill đang cooldown! Còn " + remainingSeconds + "s");
            return false;
        }
        
        // Check: cost (LingQi)
        if (!skill.getCost().canAfford(profile)) {
            player.sendMessage("§cKhông đủ Linh Khí! Cần: " + skill.getCost().getLingQi());
            return false;
        }
        
        // Execute skill
        boolean success = executor.executeSkill(player, profile, skill);
        
        if (success) {
            // Deduct cost
            skill.getCost().deduct(profile);
            
            // Set cooldown
            cooldownManager.setCooldown(player.getUniqueId(), skillId, skill.getCooldown());
            
            // Visual feedback
            player.sendMessage("§a✦ Đã cast skill: " + skill.getDisplayName());
        }
        
        return success;
    }
    
    /**
     * Learn skill
     */
    public boolean learnSkill(PlayerProfile profile, String skillId) {
        PlayerSkill skill = registry.getSkill(skillId);
        if (skill == null) return false;
        
        // Check: đã học rồi?
        if (profile.hasLearnedSkill(skillId)) {
            return false;
        }
        
        // Check: realm/level requirement
        if (!canUseSkill(profile, skill)) {
            return false;
        }
        
        // Learn skill
        profile.learnSkill(skillId);
        return true;
    }
    
    /**
     * Bind skill to hotbar slot
     */
    public boolean bindSkill(PlayerProfile profile, int slot, String skillId) {
        if (slot < 1 || slot > 9) return false;
        
        // Check: đã học skill chưa?
        if (!profile.hasLearnedSkill(skillId)) return false;
        
        profile.bindSkill(slot, skillId);
        return true;
    }
    
    /**
     * Check if player can use skill
     */
    private boolean canUseSkill(PlayerProfile profile, PlayerSkill skill) {
        return profile.getRealm().ordinal() >= skill.getMinRealm().ordinal()
            && profile.getLevel() >= skill.getMinLevel();
    }
}
```

### **PlayerSkillExecutor**
```java
/**
 * PLAYER SKILL EXECUTOR - Execute skill effects
 * Tương tự SkillExecutor (Phase 7.2) nhưng cho player
 */
public class PlayerSkillExecutor {
    private final CombatService combatService;
    
    public PlayerSkillExecutor(CombatService combatService) {
        this.combatService = combatService;
    }
    
    /**
     * Execute skill
     */
    public boolean executeSkill(Player player, PlayerProfile profile, PlayerSkill skill) {
        return switch (skill.getType()) {
            case MELEE -> executeMelee(player, profile, skill);
            case RANGED -> executeRanged(player, profile, skill);
            case AOE -> executeAOE(player, profile, skill);
            case BUFF -> executeBuff(player, profile, skill);
            case HEAL -> executeHeal(player, profile, skill);
            case TELEPORT -> executeTeleport(player, skill);
            default -> false;
        };
    }
    
    /**
     * Execute melee skill - tìm entity gần nhất và attack
     */
    private boolean executeMelee(Player player, PlayerProfile profile, PlayerSkill skill) {
        // Find nearest entity within range
        LivingEntity target = findNearestEntity(player, skill.getRange());
        if (target == null) {
            player.sendMessage("§cKhông có mục tiêu!");
            return false;
        }
        
        // Trigger custom combat through CombatService
        EntityProfile targetProfile = getEntityProfile(target);
        if (targetProfile != null) {
            combatService.handleCombat(profile, targetProfile);
        }
        
        // Apply skill effects
        applyEffects(target, skill);
        
        // Visual
        playSkillEffects(player, skill);
        
        return true;
    }
    
    /**
     * Execute ranged skill - spawn projectiles
     */
    private boolean executeRanged(Player player, PlayerProfile profile, PlayerSkill skill) {
        Location eyeLoc = player.getEyeLocation();
        Vector direction = eyeLoc.getDirection();
        
        int projectiles = skill.getProjectileCount();
        
        for (int i = 0; i < projectiles; i++) {
            Vector spread = direction.clone();
            if (projectiles > 1) {
                double spreadAngle = (i - projectiles / 2.0) * 0.1;
                spread.rotateAroundY(spreadAngle);
            }
            
            // Spawn arrow/fireball
            Arrow arrow = player.getWorld().spawnArrow(eyeLoc, spread, 2.0f, 0);
            arrow.setShooter(player);
            
            // Store skill info in metadata
            arrow.setMetadata("player_skill", 
                new FixedMetadataValue(plugin, skill.getSkillId())
            );
        }
        
        playSkillEffects(player, skill);
        return true;
    }
    
    /**
     * Execute AOE skill
     */
    private boolean executeAOE(Player player, PlayerProfile profile, PlayerSkill skill) {
        Location center = player.getLocation();
        double radius = skill.getAreaRadius();
        
        // Find all entities in radius
        List<LivingEntity> entities = center.getWorld().getLivingEntities().stream()
            .filter(e -> !(e instanceof Player))
            .filter(e -> e.getLocation().distance(center) <= radius)
            .toList();
        
        for (LivingEntity entity : entities) {
            EntityProfile targetProfile = getEntityProfile(entity);
            if (targetProfile != null) {
                combatService.handleCombat(profile, targetProfile);
            }
            applyEffects(entity, skill);
        }
        
        // Visual explosion
        center.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, center, 10, radius, 1, radius);
        playSkillEffects(player, skill);
        
        return true;
    }
    
    /**
     * Execute buff skill
     */
    private boolean executeBuff(Player player, PlayerProfile profile, PlayerSkill skill) {
        applyEffects(player, skill);
        playSkillEffects(player, skill);
        return true;
    }
    
    /**
     * Execute heal skill
     */
    private boolean executeHeal(Player player, PlayerProfile profile, PlayerSkill skill) {
        double healAmount = profile.getStats().getMaxHP() * 0.3; // 30% max HP
        double newHP = Math.min(profile.getCurrentHP() + healAmount, profile.getStats().getMaxHP());
        profile.setCurrentHP(newHP);
        
        player.sendMessage("§a+§c" + (int)healAmount + " §fSinh Mạng!");
        player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0, 2, 0), 10);
        
        return true;
    }
    
    /**
     * Execute teleport skill
     */
    private boolean executeTeleport(Player player, PlayerSkill skill) {
        // Teleport forward
        Location playerLoc = player.getLocation();
        Vector direction = playerLoc.getDirection().multiply(skill.getRange());
        Location teleportLoc = playerLoc.add(direction);
        
        player.teleport(teleportLoc);
        player.getWorld().spawnParticle(Particle.PORTAL, teleportLoc, 50);
        player.playSound(teleportLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        
        return true;
    }
    
    // Helper methods (findNearestEntity, applyEffects, playSkillEffects, etc.)
}
```

---

## 🔗 **INTEGRATION VỚI HỆ THỐNG CŨ**

### **1. PlayerProfile Changes**
```java
// Thêm vào PlayerProfile.java
public class PlayerProfile implements LivingActor {
    // ... existing fields ...
    
    // NEW: Skill system
    private final Set<String> learnedSkills = new HashSet<>();
    private final Map<Integer, String> skillHotbar = new HashMap<>(); // Slot 1-9 -> SkillId
    
    /**
     * Learn skill
     */
    public void learnSkill(String skillId) {
        learnedSkills.add(skillId);
    }
    
    /**
     * Check if learned skill
     */
    public boolean hasLearnedSkill(String skillId) {
        return learnedSkills.contains(skillId);
    }
    
    /**
     * Get all learned skills
     */
    public Set<String> getLearnedSkills() {
        return new HashSet<>(learnedSkills);
    }
    
    /**
     * Bind skill to hotbar slot (1-9)
     */
    public void bindSkill(int slot, String skillId) {
        if (slot < 1 || slot > 9) return;
        skillHotbar.put(slot, skillId);
    }
    
    /**
     * Get skill at hotbar slot
     */
    public String getSkillAtSlot(int slot) {
        return skillHotbar.get(slot);
    }
    
    /**
     * Unbind skill from slot
     */
    public void unbindSkill(int slot) {
        skillHotbar.remove(slot);
    }
}
```

### **2. CultivationContext Changes**
```java
// Thêm vào CultivationContext.java
public class CultivationContext {
    // ... existing fields ...
    
    // NEW: Skill system
    private final PlayerSkillRegistry skillRegistry;
    private final PlayerSkillService skillService;
    private final PlayerSkillExecutor skillExecutor;
    
    public CultivationContext(/* dependencies */) {
        // ... existing init ...
        
        // Init skill system
        this.skillRegistry = new PlayerSkillRegistry();
        this.skillExecutor = new PlayerSkillExecutor(combatService);
        this.skillService = new PlayerSkillService(
            skillRegistry, 
            cooldownManager,  // Reuse from Phase 7.2
            skillExecutor
        );
        
        // Load skills from config
        loadSkills();
    }
    
    private void loadSkills() {
        File skillsFile = new File(plugin.getDataFolder(), "player-skills.yml");
        if (!skillsFile.exists()) {
            plugin.saveResource("player-skills.yml", false);
        }
        
        FileConfiguration config = YamlConfiguration.loadConfiguration(skillsFile);
        skillRegistry.loadFromConfig(config);
    }
    
    // Getters
    public PlayerSkillRegistry getSkillRegistry() { return skillRegistry; }
    public PlayerSkillService getSkillService() { return skillService; }
}
```

### **3. PlayerSkillListener (Hotbar Cast)**
```java
/**
 * PLAYER SKILL LISTENER - Listen player interact to cast skill from hotbar
 */
public class PlayerSkillListener implements Listener {
    private final PlayerSkillService skillService;
    private final PlayerManager playerManager;
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerProfile profile = playerManager.get(player.getUniqueId());
        if (profile == null) return;
        
        // Check if right-click with empty hand
        if (event.getAction() != Action.RIGHT_CLICK_AIR 
            && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        // Check if holding empty hand or specific item
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() != Material.AIR) {
            return; // Not empty hand
        }
        
        // Get hotbar slot (0-8 → 1-9)
        int slot = player.getInventory().getHeldItemSlot() + 1;
        
        // Get skill bound to this slot
        String skillId = profile.getSkillAtSlot(slot);
        if (skillId == null) return;
        
        // Cast skill!
        event.setCancelled(true);
        skillService.castSkill(player, profile, skillId);
    }
}
```

---

## 📝 **CONFIG FORMAT (YAML)**

### **player-skills.yml**
```yaml
# PLAYER SKILLS CONFIG
# Format: skillId -> skill data

skills:
  # ===== CẬN CHIẾN (MELEE) =====
  basic_strike:
    display_name: "§fCơ Bản Tấn Công"
    type: MELEE
    cost: 10
    cooldown: 1
    damage_multiplier: 1.2
    range: 5.0
    min_realm: PHAM_NHAN
    min_level: 1
    description:
      - "§7Tấn công cơ bản"
      - "§7Công kích +20%"
    
  sword_slash:
    display_name: "§cKiếm Chém"
    type: MELEE
    cost: 30
    cooldown: 3
    damage_multiplier: 1.8
    range: 6.0
    min_realm: PHAM_NHAN
    min_level: 3
    effects:
      - type: SLOW
        duration: 40  # 2 seconds
        amplifier: 1
    description:
      - "§7Chém mạnh với kiếm"
      - "§7Công kích +80%"
      - "§7Làm chậm mục tiêu"
  
  # ===== TẦM XA (RANGED) =====
  fireball:
    display_name: "§cHỏa Cầu"
    type: RANGED
    cost: 50
    cooldown: 5
    damage_multiplier: 2.0
    range: 32.0
    projectile_count: 1
    min_realm: KIEN_CO
    min_level: 1
    effects:
      - type: FIRE_RESISTANCE
        duration: 60
        amplifier: 0
    description:
      - "§7Bắn cầu lửa"
      - "§7Công kích +100%"
      - "§7Gây cháy mục tiêu"
  
  ice_barrage:
    display_name: "§bBăng Phong Vũ"
    type: RANGED
    cost: 80
    cooldown: 8
    damage_multiplier: 1.5
    range: 32.0
    projectile_count: 5
    min_realm: KIEN_CO
    min_level: 5
    effects:
      - type: SLOW
        duration: 100
        amplifier: 2
    description:
      - "§7Bắn 5 mũi băng"
      - "§7Công kích +50% mỗi mũi"
      - "§7Làm chậm mạnh"
  
  # ===== PHẠM VI (AOE) =====
  earthquake:
    display_name: "§6Địa Chấn"
    type: AOE
    cost: 100
    cooldown: 10
    damage_multiplier: 1.5
    range: 0
    area_radius: 8.0
    min_realm: TRUC_CO
    min_level: 1
    description:
      - "§7Tạo chấn động đất"
      - "§7Sát thương phạm vi 8 blocks"
      - "§7Công kích +50%"
  
  # ===== BUFF =====
  qi_burst:
    display_name: "§9Linh Khí Bùng Nổ"
    type: BUFF
    cost: 50
    cooldown: 15
    damage_multiplier: 0
    range: 0
    min_realm: PHAM_NHAN
    min_level: 5
    effects:
      - type: SPEED
        duration: 200  # 10 seconds
        amplifier: 1
      - type: INCREASE_DAMAGE
        duration: 200
        amplifier: 0
    description:
      - "§7Tăng tốc độ và sát thương"
      - "§7Kéo dài 10 giây"
  
  # ===== HỒI PHỤC (HEAL) =====
  meditation:
    display_name: "§aTu Luyện Hồi Phục"
    type: HEAL
    cost: 30
    cooldown: 20
    damage_multiplier: 0
    range: 0
    min_realm: PHAM_NHAN
    min_level: 1
    description:
      - "§7Hồi 30% sinh mạng"
      - "§7Yêu cầu đứng yên"
  
  # ===== DỊCH CHUYỂN (TELEPORT) =====
  flash_step:
    display_name: "§dThoát Bộ"
    type: TELEPORT
    cost: 40
    cooldown: 5
    damage_multiplier: 0
    range: 10.0
    min_realm: KIEN_CO
    min_level: 3
    description:
      - "§7Dịch chuyển về phía trước"
      - "§7Khoảng cách 10 blocks"
```

---

## 🎮 **COMMANDS**

### **/skill learn <skillId>**
```
Học skill mới
Example: /skill learn fireball
Permission: hcontrol.skill.learn
```

### **/skill list**
```
Hiện danh sách skills đã học
Show: Skill name, type, cost, cooldown
```

### **/skill bind <slot> <skillId>**
```
Gán skill vào hotbar slot (1-9)
Example: /skill bind 1 fireball
```

### **/skill unbind <slot>**
```
Gỡ skill khỏi slot
```

### **/skill info <skillId>**
```
Xem chi tiết skill
Show: Description, requirements, effects
```

### **/skill give <player> <skillId>** (Admin)
```
Force học skill cho player
Permission: hcontrol.admin
```

---

## 📊 **IMPLEMENTATION PHASES**

### **Phase 6.1: Core Models (30 phút)**
- [ ] PlayerSkill.java
- [ ] SkillCost.java
- [ ] SkillType.java (nếu khác với Phase 7.2)

### **Phase 6.2: Registry (1 giờ)**
- [ ] PlayerSkillRegistry.java
- [ ] Load từ YAML config
- [ ] Default skills registration

### **Phase 6.3: Service Layer (2 giờ)**
- [ ] PlayerSkillService.java
  - castSkill()
  - learnSkill()
  - bindSkill()
  - Check cost/cooldown/requirements
- [ ] PlayerSkillExecutor.java
  - Execute different skill types
  - Integration với CombatService

### **Phase 6.4: Integration (1 giờ)**
- [ ] PlayerProfile modifications
  - learnedSkills field
  - skillHotbar field
  - Methods for skill management
- [ ] CultivationContext changes
  - Add skill services
  - Load config
- [ ] PlayerSkillListener
  - Hotbar interact detection
  - Cast skill on right-click

### **Phase 6.5: Commands (1 giờ)**
- [ ] SkillCommand.java
  - /skill learn
  - /skill list
  - /skill bind
  - /skill info
  - Tab completion

### **Phase 6.6: Config & Data (30 phút)**
- [ ] player-skills.yml
- [ ] Default skills (10-15 skills)
- [ ] Config loader

### **Phase 6.7: Testing (30 phút)**
- [ ] Test skill learning
- [ ] Test skill casting
- [ ] Test cost/cooldown
- [ ] Test hotbar binding
- [ ] Test different skill types

---

## ⚠️ **LƯU Ý QUAN TRỌNG**

### **1. Reuse Code từ Phase 7.2**
```java
// REUSE:
- SkillCooldownManager (already built)
- SkillType enum (có thể reuse)
- Skill execution pattern

// NEW:
- Cost system (LingQi)
- Learning system
- Hotbar binding
```

### **2. Balance với Mob Skills**
```yaml
# Player skills phải mạnh hơn mob skills một chút
# Vì player phải trả cost (LingQi)

Mob damage: 1.5x
Player damage: 2.0x (nhưng có cost)
```

### **3. Persistence**
```java
// Skills phải save trong PlayerProfile JSON
{
  "learnedSkills": ["fireball", "sword_slash"],
  "skillHotbar": {
    "1": "fireball",
    "2": "sword_slash"
  }
}
```

### **4. Hotbar Detection**
```java
// Right-click với empty hand = cast skill
// Slot hotbar 1-9 → skill slots
// Hold shift + right-click = alternative casting (tương lai)
```

---

## 🚀 **NEXT STEPS AFTER PHASE 6**

### **Phase 5 - Class System**
- Class modify skill properties
  - SwordCultivator: -20% melee cooldown, +15% melee damage
  - SpellCultivator: -30% spell cost, +20% spell range
  - BodyCultivator: +50% max HP, +healing effectiveness

### **Phase 7 - Hoàn thiện Mob AI**
- Mob reactions to player skills
- Dodge/counter systems
- Boss unique skills

### **Phase 8 - Item System**
- Items reduce skill cost
- Items add bonus skill damage
- Special items unlock unique skills

---

## 📌 **SUCCESS CRITERIA**

Phase 6 считается успешным если:
- ✅ Player có thể learn skills
- ✅ Player có thể cast skills bằng hotbar
- ✅ Skills trừ LingQi cost
- ✅ Cooldown system hoạt động
- ✅ Skills deal damage qua CombatService
- ✅ Config load từ YAML
- ✅ Balance với mob skills
- ✅ Không có bugs khi cast liên tục

---

**Estimated total time:** 6-7 giờ  
**Priority:** CRITICAL - Cần gấp để balance với mob skills  
**Dependencies:** Phase 7.2 (SkillCooldownManager), CombatService

---

**Bắt đầu Phase 6.1 (Core Models)?** 🎯
