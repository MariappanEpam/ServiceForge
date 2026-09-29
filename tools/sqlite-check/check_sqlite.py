import sqlite3
import sys
from pathlib import Path


def main() -> int:
    if len(sys.argv) != 2:
        print("Usage: python check_sqlite.py <path-to-sqlite-db>")
        return 2

    db_path = Path(sys.argv[1]).expanduser().resolve()
    if not db_path.exists():
        print(f"DB file not found: {db_path}")
        return 1

    con = sqlite3.connect(str(db_path))
    cur = con.cursor()

    tables = cur.execute("select name from sqlite_master where type='table' order by name").fetchall()
    tech = cur.execute("select id,name,region from technicians order by id").fetchall()
    jobs_t1 = cur.execute(
        "select id,technician_id,customer_name,start_time,end_time,status from jobs where technician_id=1 order by id"
    ).fetchall()

    con.close()

    print("tables", tables)
    print("tech", tech)
    print("jobs_t1", jobs_t1)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
