# 📋 BÁO CÁO CODE KHÔNG ĐƯỢC SỬ DỤNG

> **Ngày tạo:** 2025-01-XX  
> **Cập nhật:** 2025-01-XX - Đã xóa các method/field không được sử dụng  
> **Mục đích:** Tìm và liệt kê tất cả logic code không được sử dụng trong project

---

## ❌ METHOD KHÔNG ĐƯỢC SỬ DỤNG

### 1. **CombatService.java**

#### `dealDamage(LivingEntity target, double damage)` - Line 502
- **Tình trạng:** ❌ Không được gọi ở đâu cả
- **Lý do:** Logic damage đã được xử lý trực tiếp trong `handleCombat()` qua `defender.setCurrentHP()`
- **Quyết định:** ✅ **XÓA** - Logic đã được thay thế bằng unified combat system

#### `getTechniqueRank(double modifier)` - Line 550
- **Tình trạng:** ❌ Không được gọi ở đâu cả
- **Mục đích ban đầu:** Trả về tên công pháp dựa trên modifier (Cảm Thuật, Thiên Cấp, Địa Cấp, Linh Cấp, Phàm Pháp)
- **Quyết định:** ⚠️ **GIỮ LẠI** - Có thể dùng sau này cho technique system (TODO: Technique modifier)

#### `getPlayerProfile(Player player)` - Line 609
- **Tình trạng:** ❌ Không được gọi ở đâu cả
- **Lý do:** Code đã dùng trực tiếp `playerManager.get(player.getUniqueId())` thay vì wrapper method
- **Quyết định:** ✅ **XÓA** - Wrapper method không cần thiết

#### `nameplateService` field - Line 38
- **Tình trạng:** ❌ Field được khai báo nhưng không được sử dụng
- **Lý do:** Logic update nameplate đã được comment out (line 407-428)
- **Quyết định:** ✅ **XÓA** - Field và setter method không được sử dụng

---

## ⚠️ CODE ĐÃ ĐƯỢC ĐÁNH DẤU DEPRECATED

### 1. **CoreContext.java** - Các deprecated getters
- **Vị trí:** Line 826-907
- **Tình trạng:** ⚠️ Đã đánh dấu `@Deprecated` nhưng vẫn được sử dụng ở một số nơi
- **Mục đích:** Backward compatibility - sẽ xóa trong tương lai
- **Quyết định:** ⚠️ **GIỮ LẠI TẠM THỜI** - Cần migrate tất cả code sang dùng Context pattern

### 2. **CustomSkill.java** - Legacy package
- **Vị trí:** `src/main/java/hcontrol/plugin/legacy/skill/CustomSkill.java`
- **Tình trạng:** ✅ Đã đánh dấu `@Deprecated` và di chuyển vào `legacy/` package
- **Quyết định:** ✅ **GIỮ LẠI** - Chỉ dùng cho data migration

### 3. **SpiritualRoot.randomSpiritualRoot()** - Line 64
- **Tình trạng:** ⚠️ Đã đánh dấu `@Deprecated`
- **Lý do:** Logic đã được chuyển sang `SpiritualRootService.randomSpiritualRoot()`
- **Quyết định:** ⚠️ **GIỮ LẠI** - Có thể còn được dùng ở đâu đó

### 4. **RootQuality.randomQuality()** - Line 37
- **Tình trạng:** ⚠️ Đã đánh dấu `@Deprecated`
- **Lý do:** Logic đã được chuyển sang `SpiritualRootService.randomRootQuality()`
- **Quyết định:** ⚠️ **GIỮ LẠI** - Có thể còn được dùng ở đâu đó

### 5. **EntityNameplateService.enableNameplate()** - Line 293
- **Tình trạng:** ⚠️ Đã đánh dấu `@Deprecated`
- **Quyết định:** ⚠️ **GIỮ LẠI** - Chỉ dùng cho manual init

---

## 🔍 CODE COMMENTED OUT (KHÔNG ĐƯỢC SỬ DỤNG)

### 1. **CombatService.java**

#### Nameplate update logic - Line 407-428
```java
// Update nameplate cho Entity (mob/boss) - KHONG update cho Player de tranh flash
//         var entityNameplateService = CoreContext.getInstance().getUIContext().getEntityNameplateService();
//         if (entityNameplateService != null) {
//             ...
//         }
```
- **Tình trạng:** ❌ Đã bị comment out
- **Lý do:** Có thể gây lag hoặc flash khi update quá nhiều
- **Quyết định:** ⚠️ **GIỮ LẠI** - Có thể enable lại sau khi optimize

#### ActionBar feedback - Line 367-377
```java
// // ActionBar feedback cho attacker (neu la player)
// if (attackerEntity instanceof Player attackerPlayer) {
//     attackerPlayer.sendActionBar(String.format("§e⚔ %.1f", damage));
// }
```
- **Tình trạng:** ❌ Đã bị comment out
- **Lý do:** Không muốn hiển thị action bar trong combat
- **Quyết định:** ✅ **XÓA** - Đã có floating damage text thay thế

#### Floating damage text trong tribulation - Line 262-266
```java
// Floating damage text (optional - co the comment neu qua nhieu)
// var effectService = ...
// if (effectService != null) {
//     effectService.spawnFloatingDamage(...);
// }
```
- **Tình trạng:** ❌ Đã bị comment out
- **Lý do:** Có thể quá nhiều text khi sét đánh liên tục
- **Quyết định:** ⚠️ **GIỮ LẠI** - Có thể enable lại nếu cần

---

## 📊 TỔNG KẾT

### ✅ NÊN XÓA NGAY:
1. `CombatService.dealDamage()` - Logic đã được thay thế
2. `CombatService.getPlayerProfile()` - Wrapper không cần thiết
3. `CombatService.nameplateService` field - Không được sử dụng
4. ActionBar feedback code (commented) - Đã có floating text

### ⚠️ GIỮ LẠI (CÓ THỂ DÙNG SAU):
1. `CombatService.getTechniqueRank()` - Có thể dùng cho technique system
2. Nameplate update logic (commented) - Có thể enable lại sau khi optimize
3. Floating damage trong tribulation (commented) - Có thể enable lại

### ⚠️ DEPRECATED (CẦN MIGRATE):
1. CoreContext deprecated getters - Cần migrate sang Context pattern
2. SpiritualRoot/RootQuality enum methods - Cần migrate sang Service
3. EntityNameplateService.enableNameplate() - Chỉ dùng cho manual init

---

## 🎯 KHUYẾN NGHỊ

### 1. **Xóa code không dùng:** ✅ **ĐÃ HOÀN THÀNH**
- ✅ Xóa `dealDamage()` method - Line 480-483
- ✅ Xóa `getPlayerProfile()` method - Line 609-611
- ✅ Xóa `nameplateService` field và `setNameplateService()` method - Line 38, 118-120
- ✅ Xóa ActionBar feedback code đã comment - Line 387-397
- ✅ Xóa import `NameplateService` - Line 24
- ✅ Xóa dòng gọi `setNameplateService()` trong `CoreContext.java` - Line 392

### 2. **Migrate deprecated code:**
- Tìm tất cả nơi dùng deprecated getters trong CoreContext
- Migrate sang dùng Context pattern (PlayerContext, CombatContext, etc.)
- Sau đó xóa deprecated methods

### 3. **Review commented code:**
- Quyết định enable lại hoặc xóa hẳn
- Nếu giữ lại, thêm TODO với lý do rõ ràng

---

## 📝 GHI CHÚ

- File này sẽ được cập nhật khi tìm thấy thêm unused code
- Các method/field được đánh dấu `@Deprecated` sẽ được xóa sau khi migrate xong
- Code trong `legacy/` package được giữ lại cho data migration
