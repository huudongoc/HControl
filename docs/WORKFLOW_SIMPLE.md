# 🔄 WORKFLOW ĐƠN GIẢN — HCONTROL RPG

> **File này giúp bạn hiểu code chạy như thế nào theo thứ tự từ đầu đến cuối**

---

## 📋 MỤC LỤC

1. [Plugin khởi động](#1-plugin-khởi-động)
2. [Player join server](#2-player-join-server)
3. [Player đánh mob](#3-player-đánh-mob)
4. [Player dùng command](#4-player-dùng-command)
5. [Player đột phá (breakthrough)](#5-player-đột-phá-breakthrough)

---

## 1. PLUGIN KHỞI ĐỘNG

```
📦 Main.java (onEnable)
    │
    ├─> Tạo LifecycleManager
    │
    ├─> CoreContext.initialize()
    │     │
    │     ├─> Tạo EntityContext
    │     │     └─> EntityManager, EntityRegistry, EntityService, BossManager
    │     │
    │     ├─> Tạo PlayerContext
    │     │     └─> PlayerManager, PlayerStorage, StatService, PlayerHealthService
    │     │
    │     ├─> Tạo CombatContext
    │     │     └─> CombatService, DamageEffectService, SoundService...
    │     │
    │     ├─> Inject LevelService vào PlayerContext
    │     │     └─> Tạo CultivationProgressService
    │     │
    │     ├─> Tạo CultivationContext
    │     │     └─> BreakthroughService, TribulationService, TitleService, RoleService
    │     │
    │     └─> Tạo UIContext
    │           └─> (UI services được init sau)
    │
    ├─> Set EventRegistry (BukkitEventRegistry)
    │
    ├─> registerAllModules()
    │     │
    │     ├─> registerCommands()
    │     │     └─> Đăng ký: /stat, /dokiep, /tuvi...
    │     │
    │     ├─> registerPlayerSystem()
    │     │     └─> Đăng ký callbacks:
    │     │           - Init Player UI
    │     │           - Register listeners: Join, Quit, Chat, Respawn
    │     │           - Start AutoSaveTask (mỗi 5 phút)
    │     │           - Start ScoreboardUpdateTask (mỗi 1 giây)
    │     │
    │     └─> registerCombatSystem()
    │           └─> Đăng ký callbacks:
    │                 - Register listeners: Combat, Death, EntityLifecycle
    │                 - Init Entity UI
    │
    └─> lifecycleManager.enableAll()
          └─> Chạy tất cả callbacks đã đăng ký theo thứ tự
```

**Kết quả:**
- ✅ Tất cả services đã được tạo
- ✅ Tất cả listeners đã được đăng ký
- ✅ Tất cả tasks đã được start
- ✅ Plugin sẵn sàng nhận events

---

## 2. PLAYER JOIN SERVER

```
🎮 PlayerJoinEvent
    │
    └─> JoinServerListener.onPlayerJoin()
          │
          ├─> PlayerStorage.load(uuid)
          │     └─> Đọc file YAML từ disk
          │     └─> Tạo PlayerProfile (HP, Realm, Level, Stats...)
          │
          ├─> PlayerManager.add(profile)
          │     └─> Cache vào RAM (Map<UUID, PlayerProfile>)
          │
          ├─> lifecycleManager.onPlayerLoad(profile)
          │     └─> Gọi các callbacks đã đăng ký (nếu có)
          │
          ├─> PlayerUIService.updateActionBar(player)
          │     └─> Hiển thị HP, Qi trên action bar
          │
          ├─> ScoreboardService.updateScoreboard(player)
          │     └─> Hiển thị thông tin trên scoreboard bên phải
          │
          └─> NameplateService.updateNameplate(player)
                └─> Hiển thị tên, realm, HP trên đầu player
```

**Kết quả:**
- ✅ Player data đã được load từ disk
- ✅ Player đã được cache vào RAM
- ✅ UI đã được hiển thị (scoreboard, nameplate, action bar)

---

## 3. PLAYER ĐÁNH MOB

```
⚔️ EntityDamageByEntityEvent (Player đánh Mob)
    │
    └─> PlayerCombatListener.onEntityDamageByEntity()
          │
          ├─> Hủy event vanilla: event.setCancelled(true)
          │
          ├─> Lấy PlayerProfile từ PlayerManager
          │
          ├─> Kiểm tra target là gì:
          │     │
          │     ├─> Nếu là Player:
          │     │     └─> Lấy PlayerProfile của target
          │     │
          │     └─> Nếu là Mob:
          │           └─> EntityManager.getOrCreate(target)
          │                 └─> Tạo EntityProfile nếu chưa có
          │
          └─> CombatService.handlePlayerAttack(playerProfile, targetProfile)
                │
                └─> handleCombat(attacker, defender, modifier)
                      │
                      ├─> 1. TÍNH DAMAGE:
                      │     │
                      │     ├─> baseDamage = attacker.getRealm().getBaseDamage()
                      │     ├─> realmSuppression = calculateRealmSuppression(attacker, defender)
                      │     ├─> mitigation = calculateDefenseMitigation(defender.getDefense())
                      │     └─> finalDamage = baseDamage × realmSuppression × (1 - mitigation) × modifier
                      │
                      ├─> 2. ÁP DỤNG DAMAGE:
                      │     │
                      │     └─> defender.setCurrentHP(defender.getCurrentHP() - finalDamage)
                      │
                      ├─> 3. SYNC VANILLA HEALTH:
                      │     │
                      │     ├─> Nếu defender là Player:
                      │     │     └─> PlayerHealthService.updateCurrentHealth(player, profile)
                      │     │           └─> player.setHealth(...)
                      │     │
                      │     └─> Nếu defender là Mob:
                      │           └─> entity.setHealth(...)
                      │
                      ├─> 4. HIỆN EFFECT:
                      │     │
                      │     ├─> DamageEffectService.playHitEffect(location)
                      │     │     └─> Particle + sound
                      │     │
                      │     └─> DamageEffectService.spawnFloatingDamage(location, damage)
                      │           └─> Text bay lên trên
                      │
                      └─> 5. UPDATE UI:
                            │
                            ├─> Nếu defender là Mob:
                            │     └─> EntityNameplateService.updateNameplate(entity)
                            │           └─> Update HP trên đầu mob
                            │
                            └─> Nếu defender là Player:
                                  └─> NameplateService.updateNameplate(player)
                                        └─> Update HP trên đầu player
```

**Kết quả:**
- ✅ Damage đã được tính và áp dụng
- ✅ Vanilla health đã được sync
- ✅ Effect đã được hiển thị
- ✅ UI đã được update

---

## 4. PLAYER DÙNG COMMAND

### 4.1. Command `/stat add Root 10`

```
💬 Player gõ: /stat add Root 10
    │
    └─> StatCommand.onCommand()
          │
          ├─> Parse arguments: StatType.ROOT, amount = 10
          │
          ├─> Lấy PlayerProfile từ PlayerManager
          │
          └─> StatService.allocateStatPoints(profile, ROOT, 10)
                │
                ├─> Validate: profile.getStatPoints() >= 10?
                │     └─> Nếu không đủ → return error
                │
                ├─> profile.getStats().addPrimaryStat(ROOT, 10)
                │     └─> Tăng Root stat
                │
                ├─> profile.removeStatPoints(10)
                │     └─> Giảm điểm stat còn lại
                │
                ├─> Recalculate derived stats
                │     └─> Tính lại: MaxHP, Attack, Defense từ primary stats
                │
                └─> Update UI
                      ├─> ScoreboardService.updateScoreboard(player)
                      └─> NameplateService.updateNameplate(player)
```

### 4.2. Command `/dokiep` (Đột phá)

```
💬 Player gõ: /dokiep
    │
    └─> BreakthroughCommand.onCommand()
          │
          ├─> Lấy PlayerProfile từ PlayerManager
          │
          └─> BreakthroughService.attemptBreakthrough(profile)
                │
                ├─> Check conditions:
                │     ├─> Đủ tu vi để đột phá?
                │     ├─> Chưa đột phá gần đây?
                │     └─> Realm tiếp theo tồn tại?
                │
                ├─> Nếu không đủ điều kiện → return error
                │
                └─> TribulationService.startTribulation(profile, nextRealm)
                      │
                      ├─> Tạo TribulationContext (state machine)
                      │     └─> Phase: PREPARE → WAVE_1 → ... → QUESTION → SUCCESS/FAIL
                      │
                      ├─> Start TribulationTask (tick mỗi giây)
                      │     │
                      │     └─> Mỗi tick:
                      │           ├─> Nếu PREPARE: Countdown 10 giây
                      │           ├─> Nếu WAVE_X: Lightning strike, damage player
                      │           ├─> Nếu QUESTION: Hiển thị câu hỏi, chờ trả lời
                      │           └─> Nếu SUCCESS/FAIL: Complete breakthrough
                      │
                      └─> TribulationUI.showUI(player, context)
                            └─> Hiển thị boss bar, action bar
```

---

## 5. PLAYER ĐỘT PHÁ (BREAKTHROUGH)

```
🌟 TribulationTask tick (mỗi giây)
    │
    ├─> Lấy TribulationContext từ UiStateService
    │
    ├─> Switch phase:
    │     │
    │     ├─> PREPARE (10 giây):
    │     │     ├─> Countdown
    │     │     └─> Chuyển sang WAVE_1
    │     │
    │     ├─> WAVE_1 đến WAVE_9:
    │     │     ├─> Spawn lightning tại vị trí player
    │     │     ├─> Damage player (CombatService)
    │     │     ├─> Particle effects
    │     │     └─> Chuyển sang wave tiếp theo
    │     │
    │     ├─> QUESTION:
    │     │     ├─> Hiển thị câu hỏi trên chat
    │     │     ├─> Chờ player trả lời trong chat
    │     │     └─> TribulationInputListener bắt chat message
    │     │           ├─> Nếu đúng → SUCCESS
    │     │           └─> Nếu sai → FAIL
    │     │
    │     ├─> SUCCESS:
    │     │     ├─> BreakthroughService.completeBreakthrough(profile, nextRealm)
    │     │     │     ├─> profile.setRealm(nextRealm)
    │     │     │     ├─> Tăng MaxHP, MaxQi
    │     │     │     └─> Reset tu vi
    │     │     ├─> LevelUpEffectService.playLevelUpEffect(player)
    │     │     └─> Update UI
    │     │
    │     └─> FAIL:
    │           ├─> TribulationService.handleFailure(profile)
    │           └─> Update UI
    │
    └─> Update TribulationUI (boss bar, action bar)
```

---

## 🗺️ BẢN ĐỒ CÁC CONTEXT

```
CoreContext (Singleton)
│
├─> PlayerContext
│     ├─> PlayerManager (cache)
│     ├─> PlayerStorage (I/O)
│     ├─> LevelService
│     ├─> StatService
│     └─> PlayerHealthService
│
├─> CombatContext
│     ├─> CombatService (tính damage)
│     ├─> DamageEffectService (effect)
│     └─> SoundService
│
├─> EntityContext
│     ├─> EntityManager (cache)
│     ├─> EntityRegistry (templates)
│     └─> EntityService
│
├─> UIContext
│     ├─> PlayerUIService
│     ├─> ScoreboardService
│     ├─> NameplateService
│     └─> EntityNameplateService
│
└─> CultivationContext
      ├─> BreakthroughService
      ├─> TribulationService
      └─> TitleService
```

---

## 🎯 NGUYÊN TẮC QUAN TRỌNG

### ✅ ĐÚNG:
- **CombatService** là nơi DUY NHẤT tính damage
- **Profile** là nguồn sự thật (single source of truth)
- **UI** chỉ đọc state, không modify
- **Listener** chỉ nhận event và gọi service

### ❌ SAI:
- ❌ Tính damage trong listener
- ❌ UI modify state
- ❌ Logic trong command (command chỉ parse input)

---

## 📚 NẾU MUỐN TÌM CODE

### "Combat tính damage ở đâu?"
→ `CombatService.handleCombat()`

### "Player data được lưu ở đâu?"
→ `PlayerStorage.save()` (YAML)
→ `PlayerManager` (RAM cache)

### "UI hiển thị thế nào?"
→ `NameplateService`, `ScoreboardService`, `PlayerUIService`

### "Event được xử lý ở đâu?"
→ `listener/` folder (PlayerCombatListener, JoinServerListener...)

---

## 🔍 CÁCH DEBUG

1. **Kiểm tra log khi plugin start:**
   - Xem các dòng `[PHASE 1]`, `[PHASE 3]` có chạy không?

2. **Kiểm tra player data:**
   - File: `plugins/HControl/players/<uuid>.yml`

3. **Trace một event:**
   - Tìm listener trong `listener/`
   - Xem listener gọi service nào
   - Xem service modify gì

---

**Cuối cùng:** Nếu vẫn không hiểu, đọc file `WORKFLOW_GUIDE.md` để có thêm chi tiết!
