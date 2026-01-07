# HControl RPG - MASTER TASK LIST
một người bạn thân lâu năm , vua it , hay chửi , nói bậy, hay code c# , python. ghi chú trong code là tiếng việt không dấu, code theo moldule ghép vào với nhau nhưng không rời quá,đang học tiếng trung
> Mục tiêu:
> Xây dựng plugin RPG lâu dài, cân mọi server, mở rộng không giới hạn.
> Làm theo thứ tự, không vội, không đập core.

==================================================
PHASE 0 — FOUNDATION (KHÔNG BAO GIỜ LÀM LẠI)
==================================================

## Core Architecture


==================================================
PHASE 1 — PLAYER SYSTEM (LINH HỒN RPG)
==================================================

## Player Profile

==================================================
PHASE 2 — STAT SYSTEM (CỐT LÕI RPG)
==================================================


==================================================
PHASE 3 — COMBAT SYSTEM (THAY DAMAGE VANILLA)
==================================================

## Combat Core


==================================================
PHASE 4 — MANA & RESOURCE
==================================================



==================================================
PHASE 5 — CLASS / JOB SYSTEM
==================================================



==================================================
PHASE 6 — SKILL SYSTEM (KHÔNG HARD-CODE)
==================================================



==================================================
PHASE 7 — AI & MOB RPG
==================================================


==================================================
PHASE 8 — ITEM & EQUIPMENT
==================================================


==================================================
PHASE 9 — WORLD & CONTENT
==================================================


==================================================
PHASE 10 — ECONOMY & SOCIAL
==================================================



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
---

# ☯️ TRIẾT LÝ TU TIÊN - DAMAGE & LEVEL SYSTEM

> **CÂU CHỐT NÃO:**
> Tu tiên không phải ai đánh mạnh hơn thì thắng
> Mà là: **cảnh giới > công pháp > căn cơ > pháp thuật**

## 🧠 DAMAGE TU TIÊN - 4 TẦNG NHÂN

**Damage cuối KHÔNG BAO GIỜ chỉ là 1 con số.**

```
FinalDamage = BaseRealmDamage 
            × RealmSuppression 
            × TechniqueModifier 
            × (1 - DefenseMitigation) 
            × RandomDaoFactor
```

### 🔹 (1) BaseRealmDamage — SỨC MẠNH CẢNH GIỚI

**Damage tăng theo cảnh giới, KHÔNG theo level nhỏ.**

| Cảnh giới    | Base Damage |
|--------------|-------------|
| Luyện Khí    | 10          |
| Trúc Cơ      | 25          |
| Kim Đan      | 70          |
| Nguyên Anh   | 200         |
| Hóa Thần     | 600         |

📌 **Không crit, không luck ở đây.**

### 🔹 (2) RealmSuppression — UY ÁP CẢNH GIỚI (CỰC QUAN TRỌNG)

**Cao cảnh giới đánh thấp = áp chế**  
**Thấp đánh cao = phản phệ**

```java
int diff = attackerRealm - targetRealm;

double realmModifier;
if (diff >= 1) realmModifier = 1 + diff * 0.5;
else realmModifier = Math.max(0.1, 1 + diff * 0.7);
```

| Chênh lệch | Modifier |
|------------|----------|
| +2 realm   | ×2.0     |
| +1 realm   | ×1.5     |
| 0          | ×1.0     |
| -1         | ×0.3     |
| -2         | ×0.1     |

👉 **Đánh vượt cấp gần như không hiệu quả**  
👉 **Nhưng không phải 0 damage (có hy vọng)**

### 🔹 (3) TechniqueModifier — CÔNG PHÁP / PHÁP THUẬT

**Damage không đến từ stat, mà từ công pháp.**

| Cấp bậc    | Modifier |
|------------|----------|
| Phàm pháp  | ×1.0     |
| Linh cấp   | ×1.3     |
| Địa cấp    | ×1.7     |
| Thiên cấp  | ×2.5     |
| Cấm thuật  | ×4.0 (có phản phệ) |

📌 **Đây là nơi build khác nhau.**

### 🔹 (4) DefenseMitigation — PHÒNG THỦ TU TIÊN

**Không dùng armor kiểu RPG.**

```java
double mitigate = defense / (defense + attackerBase * 3);
```

👉 **Không bao giờ giảm quá 80%.**

### 🔹 (5) RandomDaoFactor — "NGỘ ĐẠO"

**Không phải crit chí mạng.**

```java
double daoFactor = random(0.9, 1.1);
```

✔ Nhẹ  
✔ Có cảm giác "linh động"  
❌ **Không one-shot**

---

## ⚔️ VƯỢT CẢNH GIỚI ĐÁNH NHAU THẾ NÀO?

### ❌ Cách ngu

- Thấp cảnh giới vẫn crit 1k damage

### ✅ Cách tu tiên đúng

**Damage rất thấp, nhưng có:**

- Nội thương
- Tâm ma
- Phá pháp bảo
- Tạo cơ hội chạy

**Ví dụ:**
```
Luyện Khí đánh Kim Đan
→ damage 3–5
→ nhưng gây nội thương +5%
```

👉 **Tu tiên = đánh để sống, không phải thắng.**

---

## 🧪 VÍ DỤ SỐ THỰC

### Kim Đan đánh Trúc Cơ

```
BaseRealmDamage = 70
RealmSuppression = 1.5
Technique = ×1.3
Defense = 20%

70 × 1.5 × 1.3 × 0.8 ≈ 109
```

✔ Đánh mạnh  
❌ **Không one-shot**

### Trúc Cơ đánh Kim Đan

```
Base = 25
Suppression = 0.3
Technique = ×1.7
Defense = 30%

25 × 0.3 × 1.7 × 0.7 ≈ 9
```

👉 **Có tác dụng, nhưng không vô lý**

---

## ⚡ THIÊN KIẾP DAMAGE (KHÔNG GIỐNG COMBAT)

**Thiên kiếp không dùng công thức combat.**

```
TribulationDamage = MaxHP × RealmMultiplier × Random(0.8 – 1.2)
```

| Cảnh giới  | % HP      |
|------------|-----------|
| Kim Đan    | 40–60%    |
| Nguyên Anh | 60–80%    |
| Hóa Thần   | 80–120%   |

👉 **Có thể chết thật**  
👉 **Không armor, không né**

---

## ☯️ 3 NGUYÊN TẮC VÀNG DAMAGE TU TIÊN

1. **Cảnh giới > tất cả**
2. **Không crit damage kiểu RPG**
3. **Vượt cấp = sống sót, không phải thắng**

---

# 🌱 LEVEL TRONG TU TIÊN LÀ GÌ?

> **CÂU CHỐT:**  
> Level trả lời câu hỏi: **"Ngươi đã luyện đủ chưa?"**  
> Cảnh giới trả lời câu hỏi: **"Ngươi mạnh tới đâu?"**

## ☯️ LEVEL KHÔNG PHẢI SỨC MẠNH

**Level = độ hoàn thiện trong cùng cảnh giới**

### ❌ LEVEL KHÔNG DÙNG ĐỂ

- ❌ Quyết định damage chính
- ❌ One-shot người khác
- ❌ Vượt cảnh giới

**Nếu level làm mấy việc này → tu tiên giả.**

### ✅ LEVEL NÊN DÙNG ĐỂ

#### 1️⃣ LEVEL = TIẾN ĐỘ TRONG CÙNG CẢNH GIỚI

```
Luyện Khí tầng 1 → tầng 9
Trúc Cơ sơ kỳ → trung kỳ → hậu kỳ
```

👉 Vẫn là cùng 1 cảnh giới, nhưng:
- ổn định hơn
- ít rủi ro hơn
- dễ đột phá hơn

📌 **Damage chỉ tăng rất nhẹ.**

#### 2️⃣ LEVEL = GIẢM RỦI RO ĐỘT PHÁ (CỐT LÕI)

```java
breakthroughChance += level * 0.5;
tribulationDamage -= level * 0.3;
```

| Level      | Đột phá              |
|------------|----------------------|
| Level thấp | Rất dễ thất bại      |
| Level cao  | Ổn định, an toàn     |

👉 **Level = chuẩn bị, không phải sức mạnh.**

#### 3️⃣ LEVEL = MỞ KHÓA CÔNG PHÁP / PHÁP THUẬT

**Không phải ai lên cảnh giới là xài được skill mạnh.**

```
Kim Đan level 1 → không dùng cấm thuật
Kim Đan level 7 → mở
```

👉 **Tạo cảm giác tu luyện có chiều sâu, không nhảy cóc.**

#### 4️⃣ LEVEL = GIỚI HẠN STAT TRONG CẢNH GIỚI

```java
maxSpirit = realmCap + level * smallScale;
```

👉 Level giúp:
- Khai thác hết tiềm năng cảnh giới
- Không cho "mới lên cảnh giới đã max"

#### 5️⃣ LEVEL = KHẢ NĂNG KIỂM SOÁT LINH LỰC

```java
manaCost *= (1 - level * 0.02);
```

**Level thấp:**
- Skill tốn nhiều linh lực
- Dễ nội thương

**Level cao:**
- Skill ổn định
- Ít phản phệ

---

## ⚔️ LEVEL ẢNH HƯỞNG DAMAGE NHƯ THẾ NÀO LÀ ĐÚNG?

**Rất nhẹ, chỉ để phân biệt "lão luyện vs mới đột phá".**

```java
damage *= 1 + (level * 0.01);
```

| Level | Damage bonus |
|-------|--------------|
| 1     | +1%          |
| 9     | +9%          |

📌 **Không bao giờ vượt realm.**

---

## 🧠 SO SÁNH RPG THƯỜNG VS TU TIÊN

| Thứ         | RPG thường      | Tu tiên             |
|-------------|-----------------|---------------------|
| **Level**   | Sức mạnh        | Độ hoàn thiện       |
| **Realm**   | Không có        | Sức mạnh thật       |
| **Damage**  | Theo level      | Theo realm          |
| **Đột phá** | Auto            | Rủi ro              |

---

# 🧱 LEVEL SYSTEM TRONG 1 CẢNH GIỚI

## ☯️ ĐỊNH NGHĨA

**Level = độ thuần thục trong cùng cảnh giới**

Không phải sức mạnh nhảy vọt, mà là:
- ổn định linh lực
- giảm rủi ro
- mở khóa tiềm năng

📌 **Cảnh giới quyết định trần sức mạnh**  
📌 **Level quyết định "đi vững hay đi liều"**

## 🧱 CẤU TRÚC LEVEL TRONG 1 CẢNH GIỚI

Mỗi cảnh giới có:
- `minLevel = 1`
- `maxLevel` = cố định (ví dụ 9 hoặc 10)

### Ví dụ chuẩn:

| Cảnh giới   | Level  |
|-------------|--------|
| Luyện Khí   | 1 → 9  |
| Trúc Cơ     | 1 → 9  |
| Kim Đan     | 1 → 9  |
| Nguyên Anh  | 1 → 7  |
| Hóa Thần    | 1 → 5  |

👉 **Cảnh giới cao → level ít hơn → mỗi level nặng hơn**

### Enum gợi ý:

```java
enum CultivationRealm {
    LUYEN_KHI(9),
    TRUC_CO(9),
    KIM_DAN(9),
    NGUYEN_ANH(7),
    HOA_THAN(5);

    private final int maxLevel;

    CultivationRealm(int maxLevel) {
        this.maxLevel = maxLevel;
    }

    public int getMaxLevel() {
        return maxLevel;
    }
}
```

---

## 🌱 TU VI → LEVEL

**Nguyên tắc:**
- Level không tăng tự động
- Phải tích đủ tu vi
- Không vượt quá maxLevel

```java
requiredCultivation = base × realmMultiplier × level²
```

### Ví dụ số:

| Level  | Tu vi cần |
|--------|-----------|
| 1 → 2  | 100       |
| 2 → 3  | 400       |
| 3 → 4  | 900       |
| 4 → 5  | 1600      |

👉 **Level cao = grind nặng, đúng tu tiên.**

### Pseudo-code:

```java
boolean tryLevelUp(profile) {
    if (profile.level >= realm.getMaxLevel()) return false;
    if (profile.cultivation < requiredCultivation) return false;

    profile.level++;
    profile.cultivation -= requiredCultivation;
    return true;
}
```

---

## ⚙️ LEVEL ẢNH HƯỞNG NHỮNG GÌ?

### ❌ LEVEL KHÔNG ẢNH HƯỞNG

- ❌ Damage chính
- ❌ Realm suppression
- ❌ One-shot

### ✅ LEVEL ẢNH HƯỞNG 5 THỨ SAU

#### 🔹 1. Giảm rủi ro đột phá (CỐT LÕI)

```java
breakthroughChance += level * 0.5;
```

| Level | Bonus  |
|-------|--------|
| 1     | +0.5%  |
| 9     | +4.5%  |

👉 **Level cao = đột phá an toàn hơn.**

#### 🔹 2. Giảm damage thiên kiếp

```java
tribulationDamage -= level * 3%; // cap 30%
```

👉 **Level cao = sống sót dễ hơn.**

#### 🔹 3. Ổn định linh lực (skill cost)

```java
manaCost *= (1 - level * 0.02);
```

👉 **Level cao:**
- skill rẻ hơn
- ít phản phệ

#### 🔹 4. Mở khóa công pháp / pháp thuật

```
Kim Đan level 1 → pháp thường
Kim Đan level 5 → bí thuật
Kim Đan level 8 → cấm thuật
```

👉 **Không có chuyện vừa đột phá xong là dùng skill max.**

#### 🔹 5. Kháng tâm ma / nội thương

```java
innerDemonChance -= level * 0.5;
```

👉 **Level cao = tâm cảnh vững.**

---

## 🧠 FLOW CHUẨN CỦA LEVEL SYSTEM

```
Tu luyện
  ↓
Tích tu vi
  ↓
Lên level (trong realm)
  ↓
Ổn định linh lực
  ↓
Giảm rủi ro đột phá
  ↓
Khi đủ → ĐỘT PHÁ
```

👉 **Level là bước chuẩn bị, không phải mục tiêu cuối.**

---

## ☯️ CÂU CHỐT NÃO (NHỚ KỸ)

> **Level không làm ngươi mạnh hơn nhiều**  
> **Level làm ngươi chết ít hơn**

Nếu giữ đúng câu này:
- ✅ Tu luyện có ý nghĩa
- ✅ Đột phá có căng thẳng
- ✅ Damage không vỡ

---