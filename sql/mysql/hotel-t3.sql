-- ============================================================================
-- T3 · C 端 uni-app —— 房型展示字段
-- 归属：T3（C 端预订站）
-- 说明：只允许追加本包字段，禁止修改 sql/mysql/hotel.sql（由派发者统一合并）。
--       本文件可重复执行（MariaDB 10.0+ 支持 ADD COLUMN IF NOT EXISTS）。
-- 号段：无新增表、无菜单 ID、无错误码。
-- ============================================================================

-- ----------------------------
-- 1. 房型表追加 C 端展示字段
-- ----------------------------
ALTER TABLE `hotel_room_type`
    ADD COLUMN IF NOT EXISTS `cover_image` varchar(500) NULL COMMENT '封面图（URL 或静态资源路径）' AFTER `description`,
    ADD COLUMN IF NOT EXISTS `images`      varchar(2000) NULL COMMENT '轮播图，多张用英文逗号分隔' AFTER `cover_image`,
    ADD COLUMN IF NOT EXISTS `bed_type`    varchar(50) NULL COMMENT '床型，如 1.5m 大床' AFTER `images`,
    ADD COLUMN IF NOT EXISTS `breakfast`   varchar(50) NULL COMMENT '早餐说明，如 含双早 / 无早' AFTER `bed_type`,
    ADD COLUMN IF NOT EXISTS `window`      varchar(20) NULL COMMENT '窗户说明，如 有窗 / 落地窗 / 无窗' AFTER `breakfast`;

-- ----------------------------
-- 2. 演示数据：为基线四个房型补齐 C 端展示信息
--    （只更新房型档案字段，不动价格、库存、订单等业务数据）
-- ----------------------------
UPDATE `hotel_room_type`
SET `bed_type`  = '1.2m 单人床',
    `breakfast` = '无早',
    `window`    = '有窗'
WHERE `code` = 'SINGLE'
  AND `deleted` = b'0';

UPDATE `hotel_room_type`
SET `bed_type`  = '1.5m 大床',
    `breakfast` = '含双早',
    `window`    = '有窗'
WHERE `code` = 'DOUBLE'
  AND `deleted` = b'0';

UPDATE `hotel_room_type`
SET `bed_type`  = '1.2m 双床',
    `breakfast` = '含双早',
    `window`    = '落地窗'
WHERE `code` = 'TWIN'
  AND `deleted` = b'0';

UPDATE `hotel_room_type`
SET `bed_type`  = '2m 大床 + 沙发床',
    `breakfast` = '含双早',
    `window`    = '落地窗'
WHERE `code` = 'SUITE'
  AND `deleted` = b'0';

-- 封面图与轮播图留空：C 端在无图时会渲染品牌色块，避免演示现场断图。
-- 后续如需真实图片，将图片放入 yudao-ui/yudao-ui-hotel-app/src/static/rooms/，
-- 或填写可公网访问的 URL，例如：
-- UPDATE hotel_room_type SET cover_image = 'https://cdn.example.com/room/double.jpg' WHERE code = 'DOUBLE';

-- ----------------------------
-- 3. 回滚（仅限本包字段；生产环境禁止直接执行）
-- ----------------------------
-- ALTER TABLE `hotel_room_type`
--     DROP COLUMN `cover_image`,
--     DROP COLUMN `images`,
--     DROP COLUMN `bed_type`,
--     DROP COLUMN `breakfast`,
--     DROP COLUMN `window`;
