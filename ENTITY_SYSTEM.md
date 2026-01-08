# 🐉 ENTITY SYSTEM - HỆ THỐNG MOB TU TIÊN

> **Mục tiêu:** Vanilla mobs + Custom mods có realm, stats, HP riêng  
> **Extensible:** Dễ dàng thêm mob type mới, elite, boss

---

## 📁 KIẾN TRÚC

```
/entity/
├── EntityProfile.java      — Model (HP, realm, stats...)
├── EntityManager.java       — RAM cache (UUID -> Profile)
├── EntityRegistry.java      — Template cho mob types
├── EntityService.java       — Logic (spawn elite, boss...)
└── EntityLifecycleListener  — Track spawn/death
```

---

## 🧱 COMPONENTS

### **1. EntityProfile** — Mob Stats
```java
EntityProfile profile = new EntityProfile(uuid, EntityType.ZOMBIE);
profile.getRealm();          // CultivationRealm.LUYEN_KHI
profile.getMaxHP();          // 20.0
profile.getCurrentHP();      // 20.0
profile.getAttack();         // 3.0
profile.getDefense();        // 0.0
profile.isBoss();            // false
profile.isElite();           // false
```

**Hỗ trợ:**
- Vanilla mobs (Zombie, Skeleton...)
- Custom name mobs
- Boss/Elite flags

---

### **2. EntityManager** — Quản lý RAM
```java
EntityManager manager = new EntityManager();

// Lay profile
EntityProfile profile = manager.get(entity.getUniqueId());

// Lay hoac tao
EntityProfile profile = manager.getOrCreate(entity);

// Remove khi chet
manager.remove(entity.getUniqueId());
```

**Pattern tương tự PlayerManager:**
- Cache RAM only (mob không lưu YAML)
- Auto cleanup khi mob despawn/die

---

### **3. EntityRegistry** — Mob Templates
```java
EntityRegistry registry = new EntityRegistry();

// Dang ky vanilla mobs (constructor)
registry.register(EntityType.ZOMBIE, 
    CultivationRealm.LUYEN_KHI, 
    level: 1, 
    maxHP: 20, 
    attack: 3, 
    defense: 0
);

// Lay template
EntityTemplate template = registry.getTemplate(EntityType.WITHER);
// → realm: HOA_THAN, level: 8, maxHP: 300
```

**Vanilla mobs đã đăng ký:**

| Mob Type         | Realm       | Level | HP  | ATK | DEF |
|------------------|-------------|-------|-----|-----|-----|
| Zombie           | Luyện Khí   | 1     | 20  | 3   | 0   |
| Skeleton         | Luyện Khí   | 1     | 15  | 4   | 0   |
| Creeper          | Luyện Khí   | 2     | 20  | 25  | 0   |
| Enderman         | Trúc Cơ     | 3     | 40  | 7   | 5   |
| Blaze            | Trúc Cơ     | 4     | 20  | 6   | 3   |
| Wither Skeleton  | Kim Đan     | 5     | 50  | 8   | 6   |
| Elder Guardian   | Nguyên Anh  | 6     | 80  | 10  | 8   |
| Wither           | Hóa Thần    | 8     | 300 | 15  | 10  |
| Ender Dragon     | Hóa Thần    | 9     | 200 | 12  | 12  |

---

### **4. EntityService** — Spawn Logic
```java
EntityService service = new EntityService(manager, registry);

// Spawn binh thuong (theo template)
EntityProfile profile = service.initializeEntity(entity);

// Spawn elite (+50% stats, +1 realm)
EntityProfile elite = service.spawnElite(entity);

// Spawn boss (+200% stats, +2 realms, custom name)
EntityProfile boss = service.spawnBoss(entity, "§4§l魔王");
```

**Elite multipliers:**
- HP: ×1.5
- Attack: ×1.5
- Defense: ×1.5
- Realm: +1

**Boss multipliers:**
- HP: ×3.0
- Attack: ×3.0
- Defense: ×3.0
- Realm: +2

---

### **5. EntityLifecycleListener** — Auto Tracking
```java
@EventHandler
public void onEntitySpawn(CreatureSpawnEvent event) {
    // Auto tao profile khi spawn
    entityService.initializeEntity(event.getEntity());
}

@EventHandler
public void onEntityDeath(EntityDeathEvent event) {
    // Auto cleanup profile
    entityService.onEntityDeath(event.getEntity().getUniqueId());
}
```

---

## 🔧 TÍCH HỢP VÀO COMBATSERVICE

### **Player đánh Mob có realm:**
```java
public void handlePlayerAttackEntity(Player player, LivingEntity entity, 
                                    PlayerProfile playerProfile) {
    // lay mob profile
    EntityProfile mobProfile = entityManager.get(entity.getUniqueId());
    
    if (mobProfile == null) {
        // vanilla mob chua co profile -> tao
        mobProfile = entityService.initializeEntity(entity);
    }
    
    // tinh damage theo DamageFormula (realm suppression)
    double damage = DamageFormula.calculateFinalDamage(
        playerProfile.getRealm(), 
        mobProfile.getRealm(),
        techniqueModifier,
        mobProfile.getDefense()
    );
    
    // apply vao mob tu tien HP
    mobProfile.setCurrentHP(mobProfile.getCurrentHP() - damage);
    
    // check chet
    if (!mobProfile.isAlive()) {
        entity.setHealth(0);
    }
}
```

### **Mob đánh Player (đã có):**
```java
public void handleMobAttackPlayer(LivingEntity mob, Player player, 
                                 PlayerProfile playerProfile) {
    // lay mob profile
    EntityProfile mobProfile = entityManager.getOrCreate(mob);
    
    // dung mob attack tu profile (khong phai vanilla damage)
    double baseDamage = mobProfile.getAttack();
    
    // tinh damage voi defense player
    // ... (code hien tai)
}
```

---

## 🎯 SỬ DỤNG CHO CUSTOM MODS

### **Cách 1: Dùng ScoreboardTags**
```java
// Spawn command
/summon zombie ~ ~ ~ {Tags:["elite"]}

// Listener
@EventHandler
public void onEntitySpawn(CreatureSpawnEvent event) {
    LivingEntity entity = event.getEntity();
    
    if (entity.getScoreboardTags().contains("elite")) {
        entityService.spawnElite(entity);
        entity.setCustomName("§6§l[Tinh Anh] " + entity.getType().name());
    } else if (entity.getScoreboardTags().contains("boss")) {
        entityService.spawnBoss(entity, "§4§l魔王 - " + entity.getType());
    } else {
        entityService.initializeEntity(entity);
    }
}
```

### **Cách 2: Dùng Custom Metadata**
```java
// Mod spawn code
entity.setMetadata("realm", new FixedMetadataValue(plugin, "KIM_DAN"));
entity.setMetadata("level", new FixedMetadataValue(plugin, 7));

// Listener
@EventHandler
public void onEntitySpawn(CreatureSpawnEvent event) {
    if (event.getEntity().hasMetadata("realm")) {
        String realmName = event.getEntity().getMetadata("realm").get(0).asString();
        CultivationRealm realm = CultivationRealm.valueOf(realmName);
        
        // tao profile custom
        EntityProfile profile = new EntityProfile(...);
        profile.setRealm(realm);
        entityManager.add(profile);
    }
}
```

### **Cách 3: API cho Mod Developers**
```java
// Public API trong EntityService
public EntityProfile registerCustomMob(LivingEntity entity, 
                                      CultivationRealm realm,
                                      int level,
                                      double maxHP,
                                      double attack,
                                      double defense) {
    UUID uuid = entity.getUniqueId();
    EntityProfile profile = new EntityProfile(
        uuid, entity.getType(), entity.getCustomName(),
        realm, level, maxHP, attack, defense
    );
    entityManager.add(profile);
    return profile;
}
```

---

## 📊 DATA FLOW

```
Mob Spawn
  → CreatureSpawnEvent
    → EntityLifecycleListener.onEntitySpawn()
      → EntityService.initializeEntity()
        → EntityRegistry.getTemplate() — get stats theo type
        → EntityProfile created
        → EntityManager.add()

Player Attack Mob
  → EntityDamageByEntityEvent
    → CombatService.handlePlayerAttackEntity()
      → EntityManager.get(mobUUID)
      → DamageFormula.calculate() — realm suppression
      → mobProfile.setCurrentHP()
      → entity.setHealth() nếu chết

Mob Attack Player (đã có)
  → EntityDamageByEntityEvent
    → CombatService.handleMobAttackPlayer()
      → EntityManager.getOrCreate(mob)
      → mobProfile.getAttack() — dùng attack từ profile
      → Apply vào player tu tien HP

Mob Death
  → EntityDeathEvent
    → EntityLifecycleListener.onEntityDeath()
      → EntityService.onEntityDeath()
        → EntityManager.remove()
```

---

## ✅ LỢI ÍCH

1. **Vanilla mobs có stats tu tiên**
   - Zombie Luyện Khí vs Wither Hóa Thần
   - Damage theo realm suppression
   - Defense riêng cho từng loại

2. **Elite/Boss system**
   - `spawnElite()` — +50% stats
   - `spawnBoss()` — ×3 stats
   - Tự động tăng realm

3. **Extensible cho mods**
   - ScoreboardTags
   - Metadata
   - Public API
   - Custom registry

4. **Clean architecture**
   - Tách biệt player vs entity
   - Không hard-code mob stats
   - Template pattern
   - Easy testing

---

## 🔮 PHASE TIẾP THEO

**PHASE 7 — AI & MOB RPG** (sau khi integrate entity system):
- [ ] Mob combat AI (khi nào attack, flee...)
- [ ] Mob skill system (boss có skill)
- [ ] Aggro system (taunt, threat...)
- [ ] Mob drops theo realm (linh thạch, đan dược...)

**PHASE 8 — ITEM & EQUIPMENT**:
- [ ] Drop items từ mob (linh thạch, da quái...)
- [ ] Item quality theo mob realm

**PHASE 10 — ECONOMY**:
- [ ] Kill mob → exp
- [ ] Kill mob → karma (good/bad)
- [ ] Mob reputation system

---

## 📝 CHECKLIST INTEGRATION

- [ ] Thêm EntityManager vào CoreContext
- [ ] Thêm EntityRegistry vào CoreContext
- [ ] Thêm EntityService vào CoreContext
- [ ] Register EntityLifecycleListener
- [ ] Update CombatService.handlePlayerAttack() dùng EntityProfile
- [ ] Update CombatService.handleMobAttackPlayer() dùng EntityProfile.attack
- [ ] Test vanilla mobs có stats đúng
- [ ] Test elite/boss spawn
- [ ] Test realm suppression player vs mob
- [ ] Document API cho mod devs
