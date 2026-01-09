# 🏗️ CONTEXT EXPANSION GUIDE

> **Mục đích:** Hướng dẫn cách thêm Context mới và service mới theo đúng architecture

---

## 📋 QUY TRÌNH THÊM CONTEXT MỚI

### **Bước 1: Xác định có cần Context mới không?**

**✅ NÊN tạo Context mới khi:**
- Domain rõ ràng, độc lập (ví dụ: Item, World, Economy)
- Có 3+ services liên quan đến domain đó
- Cần lifecycle riêng (enable/disable độc lập)
- Domain đủ lớn để tách biệt

**❌ KHÔNG NÊN tạo Context mới khi:**
- Chỉ có 1-2 services → thêm vào Context gần nhất
- Logic đơn giản → thêm vào service hiện có
- Chỉ là helper → singleton static hoặc trong SharedContext

### **Bước 2: Tạo Context class**

```java
package hcontrol.plugin.core;

import hcontrol.plugin.Main;

/**
 * [DOMAIN] CONTEXT — PHASE [X]
 * Quan ly tat ca service lien quan den [Domain]
 */
public class [Domain]Context {
    
    // Services trong Context này
    private final Service1 service1;
    private final Service2 service2;
    private final Service3 service3;
    
    public [Domain]Context(Main plugin, ...) {
        // Khởi tạo services
        this.service1 = new Service1(...);
        this.service2 = new Service2(...);
        this.service3 = new Service3(...);
    }
    
    // ========== GETTERS ==========
    
    public Service1 getService1() { return service1; }
    public Service2 getService2() { return service2; }
    public Service3 getService3() { return service3; }
}
```

### **Bước 3: Thêm vào CoreContext**

```java
// 1. Thêm field trong CoreContext
private final [Domain]Context [domain]Context;

// 2. Khởi tạo trong constructor CoreContext
this.[domain]Context = new [Domain]Context(plugin, ...);

// 3. Thêm getter
public [Domain]Context get[Domain]Context() { return [domain]Context; }

// 4. Đăng ký lifecycle trong registerAllModules()
private void register[Domain]System() {
    lifecycleManager.registerOnEnable(() -> {
        plugin.getLogger().info("[PHASE X] Đang khởi tạo [Domain] System...");
        
        // Khởi tạo services, listeners, etc.
        
        lifecycleManager.enableModule("[Domain]System");
        plugin.getLogger().info("[PHASE X] ✓ [Domain] System đã sẵn sàng!");
    });
    
    lifecycleManager.registerOnDisable(() -> {
        plugin.getLogger().info("[PHASE X] Đang tắt [Domain] System...");
        
        // Cleanup
        
        lifecycleManager.disableModule("[Domain]System");
        plugin.getLogger().info("[PHASE X] ✓ [Domain] System đã tắt!");
    });
}
```

---

## 📝 QUY TRÌNH THÊM SERVICE VÀO CONTEXT CÓ SẴN

### **Bước 1: Xác định Context đúng**

- **Player** → `PlayerContext`
- **Combat** → `CombatContext`
- **Entity** → `EntityContext`
- **UI** → `UIContext`
- **Cultivation** → `CultivationContext`

### **Bước 2: Tạo Service class**

```java
package hcontrol.plugin.service;

/**
 * [FEATURE] SERVICE
 * Logic của [feature], không phải command/listener
 */
public class [Feature]Service {
    
    // Dependencies (inject qua constructor)
    private final Dependency1 dep1;
    private final Dependency2 dep2;
    
    public [Feature]Service(Dependency1 dep1, Dependency2 dep2) {
        this.dep1 = dep1;
        this.dep2 = dep2;
    }
    
    // Business logic methods
    public void handleSomething(...) {
        // Logic ở đây
    }
}
```

### **Bước 3: Thêm vào Context**

```java
// 1. Thêm field trong Context
private final [Feature]Service [feature]Service;

// 2. Khởi tạo trong constructor
this.[feature]Service = new [Feature]Service(dep1, dep2);

// 3. Thêm getter
public [Feature]Service get[Feature]Service() { return [feature]Service; }
```

---

## 🔮 DỰ ĐOÁN CONTEXT MỚI (PHASE 5-15)

### **ItemContext (PHASE 8)**

**Khi nào tạo:** Khi có ItemService, EquipmentService, ArtifactService

**Services sẽ có:**
- `ItemService` - Quản lý item inventory
- `EquipmentService` - Equip/unequip logic
- `ArtifactService` - Artifact system

**Dependencies:**
- PlayerManager (để lấy profile)
- Có thể cần CombatContext (để apply stat từ equipment)

### **WorldContext (PHASE 9)**

**Khi nào tạo:** Khi có DimensionService, WorldRuleService

**Services sẽ có:**
- `DimensionService` - Quản lý dimension/teleport
- `WorldRuleService` - World rules (realm cap, qi density, death penalty)
- `RegionService` - Region/location rules

**Dependencies:**
- PlayerManager (để check player location)

### **EconomyContext (PHASE 10)**

**Khi nào tạo:** Khi có TradeService, AuctionService, SectService

**Services sẽ có:**
- `TradeService` - Player-to-player trade
- `AuctionService` - Auction house
- `SectService` - Sect/guild system
- `ReputationService` - Reputation system

**Dependencies:**
- PlayerManager
- ItemContext (nếu đã có)

### **SharedContext (PHASE 12) - Optional**

**Khi nào tạo:** Khi có 3+ services chung không thuộc domain cụ thể

**Services sẽ có:**
- `ConfigService` - Config management chung
- `EventBusService` - Event bus cho decoupling (PHASE 4.5+)
- `ValidationService` - Validation helpers chung

**Lưu ý:** Chỉ tạo nếu thực sự cần. Nếu chỉ có 1-2 service chung → dùng singleton static.

---

## ⚠️ NGUYÊN TẮC QUAN TRỌNG

### **1. Dependency Injection**

Luôn inject dependencies qua constructor:

```java
// ✅ ĐÚNG
public MyService(Dependency1 dep1, Dependency2 dep2) {
    this.dep1 = dep1;
    this.dep2 = dep2;
}

// ❌ SAI
public MyService() {
    this.dep1 = CoreContext.getInstance().getSomething(); // Không inject
}
```

### **2. Không Circular Dependency**

```
PlayerContext ──depends──> CombatContext  ✅ OK
CombatContext ──depends──> PlayerContext  ❌ Circular!
```

Giải pháp:
- Dùng interface/callback
- Tách service chung ra
- Inject qua setter (sau khi khởi tạo)

### **3. Lifecycle Management**

Mọi Context phải đăng ký lifecycle:

```java
lifecycleManager.registerOnEnable(() -> {
    // Khởi tạo
});

lifecycleManager.registerOnDisable(() -> {
    // Cleanup
});
```

### **4. Backward Compatibility**

Nếu thêm getter mới trong CoreContext, giữ lại @Deprecated getter cũ:

```java
// Getter mới (qua SubContext)
public PlayerContext getPlayerContext() { return playerContext; }

// Getter cũ (deprecated, sẽ remove sau)
@Deprecated
public PlayerManager getPlayerManager() { 
    return playerContext.getPlayerManager(); 
}
```

---

## 📚 VÍ DỤ CỤ THỂ

### **Ví dụ: Thêm SkillService vào CombatContext (PHASE 6)**

```java
// 1. Tạo SkillService
public class SkillService {
    private final CombatService combatService;
    
    public SkillService(CombatService combatService) {
        this.combatService = combatService;
    }
    
    public void castSkill(LivingActor caster, LivingActor target, Skill skill) {
        // Không deal damage trực tiếp
        // Tạo CombatRequest và gọi CombatService
        CombatRequest request = new CombatRequest(caster, target, skill.getDamageModifier());
        combatService.handleCombatRequest(request);
    }
}

// 2. Thêm vào CombatContext
public class CombatContext {
    private final SkillService skillService;
    
    public CombatContext(..., CombatService combatService) {
        // ...
        this.skillService = new SkillService(combatService);
    }
    
    public SkillService getSkillService() { return skillService; }
}
```

---

## ✅ CHECKLIST

Khi thêm Context/Service mới, đảm bảo:

- [ ] Đã xác định đúng domain
- [ ] Service có constructor injection
- [ ] Không có circular dependency
- [ ] Đã đăng ký lifecycle (onEnable/onDisable)
- [ ] Đã thêm getter trong Context
- [ ] Đã cập nhật CoreContext
- [ ] Đã cập nhật documentation
- [ ] Code tuân thủ nguyên tắc (logic trong service, không trong listener/command)

---

**Kết luận:** Architecture hiện tại rất linh hoạt, dễ mở rộng. Chỉ cần follow quy trình này là có thể thêm feature mới mà không phá vỡ code cũ.
