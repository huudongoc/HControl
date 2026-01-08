# 🔧 HControl RPG - Quick Reference Guide

> **Mục đích:** Tra cứu nhanh commands, paths, patterns  
> **For:** Developers

---

## 📁 FILE STRUCTURE

```
HControl/
├── src/main/java/hcontrol/plugin/
│   ├── Main.java                          # Plugin entry point
│   ├── core/
│   │   ├── CoreContext.java               # Singleton DI container
│   │   ├── LifecycleManager.java          # Plugin lifecycle
│   │   ├── PlayerContext.java             # Player domain services
│   │   ├── CombatContext.java             # Combat domain services
│   │   ├── EntityContext.java             # Entity domain services
│   │   ├── UIContext.java                 # UI domain services
│   │   └── CultivationContext.java        # Cultivation domain services
│   ├── model/
│   │   ├── PlayerProfile.java             # Player data model
│   │   ├── EntityProfile.java             # Entity data model
│   │   ├── PlayerStats.java               # Stat container
│   │   ├── LivingActor.java               # Combat interface
│   │   ├── CultivationRealm.java          # Realm enum
│   │   └── ...
│   ├── player/
│   │   ├── PlayerManager.java             # RAM cache
│   │   ├── PlayerStorage.java             # YAML I/O
│   │   ├── LevelService.java              # Level/exp logic
│   │   ├── BreakthroughService.java       # Breakthrough logic
│   │   └── PlayerHealthService.java       # HP sync
│   ├── stats/
│   │   ├── StatService.java               # Stat allocation
│   │   ├── StatType.java                  # Stat enum
│   │   └── StatContainer.java             # Base + bonus stats
│   ├── service/
│   │   └── CombatService.java             # Combat damage logic
│   ├── tribulation/
│   │   ├── TribulationService.java        # Tribulation orchestrator
│   │   ├── TribulationContext.java        # State machine
│   │   ├── TribulationTask.java           # BukkitTask ticker
│   │   ├── TribulationPhase.java          # Phase enum
│   │   └── TribulationResult.java         # Result enum
│   ├── ui/
│   │   ├── ScoreboardService.java         # Scoreboard
│   │   ├── NameplateService.java          # Player nameplate
│   │   ├── EntityNameplateService.java    # Entity nameplate
│   │   └── PlayerUIService.java           # ActionBar, messages
│   ├── command/
│   │   ├── TuViCommand.java               # /tuvi
│   │   ├── StatCommand.java               # /stat
│   │   ├── BreakthroughCommand.java       # /breakthrough
│   │   └── ReloadCommand.java             # /hc reload
│   ├── listener/
│   │   ├── JoinServerListener.java        # Player join
│   │   ├── QuitServerListener.java        # Player quit
│   │   ├── PlayerCombatListener.java      # Combat events
│   │   └── ...
│   ├── module/
│   │   └── boss/                          # Boss system
│   └── event/
│       └── PlayerLevelUpEvent.java        # Custom event
└── plugins/HControl/
    └── players/
        └── <uuid>.yml                     # Player data files
```

---

## 🎮 PLAYER COMMANDS

### Basic Commands
```
/tuvi                    # Xem thông tin tu vi
/stat add <stat> <value> # Thêm stat (STR, VIT, AGI, INT, WIS)
/stat info [player]      # Xem stat (admin)
/breakthrough            # Thử đột phá (tribulation)
```

### Admin Commands (Future)
```
/hc reload               # Reload plugin
/hc debug <player>       # Debug player data
/hc setstat <p> <s> <v>  # Set stat
/hc setrealm <p> <realm> # Set realm
/hc heal <player>        # Heal player
```

---

## 💻 CODE PATTERNS

### 1. Get Player Profile
```java
// In Service/Command
PlayerProfile profile = CoreContext.getInstance()
    .getPlayerContext()
    .getPlayerManager()
    .getProfile(player.getUniqueId());

if (profile == null) {
    player.sendMessage("§cError: Profile not found");
    return;
}
```

### 2. Update Stats
```java
// In Service
PlayerStats stats = profile.getStats();
stats.addPrimaryStat(StatType.STRENGTH, 5);
profile.removeStatPoints(5);

// Save (auto saved on quit, or manual)
CoreContext.getInstance()
    .getPlayerContext()
    .getPlayerStorage()
    .save(profile);
```

### 3. Combat Damage
```java
// In CombatService
public void handleCombat(LivingActor attacker, LivingActor defender, double techniqueModifier) {
    // Realm suppression
    double suppression = calculateRealmSuppression(
        attacker.getRealm(), 
        defender.getRealm()
    );
    
    // Base damage
    double baseDamage = getBaseRealmDamage(attacker.getRealm());
    
    // Defense mitigation
    double mitigation = calculateDefenseMitigation(
        defender.getDefense(), 
        baseDamage
    );
    
    // Final damage
    double finalDamage = baseDamage * suppression * techniqueModifier * (1 - mitigation);
    
    // Apply
    defender.setCurrentHP(defender.getCurrentHP() - finalDamage);
}
```

### 4. Create Command
```java
public class MyCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final MyService myService;
    
    // Constructor injection
    public MyCommand(PlayerManager playerManager, MyService myService) {
        this.playerManager = playerManager;
        this.myService = myService;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players");
            return true;
        }
        
        // Get profile
        PlayerProfile profile = playerManager.getProfile(player.getUniqueId());
        if (profile == null) return true;
        
        // Call service
        boolean success = myService.doSomething(profile, args);
        
        // Feedback
        player.sendMessage(success ? "Success" : "Failed");
        return true;
    }
}
```

### 5. Register Command
```java
// In CoreContext
private void registerCommands() {
    // Get services
    PlayerManager pm = playerContext.getPlayerManager();
    MyService ms = myContext.getMyService();
    
    // Create command
    MyCommand cmd = new MyCommand(pm, ms);
    
    // Register
    PluginCommand pluginCmd = plugin.getCommand("mycommand");
    if (pluginCmd != null) {
        pluginCmd.setExecutor(cmd);
    }
}
```

### 6. Create Service
```java
public class MyService {
    
    private final PlayerManager playerManager;
    // ... dependencies
    
    public MyService(PlayerManager playerManager) {
        this.playerManager = playerManager;
    }
    
    /**
     * Do something with validation
     * @return true if success
     */
    public boolean doSomething(PlayerProfile profile, String[] args) {
        // Validate
        if (args.length < 2) return false;
        
        // Parse
        int value = Integer.parseInt(args[1]);
        if (value <= 0) return false;
        
        // Business logic
        profile.setSomething(value);
        
        return true;
    }
}
```

### 7. Create Listener
```java
public class MyListener implements Listener {
    
    private final MyService myService;
    
    public MyListener(MyService myService) {
        this.myService = myService;
    }
    
    @EventHandler
    public void onSomeEvent(SomeEvent event) {
        // Extract data
        Player player = event.getPlayer();
        
        // Call service (NO LOGIC HERE)
        myService.handleEvent(player);
    }
}
```

### 8. SubContext Pattern
```java
public class MyContext {
    
    private final MyService myService;
    private final MyManager myManager;
    
    public MyContext(/* dependencies */) {
        // Init services
        this.myManager = new MyManager();
        this.myService = new MyService(myManager);
    }
    
    // Getters
    public MyService getMyService() { return myService; }
    public MyManager getMyManager() { return myManager; }
    
    // Lifecycle (optional)
    public void onEnable() {
        // Register tasks, listeners...
    }
    
    public void onDisable() {
        // Cleanup...
    }
}
```

---

## 📊 STAT FORMULAS

### Primary Stats
- **STR** (Strength) - Attack power
- **VIT** (Vitality) - HP, defense
- **AGI** (Agility) - Crit rate, dodge
- **INT** (Intelligence) - Ling Qi, magic damage
- **WIS** (Wisdom) - Ling Qi regen, XP gain

### Derived Stats
```java
// PlayerStats.java
public int getMaxHP() {
    int vit = getVitality();
    return vit * 10 + level * 5; // VIT dominant
}

public double getAttackPower() {
    int str = getStrength();
    return str * 2.0; // STR dominant
}

public double getDefense() {
    int vit = getVitality();
    return vit * 1.5; // VIT dominant
}

public double getCriticalChance() {
    int agi = getAgility();
    return Math.min(50.0, agi * 0.5); // Cap 50%
}

public int getMaxLingQi() {
    int intel = getIntelligence();
    return intel * 8 + level * 3; // INT dominant
}
```

---

## ⚔️ COMBAT FORMULAS

### Realm Suppression
```java
int diff = attackerRealm.ordinal() - defenderRealm.ordinal();

double modifier;
if (diff >= 1) {
    // Attacker higher realm
    modifier = 1.0 + (diff * 0.5); // +50% per realm
} else {
    // Attacker lower/same realm
    modifier = Math.max(0.1, 1.0 + (diff * 0.7)); // -70% per realm, min 10%
}
```

### Defense Mitigation
```java
double mitigation = defense / (defense + attackerBaseDamage * 3.0);
mitigation = Math.min(0.8, mitigation); // Cap 80%
```

### Base Realm Damage
```java
double baseDamage = switch (realm) {
    case QI_CONDENSATION -> 10.0;
    case FOUNDATION_ESTABLISHMENT -> 25.0;
    case GOLDEN_CORE -> 70.0;
    case NASCENT_SOUL -> 200.0;
    case SOUL_TRANSFORMATION -> 600.0;
};
```

---

## 🌩️ TRIBULATION

### Wave Count by Realm
```java
int maxWaves = switch (toRealm) {
    case FOUNDATION_ESTABLISHMENT -> 3;
    case GOLDEN_CORE -> 5;
    case NASCENT_SOUL -> 7;
    case SOUL_TRANSFORMATION -> 9;
    default -> 1;
};
```

### Lightning Damage
```java
double damage = playerMaxHP * 0.4; // 40% HP per strike
```

---

## 🎨 UI FORMATS

### Nameplate
```java
// Player
String format = "§7[%s] §f%s §%c%.1f%%";
// %s = realm short name (LK, TC, KD...)
// %s = player name
// %c = color (a=green, e=yellow, c=red)
// %.1f = HP percent

// Entity
String format = "§c[%s %d] %s §%c%.1f%%";
// %s = realm short
// %d = level
// %s = entity name
```

### Scoreboard
```java
"§6§lTU VI"
"§7Realm: §f" + realmName + " " + level
"§7HP: §c" + hpPercent + "%"
"§7Tu Vi: §e" + cultivation + "/" + required
"§7Stat Points: §a" + statPoints
```

### ActionBar
```java
"§c❤ %d/%d HP §7| §bSTR:%d VIT:%d AGI:%d"
```

---

## 🔄 LIFECYCLE CALLBACKS

### Register Callback
```java
// In SubContext or CoreContext
lifecycleManager.registerOnEnable(() -> {
    // Init code
    myService.init();
});

lifecycleManager.registerOnDisable(() -> {
    // Cleanup code
    myService.cleanup();
});

lifecycleManager.registerOnPlayerLoad((profile) -> {
    // When player join
    myService.onPlayerLoad(profile);
});

lifecycleManager.registerOnPlayerSave((profile) -> {
    // Before player quit
    myService.onPlayerSave(profile);
});
```

---

## 💾 YAML STORAGE

### Load Player
```java
PlayerStorage storage = CoreContext.getInstance()
    .getPlayerContext()
    .getPlayerStorage();

PlayerProfile profile = storage.load(uuid);
// Returns null if file not exist
```

### Save Player
```java
storage.save(profile);
// Auto create file if not exist
// Path: plugins/HControl/players/<uuid>.yml
```

### YAML Structure
```yaml
uuid: "xxx-xxx-xxx"
playerName: "Steve"
level: 5
realm: "QI_CONDENSATION"
realmLevel: 5
cultivation: 1250
statPoints: 3
stats:
  strength: 10
  vitality: 12
  agility: 8
  intelligence: 6
  wisdom: 5
currentHP: 85.5
currentLingQi: 42.0
spiritualRoot: "FIRE"
rootQuality: "EXCELLENT"
daoHeart: 80.0
innerInjury: 5.0
```

---

## 🐛 DEBUG TIPS

### Check TPS
```java
// Via command or Spark/Timings
double tps = Bukkit.getTPS()[0]; // 1min average
```

### Profile Memory
```bash
# Use Spark profiler
/spark profiler
/spark profiler --stop
# View report in browser
```

### Log Debug Info
```java
Main.getInstance().getLogger().info("Debug: " + value);
Main.getInstance().getLogger().warning("Warning: " + msg);
Main.getInstance().getLogger().severe("Error: " + error);
```

### Check Plugin Version
```java
String version = plugin.getDescription().getVersion();
```

---

## ⚙️ BUILD & DEPLOY

### Build
```bash
./gradlew clean build
# Output: build/libs/HControl-1.0.0.jar
```

### Deploy to Server
```bash
cp build/libs/HControl-1.0.0.jar /path/to/server/plugins/
```

### Reload Plugin (in-game)
```
/hc reload
# or restart server
```

---

## 📚 REFERENCES

- **ISSUES.md** - All TODO issues
- **TODO.md** - Immediate tasks
- **TESTING_CHECKLIST.md** - Test plan
- **REFACTOR_PLAN.md** - Architecture
- **REFACTOR_PROGRESS.md** - History
- **PROJECT_SUMMARY.md** - Overview
- **NGUYÊN TẮC VÀNG** (instructions) - Coding rules

---

**Last Updated:** 2026-01-08  
**Version:** 1.0.0  
**Next:** Update when adding PHASE 4-6
