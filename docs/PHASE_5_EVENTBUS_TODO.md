# PHASE 5 — EVENT BUS TODO

> **KHÔNG TẠO TRONG PHASE 5**  
> **CHỈ GHI TODO CHO PHASE 6+**

==================================================
## EventBus Architecture (Phase 6+)
==================================================

### Purpose
Decouple skill/buff/combat logic from direct service calls

### Design
```java
// TODO PHASE 6+: Tạo EventBus
public class EventBus {
    private final Map<Class<?>, List<EventHandler<?>>> handlers;
    
    public <T> void subscribe(Class<T> eventType, EventHandler<T> handler);
    public <T> void publish(T event);
}
```

### Events to implement
```java
// TODO PHASE 6: Skill events
SkillCastEvent(player, skill)
SkillHitEvent(player, target, skill)
SkillCooldownEvent(player, skill)

// TODO PHASE 7: Buff events
BuffApplyEvent(entity, buff)
BuffRemoveEvent(entity, buff)
BuffTickEvent(entity, buff)

// TODO PHASE 8: Combat events
DamageEvent(attacker, victim, damage, context)
DeathEvent(victim, killer, context)
```

### Integration Points
```java
// TODO PHASE 6: PlayerSkillService sử dụng EventBus
public class PlayerSkillService {
    private final EventBus eventBus;
    
    public void castSkill(Player player, String skillId) {
        // ...
        eventBus.publish(new SkillCastEvent(player, skill));
    }
}

// TODO PHASE 6: IdentityRuleService subscribe vào events
eventBus.subscribe(SkillCastEvent.class, event -> {
    PlayerIdentity identity = event.getPlayer().getProfile().getIdentity();
    if (!identityRuleService.canUseSkill(identity, event.getSkill().getId())) {
        event.cancel();
    }
});
```

==================================================
## Why NOT in Phase 5?
==================================================

1. **Không có consumer** - Phase 5 chỉ có data + rules, không có skill/buff
2. **Over-engineering** - EventBus cần khi có nhiều listeners, Phase 5 chỉ có 1-2 services
3. **Tăng complexity** - EventBus thêm indirection, khó debug
4. **YAGNI** - You Ain't Gonna Need It (yet)

👉 **PHASE 6 mới cần EventBus** khi có:
- Multiple skill handlers
- Buff system
- Combat modifiers
- Achievement tracking
- Quest system

==================================================
## Alternative: Direct Calls (Phase 5-6)
==================================================

```java
// Phase 6: Direct call trong PlayerSkillService
public boolean castSkill(Player player, String skillId) {
    PlayerIdentity identity = profile.getIdentity();
    
    // Check identity rule
    if (!identityRuleService.canUseSkill(identity, skillId)) {
        player.sendMessage("§cKhông thể dùng skill này!");
        return false;
    }
    
    // Execute skill
    // ...
}
```

**Pros:**
- Simple, dễ hiểu
- Dễ debug
- Ít overhead

**Cons:**
- Tight coupling
- Khó extend sau này

👉 **Decision**: Phase 6 dùng direct calls, Phase 7+ refactor sang EventBus nếu cần

==================================================
## LOCK PHASE 5
==================================================

✅ PlayerIdentity - DONE  
✅ IdentityRuleService - DONE  
✅ CombatContext hook - DONE  
✅ Data placeholders - DONE  
🔒 EventBus - SKIP (Phase 6+)  

**PHASE 5 = KHÓA**
