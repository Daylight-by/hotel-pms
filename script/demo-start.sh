#!/usr/bin/env bash
# ============================================================
# 酒店 PMS T4 演示一键启动脚本（Linux/macOS；Windows 用 PowerShell 命令见文末）
# 功能：①环境检查 ②导入 hotel-t4.sql（建表+菜单） ③登录拿 token ④生成演示数据 ⑤打开大屏
# 用法：bash script/demo-start.sh
# ============================================================
set -e

API="http://localhost:48080/admin-api"
MYSQL_BIN=${MYSQL_BIN:-mysql}
DB_USER=${DB_USER:-root}
DB_PASS=${DB_PASS:-123456}
DB_NAME=${DB_NAME:-ruoyi-vue-pro}
REPO_DIR="$(cd "$(dirname "$0")/.." && pwd)"

echo "==> [1/5] 环境检查"
for port in 3306 6379 48080; do
  (echo > "/dev/tcp/127.0.0.1/$port") 2>/dev/null \
    && echo "    OK  端口 $port 在线" \
    || echo "    !!  端口 $port 未监听，请先启动 MySQL/Redis/后端"
done

echo "==> [2/5] 导入 hotel-t4.sql（表结构 + 菜单权限，幂等）"
if [ -f "$REPO_DIR/sql/mysql/hotel-t4.sql" ]; then
  $MYSQL_BIN -u"$DB_USER" -p"$DB_PASS" --default-character-set=utf8mb4 "$DB_NAME" < "$REPO_DIR/sql/mysql/hotel-t4.sql"
  echo "    完成"
else
  echo "    !! 未找到 sql/mysql/hotel-t4.sql，跳过"
fi

echo "==> [3/5] 登录并获取 token"
LOGIN=$(curl -s -X POST "$API/system/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123","tenant-name":"来财酒店"}')
TOKEN=$(echo "$LOGIN" | sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p')
if [ -z "$TOKEN" ]; then
  echo "    登录失败：$LOGIN"; exit 1
fi
echo "    token 获取成功"

echo "==> [4/5] 清理旧演示数据并重新生成（6 个月）"
curl -s -X POST "$API/hotel/demo/clean" -H "Authorization: Bearer $TOKEN" > /dev/null || true
RESULT=$(curl -s -X POST "$API/hotel/demo/generate?months=6" -H "Authorization: Bearer $TOKEN")
echo "    生成结果：$RESULT"

echo "==> [5/5] 打开大屏"
STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$API/hotel/dashboard/overview" -H "Authorization: Bearer $TOKEN")
echo "    大屏接口状态：$STATUS（200=正常）"
echo ""
echo "=============================================================="
echo " 演示就绪！请打开："
echo "   · 大屏：   http://localhost:5173  → 菜单「经营数据大屏」"
echo "   · 报表：   菜单「报表中心」（日报/月报/导出 xlsx）"
echo "   · 审计：   菜单「操作审计」（展开行看 before/after）"
echo "=============================================================="

# ------------------------------------------------------------
# Windows PowerShell 等价命令（无需安装 bash）：
#   1) 导入 SQL：
#      Get-Content sql\mysql\hotel-t4.sql -Raw | & "D:\tools\db\mariadb\mariadb-11.4.4-winx64\bin\mariadb.exe" -uroot -p123456 ruoyi-vue-pro
#   2) 登录取 token：
#      $t = (Invoke-RestMethod -Method Post -Uri "http://localhost:48080/admin-api/system/auth/login" -ContentType "application/json" -Body '{"username":"admin","password":"admin123","tenant-name":"来财酒店"}').data.accessToken
#   3) 生成演示数据：
#      Invoke-RestMethod -Method Post -Uri "http://localhost:48080/admin-api/hotel/demo/generate?months=6" -Headers @{Authorization="Bearer $t"}
#   4) 大屏地址：http://localhost:5173（菜单「经营数据大屏」）
# ------------------------------------------------------------
