-- ============================================================
-- T1 卖房引擎（渠道 + 房价计划 + 预订占位 + 智能定价 + 回测）
-- 号段：菜单 20300-20349 / 错误码 1-003-004-4201~4213、4221~4222
-- 说明：本文件独立于 sql/mysql/hotel.sql，由派发者按 T1→T5 顺序合并导入
-- 幂等：只 DROP 本包自己的表，不动任何既有表数据
-- ============================================================

SET NAMES utf8mb4;

-- ------------------------------------------------------------
-- 一、hotel_order 加字段（T1 只加这三个，其余字段归 T2）
-- ------------------------------------------------------------
ALTER TABLE `hotel_order`
    ADD COLUMN `rate_plan_id` bigint NULL COMMENT '房价计划编号' AFTER `order_type`,
    ADD COLUMN `channel_id`   bigint NULL COMMENT '渠道编号' AFTER `rate_plan_id`,
    ADD COLUMN `member_id`    bigint NULL COMMENT '会员编号' AFTER `channel_id`,
    MODIFY COLUMN `status` tinyint NOT NULL DEFAULT 10 COMMENT '订单状态：10 待支付 11 待确认 20 已确认 30 已入住 40 已退房 50 已取消 60 已超时关闭',
    ADD KEY `idx_channel` (`channel_id`),
    ADD KEY `idx_member` (`member_id`);

-- ------------------------------------------------------------
-- 二、渠道
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `hotel_channel`;
CREATE TABLE `hotel_channel` (
    `id`              bigint       NOT NULL AUTO_INCREMENT COMMENT '编号',
    `name`            varchar(100) NOT NULL COMMENT '渠道名称',
    `code`            varchar(50)  NOT NULL COMMENT '渠道编码，如 DIRECT/CTRIP/MEITUAN/CORP',
    `type`            tinyint      NOT NULL DEFAULT 10 COMMENT '类型：10 直销 20 OTA 30 协议 40 会员 50 门店',
    `commission_rate` decimal(5,2) NOT NULL DEFAULT 0 COMMENT '佣金比例（%）',
    `status`          tinyint      NOT NULL DEFAULT 0 COMMENT '状态：0 开启 1 关闭',
    `sort`            int          NOT NULL DEFAULT 0 COMMENT '排序',
    `remark`          varchar(500) NULL COMMENT '备注',
    `tenant_id`       bigint       NOT NULL DEFAULT 0 COMMENT '租户编号',
    `creator`         varchar(64)  NULL DEFAULT '' COMMENT '创建者',
    `create_time`     datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`         varchar(64)  NULL DEFAULT '' COMMENT '更新者',
    `update_time`     datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         bit(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_code` (`code`, `tenant_id`, `deleted`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '酒店渠道';

-- ------------------------------------------------------------
-- 三、房价计划（Rate Plan）
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `hotel_rate_plan`;
CREATE TABLE `hotel_rate_plan` (
    `id`                 bigint         NOT NULL AUTO_INCREMENT COMMENT '编号',
    `name`               varchar(100)   NOT NULL COMMENT '房价计划名称，如「直销含双早不可取消」',
    `code`               varchar(50)    NOT NULL COMMENT '编码',
    `channel_id`         bigint         NOT NULL COMMENT '渠道编号',
    `room_type_id`       bigint         NULL COMMENT '适用房型（为空表示全部房型）',
    `price_rule_type`    tinyint        NOT NULL DEFAULT 10 COMMENT '价格规则：10 按日历价 20 日历价折扣 30 日历价减额 40 固定价',
    `price_rule_value`   decimal(10,2)  NULL COMMENT '规则值：折扣率(如90表示9折)/减额/固定价',
    `breakfast`          tinyint        NOT NULL DEFAULT 0 COMMENT '早餐：0 不含 1 单早 2 双早',
    `min_nights`         int            NOT NULL DEFAULT 1 COMMENT '最少入住晚数',
    `max_nights`         int            NULL COMMENT '最多入住晚数（空为不限）',
    `min_advance_hours`  int            NULL COMMENT '最少提前预订小时数（空为不限）',
    `cancel_policy`      tinyint        NOT NULL DEFAULT 10 COMMENT '取消政策：10 免费取消 20 入住前N小时可免费取消 30 不可取消',
    `cancel_free_hours`  int            NULL COMMENT '免费取消截止小时数（cancel_policy=20 时必填）',
    `need_deposit`       bit(1)         NOT NULL DEFAULT b'0' COMMENT '是否需预付',
    `deposit_rate`       decimal(5,2)   NULL COMMENT '预付比例（%）',
    `status`             tinyint        NOT NULL DEFAULT 0 COMMENT '状态：0 开启 1 关闭',
    `sort`               int            NOT NULL DEFAULT 0 COMMENT '排序',
    `remark`             varchar(500)   NULL COMMENT '备注',
    `tenant_id`          bigint         NOT NULL DEFAULT 0 COMMENT '租户编号',
    `creator`            varchar(64)    NULL DEFAULT '' COMMENT '创建者',
    `create_time`        datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`            varchar(64)    NULL DEFAULT '' COMMENT '更新者',
    `update_time`        datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`            bit(1)         NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_code` (`code`, `tenant_id`, `deleted`),
    KEY `idx_channel` (`channel_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '酒店房价计划';

-- ------------------------------------------------------------
-- 四、预订占位（防超售：每日一行，同一 hold_no 多行=一个区间占位）
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `hotel_booking_hold`;
CREATE TABLE `hotel_booking_hold` (
    `id`           bigint      NOT NULL AUTO_INCREMENT COMMENT '编号',
    `hold_no`      varchar(64) NOT NULL COMMENT '占位号（UUID）',
    `room_type_id` bigint      NOT NULL COMMENT '房型编号',
    `biz_date`     date        NOT NULL COMMENT '日期',
    `room_count`   int         NOT NULL DEFAULT 1 COMMENT '占房数',
    `order_id`     bigint      NULL COMMENT '转化后的订单编号',
    `status`       tinyint     NOT NULL DEFAULT 10 COMMENT '状态：10 占用中 20 已转化 30 已释放 40 已过期',
    `expire_time`  datetime    NOT NULL COMMENT '过期时间（一般 15 分钟后自动释放）',
    `tenant_id`    bigint      NOT NULL DEFAULT 0 COMMENT '租户编号',
    `creator`      varchar(64) NULL DEFAULT '' COMMENT '创建者',
    `create_time`  datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`      varchar(64) NULL DEFAULT '' COMMENT '更新者',
    `update_time`  datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`      bit(1)      NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_hold_no` (`hold_no`, `biz_date`),
    KEY `idx_type_date` (`room_type_id`, `biz_date`),
    KEY `idx_expire` (`expire_time`, `status`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '酒店预订占位';

-- ------------------------------------------------------------
-- 五、智能定价建议（核心创新点）
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `hotel_price_suggestion`;
CREATE TABLE `hotel_price_suggestion` (
    `id`              bigint         NOT NULL AUTO_INCREMENT COMMENT '编号',
    `room_type_id`    bigint         NOT NULL COMMENT '房型编号',
    `biz_date`        date           NOT NULL COMMENT '业务日期',
    `base_price`      decimal(10,2)  NOT NULL COMMENT '基线价（日历价）',
    `suggested_price` decimal(10,2)  NOT NULL COMMENT '建议售价',
    `occ_factor`      decimal(6,3)   NOT NULL DEFAULT 1 COMMENT '出租率系数',
    `advance_factor`  decimal(6,3)   NOT NULL DEFAULT 1 COMMENT '提前期系数',
    `weekend_factor`  decimal(6,3)   NOT NULL DEFAULT 1 COMMENT '周末/节假日系数',
    `hist_occupancy`  decimal(6,3)   NULL COMMENT '历史同期出租率（0-1）',
    `booking_pace`    decimal(6,3)   NULL COMMENT '库存进度（已售/总量）',
    `lead_days`       int            NULL COMMENT '提前天数',
    `reason`          varchar(500)   NULL COMMENT '建议理由（人话，给老板看）',
    `status`          tinyint        NOT NULL DEFAULT 10 COMMENT '状态：10 建议中 20 已采纳 30 已忽略',
    `adopted_price`   decimal(10,2)  NULL COMMENT '实际采纳价',
    `tenant_id`       bigint         NOT NULL DEFAULT 0 COMMENT '租户编号',
    `creator`         varchar(64)    NULL DEFAULT '' COMMENT '创建者',
    `create_time`     datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`         varchar(64)    NULL DEFAULT '' COMMENT '更新者',
    `update_time`     datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         bit(1)         NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_type_date` (`room_type_id`, `biz_date`, `tenant_id`, `deleted`),
    KEY `idx_date` (`biz_date`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '酒店智能定价建议';

-- ------------------------------------------------------------
-- 六、定价回测（答辩用：原价 vs 建议价 RevPAR）
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `hotel_price_backtest`;
CREATE TABLE `hotel_price_backtest` (
    `id`            bigint         NOT NULL AUTO_INCREMENT COMMENT '编号',
    `name`          varchar(100)   NOT NULL COMMENT '回测名称',
    `room_type_id`  bigint         NULL COMMENT '房型编号（空=全部房型）',
    `begin_date`    date           NOT NULL COMMENT '回测区间起',
    `end_date`      date           NOT NULL COMMENT '回测区间止',
    `actual_revpar` decimal(12,2)  NULL COMMENT '按原价的 RevPAR',
    `model_revpar`  decimal(12,2)  NULL COMMENT '按建议价的 RevPAR',
    `uplift_rate`   decimal(8,4)   NULL COMMENT '提升比例（%）',
    `detail`        varchar(4000)  NULL COMMENT '逐日明细 JSON',
    `tenant_id`     bigint         NOT NULL DEFAULT 0 COMMENT '租户编号',
    `creator`       varchar(64)    NULL DEFAULT '' COMMENT '创建者',
    `create_time`   datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`       varchar(64)    NULL DEFAULT '' COMMENT '更新者',
    `update_time`   datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`       bit(1)         NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_date` (`begin_date`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '酒店定价回测';

-- ------------------------------------------------------------
-- 七、种子数据（答辩演示用：直销渠道 + 9 折房价计划）
--    房价计划挂在 SINGLE 房型上，日历价 300 × 90% = 270，对应 DoD3
-- ------------------------------------------------------------
INSERT INTO `hotel_channel` (`name`, `code`, `type`, `commission_rate`, `status`, `sort`, `remark`, `tenant_id`, `creator`, `updater`)
VALUES ('直销（官网/小程序）', 'DIRECT', 10, 0.00, 0, 1, '零佣金自有渠道', 1, 'admin', 'admin'),
       ('携程', 'CTRIP', 20, 15.00, 0, 2, 'OTA 渠道（演示）', 1, 'admin', 'admin'),
       ('协议单位', 'CORP', 30, 0.00, 0, 3, '企业协议价', 1, 'admin', 'admin');

INSERT INTO `hotel_rate_plan` (`name`, `code`, `channel_id`, `room_type_id`, `price_rule_type`, `price_rule_value`,
                              `breakfast`, `min_nights`, `cancel_policy`, `cancel_free_hours`, `need_deposit`,
                              `deposit_rate`, `status`, `sort`, `remark`, `tenant_id`, `creator`, `updater`)
VALUES ('直销 9 折·含双早·不可取消', 'DIRECT90',
        (SELECT id FROM `hotel_channel` WHERE `code` = 'DIRECT' AND `deleted` = b'0' LIMIT 1),
        (SELECT id FROM `hotel_room_type` WHERE `code` = 'SINGLE' AND `deleted` = b'0' LIMIT 1),
        20, 90.00, 2, 1, 30, NULL, b'1', 100.00, 0, 1, 'DoD3 算价样例：日历价 × 90%', 1, 'admin', 'admin'),
       ('直销标准价·免费取消', 'DIRECTBAR',
        (SELECT id FROM `hotel_channel` WHERE `code` = 'DIRECT' AND `deleted` = b'0' LIMIT 1),
        NULL, 10, NULL, 0, 1, 20, 24, b'0', NULL, 0, 2, '按日历价销售', 1, 'admin', 'admin');

-- ------------------------------------------------------------
-- 八、菜单与权限（ID 段 20300-20349）
-- ------------------------------------------------------------
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`)
VALUES (20301, '渠道管理', 'hotel:channel:query', 2, 21, 20000, 'channel', 'ep:connection', 'hotel/channel/index', 'HotelChannel', 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
       (20302, '房价计划', 'hotel:rate-plan:query', 2, 22, 20000, 'rate-plan', 'ep:price-tag', 'hotel/ratePlan/index', 'HotelRatePlan', 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
       (20306, '智能定价', 'hotel:pricing:query', 2, 23, 20000, 'pricing', 'ep:trend-charts', 'hotel/pricing/index', 'HotelPricing', 0, b'1', b'1', b'1', 'admin', 'admin', b'0');

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`)
VALUES (20311, '渠道查询', 'hotel:channel:query', 3, 1, 20301, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
       (20312, '渠道创建', 'hotel:channel:create', 3, 2, 20301, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
       (20313, '渠道更新', 'hotel:channel:update', 3, 3, 20301, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
       (20314, '渠道删除', 'hotel:channel:delete', 3, 4, 20301, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),

       (20321, '房价计划查询', 'hotel:rate-plan:query', 3, 1, 20302, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
       (20322, '房价计划创建', 'hotel:rate-plan:create', 3, 2, 20302, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
       (20323, '房价计划更新', 'hotel:rate-plan:update', 3, 3, 20302, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
       (20324, '房价计划删除', 'hotel:rate-plan:delete', 3, 4, 20302, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),

       (20331, '定价建议查询', 'hotel:pricing:query', 3, 1, 20306, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
       (20332, '生成定价建议', 'hotel:pricing:create', 3, 2, 20306, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
       (20333, '采纳/忽略建议', 'hotel:pricing:update', 3, 3, 20306, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
