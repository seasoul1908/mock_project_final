# Design: Merge Remote Main into Local Branch

Tài liệu thiết kế quy trình lấy code mới từ nhánh remote `main` về nhánh làm việc cục bộ `trung` mà không ảnh hưởng đến nhánh `main` trên Git remote.

## Khảo sát hiện trạng (Context Exploration)
- **Nhánh hiện tại ở local:** `trung` (Working tree sạch).
- **Nhánh remote cần lấy code:** `origin/main` (Có các commit mới).
- **Yêu cầu quan trọng:** Không thực hiện bất kỳ hành động ghi (push) hay sửa đổi nào lên nhánh `main` trên remote.

## Phương án thiết kế (Proposed Design)

Chúng ta sử dụng **Phương án 1 (Fetch và Merge trực tiếp)**:
1. Thực hiện tải thông tin cập nhật từ server (fetch) thông qua lệnh:
   ```bash
   git fetch origin
   ```
2. Thực hiện gộp (merge) nhánh `origin/main` trực tiếp vào nhánh cục bộ `trung`:
   ```bash
   git merge origin/main
   ```
   *Lưu ý:* Việc merge này hoàn toàn diễn ra ở local trên nhánh `trung`. Nhánh `main` trên Git remote sẽ không bị tác động hay thay đổi gì.

## Kịch bản xử lý xung đột (Conflict Resolution)
- Nếu có xung đột trong quá trình merge, chúng ta sẽ rà soát các file xung đột và phối hợp để sửa đổi từng file, sau đó tiến hành commit để hoàn tất quá trình merge.
