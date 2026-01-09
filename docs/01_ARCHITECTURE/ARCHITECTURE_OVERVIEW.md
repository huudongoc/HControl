🧱 NỘI DUNG ĐÚNG CỦA ARCHITECTURE_OVERVIEW.md
❌ KHÔNG NÊN CÓ

Không task chi tiết

Không công thức damage dài

Không ví dụ code 200 dòng

Không lịch trình phase

✅ NÊN CÓ

Sơ đồ layer

Trách nhiệm từng layer

Ranh giới bất biến

Data flow

Những điều cấm

🧩 BẢN CHUẨN CHO PROJECT CỦA M (RÚT GỌN + ĐỦ)

👉 File này m có thể copy gần nguyên xi.

🧠 HCONTROL RPG — ARCHITECTURE OVERVIEW

Đây không phải plugin, mà là Cultivation Game Engine chạy trên Minecraft.

🧱 ENGINE LAYERS (FINAL)
┌──────────────────────────────────────────────┐
│ PRESENTATION LAYER                           │
│ (UI / Menu / Effect / Animation / Model)     │
└──────────────────────────────────────────────┘
                 ▲
                 │ read-only
┌──────────────────────────────────────────────┐
│ APPLICATION / ORCHESTRATION LAYER             │
│ (Command / Listener / Request / Event Bus)   │
└──────────────────────────────────────────────┘
                 ▲
                 │ request
┌──────────────────────────────────────────────┐
│ DOMAIN CORE                                  │
│ (Rules / Logic / State Machine)              │
└──────────────────────────────────────────────┘
                 ▲
                 │ state
┌──────────────────────────────────────────────┐
│ STATE & MODEL                                 │
│ (PlayerProfile / EntityProfile / BuffState)  │
└──────────────────────────────────────────────┘
                 ▲
                 │ persist
┌──────────────────────────────────────────────┐
│ DATA & CONFIG                                 │
│ (YAML / JSON / Resource Pack)                 │
└──────────────────────────────────────────────┘

🔹 DOMAIN CORE (LUẬT TU TIÊN)

Chịu trách nhiệm:

Cultivation & Realm

Combat rules

Breakthrough & Tribulation

Skill execution rules

Buff/Debuff logic

Item/Artifact logic

Dimension rules

KHÔNG BAO GIỜ:

❌ Gọi Bukkit API

❌ Spawn particle / sound

❌ Biết model / texture

🔹 ACTOR SYSTEM

Player

NPC Cultivator

Boss / Mob

Linh thú

👉 Tất cả đều là LivingActor

🔹 STATE SYSTEM (SINGLE SOURCE OF TRUTH)

HP / Qi

Realm / Level

BuffState

CooldownState

Luật:

UI chỉ đọc

Effect chỉ đọc

Không state nào nằm trong ItemStack

🔹 EVENT BUS / REQUEST PIPELINE
Input (Player / AI)
  → Request
    → Domain Service
      → Result
        → Event
          → Presentation


👉 Không gọi chéo Service

🔹 PRESENTATION LAYER (CẢM GIÁC)

Menu GUI

HUD / ActionBar

Particle / Sound

Animation

Item model

World transition

👉 Có thể thay toàn bộ không ảnh hưởng gameplay

🔹 DATA-DRIVEN SYSTEM

Realm definition

Skill definition

Buff definition

Artifact definition

Dimension rule

👉 90% chỉnh YAML / JSON, không sửa code

⛔ KIẾN TRÚC BẤT BIẾN (CẤM PHÁ)

UI không xử lý logic

Skill không apply damage trực tiếp

Effect không ảnh hưởng gameplay

ItemStack không là nguồn sự thật

Không bypass CombatService

Không hard-code balance

☯️ TRIẾT LÝ THIẾT KẾ

Realm > Level > Stat

Level = ổn định, không phải sức mạnh

Vượt cảnh giới = sống sót, không phải thắng

Nội dung mở rộng không đập core

🎯 MỤC TIÊU CUỐI

Một engine tu tiên có thể mở rộng vô hạn,
thêm nội dung mà không cần refactor core.

🔗 MỐI QUAN HỆ VỚI CÁC FILE KHÁC
File	Vai trò
MASTER_TASK_LIST.md	Hiến pháp + roadmap
ARCHITECTURE_OVERVIEW.md	Bản đồ tổng thể
REFACTOR_PROGRESS.md	Nhật ký tiến hóa
copilot-instructions.md	Luật cho AI