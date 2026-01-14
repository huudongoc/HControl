# HControl RPG — MASTER TASK LIST

> Người viết: dev lâu năm, IT già, code C#/Java/Python  
> Ghi chú code: tiếng Việt không dấu  
> Phong cách: module hóa, ghép được với nhau, không rời rạc  
> Mục tiêu: build 1 **thế giới RPG / Tu Tiên sống lâu**, không đập core

> ❗ Đây KHÔNG phải todo list ngắn hạn  
> ❗ Đây là **xương sống kiến trúc cho cả đời plugin**

==================================================
MỤC TIÊU TỔNG
==================================================

- Plugin RPG lâu dài, cân server đông
- Mở rộng không giới hạn bằng module
- Không rewrite core
- Không hard-code
- Không lệ thuộc UI / Effect
- 90% thay đổi sau này = data / config

==================================================
PHASE 0 — TRUCCO (KHÔNG BAO GIỜ LÀM LẠI)
==================================================

## Core Architecture (DONE)

- [x] CoreContext (DI container, singleton)
- [x] LifecycleManager (enable / disable / reload an toàn)
- [x] SubContext architecture:
  - PlayerContext
  - CombatContext
  - EntityContext
  - UIContext
  - CultivationContext
- [x] Dependency Injection qua constructor
- [x] Không logic trong Main.java

📌 **Luật bất biến:**
- Core chỉ wiring + lifecycle
- Không business logic trong core

==================================================
PHASE 1 — ACTOR & PLAYER SYSTEM (LINH HỒN GAME)
==================================================

## Actor System (DONE)

- [x] LivingActor interface
- [x] PlayerProfile implements LivingActor
- [x] EntityProfile implements LivingActor
- [x] Không phân biệt PvP / PvE ở tầng combat

📌 Actor = Player / Mob / Boss / NPC tu sĩ

## Player Profile (DONE)

- [x] PlayerProfile = nguồn sự thật (state)
- [x] Không UI nào modify profile
- [x] PlayerManager chỉ cache RAM
- [x] PlayerStorage chỉ I/O

==================================================
PHASE 2 — STATE SYSTEM (CỐT LÕI THẬT)
==================================================

## State Rules (DONE – implicit)

- [x] HP / Qi / Realm / Level nằm trong Profile
- [x] UI chỉ đọc state
- [x] Effect chỉ đọc state
- [ ] (PHASE 5+) Snapshot state cho Event Bus

📌 **Không có state rải rác**

==================================================
PHASE 3 — COMBAT SYSTEM (THAY DAMAGE VANILLA)
==================================================

## Combat Core (DONE)

- [x] Unified combat:
  - handleCombat(LivingActor, LivingActor)
- [x] Realm-based damage (tu tiên)
- [x] Không crit RPG
- [x] Không scale damage theo level
- [x] Entity / Player chung pipeline

📌 CombatService hiện có Bukkit dependency  
→ **CHẤP NHẬN ở PHASE 3–4**

==================================================
PHASE 4 — RESOURCE / CULTIVATION
==================================================

## Mana / LingQi / Tu Vi (DONE cơ bản)

- [x] LingQi trong PlayerProfile
- [x] Level là độ ổn định, không phải sức mạnh
- [x] Tu vi dùng cho level & đột phá

## Tribulation System (DONE)

- [x] TribulationContext (state machine)
- [x] Multi-wave tribulation
- [x] Question / Result tracking
- [x] Không logic rải rác

==================================================
PHASE 4.5 — REQUEST & ADAPTER (CHUẨN BỊ EVENT BUS)
==================================================

⚠️ **PHASE CHUYỂN TIẾP – KHÔNG THAY BEHAVIOR**

- [ ] Introduce Request objects:
  - BreakthroughRequest
  - CombatRequest
- [ ] Service.handle(Request) thay vì gọi trực tiếp logic
- [ ] Comment rõ: service sẽ nhận từ EventBus ở PHASE 5

==================================================
PHASE 5 — CLASS / JOB SYSTEM
==================================================

## Class System (CHƯA LÀM)

- Class = modifier + hook
- Không kế thừa PlayerProfile
- Không chứa combat logic

```java
interface PlayerClass {
    void beforeCombat(CombatContext ctx);
    void afterCombat(CombatContext ctx);
}
📌 Class chỉ can thiệp context, không deal damage

==================================================
PHASE 6 — SKILL SYSTEM (DATA-DRIVEN)
Skill = Request

Không damage trực tiếp

Không hard-code effect

Flow:

rust
Sao chép mã
SkillCast
 → SkillRequest
 → CombatService
 → Result
 → Effect/UI
==================================================
PHASE 7 — AI & MOB RPG
Mob = LivingActor

AI = Brain module

Không special-case Player

==================================================
PHASE 8 — ITEM / EQUIPMENT / ARTIFACT
Artifact ≠ ItemStack

ItemStack chỉ là skin

Effect / stat lấy từ data

==================================================
PHASE 9 — WORLD / DIMENSION / CONTENT
World = luật

DimensionRule:

realm cap

qi density

death penalty

Teleport ≠ logic

==================================================
PHASE 10 — ECONOMY & SOCIAL
Sect

Trade

Auction

Reputation

==================================================
PHASE 11 — UI & UX (PRESENTATION)
ActionBar

BossBar

Custom GUI

Skill bar

Map / Realm UI

📌 UI = hiển thị, không logic

==================================================
PHASE 12 — CONFIG & DATA
YAML / JSON

Hot reload

Per-module config

Migration system

==================================================
PHASE 13 — PERFORMANCE & SCALE
Tick throttle

Async calculation

Chunk-aware logic

Memory cleanup

==================================================
PHASE 14 — ADMIN & DEBUG
Debug command

Inspect player state

Force module reload

Balance tool

==================================================
PHASE 15 — ENDGAME (CẢ ĐỜI)
World boss

Secret realm

Ascension

Infinite scaling (soft cap)

==================================================
NGUYÊN TẮC VÀNG (BẤT BIẾN)
UI không xử lý logic

Listener không tính toán

Service là nơi duy nhất có nghiệp vụ

Model chỉ data + derived stat

Không bypass architecture vì “cho nhanh”

Feature mới phải gắn được vào Event Bus

Core không đập lại

==================================================
DONE = SERVER SỐNG
yaml
Sao chép mã

---

# ✅ KẾT LUẬN THẲNG

- File này **KHỚP 100% với code hiện tại**
- **Không mâu thuẫn** REFACTOR_PROGRESS.md
- Đã:
  - Ghi nhận cái m đã DONE
  - Không ép m làm Event Bus sớm
  - Mở đường rõ ràng cho PHASE 5+

👉 **TỪ GIỜ TRỞ ĐI**:
- Làm feature → đối chiếu file này
- Lệch 1 dòng → coi như nợ kiến trúc

---

