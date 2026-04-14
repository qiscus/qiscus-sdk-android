#!/usr/bin/env python3
"""
Loki CLI - Melihat log dari Loki server via HTTP API
Usage:
  python3 loki.py -u admin -p password -q '{app="myapp"}'
  python3 loki.py -u admin -p password --labels
  python3 loki.py -u admin -p password -q '{job="varlogs"}' --since 1h --limit 200
"""

import argparse
import sys
import os
from datetime import datetime, timedelta, timezone

import requests
from requests.auth import HTTPBasicAuth

LOKI_BASE_URL = "http://loki.qiscus.io"


# ─────────────────────────────────────────────
# Helpers
# ─────────────────────────────────────────────

def to_ns(dt: datetime) -> str:
    """Konversi datetime ke nanosecond Unix epoch string."""
    return str(int(dt.timestamp() * 1e9))


def parse_since(since: str) -> datetime:
    """Parse nilai --since seperti '30m', '2h', '1d' ke datetime."""
    unit  = since[-1]
    value = int(since[:-1])
    now   = datetime.now(timezone.utc)
    if unit == 'm':
        return now - timedelta(minutes=value)
    elif unit == 'h':
        return now - timedelta(hours=value)
    elif unit == 'd':
        return now - timedelta(days=value)
    else:
        print(f"[!] Format --since tidak dikenal: {since}  (contoh: 30m, 2h, 1d)")
        sys.exit(1)


def format_ts(ns_str: str) -> str:
    """Konversi nanosecond string ke timestamp yang mudah dibaca."""
    try:
        ms = int(ns_str) / 1e6
        dt = datetime.fromtimestamp(ms / 1000, tz=timezone.utc).astimezone()
        return dt.strftime("%Y-%m-%d %H:%M:%S")
    except Exception:
        return ns_str


def make_session(username: str, password: str) -> requests.Session:
    s = requests.Session()
    s.auth = HTTPBasicAuth(username, password)
    s.headers.update({"Accept": "application/json"})
    return s


# ─────────────────────────────────────────────
# Loki API calls
# ─────────────────────────────────────────────

def get_labels(session: requests.Session):
    """Tampilkan semua label yang tersedia."""
    url = f"{LOKI_BASE_URL}/loki/api/v1/labels"
    r = session.get(url, timeout=15)
    r.raise_for_status()
    data = r.json()
    labels = data.get("data", [])
    print(f"\n{'─'*40}")
    print(f"  Labels tersedia ({len(labels)})")
    print(f"{'─'*40}")
    for label in sorted(labels):
        print(f"  • {label}")
    print()


def get_label_values(session: requests.Session, label: str):
    """Tampilkan nilai-nilai dari sebuah label."""
    url = f"{LOKI_BASE_URL}/loki/api/v1/label/{label}/values"
    r = session.get(url, timeout=15)
    r.raise_for_status()
    data = r.json()
    values = data.get("data", [])
    print(f"\n{'─'*40}")
    print(f"  Nilai label '{label}' ({len(values)})")
    print(f"{'─'*40}")
    for v in sorted(values):
        print(f"  • {v}")
    print()


def query_range(session: requests.Session, logql: str, start: datetime,
                end: datetime, limit: int, output: str):
    """Query log dalam rentang waktu."""
    url = f"{LOKI_BASE_URL}/loki/api/v1/query_range"
    params = {
        "query": logql,
        "start": to_ns(start),
        "end":   to_ns(end),
        "limit": limit,
        "direction": "backward",  # terbaru dulu
    }
    r = session.get(url, params=params, timeout=30)

    if r.status_code != 200:
        print(f"[!] HTTP {r.status_code}: {r.text}")
        sys.exit(1)

    data   = r.json()
    result = data.get("data", {}).get("result", [])

    if not result:
        print("[i] Tidak ada log yang ditemukan.")
        return

    entries = []
    for stream in result:
        labels = stream.get("stream", {})
        for ts_ns, line in stream.get("values", []):
            entries.append((ts_ns, line, labels))

    # sort terlama → terbaru
    entries.sort(key=lambda x: int(x[0]))

    print(f"\n{'─'*60}")
    print(f"  Query : {logql}")
    print(f"  Range : {start.strftime('%Y-%m-%d %H:%M')} → {end.strftime('%Y-%m-%d %H:%M')}")
    print(f"  Baris : {len(entries)}")
    print(f"{'─'*60}")

    for ts_ns, line, labels in entries:
        if output == "json":
            import json
            print(json.dumps({"ts": format_ts(ts_ns), "line": line, "labels": labels}))
        else:
            label_str = " ".join(f"{k}={v}" for k, v in labels.items())
            print(f"[{format_ts(ts_ns)}] {label_str}  {line}")

    print(f"{'─'*60}")
    print(f"  Total: {len(entries)} baris\n")


def tail_logs(session: requests.Session, logql: str, interval: int, limit: int):
    """Terus-menerus poll log terbaru (mirip tail -f)."""
    import time
    print(f"[i] Tailing '{logql}' setiap {interval} detik... (Ctrl+C untuk berhenti)\n")
    seen = set()
    while True:
        end   = datetime.now(timezone.utc)
        start = end - timedelta(seconds=interval * 2)
        url   = f"{LOKI_BASE_URL}/loki/api/v1/query_range"
        params = {
            "query":     logql,
            "start":     to_ns(start),
            "end":       to_ns(end),
            "limit":     limit,
            "direction": "backward",
        }
        try:
            r = session.get(url, params=params, timeout=15)
            if r.status_code == 200:
                result = r.json().get("data", {}).get("result", [])
                entries = []
                for stream in result:
                    labels = stream.get("stream", {})
                    for ts_ns, line in stream.get("values", []):
                        entries.append((ts_ns, line, labels))
                entries.sort(key=lambda x: int(x[0]))
                for ts_ns, line, labels in entries:
                    if ts_ns not in seen:
                        seen.add(ts_ns)
                        label_str = " ".join(f"{k}={v}" for k, v in labels.items())
                        print(f"[{format_ts(ts_ns)}] {label_str}  {line}")
            else:
                print(f"[!] HTTP {r.status_code}")
        except Exception as e:
            print(f"[!] Error: {e}")
        time.sleep(interval)


# ─────────────────────────────────────────────
# CLI
# ─────────────────────────────────────────────

def build_parser() -> argparse.ArgumentParser:
    p = argparse.ArgumentParser(
        description="Loki CLI — query log dari http://loki.qiscus.io",
        formatter_class=argparse.RawTextHelpFormatter,
        epilog="""
Contoh:
  # Lihat semua label
  python3 loki.py -u admin -p secret --labels

  # Lihat nilai label 'app'
  python3 loki.py -u admin -p secret --label-values app

  # Query log 1 jam terakhir
  python3 loki.py -u admin -p secret -q '{app="myapp"}' --since 1h

  # Query log 30 menit terakhir, limit 500
  python3 loki.py -u admin -p secret -q '{job="varlogs"}' --since 30m --limit 500

  # Tail log real-time
  python3 loki.py -u admin -p secret -q '{app="myapp"}' --tail

  # Output JSON
  python3 loki.py -u admin -p secret -q '{app="myapp"}' --since 2h --output json
        """
    )
    p.add_argument("-u", "--username", default=os.getenv("LOKI_USER"),
                   help="Username (atau set env LOKI_USER)")
    p.add_argument("-p", "--password", default=os.getenv("LOKI_PASS"),
                   help="Password (atau set env LOKI_PASS)")
    p.add_argument("-q", "--query",    help='LogQL query, contoh: {app="myapp"}')
    p.add_argument("--since",  default="1h",
                   help="Rentang waktu ke belakang: 30m / 2h / 1d (default: 1h)")
    p.add_argument("--limit",  type=int, default=100,
                   help="Jumlah maks baris log (default: 100)")
    p.add_argument("--labels", action="store_true",
                   help="Tampilkan semua label yang tersedia")
    p.add_argument("--label-values", metavar="LABEL",
                   help="Tampilkan nilai-nilai dari label tertentu")
    p.add_argument("--tail",   action="store_true",
                   help="Tail log secara real-time (poll setiap 5 detik)")
    p.add_argument("--interval", type=int, default=5,
                   help="Interval poll saat --tail dalam detik (default: 5)")
    p.add_argument("--output", choices=["text", "json"], default="text",
                   help="Format output: text (default) atau json")
    return p


def main():
    parser = build_parser()
    args   = parser.parse_args()

    # Validasi credentials
    if not args.username or not args.password:
        parser.error(
            "Username dan password wajib diisi.\n"
            "Gunakan -u / -p, atau set environment variable LOKI_USER / LOKI_PASS"
        )

    session = make_session(args.username, args.password)

    # ── Mode: list labels
    if args.labels:
        get_labels(session)
        return

    # ── Mode: label values
    if args.label_values:
        get_label_values(session, args.label_values)
        return

    # ── Mode: query
    if not args.query:
        parser.error("Tentukan query dengan -q / --query, atau gunakan --labels / --label-values")

    if args.tail:
        tail_logs(session, args.query, args.interval, args.limit)
    else:
        end   = datetime.now(timezone.utc)
        start = parse_since(args.since)
        query_range(session, args.query, start, end, args.limit, args.output)


if __name__ == "__main__":
    main()
