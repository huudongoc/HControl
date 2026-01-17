# PHASE 8 — ITEM / EQUIPMENT / ARTIFACT SYSTEM

> **Ngày bắt đầu:** 2026-01-16  
> **Mục tiêu:** Build hệ thống item/artifact data-driven, không phá architecture

---

## 🎯 **MỤC TIÊU CHÍNH**

### **Nguyên tắc vàng:**
```
Artifact ≠ ItemStack
ItemStack chỉ là "skin" (visual representation)
Effect / stat lấy từ data
```

### **Không làm:**
- ❌ Hard-code item stats trong code
- ❌ Lưu data vào ItemStack lore/NBT (dễ mất)
- ❌ Duplicate logic cho mỗi item type
- ❌ Bypass PlayerProfile để add stats

### **Phải làm:**
- ✅ Artifact = data object (model)
- ✅ ItemStack = visual wrapper
- ✅ Stats/effects từ Artifact definition
- ✅ Hook vào CombatService qua modifier pattern
- ✅ Save artifact data trong PlayerProfile

---

## 📐 **KIẾN TRÚC TỔNG QUAN**

```
┌─────────────────────────────────────────────────┐
│              ITEM CONTEXT                       │
│  (SubContext mới trong CoreContext)            │
├─────────────────────────────────────────────────┤
│                                                 │
│  ┌─────────────┐    ┌──────────────┐          │
│  │ ItemRegistry│    │ ArtifactDef  │          │
│  │  (templates)│ ←──│  (YAML data) │          │
│  └─────────────┘    └──────────────┘          │
│         │                                       │
│         ▼                                       │
│  ┌─────────────┐    ┌──────────────┐          │
│  │ItemManager  │◄───│PlayerArtifact│          │
│  │ (instances) │    │  (equipped)  │          │
│  └─────────────┘    └──────────────┘          │
│         │                    │                  │
│         ▼                    ▼                  │
│  ┌─────────────┐    ┌──────────────┐          │
│  │ItemService  │    │EquipmentSlot │          │
│  │  (logic)    │    │  (HEAD/CHEST)│          │
│  └─────────────┘    └──────────────┘          │
│         │                                       │
└─────────┼───────────────────────────────────────┘
          │
          ▼
    ┌─────────────┐
    │CombatService│ ← Hook stat modifiers
    └─────────────┘
```

---

## 🗂️ **CẤU TRÚC FILES MỚI**

```
src/main/java/hcontrol/plugin/
│
├── item/                          # PACKAGE MỚI
│   ├── ItemManager.java           # Quản lý instances
│   ├── ItemRegistry.java          # Templates & definitions
│   ├── ItemService.java           # Business logic
│   └── EquipmentSlot.java         # Enum: HEAD, CHEST, LEGS, FEET, HAND
│
├── model/
│   ├── Artifact.java              # Model: artifact definition
│   ├── PlayerArtifact.java        # Model: player's equipped artifact
│   └── ItemStats.java             # Model: bonus stats từ item
│
├── core/
│   └── ItemContext.java           # SubContext MỚI
│
└── command/
    └── ItemCommand.java           # Debug command (/item give, inspect)
```

---

## 📦 **DATA MODELS**

### **1. Artifact (Definition)**
```java
/**
 * ARTIFACT DEFINITION - Template cho item
 * Không chứa player-specific data
 */
public class Artifact {
    private final String id;                // "heavenly_sword"
    private final String displayName;       // "§6Thiên Kiếm"
    private final ArtifactType type;        // WEAPON, ARMOR, ACCESSORY
    private final CultivationRealm minRealm; // Yêu cầu realm tối thiểu
    private final int minLevel;             // Yêu cầu level
    private final ItemStats baseStats;      // Base stats
    private final List<String> lore;        // Mô tả
    private final Material material;        // Visual (ItemStack material)
    
    // Builder pattern
    public static class Builder { }
}
```

### **2. PlayerArtifact (Instance)**
```java
/**
 * PLAYER ARTIFACT - Instance của artifact player đang có/mang
 * Có thể có enhancement, enchants, etc.
 */
public class PlayerArtifact {
    private final UUID playerId;
    private final String artifactId;        // Reference to template
    private final UUID instanceId;          // Unique instance
    private EquipmentSlot slot;             // Đang mang ở slot nào
    private int enhancementLevel;           // +1, +2, ...
    private Map<String, Object> customData; // Extend data
    
    // Derived stats (base + enhancement)
    public ItemStats getTotalStats() { }
}
```

### **3. ItemStats (Bonus Stats)**
```java
/**
 * ITEM STATS - Bonus stats từ item
 * Giống như PlayerStats nhưng là bonus
 */
public class ItemStats {
    private final double attack;
    private final double defense;
    private final double maxHP;
    private final double maxLingQi;
    private final double critChance;       // Tương lai: crit system
    private final double critDamage;
    
    // Merge stats
    public ItemStats merge(ItemStats other) { }
}
```

### **4. EquipmentSlot (Enum)**
```java
public enum EquipmentSlot {
    WEAPON,      // Vũ khí chính
    HEAD,        // Mũ / Mão
    CHEST,       // Áo giáp
    LEGS,        // Quần
    FEET,        // Giày
    ACCESSORY_1, // Phụ kiện 1 (nhẫn, vòng)
    ACCESSORY_2  // Phụ kiện 2
}
```

---

## 🔧 **SERVICES & LOGIC**

### **ItemRegistry**
```java
/**
 * ITEM REGISTRY - Quản lý templates
 * Load từ YAML, cache trong RAM
 */
public class ItemRegistry {
    private final Map<String, Artifact> artifacts = new HashMap<>();
    
    public void registerArtifact(Artifact artifact) { }
    public Artifact getArtifact(String id) { }
    public void loadFromConfig(FileConfiguration config) { }
}
```

### **ItemManager**
```java
/**
 * ITEM MANAGER - Quản lý instances
 * Track tất cả artifact instances
 */
public class ItemManager {
    private final Map<UUID, PlayerArtifact> instances = new HashMap<>();
    
    public PlayerArtifact createInstance(UUID playerId, String artifactId) { }
    public List<PlayerArtifact> getPlayerArtifacts(UUID playerId) { }
    public void removeInstance(UUID instanceId) { }
}
```

### **ItemService**
```java
/**
 * ITEM SERVICE - Business logic
 */
public class ItemService {
    private final ItemRegistry registry;
    private final ItemManager manager;
    
    // Equip/unequip
    public boolean equipArtifact(PlayerProfile profile, UUID instanceId, EquipmentSlot slot) { }
    public void unequipArtifact(PlayerProfile profile, EquipmentSlot slot) { }
    
    // Get total stats từ tất cả items equipped
    public ItemStats getTotalEquippedStats(PlayerProfile profile) { }
    
    // Validate: check realm/level requirement
    public boolean canEquip(PlayerProfile profile, Artifact artifact) { }
    
    // Drop artifact to ItemStack
    public ItemStack toItemStack(PlayerArtifact artifact) { }
}
```

---

## 🔗 **INTEGRATION VỚI HỆ THỐNG CŨ**

### **1. PlayerProfile Changes**
```java
// Thêm vào PlayerProfile.java
public class PlayerProfile implements LivingActor {
    // ... existing fields ...
    
    // NEW: Equipment tracking
    private Map<EquipmentSlot, UUID> equippedArtifacts = new HashMap<>();
    
    public void equipArtifact(EquipmentSlot slot, UUID artifactInstanceId) { }
    public void unequipArtifact(EquipmentSlot slot) { }
    public UUID getEquippedArtifact(EquipmentSlot slot) { }
}
```

### **2. CombatService Integration**
```java
// Trong CombatService.calculateDamage()
public double calculateDamage(LivingActor attacker, LivingActor defender) {
    double baseDamage = attacker.getAttack();
    
    // NEW: Add item bonus stats
    if (attacker instanceof PlayerProfile playerProfile) {
        ItemStats itemBonus = itemService.getTotalEquippedStats(playerProfile);
        baseDamage += itemBonus.getAttack();
    }
    
    // ... rest of damage calculation ...
}
```

### **3. StatService Integration**
```java
// Trong StatService.recalculateStats()
public void recalculateStats(PlayerProfile profile) {
    // Base stats
    PlayerStats baseStats = calculateBaseStats(profile);
    
    // NEW: Add item bonus
    ItemStats itemBonus = itemService.getTotalEquippedStats(profile);
    
    // Merge
    double finalAttack = baseStats.getAttack() + itemBonus.getAttack();
    // ... set vào profile ...
}
```

---

## 📝 **CONFIG FORMAT (YAML)**

### **artifacts.yml**
```yaml
artifacts:
  heavenly_sword:
    display_name: "§6§lThiên Kiếm"
    type: WEAPON
    material: DIAMOND_SWORD
    min_realm: KIEN_CO  # Kiến Cơ
    min_level: 1
    stats:
      attack: 50
      defense: 0
      max_hp: 0
      max_lingqi: 0
    lore:
      - "§7Thanh kiếm huyền thoại"
      - "§7từ thời cổ đại"
      - ""
      - "§c+50 Công Kích"
    
  jade_armor:
    display_name: "§a§lNgọc Giáp"
    type: ARMOR
    material: DIAMOND_CHESTPLATE
    min_realm: PHAM_NHAN
    min_level: 5
    stats:
      attack: 0
      defense: 100
      max_hp: 500
      max_lingqi: 0
    lore:
      - "§7Giáp bảo vệ bằng ngọc"
      - ""
      - "§a+100 Phòng Thủ"
      - "§c+500 Sinh Mạng"
```

---

## 🎮 **COMMANDS**

### **/item give <player> <artifactId>**
```
Tạo artifact instance và give cho player
Example: /item give hoang132 heavenly_sword
```

### **/item equip <slot> <instanceId>**
```
Equip artifact vào slot
Example: /item equip WEAPON abc-123-def
```

### **/item unequip <slot>**
```
Unequip artifact từ slot
```

### **/item list**
```
List tất cả artifacts player đang có
```

### **/item inspect**
```
Show detailed stats của equipped items
```

---

## 📊 **IMPLEMENTATION PHASES**

### **Phase 8.1: Core Models (1-2 giờ)**
- [ ] Artifact.java
- [ ] PlayerArtifact.java
- [ ] ItemStats.java
- [ ] EquipmentSlot.java
- [ ] ArtifactType.java (enum)

### **Phase 8.2: Registry & Manager (2-3 giờ)**
- [ ] ItemRegistry.java
- [ ] ItemManager.java
- [ ] Load từ YAML config
- [ ] Cache system

### **Phase 8.3: Service Layer (3-4 giờ)**
- [ ] ItemService.java
- [ ] Equip/unequip logic
- [ ] Validation logic
- [ ] ItemStack conversion

### **Phase 8.4: Integration (2-3 giờ)**
- [ ] PlayerProfile changes
- [ ] CombatService integration
- [ ] StatService integration
- [ ] ItemContext creation

### **Phase 8.5: Commands & UI (2 giờ)**
- [ ] ItemCommand.java
- [ ] Debug commands
- [ ] Visual feedback

### **Phase 8.6: Config & Data (1 giờ)**
- [ ] artifacts.yml
- [ ] Default artifacts
- [ ] Config loader

### **Phase 8.7: Testing (1 giờ)**
- [ ] Test equip/unequip
- [ ] Test stat calculation
- [ ] Test persistence

---

## ⚠️ **LƯU Ý QUAN TRỌNG**

### **1. ItemStack vs Artifact**
```java
// ❌ WRONG: Lưu stats vào ItemStack
ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
ItemMeta meta = item.getItemMeta();
meta.setLore(Arrays.asList("Attack: 50")); // Dễ mất!

// ✅ RIGHT: ItemStack chỉ là visual
PlayerArtifact artifact = new PlayerArtifact(...);
ItemStack visual = itemService.toItemStack(artifact);
// Stats nằm trong PlayerProfile.equippedArtifacts
```

### **2. Persistence**
```java
// Artifact instances PHẢI save trong PlayerProfile
// KHÔNG dựa vào Minecraft inventory persistence
public class PlayerProfile {
    private Map<EquipmentSlot, UUID> equippedArtifacts; // SAVE VÀO JSON!
}
```

### **3. Stat Recalculation**
```java
// Khi equip/unequip → PHẢI recalculate stats
itemService.equipArtifact(profile, instanceId, slot);
statService.recalculateStats(profile); // ← CRITICAL!
healthService.syncHealth(player, profile);
```

---

## 🚀 **NEXT STEPS**

1. **Create ItemContext** trong CoreContext
2. **Implement models** (Phase 8.1)
3. **Build registry** (Phase 8.2)
4. **Implement services** (Phase 8.3)
5. **Integrate với combat** (Phase 8.4)
6. **Add commands** (Phase 8.5)
7. **Create config** (Phase 8.6)
8. **Test thoroughly** (Phase 8.7)

---

**Estimated total time:** 12-16 giờ  
**Priority:** HIGH - Core system cho RPG progression  
**Dependencies:** None (đã có đủ infrastructure)

---

**Bắt đầu với Phase 8.1?** 🎯
