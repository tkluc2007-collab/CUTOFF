# SPEC: Find the best CUTOFF for Mergesort & Quicksort (insertion-sort cutoff)

Goal: experimentally find the best CUTOFF (switch to insertion sort for small subarrays)
for Mergesort and Quicksort on THIS machine. Reference values: 7 (old textbook), 10 (new).
Implementations MUST follow the lecture code (Sedgewick & Wayne, algs4 slides 2.2 / 2.3). No other implementations.
Language: Java 17+ (matches the lecture). Data type: `int[]` (lecture uses `Comparable[]`; replace `less(a,b)` by `a<b`, note this in the report).

## 1. Folder layout
```
project/
  SPEC.md
  src/        Insertion.java  MergeSort.java  QuickSort.java  Benchmark.java  GenData.java  Verify.java
  data/       <dist>_<n>.txt   (one int per line)
  scripts/    run_all.sh (or .bat)   analyze.py
  results/    raw.csv  summary.csv  best_cutoff.csv  plots/*.png  machine.txt
  report/     report.md  (Vietnamese)
  ai_logs/    (export of AI chats)
```

## 2. Algorithms (exact lecture logic)
Insertion sort on range: `insertion(a, lo, hi)`: for i in lo..hi, for j=i; j>lo && a[j]<a[j-1]; j-- swap.

**Mergesort (top-down, slides 8 + 19)**
```
merge(a, aux, lo, mid, hi):
  for k=lo..hi: aux[k]=a[k]
  i=lo; j=mid+1
  for k=lo..hi:
    if i>mid: a[k]=aux[j++]
    else if j>hi: a[k]=aux[i++]
    else if aux[j]<aux[i]: a[k]=aux[j++]
    else a[k]=aux[i++]

sort(a, aux, lo, hi):
  if hi <= lo + CUTOFF - 1: insertion(a, lo, hi); return
  mid = lo + (hi-lo)/2
  sort(a,aux,lo,mid); sort(a,aux,mid+1,hi); merge(a,aux,lo,mid,hi)
```
`aux` allocated ONCE per call of public `sort(int[] a, int cutoff)`. NO other optimizations
(no stop-if-sorted, no aux-swap trick) so CUTOFF is the only variable.

**Quicksort (slides 13, 15, 27)**
```
sort(a, cutoff): shuffle(a) (Fisher-Yates, fixed seed per run); sort(a, 0, n-1)

sort(a, lo, hi):
  if hi <= lo + CUTOFF - 1: insertion(a, lo, hi); return
  j = partition(a, lo, hi)
  sort(a, lo, j-1); sort(a, j+1, hi)

partition(a, lo, hi):
  i=lo; j=hi+1
  while true:
    while a[++i] < a[lo]: if i==hi break
    while a[lo] < a[--j]: if j==lo break
    if i>=j break
    swap(a,i,j)
  swap(a,lo,j); return j
```
Scans STOP on equal keys (as in lecture). No median-of-3, no 3-way.

IMPORTANT: valid CUTOFF >= 1. CUTOFF=1 means "no cutoff" (`hi<=lo`). CUTOFF=0 would recurse forever; never use it.

## 3. Test (Verify.java)
For every algorithm, for CUTOFF in the experiment list, on random / sorted / reversed / duplicates inputs of
sizes {0,1,2,3,10,100,1000,10^5}: result must equal `Arrays.sort` copy. Print PASS/FAIL. Run before benchmarking.

## 4. Data (GenData.java, fixed seed 42)
Distributions (name -> description):
- `random`  : uniform ints in [0, 2^31-1)
- `sorted`  : ascending
- `reversed`: descending
- `fewuniq` : random from only 100 distinct values (many duplicates)
Sizes n: 10^4, 10^5, 10^6. Files `data/<dist>_<n>.txt`. (If data/ > 100MB, store 10^6 as .txt.gz or binary and say so.)

## 5. Experiment design (Benchmark.java)
- Algorithms: mergesort, quicksort.
- CUTOFF list: 1,2,3,4,5,6,7,8,9,10,12,15,20,25,30,40,50,75,100
- For each (algo, dist, n): load data once; for each rep r in 1..REPS: for each cutoff (iterate in a
  RANDOMIZED order per rep to cancel drift): copy original array, time ONLY `sort(copy, cutoff)` with `System.nanoTime()`.
  Quicksort shuffle is inside the timed region (it is part of the algorithm); shuffle seed = fixed per (rep).
- JVM warm-up: before measuring each (algo, dist, n), run 5 untimed rounds over all cutoffs.
- REPS = 15 for n<=10^5; REPS = 9 for n=10^6 (adjust if total runtime > ~30 min, and say so).
- Run with `java -Xms2g -Xmx2g -XX:+UseSerialGC` for stable timing. Close other apps, plug in laptop.
- Output `results/raw.csv` columns: `algo,dist,n,cutoff,rep,time_ns`
- Output `results/machine.txt`: CPU model, cores, RAM, OS, Java version, JVM flags.
- Optional extra (bonus): count compares per run -> column `compares` in a separate `results/compares.csv` (n=10^5, rep=1 only).

## 6. Analysis (scripts/analyze.py, pandas + matplotlib)
- `summary.csv`: per (algo,dist,n,cutoff): median_ms, mean_ms, std_ms, min_ms, n_reps.
- `best_cutoff.csv`: per (algo,dist,n): cutoff with min median; plus the set of cutoffs within 2% (or within 1 std)
  of the best ("statistically near-best"); plus median_ms at cutoff 7 and 10 and % difference vs best.
- Plots (png, saved in results/plots/): for each algo & n: time (ms, median, error bars=std) vs CUTOFF, one line per dist,
  x-axis log or categorical. Plus one overall plot: normalized time (time / time at best cutoff) vs CUTOFF, averaged over dists, for n=10^6.
- Print a short text recap to stdout (<= 40 lines) so the agent writing the report does not need to read raw.csv.

## 7. Report (report/report.md, in Vietnamese)
Sections: 1) Mục tiêu 2) Môi trường (from machine.txt) 3) Cài đặt (mô tả ngắn, trích lecture) 4) Thiết kế thí nghiệm
(dữ liệu, kích thước, số lần lặp, warm-up, cách đo, vì sao) 5) Kết quả (bảng thống kê + đồ thị) 6) Phân tích
(ảnh hưởng của loại dữ liệu và n; vùng CUTOFF phẳng; so với 7 và 10; mergesort vs quicksort)
7) Kết luận: giá trị CUTOFF khuyến nghị cho từng thuật toán trên máy này + giới hạn/threats to validity
(JIT, noise, int[] thay Comparable[], một máy duy nhất) 8) Cách chạy lại. Phụ lục: mô tả việc dùng AI.
Conclusions must be backed by numbers from summary.csv / best_cutoff.csv only. Never invent numbers.

## 8. Rules for agents
- Edit only files in your assigned scope. Do not reprint whole files in chat; reply with a <= 10-line summary.
- Do not change the algorithms' logic described in section 2 without asking.