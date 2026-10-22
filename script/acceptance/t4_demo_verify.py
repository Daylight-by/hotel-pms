# -*- coding: utf-8 -*-
"""核对 T4 演示数据质量（直接查库，与生成器输出对照）"""
import subprocess

MYSQL = r"D:\tools\db\mariadb\mariadb-11.4.4-winx64\bin\mariadb.exe"
DB = "ruoyi-vue-pro"

def q(sql):
    r = subprocess.run([MYSQL, "-uroot", "-p123456", DB, "-N", "-e", sql],
                       capture_output=True, text=True, encoding="utf-8")
    if r.returncode != 0:
        return f"ERR {r.stderr.strip()[:200]}"
    return r.stdout.strip()

checks = {
    "订单总数(T4-demo)": "SELECT COUNT(*) FROM hotel_order WHERE remark LIKE '%T4-demo%'",
    "订单状态分布": "SELECT CONCAT(status, ':', COUNT(*)) FROM hotel_order WHERE remark LIKE '%T4-demo%' GROUP BY status ORDER BY status",
    "订单未来预抵(>今天)": "SELECT COUNT(*) FROM hotel_order WHERE remark LIKE '%T4-demo%' AND check_in_date > CURDATE()",
    "今日在住": "SELECT COUNT(*) FROM hotel_order WHERE remark LIKE '%T4-demo%' AND status = 30",
    "账页总数": "SELECT COUNT(*) FROM hotel_folio WHERE remark LIKE '%T4-demo%'",
    "账务明细(借/贷)": "SELECT CONCAT(direction, ':', COUNT(*), ':', ROUND(SUM(amount),2)) FROM hotel_folio_item WHERE remark LIKE '%T4-demo%' GROUP BY direction",
    "支付流水": "SELECT COUNT(*), ROUND(SUM(amount),2) FROM hotel_payment_record WHERE remark LIKE '%T4-demo%'",
    "住客": "SELECT COUNT(*) FROM hotel_guest WHERE remark LIKE '%T4-demo%'",
    "会员(guest_id 关联)": "SELECT COUNT(*) FROM hotel_member m JOIN hotel_guest g ON m.guest_id = g.id WHERE g.remark LIKE '%T4-demo%'",
    "房价日历演示行": "SELECT COUNT(*) FROM hotel_room_daily_rate WHERE remark = 'T4-demo'",
    "房价日历在售合计": "SELECT SUM(sold_inventory) FROM hotel_room_daily_rate WHERE remark = 'T4-demo'",
    "清洁任务": "SELECT CONCAT(status, ':', COUNT(*)) FROM hotel_cleaning_task WHERE remark LIKE '%T4-demo%' GROUP BY status ORDER BY status",
    "夜审(-1 打标)": "SELECT COUNT(*) FROM hotel_night_audit WHERE operator_id = -1",
    "在住房间房态=20": "SELECT COUNT(*) FROM hotel_room r JOIN hotel_order o ON o.room_id = r.id AND o.status = 30 AND o.remark LIKE '%T4-demo%' WHERE r.status = 20",
    "演示批次": "SELECT CONCAT(batch_no, '|', status, '|', order_count) FROM hotel_demo_seed ORDER BY id DESC LIMIT 1",
}
for name, sql in checks.items():
    print(f"{name}: {q(sql)}")
