# CUTOFF: Mergesort & Quicksort

Empirical study of the insertion-sort CUTOFF for Mergesort and Quicksort in Java: implementations from lecture, benchmark scripts, datasets, and analysis.

Dự án đo thực nghiệm để tìm ngưỡng CUTOFF (chuyển sang insertion sort cho mảng con nhỏ) phù hợp nhất cho Mergesort và Quicksort trên máy của nhóm, và so sánh với các giá trị trong giáo trình (7 và 10).

## Kết quả chính

Trên máy thử nghiệm (Intel i5-6200U, Java 21), vùng CUTOFF cho kết quả gần tốt nhất (n = 10^6, giao của 4 loại dữ liệu):

| Thuật toán | Vùng CUTOFF khuyến nghị |
|---|---|
| Mergesort | khoảng 15 đến 40 |
| Quicksort | khoảng 25 đến 100 (100 là giá trị lớn nhất được thử) |

Các giá trị trong vùng này khác nhau không đáng kể so với nhiễu đo, nên dự án không chọn một giá trị duy nhất. CUTOFF = 7 chậm hơn giá trị tốt nhất khoảng 12 đến 32% với Mergesort. Với Quicksort, CUTOFF = 10 chỉ chậm hơn khoảng 2,4% (random, n = 10^6). Chi tiết xem trong `report/report.md`.

## Cấu trúc thư mục

- `src/`: Insertion, MergeSort, QuickSort, Benchmark, GenData, Verify
- `data/`: bộ dữ liệu thí nghiệm (random, sorted, reversed, fewuniq; n = 10^4, 10^5, 10^6)
- `scripts/`: chạy thí nghiệm và phân tích kết quả
- `results/`: số liệu thô, thống kê, đồ thị, thông số máy
- `report/`: báo cáo thí nghiệm
- `ai_logs/`: log chat với AI

## Cách chạy lại

Yêu cầu: Java 17 trở lên, Python 3 với `pandas` và `matplotlib`.

1. Kiểm tra tính đúng đắn của code:
   `javac -d out src\*.java` rồi `java -cp out Verify`
2. Chạy toàn bộ thí nghiệm (khoảng 20 đến 30 phút, nên cắm sạc và đóng các app khác):
   `powershell -ExecutionPolicy Bypass -File scripts\run_all.ps1`
3. Chạy lại riêng n = 10^6 với 25 lần lặp:
   `powershell -ExecutionPolicy Bypass -File scripts\run_n1e6.ps1`
4. Tổng hợp thống kê và vẽ đồ thị:
   `python scripts\analyze.py`

## Ghi chú

- Cài đặt bám theo bài giảng (Sedgewick & Wayne, Algorithms 4th ed.), dùng `int[]` thay cho `Comparable[]`.
- CUTOFF nhỏ nhất hợp lệ là 1 (nghĩa là không dùng cutoff).
- Kết quả phụ thuộc vào máy đo, xem mục "Giới hạn" trong báo cáo.