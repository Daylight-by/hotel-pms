# 数据库脚本

数据库：MySQL 8.0 / MariaDB 10.6+，字符集 `utf8mb4`，默认库名 `ruoyi-vue-pro`。

## 导入顺序（重要）

**必须按顺序导入**，后面的脚本依赖前面的表结构。`hotel.sql` 只是 P0 的基础版本，
P1 / P2 的增量在 `hotel-t1` ~ `hotel-t5` 里。

| 顺序 | 文件 | 内容 | 表数 |
| --- | --- | --- | --- |
| 1 | `ruoyi-vue-pro.sql` | 芋道基础表（用户、权限、租户、字典、日志） | — |
| 2 | `quartz.sql` | 定时任务表 | — |
| 3 | `hotel.sql` | 酒店业务基础表 + P0 演示数据 + 菜单权限 | 14 |
| 4 | `hotel-t1.sql` | 渠道、房价计划、预订占位、收益定价建议与回测 | 5 |
| 5 | `hotel-t2.sql` | 会员、积分流水、支付单、发票申请 | 4 |
| 6 | `hotel-t3.sql` | 房型表追加 C 端展示字段（ALTER，可重复执行） | 0 |
| 7 | `hotel-t4.sql` | 操作日志、演示数据种子 | 2 |
| 8 | `hotel-t5.sql` | 多门店、公安上传、门锁卡、协议单位 AR、OTA 配置与订单 | 8 |

一条命令跑完：

```bash
DB=ruoyi-vue-pro
for f in ruoyi-vue-pro.sql quartz.sql hotel.sql hotel-t1.sql hotel-t2.sql hotel-t3.sql hotel-t4.sql hotel-t5.sql; do
  mysql -uroot -p "$DB" < "sql/mysql/$f"
done
```

Windows 下（PowerShell）：

```powershell
$db = "ruoyi-vue-pro"
$files = @("ruoyi-vue-pro.sql","quartz.sql","hotel.sql","hotel-t1.sql","hotel-t2.sql","hotel-t3.sql","hotel-t4.sql","hotel-t5.sql")
foreach ($f in $files) { Get-Content "sql\mysql\$f" -Raw | mysql -uroot -p123456 $db }
```

## 表总览（33 张 `hotel_*`）

| 域 | 表 |
| --- | --- |
| 房态与客房 | `hotel_room_type` `hotel_room` `hotel_room_daily_rate` `hotel_room_change_log` |
| 预订与住客 | `hotel_guest` `hotel_order` `hotel_booking_hold` `hotel_channel` `hotel_ota_config` `hotel_ota_order` |
| 账务结算 | `hotel_folio` `hotel_folio_item` `hotel_payment_record` `hotel_pay_order` `hotel_invoice` `hotel_invoice_apply` |
| 夜审与交班 | `hotel_business_date` `hotel_night_audit` `hotel_shift_handover` |
| 收益定价 | `hotel_rate_plan` `hotel_price_suggestion` `hotel_price_backtest` |
| 客房服务 | `hotel_cleaning_task` |
| 会员 | `hotel_member` `hotel_member_points_log` |
| 协议单位 | `hotel_ar_account` `hotel_ar_ledger` `hotel_ar_settlement` |
| 合规与硬件 | `hotel_psb_upload_log` `hotel_lock_card_log` |
| 多门店 | `hotel_store` |
| 审计与演示 | `hotel_operation_log` `hotel_demo_seed` |

所有业务表继承芋道 `TenantBaseDO`，带 `tenant_id` / `deleted` / `create_time` / `update_time`，
查询时需携带 `tenant-id` 请求头。

## 重置演示数据

后端提供演示数据清理接口（逻辑删除，不物理删）：

```
DELETE /admin-api/hotel/demo/clean
```

需要物理重建时，删库后按上表顺序重新导入即可。
