HControl RPG – Core Architecture Lock

Mục tiêu:
Giữ engine ổn định lâu dài, mở rộng được đến endgame,
tránh refactor lớn, tránh hard-code, tránh “sửa cho chạy”.

☯️ TRIẾT LÝ CỐT LÕI

Engine > Feature

Data-driven > Hard-code

Modifier > Override

Service quyết định, UI chỉ hiển thị

Một nguồn sự thật duy nhất (Single Source of Truth)

1️⃣ LAYER QUY ƯỚC (KHÔNG ĐƯỢC PHÁ)
Command
  ↓
Service
  ↓
Core Logic
  ↓
Model / Context

❌ CẤM TUYỆT ĐỐI

Command xử lý logic game

Listener chứa combat / stat logic

UI tự tính damage / stat

Model gọi Bukkit API

2️⃣ PROFILE & ACTOR RULES
PlayerProfile

👉 Đại diện con người Minecraft

ĐƯỢC:

UUID

Name

Session

UI state

Equipment slots

ClassProfile (nullable)

❌ KHÔNG ĐƯỢC:

Tự tính damage

Tự xử lý combat

Chứa logic game nặng

EntityProfile

👉 Mob / Boss / NPC

ĐƯỢC:

Realm

Level

Base stats

❌ KHÔNG ĐƯỢC:

Tu luyện

Đột phá

Có skill player-only

LivingActor (interface)

👉 Cầu nối duy nhất cho combat

CombatService CHỈ làm việc với LivingActor

Không phân biệt Player / Entity trong combat logic

3️⃣ COMBAT RULES (RẤT QUAN TRỌNG)
CombatService

👉 NƠI DUY NHẤT được phép:

Tính damage

Áp dụng mitigation

Áp dụng suppression

Gây knockback

Apply HP

❌ KHÔNG AI KHÁC ĐƯỢC TÍNH DAMAGE

Damage Flow (CỐ ĐỊNH)
Base Damage (realm + level)
 → Item modifiers
 → Skill modifiers
 → Class modifiers
 → Final damage


❌ Không được:

Đổi thứ tự

Bỏ qua modifier

Tính damage ở nơi khác

4️⃣ MODIFIER SYSTEM RULES
Modifier là GÌ?

👉 Modifier CHỈ sửa số, không quyết định logic.

double modify(actor, context, baseValue)

ĐƯỢC

Stack nhiều modifier

Data-driven

Null-safe

❌ KHÔNG ĐƯỢC

Spawn entity

Play sound

Check permission

Check Bukkit Player

5️⃣ CLASS SYSTEM RULES (PHASE 5)

Class = modifier layer

Class KHÔNG sở hữu skill

Class KHÔNG gọi combat

Class KHÔNG giữ state runtime

👉 Class chỉ trả về List<ClassModifier>

6️⃣ ITEM SYSTEM RULES (PHASE 8)

Item stat KHÔNG cộng thẳng vào PlayerProfile

Item effect CHỈ áp qua ItemService

ItemService KHÔNG phụ thuộc UI

CombatService CHỈ đọc stat tổng

❌ Không có:

item.damage +=

item tự gọi combat

7️⃣ CONTEXT & LIFECYCLE RULES
CoreContext

Là composition root

Không xử lý logic

Không chứa gameplay

SubContext

Quản lý 1 domain

Có lifecycle rõ ràng

Command

KHÔNG inject service sống sớm

DÙNG lazy-load từ CoreContext

8️⃣ UI RULES

UI KHÔNG giữ state game

UI KHÔNG tính stat

UI CHỈ đọc data

UI update phải:

throttle

cache prefix

không spam

9️⃣ NULL & FAIL-SAFE RULES

System chưa init → báo lỗi rõ

Không silent fail

Không crash server

Không assume thứ tự lifecycle

🔒 10️⃣ KHÓA KIẾN TRÚC (ABSOLUTE RULES)

❌ KHÔNG:

Hard-code damage

Copy-paste logic combat

Viết “cho chạy tạm”

Override thay vì modifier

✅ PHẢI:

Tạo layer mới nếu cần

Viết interface trước

Có lý do khi phá rule

🧠 11️⃣ KHI MUỐN PHÁ LUẬT?

👉 BẮT BUỘC:

Tạo file ADR-xxx.md

Ghi:

Vấn đề

Lý do phá

Hệ quả

Review lại toàn flow

☯️ CÂU CHỐT CUỐI (DÁN TRÊN TƯỜNG)

Combat chỉ có 1 nguồn sự thật
Modifier là con đường duy nhất để mở rộng
Engine không phục vụ feature – feature phải phục vụ engine

✅ TRẠNG THÁI

Engine đã khóa kiến trúc

Sẵn sàng scale:

Class

Item

Skill

Arena

Endgame

📌 Từ giờ trở đi:

Cursor làm sai → chỉ thẳng file này

Người khác PR → check theo file này

M quay lại sau 6 tháng → đọc là nhớ hết