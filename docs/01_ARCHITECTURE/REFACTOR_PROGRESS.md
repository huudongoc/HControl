🔧 HCONTROL RPG — REFACTOR PROGRESS LOG

File này ghi lại các refactor ảnh hưởng kiến trúc đã hoàn thành.
Mỗi milestone DONE là KHÓA, không refactor lại.

✅ MILESTONE 1 — LivingActor Unification

Status: DONE
Date: 2026-01-08

Trước refactor

PlayerProfile và EntityProfile không có interface chung

Combat logic bị chia PvP / PvE

Duplicate damage calculation

Refactor

Tạo interface LivingActor

PlayerProfile và EntityProfile implement LivingActor

CombatService dùng handleCombat(LivingActor, LivingActor)

Luật được khóa

Mọi thực thể tham gia combat phải là LivingActor

CombatService là nơi DUY NHẤT tính damage

✅ MILESTONE 2 — TribulationContext State Machine

Status: DONE
Date: 2026-01-08

Trước refactor

Logic thiên kiếp rải rác

Không có state machine rõ ràng

Dễ race condition

Refactor

Tạo TribulationContext làm single source of truth

State machine: PREPARE → WAVE → QUESTION → RESULT

UI & Listener chỉ đọc / submit input

Luật được khóa

Tribulation state chỉ tồn tại trong TribulationContext

UI không được mutate tribulation state

✅ MILESTONE 3 — SubContext Architecture

Status: DONE
Date: 2026-01-08

Trước refactor

CoreContext trở thành God Object (30+ fields)

Khó maintain, khó reload

Refactor

Tách thành:

PlayerContext

CombatContext

EntityContext

UIContext

CultivationContext

Luật được khóa

CoreContext chỉ giữ SubContext

Không thêm service trực tiếp vào CoreContext

✅ MILESTONE 4 — Unified Combat Pipeline

Status: DONE
Date: 2026-01-08

Trước refactor

PvP, PvE, MobAttack dùng logic riêng

Khó mở rộng skill / buff

Refactor

Hợp nhất combat pipeline

Damage formula thống nhất

Knockback / effect dùng chung

Luật được khóa

Không có combat logic ngoài CombatService

Skill / Class chỉ modify context, không apply damage

✅ MILESTONE 5 — Remove CultivatorProfile

Status: DONE
Date: 2026-01-08

Trước refactor

CultivatorProfile duplicate PlayerProfile

Gây nhầm lẫn nguồn state

Refactor

Xóa CultivatorProfile

PlayerProfile + EntityProfile là nguồn state duy nhất

Luật được khóa

Không tạo profile trung gian cho combat

State luôn nằm trong Profile