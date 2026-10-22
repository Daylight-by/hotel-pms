# -*- coding: utf-8 -*-
"""
三期统一验收入口（派发者用）

用法：
    python script/acceptance/run_all.py --phase p0
    python script/acceptance/run_all.py --phase p1
    python script/acceptance/run_all.py --phase p2
    python script/acceptance/run_all.py --phase all      # 基线 + P0 + P1 + P2
    python script/acceptance/run_all.py                  # 只跑基线回归

说明：
  - 每期验收都会先跑 check_baseline.py（基线回归），确保新开发没破坏已有能力。
  - 任意一项 FAIL，进程退出码为 1。
"""
import os
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
PY = sys.executable

SCRIPTS = {
    "baseline": "check_baseline.py",
    "p0": "check_p0.py",
    "p1": "check_p1.py",
    "p2": "check_p2.py",
}

# 每期验收的先后依赖（含基线回归）
PHASE_CHAIN = {
    "baseline": ["baseline"],
    "p0": ["baseline", "p0"],
    "p1": ["baseline", "p0", "p1"],
    "p2": ["baseline", "p0", "p1", "p2"],
    "all": ["baseline", "p0", "p1", "p2"],
}


def run_one(key):
    path = os.path.join(HERE, SCRIPTS[key])
    print("\n" + "#" * 60)
    print("# 运行 %s" % SCRIPTS[key])
    print("#" * 60)
    p = subprocess.run([PY, path], cwd=os.path.dirname(HERE))
    return p.returncode


def main():
    phase = "baseline"
    if "--phase" in sys.argv:
        i = sys.argv.index("--phase")
        if i + 1 < len(sys.argv):
            phase = sys.argv[i + 1].lower()
    if phase not in PHASE_CHAIN:
        print("未知期次：%s（可选 %s）" % (phase, "/".join(PHASE_CHAIN)))
        return 2

    results = {}
    for key in PHASE_CHAIN[phase]:
        results[key] = run_one(key)

    print("\n" + "#" * 60)
    print("# 总览")
    print("#" * 60)
    failed = []
    for key in PHASE_CHAIN[phase]:
        rc = results[key]
        print("  %-10s %s" % (key, "PASS" if rc == 0 else "FAIL"))
        if rc != 0:
            failed.append(key)
    print("#" * 60)
    if failed:
        print("验收未通过：%s" % ", ".join(failed))
        return 1
    print("验收通过：%s" % ", ".join(PHASE_CHAIN[phase]))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
