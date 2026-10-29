import pandas as pd
d = pd.read_csv("results/raw.csv")
print(d.algo.unique(), d.dist.unique(), sorted(d.n.unique()))
x = d[d.algo.str.contains("quick", case=False) & (d.dist == "reversed") & (d.n == d.n.max())]
x = x.assign(ms=x.time_ns / 1e6)
for c in [7, 40, 75]:
    print("cutoff", c, sorted(x[x.cutoff == c].ms.round(0).tolist()))