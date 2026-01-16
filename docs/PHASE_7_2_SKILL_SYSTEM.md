# ⚔️ PHASE 7.2 - MOB SKILLS SYSTEM

> **Hoàn thành:** 2026-01-16  
> **Trạng thái:** ✅ CORE SKILL SYSTEM READY

---

## 📋 TỔNG QUAN

PHASE 7.2 triển khai Skill System cho mobs - cho phép mobs sử dụng skills đặc biệt trong combat. System này là foundation cho Player Skills (PHASE 6) sau này.

---

## 🏗️ KIẾN TRÚC

### Package Structure:
```
hcontrol.plugin.skill/
├── MobSkill.java              - Skill data model
├── SkillType.java             - 8 loại skill
├── SkillRegistry.java         - Đăng ký skills
├── SkillExecutor.java         - Thực thi skill logic
└── SkillCooldownManager.java  - Quản lý cooldowns
```

---

## 🎯 SKILL TYPES

### 1. MELEE - Đánh cận chiến
**VD:** Zombie Bite, Spider Leap
- Damage multiplier
- Apply effects (poison, slow...)

### 2. RANGED - Tấn công xa
**VD:** Skeleton Multishot, Blaze Fireball Barrage
- Multiple projectiles
- Piercing
- Apply effects on hit

### 3. AOE - Area of Effect
**VD:** Creeper Mini Explosion, Witch Poison Cloud
- Area radius
- Damage all players in area
- Apply effects

### 4. BUFF - Tăng cường bản thân
**VD:** Zombie Rage (Speed + Strength)
- Apply potion effects to self
- No damage

### 5. DEBUFF - Làm yếu target
**VD:** Spider Web (Slowness), Witch Weakness
- Apply negative effects to target

### 6. SUMMON - Triệu hồi minions
**VD:** Necromancer Summon Zombies
- Spawn helper mobs
- (TODO PHASE 7.3)

### 7. TELEPORT - Dịch chuyển
**VD:** Enderman Teleport Strike
- Teleport to/behind target
- Deal damage after teleport

### 8. HEAL - Hồi máu
**VD:** Witch Self-heal
- Restore HP (30% max HP)
- Spawn heart particles

---

## 📊 DEFAULT SKILLS

### Zombie Skills:
```yaml
zombie_bite:
  type: MELEE
  cooldown: 8s
  damage: 150%
  effect: POISON (3s)
  
zombie_rage:
  type: BUFF
  cooldown: 20s
  effects:
    - SPEED II (5s)
    - STRENGTH I (5s)
```

### Skeleton Skills:
```yaml
skeleton_multishot:
  type: RANGED
  cooldown: 10s
  damage: 80%
  projectiles: 3
  
skeleton_poison_arrow:
  type: RANGED
  cooldown: 15s
  damage: 100%
  effect: POISON II (4s)
```

### Spider Skills:
```yaml
spider_web:
  type: DEBUFF
  cooldown: 12s
  damage: 50%
  effect: SLOWNESS IV (5s)
  
spider_leap:
  type: MELEE
  cooldown: 8s
  damage: 130%
  range: 10 blocks
```

### Creeper Skills:
```yaml
creeper_mini_explosion:
  type: AOE
  cooldown: 15s
  damage: 200%
  radius: 3 blocks
```

### Witch Skills:
```yaml
witch_poison_cloud:
  type: AOE
  cooldown: 20s
  damage: 50%
  radius: 4 blocks
  effect: POISON I (5s)
  
witch_heal:
  type: HEAL
  cooldown: 30s
  heal: 30% max HP
```

### Blaze Skills:
```yaml
blaze_fireball_barrage:
  type: RANGED
  cooldown: 12s
  damage: 70%
  projectiles: 5
```

### Enderman Skills:
```yaml
enderman_teleport_strike:
  type: TELEPORT
  cooldown: 10s
  damage: 180%
  range: 16 blocks
```

---

## 🔧 SKILL BUILDER PATTERN

```java
MobSkill skill = new MobSkill.Builder("zombie_bite")
    .displayName("§cZombie Bite")
    .type(SkillType.MELEE)
    .cooldown(8)
    .damageMultiplier(1.5)
    .range(3.0)
    .effects(List.of(
        new SkillEffect(PotionEffectType.POISON, 60, 0)
    ))
    .build();
```

---

## ⚙️ COOLDOWN SYSTEM

### Features:
- **Per-entity cooldowns:** Mỗi mob có cooldown riêng
- **Per-skill tracking:** Track từng skill riêng biệt
- **Auto cleanup:** Remove expired cooldowns
- **Thread-safe:** ConcurrentHashMap

### Usage:
```java
SkillCooldownManager cooldownManager = new SkillCooldownManager();

// Set cooldown
cooldownManager.setCooldown(mobUUID, "zombie_bite", 8);

// Check cooldown
if (!cooldownManager.isOnCooldown(mobUUID, "zombie_bite")) {
    // Can use skill
}

// Get remaining time
int remaining = cooldownManager.getRemainingCooldown(mobUUID, "zombie_bite");
player.sendMessage("Cooldown: " + remaining + "s");
```

---

## 🤖 AI INTEGRATION

### AggressiveBrain với Skills:
```java
private void tryUseSkill(LivingEntity entity, EntityProfile profile, Player target) {
    SkillRegistry skillRegistry = CoreContext.getInstance().getSkillRegistry();
    SkillExecutor skillExecutor = CoreContext.getInstance().getSkillExecutor();
    
    // Get skills cho mob type
    List<MobSkill> skills = skillRegistry.getSkillsForMob(entity.getType());
    
    // Random skill
    MobSkill skill = skills.get(random.nextInt(skills.size()));
    
    // Execute
    skillExecutor.executeSkill(entity, profile, target, skill);
}
```

**Skill Usage Rate:**
- 20% chance mỗi AI tick (mỗi giây)
- Random skill selection
- Auto check cooldown
- Auto check range

---

## 🎨 VISUAL & SOUND EFFECTS

### Particle Effects (theo skill type):
- **MELEE:** CRIT particles
- **RANGED:** FLAME particles
- **AOE:** EXPLOSION particles
- **BUFF:** ENCHANT particles
- **DEBUFF:** SMOKE particles
- **TELEPORT:** END_ROD particles
- **HEAL:** HEART particles

### Sound Effects:
- **MELEE:** ENTITY_PLAYER_ATTACK_STRONG
- **RANGED:** ENTITY_ARROW_SHOOT
- **AOE:** ENTITY_GENERIC_EXPLODE
- **BUFF:** ENTITY_PLAYER_LEVELUP
- **TELEPORT:** ENTITY_ENDERMAN_TELEPORT

---

## 📈 PERFORMANCE

### Metrics:
- **Skill check:** 20% chance per tick = 0.2 skills/second/mob
- **Per-skill cost:** ~1ms (estimate)
- **100 mobs:** ~20 skills/second = minimal impact

### Optimization:
- Cooldowns prevent spam
- Skill registry cached
- Executor stateless (no allocation)

---

## 🚀 USAGE EXAMPLES

### Example 1: Đăng ký Custom Skill
```java
SkillRegistry registry = CoreContext.getInstance().getSkillRegistry();

MobSkill customSkill = new MobSkill.Builder("custom_lightning")
    .displayName("§bLightning Strike")
    .type(SkillType.AOE)
    .cooldown(30)
    .damageMultiplier(3.0)
    .range(50.0)
    .areaRadius(5.0)
    .build();

registry.registerSkill(customSkill);
registry.registerMobSkill(EntityType.WITHER, "custom_lightning");
```

### Example 2: Force Execute Skill
```java
SkillExecutor executor = CoreContext.getInstance().getSkillExecutor();
MobSkill skill = registry.getSkill("zombie_bite");

executor.executeSkill(zombie, zombieProfile, player, skill);
```

### Example 3: Clear Cooldowns
```java
SkillCooldownManager cooldowns = CoreContext.getInstance().getCooldownManager();

// Clear specific skill
cooldowns.clearCooldown(mobUUID, "zombie_bite");

// Clear all skills của mob
cooldowns.clearCooldowns(mobUUID);
```

---

## 🎯 NEXT STEPS - PHASE 7.3

### Advanced Skills:
- [ ] **Summon skills** - Spawn minions
- [ ] **Chain skills** - Combo system
- [ ] **Conditional skills** - Use when HP < 50%
- [ ] **Charging skills** - Cast time/channel

### AI Improvements:
- [ ] **Smart skill selection** - Dùng skill phù hợp với tình huống
- [ ] **Skill priority** - Ưu tiên skill quan trọng
- [ ] **Team coordination** - Buff allies

### Boss Skills:
- [ ] **Phase-based skills** - Skill khác nhau theo phase
- [ ] **Ultimate skills** - Skill cực mạnh, cooldown lâu
- [ ] **Environment skills** - Thay đổi terrain

---

## ✅ CHECKLIST HOÀN THÀNH

- [x] MobSkill data model
- [x] SkillType enum (8 types)
- [x] SkillRegistry với 15+ default skills
- [x] SkillExecutor (8 execution types)
- [x] SkillCooldownManager
- [x] Tích hợp vào AggressiveBrain
- [x] Tích hợp vào EntityContext
- [x] Tích hợp vào CoreContext
- [x] Visual/Sound effects
- [ ] YAML config loading (PHASE 7.3)
- [ ] Summon skills (PHASE 7.3)
- [ ] Boss skills (PHASE 7.3)
- [ ] Player skills (PHASE 6)

---

## 📚 TÀI LIỆU LIÊN QUAN

- `PHASE_7_AI_SYSTEM.md` - AI System foundation
- `PHASE_STATUS_REPORT.md` - Tổng quan các phases
- `MASTER_TASK_LIST.md` - Roadmap dài hạn

---

**Cập nhật lần cuối:** 2026-01-16  
**Trạng thái:** ✅ CORE COMPLETE - Ready for advanced skills & boss mechanics
