-- ============================================================
-- T5 · 商业化横切：多门店 + 协议单位 AR + 外部对接 Mock
-- 执行：mariadb -uroot -p123456 --default-character-set=utf8mb4 ruoyi-vue-pro < sql/mysql/hotel-t5.sql
-- 说明：
--   1. 本文件只包含 T5 负责的表与数据，不改 sql/mysql/hotel.sql。
--   2. 必须在 T1–T4 的表建好之后执行（本包最后做）。
--   3. ALTER 使用 MariaDB 的 IF NOT EXISTS，可重复执行；MySQL 8 请去掉 IF NOT EXISTS 后执行一次。
--   4. 不改 T1–T4 表的其它字段，只追加 store_id 与索引。
-- ============================================================

-- ------------------------------------------------------------
-- 1. 全业务表追加门店编号（T1–T4 已建表）
-- ------------------------------------------------------------
ALTER TABLE `hotel_room_type`        ADD COLUMN IF NOT EXISTS `store_id` bigint NOT NULL DEFAULT 0 COMMENT '门店编号' AFTER `tenant_id`;
ALTER TABLE `hotel_room`             ADD COLUMN IF NOT EXISTS `store_id` bigint NOT NULL DEFAULT 0 COMMENT '门店编号' AFTER `tenant_id`;
ALTER TABLE `hotel_guest`            ADD COLUMN IF NOT EXISTS `store_id` bigint NOT NULL DEFAULT 0 COMMENT '门店编号' AFTER `tenant_id`;
ALTER TABLE `hotel_order`            ADD COLUMN IF NOT EXISTS `store_id` bigint NOT NULL DEFAULT 0 COMMENT '门店编号' AFTER `tenant_id`;
ALTER TABLE `hotel_cleaning_task`    ADD COLUMN IF NOT EXISTS `store_id` bigint NOT NULL DEFAULT 0 COMMENT '门店编号' AFTER `tenant_id`;
ALTER TABLE `hotel_room_daily_rate`  ADD COLUMN IF NOT EXISTS `store_id` bigint NOT NULL DEFAULT 0 COMMENT '门店编号' AFTER `tenant_id`;
ALTER TABLE `hotel_folio`            ADD COLUMN IF NOT EXISTS `store_id` bigint NOT NULL DEFAULT 0 COMMENT '门店编号' AFTER `tenant_id`;
ALTER TABLE `hotel_folio_item`       ADD COLUMN IF NOT EXISTS `store_id` bigint NOT NULL DEFAULT 0 COMMENT '门店编号' AFTER `tenant_id`;
ALTER TABLE `hotel_payment_record`   ADD COLUMN IF NOT EXISTS `store_id` bigint NOT NULL DEFAULT 0 COMMENT '门店编号' AFTER `tenant_id`;
ALTER TABLE `hotel_shift_handover`   ADD COLUMN IF NOT EXISTS `store_id` bigint NOT NULL DEFAULT 0 COMMENT '门店编号' AFTER `tenant_id`;
ALTER TABLE `hotel_invoice`          ADD COLUMN IF NOT EXISTS `store_id` bigint NOT NULL DEFAULT 0 COMMENT '门店编号' AFTER `tenant_id`;
ALTER TABLE `hotel_business_date`    ADD COLUMN IF NOT EXISTS `store_id` bigint NOT NULL DEFAULT 0 COMMENT '门店编号' AFTER `tenant_id`;
ALTER TABLE `hotel_night_audit`      ADD COLUMN IF NOT EXISTS `store_id` bigint NOT NULL DEFAULT 0 COMMENT '门店编号' AFTER `tenant_id`;
ALTER TABLE `hotel_room_change_log`  ADD COLUMN IF NOT EXISTS `store_id` bigint NOT NULL DEFAULT 0 COMMENT '门店编号' AFTER `tenant_id`;
ALTER TABLE `hotel_channel`          ADD COLUMN IF NOT EXISTS `store_id` bigint NOT NULL DEFAULT 0 COMMENT '门店编号' AFTER `tenant_id`;
ALTER TABLE `hotel_rate_plan`        ADD COLUMN IF NOT EXISTS `store_id` bigint NOT NULL DEFAULT 0 COMMENT '门店编号' AFTER `tenant_id`;
ALTER TABLE `hotel_booking_hold`     ADD COLUMN IF NOT EXISTS `store_id` bigint NOT NULL DEFAULT 0 COMMENT '门店编号' AFTER `tenant_id`;
ALTER TABLE `hotel_member`           ADD COLUMN IF NOT EXISTS `store_id` bigint NOT NULL DEFAULT 0 COMMENT '门店编号' AFTER `tenant_id`;
ALTER TABLE `hotel_member_points_log` ADD COLUMN IF NOT EXISTS `store_id` bigint NOT NULL DEFAULT 0 COMMENT '门店编号' AFTER `tenant_id`;
ALTER TABLE `hotel_pay_order`        ADD COLUMN IF NOT EXISTS `store_id` bigint NOT NULL DEFAULT 0 COMMENT '门店编号' AFTER `tenant_id`;
ALTER TABLE `hotel_invoice_apply`    ADD COLUMN IF NOT EXISTS `store_id` bigint NOT NULL DEFAULT 0 COMMENT '门店编号' AFTER `tenant_id`;
ALTER TABLE `hotel_price_suggestion` ADD COLUMN IF NOT EXISTS `store_id` bigint NOT NULL DEFAULT 0 COMMENT '门店编号' AFTER `tenant_id`;
ALTER TABLE `hotel_price_backtest`   ADD COLUMN IF NOT EXISTS `store_id` bigint NOT NULL DEFAULT 0 COMMENT '门店编号' AFTER `tenant_id`;
ALTER TABLE `hotel_demo_seed`        ADD COLUMN IF NOT EXISTS `store_id` bigint NOT NULL DEFAULT 0 COMMENT '门店编号' AFTER `tenant_id`;

-- 门店过滤索引（只加在常用查询表，避免无效索引）
ALTER TABLE `hotel_room_type`       ADD KEY IF NOT EXISTS `idx_store` (`store_id`);
ALTER TABLE `hotel_room`            ADD KEY IF NOT EXISTS `idx_store` (`store_id`);
ALTER TABLE `hotel_guest`           ADD KEY IF NOT EXISTS `idx_store` (`store_id`);
ALTER TABLE `hotel_order`           ADD KEY IF NOT EXISTS `idx_store` (`store_id`);
ALTER TABLE `hotel_cleaning_task`   ADD KEY IF NOT EXISTS `idx_store` (`store_id`);
ALTER TABLE `hotel_folio`           ADD KEY IF NOT EXISTS `idx_store` (`store_id`);
ALTER TABLE `hotel_folio_item`      ADD KEY IF NOT EXISTS `idx_store` (`store_id`);
ALTER TABLE `hotel_payment_record`  ADD KEY IF NOT EXISTS `idx_store` (`store_id`);

-- ------------------------------------------------------------
-- 2. 门店
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `hotel_store` (
    `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '编号',
    `name`        varchar(100) NOT NULL COMMENT '门店名称',
    `code`        varchar(50)  NOT NULL COMMENT '门店编码',
    `province`    varchar(50)  NULL COMMENT '省',
    `city`        varchar(50)  NULL COMMENT '市',
    `district`    varchar(50)  NULL COMMENT '区',
    `address`     varchar(200) NULL COMMENT '详细地址',
    `phone`       varchar(30)  NULL COMMENT '前台电话',
    `legal_person` varchar(50) NULL COMMENT '法定代表人',
    `psb_code`    varchar(64)  NULL COMMENT '公安机关旅馆业编码（治安系统分配）',
    `check_in_time`  varchar(10) NULL DEFAULT '14:00' COMMENT '标准入住时间',
    `check_out_time` varchar(10) NULL DEFAULT '12:00' COMMENT '标准离店时间',
    `status`      tinyint      NOT NULL DEFAULT 0 COMMENT '状态：0 营业 1 停业',
    `sort`        int          NOT NULL DEFAULT 0 COMMENT '排序',
    `remark`      varchar(500) NULL COMMENT '备注',
    `tenant_id`   bigint       NOT NULL DEFAULT 0 COMMENT '租户编号',
    `creator`     varchar(64)  NULL DEFAULT '' COMMENT '创建者',
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`     varchar(64)  NULL DEFAULT '' COMMENT '更新者',
    `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     bit(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_code` (`code`, `tenant_id`, `deleted`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '酒店门店';

-- ------------------------------------------------------------
-- 3. 旅业治安上传记录（PSB，公安 Mock）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `hotel_psb_upload_log` (
    `id`           bigint       NOT NULL AUTO_INCREMENT COMMENT '编号',
    `store_id`     bigint       NOT NULL DEFAULT 0 COMMENT '门店编号',
    `order_id`     bigint       NULL COMMENT '订单编号',
    `guest_id`     bigint       NOT NULL COMMENT '住客编号',
    `id_card`      varchar(30)  NOT NULL COMMENT '身份证号',
    `guest_name`   varchar(50)  NOT NULL COMMENT '姓名',
    `room_no`      varchar(20)  NULL COMMENT '房间号',
    `action_type`  tinyint      NOT NULL COMMENT '动作：10 入住登记 20 退房注销 30 换房 40 信息更正',
    `biz_time`     datetime     NOT NULL COMMENT '业务发生时间',
    `status`       tinyint      NOT NULL DEFAULT 10 COMMENT '状态：10 待上传 20 上传成功 30 上传失败 40 无需上传',
    `retry_count`  int          NOT NULL DEFAULT 0 COMMENT '重试次数',
    `request_data` varchar(4000) NULL COMMENT '上报报文',
    `response_data` varchar(2000) NULL COMMENT '响应报文',
    `fail_reason`  varchar(500) NULL COMMENT '失败原因',
    `upload_time`  datetime     NULL COMMENT '最后上传时间',
    `tenant_id`    bigint       NOT NULL DEFAULT 0 COMMENT '租户编号',
    `creator`      varchar(64)  NULL DEFAULT '' COMMENT '创建者',
    `create_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`      varchar(64)  NULL DEFAULT '' COMMENT '更新者',
    `update_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`      bit(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_status` (`status`, `retry_count`),
    KEY `idx_order` (`order_id`),
    KEY `idx_guest` (`guest_id`),
    KEY `idx_store` (`store_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '旅馆业治安上传日志';

-- ------------------------------------------------------------
-- 4. 门锁发卡记录（Mock）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `hotel_lock_card_log` (
    `id`          bigint      NOT NULL AUTO_INCREMENT COMMENT '编号',
    `store_id`    bigint      NOT NULL DEFAULT 0 COMMENT '门店编号',
    `order_id`    bigint      NOT NULL COMMENT '订单编号',
    `room_id`     bigint      NOT NULL COMMENT '房间编号',
    `room_no`     varchar(20) NULL COMMENT '房间号',
    `card_no`     varchar(64) NULL COMMENT '房卡号',
    `action_type` tinyint     NOT NULL COMMENT '动作：10 发卡 20 补卡 30 销卡 40 续卡',
    `valid_from`  datetime    NULL COMMENT '卡有效起始',
    `valid_to`    datetime    NULL COMMENT '卡有效截止（默认离店日 12:00）',
    `status`      tinyint     NOT NULL DEFAULT 10 COMMENT '状态：10 待处理 20 成功 30 失败',
    `fail_reason` varchar(500) NULL COMMENT '失败原因',
    `handle_time` datetime    NULL COMMENT '处理时间',
    `tenant_id`   bigint      NOT NULL DEFAULT 0 COMMENT '租户编号',
    `creator`     varchar(64) NULL DEFAULT '' COMMENT '创建者',
    `create_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`     varchar(64) NULL DEFAULT '' COMMENT '更新者',
    `update_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     bit(1)      NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_order` (`order_id`),
    KEY `idx_store` (`store_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '酒店门锁发卡日志';

-- ------------------------------------------------------------
-- 5. 协议单位（AR 挂账主体）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `hotel_ar_account` (
    `id`             bigint         NOT NULL AUTO_INCREMENT COMMENT '编号',
    `store_id`       bigint         NOT NULL DEFAULT 0 COMMENT '门店编号',
    `name`           varchar(200)   NOT NULL COMMENT '单位名称',
    `code`           varchar(50)    NOT NULL COMMENT '单位编码',
    `tax_no`         varchar(50)    NULL COMMENT '税号',
    `contact_name`   varchar(50)    NULL COMMENT '联系人',
    `contact_mobile` varchar(20)    NULL COMMENT '联系手机',
    `credit_limit`   decimal(12,2)  NOT NULL DEFAULT 0 COMMENT '信用额度',
    `used_amount`    decimal(12,2)  NOT NULL DEFAULT 0 COMMENT '已用额度（未结挂账）',
    `settle_cycle`   tinyint        NOT NULL DEFAULT 30 COMMENT '结算周期：10 现结 20 周结 30 月结 40 季结',
    `status`         tinyint        NOT NULL DEFAULT 0 COMMENT '状态：0 正常 1 停用',
    `remark`         varchar(500)   NULL COMMENT '备注',
    `tenant_id`      bigint         NOT NULL DEFAULT 0 COMMENT '租户编号',
    `creator`        varchar(64)    NULL DEFAULT '' COMMENT '创建者',
    `create_time`    datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`        varchar(64)    NULL DEFAULT '' COMMENT '更新者',
    `update_time`    datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`        bit(1)         NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_code` (`code`, `tenant_id`, `deleted`),
    KEY `idx_store` (`store_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '酒店协议单位账户';

-- ------------------------------------------------------------
-- 6. AR 挂账明细
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `hotel_ar_ledger` (
    `id`           bigint         NOT NULL AUTO_INCREMENT COMMENT '编号',
    `store_id`     bigint         NOT NULL DEFAULT 0 COMMENT '门店编号',
    `ar_account_id` bigint        NOT NULL COMMENT '协议单位编号',
    `order_id`     bigint         NULL COMMENT '订单编号',
    `folio_id`     bigint         NULL COMMENT '账页编号',
    `amount`       decimal(12,2)  NOT NULL COMMENT '挂账金额（正数挂账，负数冲销）',
    `type`         tinyint        NOT NULL COMMENT '类型：10 挂账 20 冲销 30 结算',
    `biz_date`     date           NOT NULL COMMENT '营业日',
    `settlement_id` bigint        NULL COMMENT '所属结算单编号（已结算时）',
    `status`       tinyint        NOT NULL DEFAULT 10 COMMENT '状态：10 未结 20 已结',
    `operator_id`  bigint         NULL COMMENT '操作人',
    `remark`       varchar(500)   NULL COMMENT '备注',
    `tenant_id`    bigint         NOT NULL DEFAULT 0 COMMENT '租户编号',
    `creator`      varchar(64)    NULL DEFAULT '' COMMENT '创建者',
    `create_time`  datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`      varchar(64)    NULL DEFAULT '' COMMENT '更新者',
    `update_time`  datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`      bit(1)         NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_account` (`ar_account_id`, `status`),
    KEY `idx_order` (`order_id`),
    KEY `idx_store` (`store_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '酒店协议单位挂账明细';

-- ------------------------------------------------------------
-- 7. AR 结算单
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `hotel_ar_settlement` (
    `id`             bigint         NOT NULL AUTO_INCREMENT COMMENT '编号',
    `store_id`       bigint         NOT NULL DEFAULT 0 COMMENT '门店编号',
    `settlement_no`  varchar(32)    NOT NULL COMMENT '结算单号',
    `ar_account_id`  bigint         NOT NULL COMMENT '协议单位编号',
    `begin_date`     date           NOT NULL COMMENT '账期起',
    `end_date`       date           NOT NULL COMMENT '账期止',
    `total_amount`   decimal(12,2)  NOT NULL COMMENT '结算金额',
    `paid_amount`    decimal(12,2)  NOT NULL DEFAULT 0 COMMENT '已收金额',
    `status`         tinyint        NOT NULL DEFAULT 10 COMMENT '状态：10 待结算 20 部分收款 30 已结清',
    `settle_time`    datetime       NULL COMMENT '结清时间',
    `remark`         varchar(500)   NULL COMMENT '备注',
    `tenant_id`      bigint         NOT NULL DEFAULT 0 COMMENT '租户编号',
    `creator`        varchar(64)    NULL DEFAULT '' COMMENT '创建者',
    `create_time`    datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`        varchar(64)    NULL DEFAULT '' COMMENT '更新者',
    `update_time`    datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`        bit(1)         NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_no` (`settlement_no`),
    KEY `idx_account` (`ar_account_id`),
    KEY `idx_store` (`store_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '酒店协议单位结算单';

-- ------------------------------------------------------------
-- 8. OTA 渠道对接配置 / 订单映射
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `hotel_ota_config` (
    `id`           bigint       NOT NULL AUTO_INCREMENT COMMENT '编号',
    `store_id`     bigint       NOT NULL DEFAULT 0 COMMENT '门店编号',
    `channel_id`   bigint       NOT NULL COMMENT '渠道编号（hotel_channel）',
    `ota_code`     varchar(50)  NOT NULL COMMENT 'OTA 编码：CTRIP/MEITUAN/FLIGGY/QUNAR',
    `app_key`      varchar(200) NULL COMMENT 'AppKey',
    `app_secret`   varchar(500) NULL COMMENT 'AppSecret（加密存储）',
    `api_url`      varchar(500) NULL COMMENT '接口地址',
    `push_rate`    bit(1)       NOT NULL DEFAULT b'1' COMMENT '是否推送房价库存',
    `pull_order`   bit(1)       NOT NULL DEFAULT b'1' COMMENT '是否拉取订单',
    `sync_interval` int         NOT NULL DEFAULT 300 COMMENT '同步间隔（秒）',
    `mock_enabled` bit(1)       NOT NULL DEFAULT b'1' COMMENT '是否启用模拟器（默认开）',
    `status`       tinyint      NOT NULL DEFAULT 0 COMMENT '状态：0 启用 1 停用',
    `last_sync_time` datetime   NULL COMMENT '最后同步时间',
    `remark`       varchar(500) NULL COMMENT '备注',
    `tenant_id`    bigint       NOT NULL DEFAULT 0 COMMENT '租户编号',
    `creator`      varchar(64)  NULL DEFAULT '' COMMENT '创建者',
    `create_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`      varchar(64)  NULL DEFAULT '' COMMENT '更新者',
    `update_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`      bit(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_channel` (`channel_id`, `ota_code`, `tenant_id`, `deleted`),
    KEY `idx_store` (`store_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '酒店 OTA 对接配置';

CREATE TABLE IF NOT EXISTS `hotel_ota_order` (
    `id`            bigint       NOT NULL AUTO_INCREMENT COMMENT '编号',
    `store_id`      bigint       NOT NULL DEFAULT 0 COMMENT '门店编号',
    `ota_code`      varchar(50)  NOT NULL COMMENT 'OTA 编码',
    `ota_order_no`  varchar(100) NOT NULL COMMENT 'OTA 订单号',
    `order_id`      bigint       NULL COMMENT '本地订单编号',
    `sync_status`   tinyint      NOT NULL DEFAULT 10 COMMENT '同步状态：10 待处理 20 已建单 30 已确认 40 已取消 50 失败',
    `raw_data`      varchar(4000) NULL COMMENT '原始报文',
    `fail_reason`   varchar(500) NULL COMMENT '失败原因',
    `sync_time`     datetime     NULL COMMENT '最后同步时间',
    `tenant_id`     bigint       NOT NULL DEFAULT 0 COMMENT '租户编号',
    `creator`       varchar(64)  NULL DEFAULT '' COMMENT '创建者',
    `create_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`       varchar(64)  NULL DEFAULT '' COMMENT '更新者',
    `update_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`       bit(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_ota_order` (`ota_code`, `ota_order_no`, `tenant_id`, `deleted`),
    KEY `idx_order` (`order_id`),
    KEY `idx_store` (`store_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '酒店 OTA 订单映射';

-- ------------------------------------------------------------
-- 9. 演示门店与演示数据
-- ------------------------------------------------------------
INSERT INTO `hotel_store` (`id`, `name`, `code`, `province`, `city`, `district`, `address`, `phone`, `legal_person`, `psb_code`, `check_in_time`, `check_out_time`, `status`, `sort`, `remark`, `tenant_id`)
VALUES
    (1, '云栖湖畔度假酒店（总店）', 'HQ001', '浙江省', '杭州市', '西湖区', '文三路 100 号', '0571-88880001', '张经理', 'PSB-HZ-0001', '14:00', '12:00', 0, 1, 'T5 演示总店', 1),
    (2, '云栖湖畔度假酒店·西溪分店', 'BR002', '浙江省', '杭州市', '余杭区', '西溪路 588 号', '0571-88880002', '李经理', 'PSB-HZ-0002', '14:00', '12:00', 0, 2, 'T5 演示分店', 1)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`), `psb_code` = VALUES(`psb_code`);

-- 历史数据归属总店（0 表示未分配，统一归到总店，演示切店才有对比）
UPDATE `hotel_room_type` SET `store_id` = 1 WHERE `store_id` = 0;
UPDATE `hotel_room` SET `store_id` = 1 WHERE `store_id` = 0;
UPDATE `hotel_guest` SET `store_id` = 1 WHERE `store_id` = 0;
UPDATE `hotel_order` SET `store_id` = 1 WHERE `store_id` = 0;
UPDATE `hotel_cleaning_task` SET `store_id` = 1 WHERE `store_id` = 0;
UPDATE `hotel_folio` SET `store_id` = 1 WHERE `store_id` = 0;
UPDATE `hotel_folio_item` SET `store_id` = 1 WHERE `store_id` = 0;
UPDATE `hotel_payment_record` SET `store_id` = 1 WHERE `store_id` = 0;
UPDATE `hotel_operation_log` SET `store_id` = 1 WHERE `store_id` IS NULL OR `store_id` = 0;
UPDATE `hotel_channel` SET `store_id` = 1 WHERE `store_id` = 0;
UPDATE `hotel_rate_plan` SET `store_id` = 1 WHERE `store_id` = 0;

-- 分店演示房型与房间（与总店房间号段区分，切店效果直观）
INSERT INTO `hotel_room_type` (`id`, `name`, `code`, `base_price`, `max_occupancy`, `area`, `amenities`, `description`, `status`, `sort`, `tenant_id`, `store_id`)
VALUES
    (9101, '西溪雅致大床房', 'XR-KING', 468.00, 2, 32, 'WiFi,空调,浴缸', 'T5 分店演示房型', 0, 10, 1, 2),
    (9102, '西溪家庭双床房', 'XR-TWIN', 528.00, 3, 38, 'WiFi,空调,冰箱', 'T5 分店演示房型', 0, 11, 1, 2)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);

INSERT INTO `hotel_room` (`id`, `room_no`, `room_type_id`, `floor`, `area`, `price`, `status`, `lock_status`, `sort`, `remark`, `tenant_id`, `store_id`)
VALUES
    (9201, 'B201', 9101, 2, 32, 468.00, 10, 0, 1, 'T5 分店演示房', 1, 2),
    (9202, 'B202', 9101, 2, 32, 468.00, 10, 0, 2, 'T5 分店演示房', 1, 2),
    (9203, 'B203', 9102, 2, 38, 528.00, 10, 0, 3, 'T5 分店演示房', 1, 2),
    (9204, 'B301', 9102, 3, 38, 528.00, 10, 0, 4, 'T5 分店演示房', 1, 2)
ON DUPLICATE KEY UPDATE `room_no` = VALUES(`room_no`);

-- 协议单位演示数据（额度 10000，方便演示挂账与超额）
INSERT INTO `hotel_ar_account` (`id`, `store_id`, `name`, `code`, `tax_no`, `contact_name`, `contact_mobile`, `credit_limit`, `used_amount`, `settle_cycle`, `status`, `remark`, `tenant_id`)
VALUES
    (9301, 1, '杭州云栖科技有限公司', 'AR-9301', '91330100MA2XXXXX1A', '王经理', '13800001111', 10000.00, 0.00, 30, 0, 'T5 演示协议单位（月结）', 1)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`), `credit_limit` = VALUES(`credit_limit`);

-- OTA 配置演示（携程，Mock 开启）
INSERT INTO `hotel_ota_config` (`id`, `store_id`, `channel_id`, `ota_code`, `app_key`, `app_secret`, `api_url`, `push_rate`, `pull_order`, `sync_interval`, `mock_enabled`, `status`, `remark`, `tenant_id`)
VALUES
    (9401, 1, 2, 'CTRIP', 'mock-app-key', 'mock-app-secret', 'https://openapi.ctrip.com/mock', b'1', b'1', 300, b'1', 0, 'T5 演示 OTA 配置（Mock）', 1)
ON DUPLICATE KEY UPDATE `ota_code` = VALUES(`ota_code`), `mock_enabled` = b'1';

-- 公安/门锁演示记录：保证首次进入页面就有成功样例（对应一条真实订单）
INSERT INTO `hotel_psb_upload_log` (`store_id`, `order_id`, `guest_id`, `id_card`, `guest_name`, `room_no`, `action_type`, `biz_time`, `status`, `retry_count`, `request_data`, `response_data`, `upload_time`, `tenant_id`)
SELECT 1, o.id, o.guest_id, IFNULL(g.id_card, '330281199203151234'), IFNULL(g.name, '演示住客'), r.room_no,
       10, NOW(), 20, 0, '{"mock":true,"action":"checkIn"}', '{"receiptNo":"PSB-MOCK-SEED","success":true}', NOW(), 1
FROM `hotel_order` o
JOIN `hotel_room` r ON r.id = o.room_id
LEFT JOIN `hotel_guest` g ON g.id = o.guest_id
WHERE o.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `hotel_psb_upload_log`)
ORDER BY o.id LIMIT 1;

INSERT INTO `hotel_lock_card_log` (`store_id`, `order_id`, `room_id`, `room_no`, `card_no`, `action_type`, `valid_from`, `valid_to`, `status`, `handle_time`, `tenant_id`)
SELECT 1, o.id, o.room_id, r.room_no, CONCAT('CARD-MOCK-', o.id), 10, NOW(), DATE_ADD(o.check_out_date, INTERVAL 12 HOUR), 20, NOW(), 1
FROM `hotel_order` o
JOIN `hotel_room` r ON r.id = o.room_id
WHERE o.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `hotel_lock_card_log`)
ORDER BY o.id LIMIT 1;

-- ------------------------------------------------------------
-- 10. 菜单与权限（号段 20450–20499）
-- ------------------------------------------------------------
DELETE FROM `system_menu` WHERE `id` BETWEEN 20450 AND 20499;

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`)
VALUES
    (20450, '商业化', '', 1, 10, 0, '/hotel-commerce', 'ep:shop', NULL, NULL, 0, b'1', b'1', b'1', 'T5'),
    (20451, '门店管理', 'hotel:store:query', 2, 1, 20450, 'store', 'ep:office-building', 'hotel/store/index', 'HotelStore', 0, b'1', b'1', b'1', 'T5'),
    (20452, '公安上传', 'hotel:psb:query', 2, 2, 20450, 'psb', 'ep:upload', 'hotel/psb/index', 'HotelPsb', 0, b'1', b'1', b'1', 'T5'),
    (20453, '门锁管理', 'hotel:lock:query', 2, 3, 20450, 'lock', 'ep:key', 'hotel/lock/index', 'HotelLock', 0, b'1', b'1', b'1', 'T5'),
    (20454, 'OTA 配置', 'hotel:ota:query', 2, 4, 20450, 'ota-config', 'ep:connection', 'hotel/otaConfig/index', 'HotelOtaConfig', 0, b'1', b'1', b'1', 'T5'),
    (20455, 'OTA 订单', 'hotel:ota:query', 2, 5, 20450, 'ota-order', 'ep:list', 'hotel/otaOrder/index', 'HotelOtaOrder', 0, b'1', b'1', b'1', 'T5'),
    (20456, '协议单位', 'hotel:ar:query', 2, 6, 20450, 'ar-account', 'ep:wallet', 'hotel/arAccount/index', 'HotelArAccount', 0, b'1', b'1', b'1', 'T5'),
    (20457, '挂账明细', 'hotel:ar:query', 2, 7, 20450, 'ar-ledger', 'ep:document', 'hotel/arLedger/index', 'HotelArLedger', 0, b'1', b'1', b'1', 'T5'),
    (20458, '结算单', 'hotel:ar:query', 2, 8, 20450, 'ar-settlement', 'ep:money', 'hotel/arSettlement/index', 'HotelArSettlement', 0, b'1', b'1', b'1', 'T5'),

    (20470, '门店查询', 'hotel:store:query', 3, 1, 20451, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'T5'),
    (20471, '门店创建', 'hotel:store:create', 3, 2, 20451, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'T5'),
    (20472, '门店更新', 'hotel:store:update', 3, 3, 20451, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'T5'),
    (20473, '门店删除', 'hotel:store:delete', 3, 4, 20451, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'T5'),
    (20474, '公安上传查询', 'hotel:psb:query', 3, 1, 20452, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'T5'),
    (20475, '公安上传重试', 'hotel:psb:update', 3, 2, 20452, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'T5'),
    (20476, '门锁日志查询', 'hotel:lock:query', 3, 1, 20453, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'T5'),
    (20477, '发卡/补卡', 'hotel:lock:create', 3, 2, 20453, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'T5'),
    (20478, '销卡/续卡', 'hotel:lock:update', 3, 3, 20453, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'T5'),
    (20479, 'OTA 配置查询', 'hotel:ota:query', 3, 1, 20454, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'T5'),
    (20480, 'OTA 配置维护', 'hotel:ota:create', 3, 2, 20454, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'T5'),
    (20481, 'OTA 同步/推价', 'hotel:ota:update', 3, 3, 20454, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'T5'),
    (20482, '协议单位查询', 'hotel:ar:query', 3, 1, 20456, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'T5'),
    (20483, '协议单位维护/挂账', 'hotel:ar:create', 3, 2, 20456, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'T5'),
    (20484, '结算收款', 'hotel:ar:update', 3, 3, 20456, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'T5');

DELETE FROM `system_role_menu` WHERE `menu_id` BETWEEN 20450 AND 20499;
INSERT INTO `system_role_menu` (`role_id`, `menu_id`, `creator`, `tenant_id`)
SELECT 1, `id`, 'T5', 1 FROM `system_menu` WHERE `id` BETWEEN 20450 AND 20499;
