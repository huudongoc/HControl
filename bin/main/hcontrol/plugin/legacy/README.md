# 📦 LEGACY PACKAGE

## ⚠️ CẢNH BÁO

**Package này chứa code LEGACY - KHÔNG được sử dụng trong runtime logic.**

## 🎯 MỤC ĐÍCH

Package `legacy/` được tạo để:
- **Đóng băng** code cũ không còn được dùng
- **Lưu trữ** code để data migration (nếu cần)
- **Tránh** Cursor/AI tự động sửa code trong package này

## 📋 QUY TẮC

1. ❌ **KHÔNG** import code từ `legacy/` vào runtime logic
2. ❌ **KHÔNG** sửa code trong `legacy/` trừ khi:
   - Data migration từ bản cũ
   - Import data từ file cũ
3. ✅ **ĐƯỢC** xóa toàn bộ package `legacy/` nếu không cần migration

## 📁 CẤU TRÚC

```
legacy/
 └─ skill/
     └─ CustomSkill.java  # Legacy custom skill model (đã thay bằng SkillTemplateRegistry)
```

## 🔄 MIGRATION

Nếu cần migrate data từ legacy code:
1. Tạo script migration riêng (không trong runtime)
2. Chạy một lần để convert data
3. Xóa package `legacy/` sau khi migration xong

---

**Lưu ý:** Cursor sẽ tự tránh code trong package `legacy/` khi suggest changes.
