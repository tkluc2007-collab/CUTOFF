#!/usr/bin/env python3
"""Summarize CUTOFF benchmark measurements and create the required plots."""
from pathlib import Path
import sys

import matplotlib.pyplot as plt
import pandas as pd

ROOT = Path(__file__).resolve().parent.parent
RESULTS = ROOT / "results"
PLOTS = RESULTS / "plots"


def main():
    raw_path = RESULTS / "raw.csv"
    if not raw_path.exists():
        raise SystemExit(f"Missing {raw_path}; run scripts/run_all.sh first.")
    raw = pd.read_csv(raw_path)
    required = {"algo", "dist", "n", "cutoff", "rep", "time_ns"}
    if missing := required - set(raw.columns):
        raise SystemExit(f"raw.csv missing columns: {sorted(missing)}")
    raw["time_ms"] = raw["time_ns"] / 1_000_000
    summary = (raw.groupby(["algo", "dist", "n", "cutoff"], as_index=False)["time_ms"]
               .agg(median_ms="median", mean_ms="mean", std_ms="std", min_ms="min", n_reps="count"))
    summary["std_ms"] = summary["std_ms"].fillna(0.0)
    summary.to_csv(RESULTS / "summary.csv", index=False)
    best = make_best(summary)
    best.to_csv(RESULTS / "best_cutoff.csv", index=False)
    make_plots(summary)
    print_recap(best)


def make_best(summary):
    rows = []
    for (algo, dist, n), group in summary.groupby(["algo", "dist", "n"], sort=True):
        group = group.sort_values("cutoff")
        winner = group.loc[group["median_ms"].idxmin()]
        best_ms = winner.median_ms
        near = group.loc[group["median_ms"] <= best_ms * 1.02, "cutoff"].astype(str).tolist()
        at_7 = group.loc[group.cutoff == 7, "median_ms"].iloc[0]
        at_10 = group.loc[group.cutoff == 10, "median_ms"].iloc[0]
        rows.append({
            "algo": algo, "dist": dist, "n": n,
            "best_cutoff": int(winner.cutoff), "best_median_ms": best_ms,
            "near_best_cutoffs_2pct": ";".join(near),
            "median_ms_cutoff_7": at_7, "pct_over_best_cutoff_7": (at_7 / best_ms - 1) * 100,
            "median_ms_cutoff_10": at_10, "pct_over_best_cutoff_10": (at_10 / best_ms - 1) * 100,
        })
    return pd.DataFrame(rows)


def make_plots(summary):
    PLOTS.mkdir(parents=True, exist_ok=True)
    for (algo, n), group in summary.groupby(["algo", "n"], sort=True):
        fig, ax = plt.subplots(figsize=(8, 5))
        for dist, series in group.groupby("dist", sort=True):
            series = series.sort_values("cutoff")
            ax.errorbar(series.cutoff, series.median_ms, yerr=series.std_ms,
                        marker="o", capsize=3, label=dist)
        ax.set_xscale("log")
        ax.set_xlabel("CUTOFF")
        ax.set_ylabel("Median time (ms)")
        ax.set_title(f"{algo}: n={n}")
        ax.grid(True, which="both", alpha=0.3)
        ax.legend()
        fig.tight_layout()
        fig.savefig(PLOTS / f"{algo}_{n}.png", dpi=150)
        plt.close(fig)
    million = summary[summary.n == 1_000_000]
    if not million.empty:
        normalized = million.copy()
        normalized["normalized"] = normalized.groupby(["algo", "dist"])["median_ms"].transform(lambda x: x / x.min())
        average = normalized.groupby(["algo", "cutoff"], as_index=False)["normalized"].mean()
        fig, ax = plt.subplots(figsize=(8, 5))
        for algo, series in average.groupby("algo", sort=True):
            series = series.sort_values("cutoff")
            ax.plot(series.cutoff, series.normalized, marker="o", label=algo)
        ax.set_xscale("log")
        ax.set_xlabel("CUTOFF")
        ax.set_ylabel("Normalized median time (mean across distributions)")
        ax.set_title("n=1,000,000 normalized cutoff performance")
        ax.grid(True, which="both", alpha=0.3)
        ax.legend()
        fig.tight_layout()
        fig.savefig(PLOTS / "normalized_1000000.png", dpi=150)
        plt.close(fig)


def print_recap(best):
    print("Best cutoff recap (median timing; near-best is within 2%):")
    for row in best.itertuples(index=False):
        print(f"{row.algo:9} {row.dist:8} n={row.n:<7} best={row.best_cutoff:<3} "
              f"{row.best_median_ms:.3f} ms; near={row.near_best_cutoffs_2pct}; "
              f"7: {row.pct_over_best_cutoff_7:+.1f}%, 10: {row.pct_over_best_cutoff_10:+.1f}%")


if __name__ == "__main__":
    main()
