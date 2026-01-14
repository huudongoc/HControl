# Hướng Dẫn Git - Pull và Commit

## 📥 Git Pull - Lấy code mới nhất từ server

### Cú pháp cơ bản:
```bash
git pull
```
Lệnh này sẽ:
- Lấy tất cả thay đổi mới nhất từ branch hiện tại trên GitHub
- Tự động merge vào code local của bạn

### Pull từ branch cụ thể:
```bash
git pull origin main
```
- `origin`: Tên remote repository (thường là GitHub)
- `main`: Tên branch bạn muốn pull

### Pull với rebase (giữ lịch sử commit sạch hơn):
```bash
git pull --rebase
```

---

## 💾 Git Commit - Lưu thay đổi của bạn

### Quy trình commit cơ bản:

#### 1. Kiểm tra trạng thái thay đổi:
```bash
git status
```
Xem những file nào đã thay đổi, thêm mới, hoặc xóa

#### 2. Xem chi tiết thay đổi:
```bash
git diff
```
Xem nội dung thay đổi cụ thể trong các file

#### 3. Thêm file vào staging area:
```bash
# Thêm tất cả file đã thay đổi
git add .

# Hoặc thêm từng file cụ thể
git add ten_file.java
git add src/main/java/hcontrol/plugin/Main.java
```

#### 4. Commit với message:
```bash
git commit -m "Mô tả ngắn gọn về thay đổi của bạn"
```

**Ví dụ:**
```bash
git commit -m "Thêm tính năng xử lý entity mới"
git commit -m "Sửa lỗi crash khi player logout"
git commit -m "Cập nhật UI cho boss battle"
```

#### 5. Push code lên GitHub:
```bash
git push
```

Hoặc chỉ định branch:
```bash
git push origin main
```

---

## 🔄 Quy trình làm việc đầy đủ

### Khi bắt đầu làm việc mỗi ngày:
```bash
# 1. Pull code mới nhất từ server
git pull

# 2. Kiểm tra trạng thái
git status
```

### Khi hoàn thành một tính năng/sửa lỗi:
```bash
# 1. Xem những gì đã thay đổi
git status
git diff

# 2. Thêm các file cần commit
git add .

# 3. Commit với message rõ ràng
git commit -m "Mô tả thay đổi của bạn"

# 4. Push lên GitHub
git push
```

---

## 📝 Best Practices - Cách viết commit message tốt

### Format chuẩn:
```
[Type]: Mô tả ngắn gọn (dưới 50 ký tự)

Mô tả chi tiết (nếu cần):
- Điểm 1
- Điểm 2
```

### Các loại Type phổ biến:
- `feat`: Tính năng mới
- `fix`: Sửa lỗi
- `docs`: Cập nhật tài liệu
- `style`: Sửa format code (không ảnh hưởng logic)
- `refactor`: Refactor code
- `test`: Thêm/sửa test
- `chore`: Công việc bảo trì (build, config...)

### Ví dụ commit message tốt:
```bash
git commit -m "feat: Thêm hệ thống tu vi cho player"

git commit -m "fix: Sửa lỗi crash khi entity spawn"

git commit -m "refactor: Tách EntityManager thành các service nhỏ hơn"
```

---

## ⚠️ Lưu ý quan trọng

### 1. Luôn pull trước khi push:
```bash
# Nếu có conflict, Git sẽ báo lỗi
git pull
# Giải quyết conflict nếu có
git push
```

### 2. Kiểm tra status trước khi commit:
```bash
git status
# Đảm bảo bạn commit đúng file
```

### 3. Commit message nên:
- ✅ Viết bằng tiếng Việt hoặc tiếng Anh
- ✅ Ngắn gọn, rõ ràng
- ✅ Mô tả được thay đổi làm gì
- ❌ Không dùng message quá chung chung như "update", "fix"

---

## 🆘 Xử lý các tình huống phổ biến

### 1. Quên pull trước khi push:
```bash
git pull
# Nếu có conflict, giải quyết conflict
git push
```

### 2. Commit nhầm message:
```bash
git commit --amend -m "Message mới"
```

### 3. Muốn hủy thay đổi chưa commit:
```bash
# Hủy thay đổi trong file cụ thể
git checkout -- ten_file.java

# Hủy tất cả thay đổi chưa commit
git reset --hard HEAD
```

### 4. Muốn xem lịch sử commit:
```bash
git log
git log --oneline  # Xem dạng ngắn gọn
```

---

## 📚 Các lệnh hữu ích khác

```bash
# Xem branch hiện tại
git branch

# Tạo branch mới
git checkout -b ten_branch_moi

# Chuyển branch
git checkout ten_branch

# Xem remote repositories
git remote -v

# Xem thông tin chi tiết về commit
git show
```

---

Chúc bạn làm việc hiệu quả với Git! 🚀
