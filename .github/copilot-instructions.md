# HControl RPG - MASTER TASK LIST
một người bạn thân lâu năm , vua it , hay chửi , nói bậy, hay code c# , python. ghi chú trong code là tiếng việt không dấu, code theo moldule ghép vào với nhau nhưng không rời quá,đang học tiếng trung
> Mục tiêu:
> Xây dựng plugin RPG lâu dài, cân mọi server, mở rộng không giới hạn.
> Làm theo thứ tự, không vội, không đập core.

==================================================
PHASE 0 — FOUNDATION (KHÔNG BAO GIỜ LÀM LẠI)
==================================================

## Core Architecture
- [x] Tách listener / command / service / model
- [x] Main chỉ làm wiring
- [x] CoreContext (singleton context)
- [x] Plugin lifecycle manager (LifecycleManager)
- [x] Module enable / disable system

==================================================
PHASE 1 — PLAYER SYSTEM (LINH HỒN RPG)
==================================================

## Player Profile
- [x] PlayerProfile class
- [x] UUID-based profile map (PlayerManager)
- [x] Create profile on join
- [x] Remove profile on quit
- [x] Save profile on quit (PlayerStorage YAML)
- [x] Load profile on join

## Player Progression
- [x] Level system (LevelService)
- [x] EXP gain logic
- [x] EXP curve (configurable - level^2 * 100)
- [x] Level up event (PlayerLevelUpEvent)
- [x] Stat point reward (+5 mỗi level)

==================================================
PHASE 2 — STAT SYSTEM (CỐT LÕI RPG)
==================================================

## Stat Core
- [x] StatType enum (STR, AGI, INT, VIT, LCK + derived)
- [x] StatContainer (base + bonus)
- [x] Base stat
- [x] Bonus stat (structure có rồi)
- [x] Temporary stat (clearBonus() method)

## Stat Scaling
- [x] Stat scale theo level (HP/Mana)
- [ ] Stat scale theo class (PHASE 5 - chưa có class)
- [x] Derived stat (HP, Mana, ATK, MATK, DEF, Crit, Dodge)

==================================================
PHASE 3 — COMBAT SYSTEM (THAY DAMAGE VANILLA)
==================================================

## Combat Core
- [ ] CombatListener
- [ ] Cancel vanilla damage
- [ ] Custom damage calculation
- [ ] Defense calculation
- [ ] Crit system
- [ ] Miss / dodge

## Damage Formula
- [ ] Physical damage
- [ ] Magic damage
- [ ] True damage
- [ ] Damage reduction formula

==================================================
PHASE 4 — MANA & RESOURCE
==================================================

## Mana System
- [ ] Mana stat
- [ ] Mana regen
- [ ] Mana cost check
- [ ] Out of mana handling

## Resource Extension
- [ ] Rage (warrior)
- [ ] Energy (assassin)
- [ ] Custom resource support

==================================================
PHASE 5 — CLASS / JOB SYSTEM
==================================================

## Class Core
- [ ] ClassType enum
- [ ] Base class stats
- [ ] Class selection command
- [ ] One class per player

## Advanced Class
- [ ] Sub-class / job change
- [ ] Class passive skill
- [ ] Class scaling formula

==================================================
PHASE 6 — SKILL SYSTEM (KHÔNG HARD-CODE)
==================================================

## Skill Core
- [ ] Skill interface
- [ ] SkillContext
- [ ] SkillTargeting
- [ ] SkillCondition
- [ ] SkillCooldown

## Skill Execution
- [ ] Instant skill
- [ ] Cast time skill
- [ ] Channel skill
- [ ] Area skill

==================================================
PHASE 7 — AI & MOB RPG
==================================================

## AI Core
- [ ] AIAgent interface
- [ ] AIState machine
- [ ] Target selection
- [ ] Aggro table
- [ ] AI memory

## Mob Types
- [ ] Normal mob AI
- [ ] Elite mob AI
- [ ] Boss AI (phase system)
- [ ] Enrage mechanic

==================================================
PHASE 8 — ITEM & EQUIPMENT
==================================================

## Item Core
- [ ] Custom item metadata
- [ ] Stat item
- [ ] Rarity system
- [ ] Random stat roll

## Equipment
- [ ] Weapon system
- [ ] Armor system
- [ ] Set bonus
- [ ] Upgrade / enchant

==================================================
PHASE 9 — WORLD & CONTENT
==================================================

## PvE Content
- [ ] Dungeon system
- [ ] Dungeon mob spawn
- [ ] Dungeon boss
- [ ] Dungeon reward

## Quest System
- [ ] Quest model
- [ ] Quest objective
- [ ] Quest reward
- [ ] Daily / weekly quest

==================================================
PHASE 10 — ECONOMY & SOCIAL
==================================================

## Economy
- [ ] Gold system
- [ ] Drop rate
- [ ] Trade
- [ ] Shop NPC

## Party / Guild
- [ ] Party system
- [ ] Shared EXP
- [ ] Guild system
- [ ] Guild buff

==================================================
PHASE 11 — UI & UX
==================================================

## UI
- [ ] ActionBar stat
- [ ] BossBar HP
- [ ] Custom GUI
- [ ] Skill bar GUI

==================================================
PHASE 12 — CONFIG & DATA
==================================================

## Config
- [ ] YAML config load
- [ ] Hot reload config
- [ ] Per-module config

## Data Storage
- [ ] YAML storage
- [ ] Async save
- [ ] Migration system
- [ ] Backup data

==================================================
PHASE 13 — PERFORMANCE & SCALE
==================================================

## Optimization
- [ ] Tick throttling
- [ ] Async calculation
- [ ] Memory cleanup
- [ ] Chunk-aware logic

==================================================
PHASE 14 — ADMIN & DEBUG
==================================================

## Admin Tool
- [ ] Debug command
- [ ] Force reload module
- [ ] Player data inspect
- [ ] Balance tool

==================================================
PHASE 15 — ENDGAME (CẢ ĐỜI MỚI XONG)
==================================================

## Endgame
- [ ] World boss
- [ ] Seasonal event
- [ ] Prestige system
- [ ] Infinite scaling (soft cap)

==================================================
NGUYÊN TẮC VÀNG — ARCHITECTURE & CODING RULES
==================================================

## 🏗️ KIẾN TRÚC TỔNG QUAN

### **Phân tầng rõ ràng (Layered Architecture)**
```
Main (wiring only)
  ↓
CoreContext (singleton DI container)
  ↓
LifecycleManager (enable/disable callbacks)
  ↓
┌─────────────┬──────────────┬──────────────┐
│  Command    │  Listener    │   Service    │
│  (UI layer) │ (Event layer)│ (Logic layer)│
└─────────────┴──────────────┴──────────────┘
  ↓               ↓                ↓
┌─────────────┬──────────────┬──────────────┐
│  Manager    │   Storage    │    Model     │
│ (RAM cache) │ (Disk I/O)   │  (Data)      │
└─────────────┴──────────────┴──────────────┘
```

---

## 📁 CẤU TRÚC FOLDER & TRÁCH NHIỆM

### **1. `/command/` — UI Layer (KHÔNG CHỨA LOGIC)**
**Trách nhiệm:**
- Parse command arguments
- Validate input format (số, tên command...)
- Gọi Service thực thi logic
- Hiển thị kết quả cho player

**KHÔNG ĐƯỢC:**
- ❌ Validate nghiệp vụ (stat points đủ không? level max chưa?)
- ❌ Tính toán (exp curve, damage formula...)
- ❌ Trực tiếp sửa model (`profile.setLevel()`)
- ❌ Trực tiếp gọi storage

**Ví dụ đúng:**
```java
// StatCommand.java
public boolean onCommand(...) {
    // ĐÚNG: Parse args
    int amount = Integer.parseInt(args[2]);
    
    // ĐÚNG: Gọi service
    boolean success = statService.allocateStatPoints(profile, type, amount);
    
    // ĐÚNG: Hiển thị kết quả
    if (!success) {
        player.sendMessage("Lỗi");
    }
}
```

**Ví dụ SAI:**
```java
// ❌ SAI: Command chứa logic
public boolean onCommand(...) {
    if (profile.getStatPoints() < amount) {  // ❌ validate logic
        return false;
    }
    profile.getStats().addPrimaryStat(...);  // ❌ trực tiếp sửa model
    profile.removeStatPoints(amount);        // ❌ logic ở command
}
```

---

### **2. `/listener/` — Event Layer**

**Trách nhiệm:**
- Bắt Bukkit events
- Gọi Service xử lý logic
- Lifecycle events (join/quit) được phép quản lý profile

**ĐƯỢC PHÉP (chỉ lifecycle listener):**
- ✅ `JoinServerListener`: load profile, add vào manager
- ✅ `OutServerListener`: save profile, remove khỏi manager

**KHÔNG ĐƯỢC (combat/chat listener):**
- ❌ Tính toán damage trong listener
- ❌ Validate stat/skill trong listener
- ❌ Trực tiếp sửa profile

**Ví dụ đúng:**
```java
// PlayerCombatListener.java
@EventHandler
public void onDamage(EntityDamageByEntityEvent e) {
    event.setCancelled(true);  // ĐÚNG: cancel vanilla
    
    // ĐÚNG: Gọi service
    combatService.handleAttack(attacker, target);
}
```

---

### **3. `/service/` — Logic Layer (TRÍ NÃO CỦA HỆ THỐNG)**

**Trách nhiệm:**
- Chứa TẤT CẢ logic nghiệp vụ
- Validate business rules
- Tính toán (exp, damage, stat...)
- Gọi Manager/Storage khi cần

**Quy tắc:**
- ✅ Method phải có return type rõ ràng (boolean success, calculated value...)
- ✅ Validate đầu vào
- ✅ Không throw exception ra ngoài (return false/null thay vì throw)

**Ví dụ:**
```java
// StatService.java — ĐÚNG
public boolean allocateStatPoints(PlayerProfile profile, StatType type, int amount) {
    // validate
    if (type == null || !type.isPrimary()) return false;
    if (amount <= 0) return false;
    if (profile.getStatPoints() < amount) return false;
    
    // execute
    profile.getStats().addPrimaryStat(type, amount);
    profile.removeStatPoints(amount);
    
    return true;
}
```

---

### **4. `/player/` — Player Context (QUẢN LÝ PLAYER)**

**Bao gồm:**
- `PlayerManager` — quản lý RAM cache (HashMap)
- `PlayerStorage` — save/load YAML
- `PlayerProfile` — model chứa data
- `LevelService` — logic level/exp (đặc thù player)

**Quy tắc:**
- ✅ Service trong `/player/` chỉ lo logic liên quan player
- ✅ `PlayerManager` chỉ CRUD profile, không logic
- ✅ `PlayerStorage` chỉ I/O, không validate

---

### **5. `/model/` — Data Models (KHÔNG CHỨA LOGIC)**

**Trách nhiệm:**
- Chứa data (fields)
- Getter/setter
- Tính toán DERIVED stat (không phải logic nghiệp vụ)

**ĐƯỢC PHÉP:**
- ✅ Tính derived stat: `getMaxHP()` từ VIT + level
- ✅ Format string: `toDetailString()`

**KHÔNG ĐƯỢC:**
- ❌ Validate nghiệp vụ
- ❌ Save/load data
- ❌ Gọi service khác

**Ví dụ:**
```java
// PlayerStats.java — ĐÚNG
public int getMaxHP() {
    int vit = getVitality();
    return vit * 10 + level * 5;  // tính toán derived stat
}

public void addPrimaryStat(StatType type, int amount) {
    if (!type.isPrimary()) {
        throw new IllegalArgumentException("...");  // validate cơ bản
    }
    statContainer.addBase(type, amount);
}
```

---

### **6. `/stats/` — Stat System Core**

**Bao gồm:**
- `StatType` enum — định nghĩa stat types
- `StatContainer` — lưu base + bonus stat

**Quy tắc:**
- ✅ `StatType.isPrimary()` — helper method OK
- ✅ `StatContainer` — data structure thuần, không logic nghiệp vụ

---

### **7. `/core/` — Foundation (PHASE 0)**

#### **CoreContext.java — Singleton Dependency Injection**
**Trách nhiệm:**
- Khởi tạo TẤT CẢ service/manager một lần duy nhất
- Cung cấp getter cho các module khác
- Register lifecycle callbacks

**Quy tắc:**
- ✅ Singleton pattern (chỉ 1 instance)
- ✅ Constructor khởi tạo tất cả dependencies
- ✅ Register callbacks trong `registerAllModules()`
- ❌ KHÔNG chứa logic nghiệp vụ

#### **LifecycleManager.java — Plugin Lifecycle**
**Trách nhiệm:**
- Quản lý plugin enable/disable callbacks
- Quản lý player load/save callbacks
- Module enable/disable system

**Quy tắc:**
- ✅ Callbacks thực thi theo thứ tự
- ✅ Disable theo thứ tự ngược lại enable
- ✅ Try-catch từng callback để tránh 1 module lỗi crash hết

---

### **8. `/ui/` — User Interface**

**Bao gồm:**
- `PlayerUIService` — hiển thị ActionBar, join/quit message
- `ActionBarService` — update action bar
- `PlayerStatusProvider` — cung cấp data cho UI

**Quy tắc:**
- ✅ Chỉ lo hiển thị, không logic
- ✅ Lấy data từ PlayerProfile/Service

---

### **9. `/event/` — Custom Events**

**Bao gồm:**
- `PlayerLevelUpEvent` — custom event khi level up

**Quy tắc:**
- ✅ Extends `org.bukkit.event.Event`
- ✅ Chứa data readonly (oldLevel, newLevel...)
- ❌ KHÔNG chứa logic

---

## 🔥 NGUYÊN TẮC CODING

### **1. KHÔNG BAO GIỜ VIẾT LOGIC TRONG LISTENER/COMMAND**
```java
// ❌ SAI
@EventHandler
public void onDamage(EntityDamageEvent e) {
    double damage = e.getDamage();
    double defense = getDefense();  // ❌ logic ở listener
    e.setDamage(damage - defense);  // ❌ tính toán ở listener
}

// ✅ ĐÚNG
@EventHandler
public void onDamage(EntityDamageEvent e) {
    e.setCancelled(true);
    combatService.handleDamage(attacker, target, e.getCause());
}
```

---

### **2. SERVICE LÀ NƠI DUY NHẤT CHỨA LOGIC**
**Một số service quan trọng:**
- `LevelService` — exp, level up
- `StatService` — stat allocation
- `CombatService` — damage calculation (sắp làm PHASE 3)
- `SkillService` — skill execution (PHASE 6)

---

### **3. DEPENDENCY INJECTION QUA CONSTRUCTOR**
```java
// ✅ ĐÚNG
public class StatCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final StatService statService;
    
    public StatCommand(PlayerManager playerManager, StatService statService) {
        this.playerManager = playerManager;
        this.statService = statService;
    }
}
```

❌ **KHÔNG dùng:**
- `CoreContext.getInstance()` trong constructor
- `Bukkit.getServicesManager()` để lấy service
- Static singleton trừ CoreContext

---

### **4. KHÔNG HARD-CODE STAT/DAMAGE**
```java
// ❌ SAI
public int getMaxHP() {
    return 100 + level * 20;  // hard-code
}

// ✅ ĐÚNG
public int getMaxHP() {
    int vit = getVitality();
    return vit * 10 + level * 5;  // scale theo stat
}
```

---

### **5. GHI CHÚ TIẾNG VIỆT KHÔNG DẤU**
```java
// khoi tao stat mac dinh
private void initDefaultStats() {
    // stat mac dinh chua chon class
    statContainer.setBase(StatType.STRENGTH, 2);
}
```

---

### **6. RETURN BOOLEAN THAY VÌ THROW EXCEPTION**
```java
// ✅ ĐÚNG (service)
public boolean allocateStatPoints(...) {
    if (amount <= 0) return false;  // validate
    // ...
    return true;
}

// ❌ SAI (model)
public void addPrimaryStat(StatType type, int amount) {
    if (!type.isPrimary()) {
        throw new IllegalArgumentException("...");  // OK trong model
    }
}
```

---

### **7. TÁCH MODULE RÕ RÀNG**
**Mỗi PHASE có folder riêng:**
- `/classjob/` — PHASE 5
- `/combat/` — PHASE 3
- `/skill/` — PHASE 6
- `/module/boss/`, `/module/dungeon/` — PHASE 9

**Quy tắc:**
- ✅ Module độc lập, ít dependency lẫn nhau
- ✅ Module có thể enable/disable
- ✅ Module có lifecycle callbacks

---

### **8. LIFECYCLE PATTERN**
```java
// CoreContext.registerPlayerSystem()
lifecycleManager.registerOnEnable(() -> {
    // khoi tao service
    playerUIService = new PlayerUIService();
    
    // register listener
    Bukkit.registerEvents(joinListener, plugin);
});

lifecycleManager.registerOnDisable(() -> {
    // save all online player
    playerManager.getAllOnline().forEach(playerStorage::save);
    
    // cleanup
    playerManager.clear();
});
```

---

### **9. YAML STORAGE PATTERN**
**File structure:**
```
plugins/HControl/
  players/
    <uuid>.yml  — player data
  config.yml    — plugin config (sau này)
```

**Storage class:**
```java
public class PlayerStorage {
    private final File dataFolder;
    
    public PlayerProfile load(UUID uuid) {
        // return null neu chua co file
    }
    
    public void save(PlayerProfile profile) {
        // ghi xuong YAML
    }
}
```

---

### **10. DATA FLOW**
```
Player Join
  → JoinServerListener
    → playerStorage.load(uuid)
    → playerManager.add(profile)
    → lifecycleManager.onPlayerLoad(profile)

Player Use Command
  → Command parse args
  → Service execute logic
  → Update model
  → Send message

Player Quit
  → OutServerListener
    → lifecycleManager.onPlayerSave(profile)
    → playerStorage.save(profile)
    → playerManager.remove(uuid)
```

---

## ⛔ CẤM TUYỆT ĐỐI

1. ❌ **Reload plugin** — dùng LifecycleManager.reloadAll() thay vì /reload
2. ❌ **Logic trong Main.java** — chỉ wiring
3. ❌ **Hard-code số** — dùng formula scale theo stat
4. ❌ **Singleton trừ CoreContext** — dùng DI
5. ❌ **Modify vanilla damage trực tiếp** — cancel rồi tính lại
6. ❌ **Validate trong Command** — để Service lo
7. ❌ **Storage logic trong Manager** — Manager chỉ CRUD RAM
8. ❌ **Business logic trong Model** — Model chỉ data + derived calculation

---

## ✅ CHECKLIST KHI TẠO CODE MỚI

**Trước khi code:**
- [ ] Thuộc PHASE nào? (1-15)
- [ ] Thuộc layer nào? (Command/Service/Model/...)
- [ ] Cần Service mới hay dùng Service có sẵn?
- [ ] Cần thêm field vào Model không?

**Khi code Command:**
- [ ] Chỉ parse args?
- [ ] Gọi Service để xử lý logic?
- [ ] Chỉ hiển thị kết quả?

**Khi code Service:**
- [ ] Validate đầu vào?
- [ ] Return boolean/value rõ ràng?
- [ ] Không throw exception ra ngoài?

**Khi code Model:**
- [ ] Chỉ chứa data?
- [ ] Derived stat tính từ base stat?
- [ ] Không gọi Service/Storage?

**Khi code Listener:**
- [ ] Lifecycle listener (join/quit) → OK quản lý profile
- [ ] Event listener (combat/chat) → chỉ gọi Service?

---

## 📚 VÍ DỤ CHUẨN

### **Command → Service → Model**
```java
// 1. Command (UI layer)
public class StatCommand implements CommandExecutor {
    public boolean onCommand(...) {
        StatType type = statService.parseStatType(args[1]);  // parse
        boolean success = statService.allocateStatPoints(profile, type, amount);  // service
        player.sendMessage(success ? "OK" : "Lỗi");  // hiển thị
    }
}

// 2. Service (Logic layer)
public class StatService {
    public boolean allocateStatPoints(PlayerProfile profile, StatType type, int amount) {
        if (profile.getStatPoints() < amount) return false;  // validate
        profile.getStats().addPrimaryStat(type, amount);  // execute
        profile.removeStatPoints(amount);
        return true;
    }
}

// 3. Model (Data layer)
public class PlayerStats {
    public void addPrimaryStat(StatType type, int amount) {
        statContainer.addBase(type, amount);  // data mutation
    }
}
```

---

## 🎯 MỤC TIÊU

**Server sống lâu = Code clean + Module hoá + Không đập core**

- Core (PHASE 0-2) → KHÔNG BAO GIỜ ĐẬP LẠI
- Content (PHASE 3-15) → Thêm module mới, không sửa core
- Balance → Sửa số trong config/formula, không sửa code

==================================================
DONE = SERVER SỐNG
==================================================
