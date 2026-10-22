-- ============================================================
-- 酒店业务模块：表结构 + 菜单权限 + 演示数据
-- 执行：mariadb -uroot -p123456 --default-character-set=utf8mb4 ruoyi-vue-pro < sql/mysql/hotel.sql
-- ============================================================

-- ----------------------------
-- 房型
-- ----------------------------
DROP TABLE IF EXISTS `hotel_room_type`;
CREATE TABLE `hotel_room_type` (
    `id`            bigint       NOT NULL AUTO_INCREMENT COMMENT '编号',
    `name`          varchar(100) NOT NULL COMMENT '房型名称',
    `code`          varchar(50)  NOT NULL COMMENT '房型编码',
    `base_price`    decimal(10, 2) NOT NULL COMMENT '基础价格',
    `max_occupancy` int          NOT NULL DEFAULT 2 COMMENT '最大入住人数',
    `area`          int          NULL COMMENT '面积（平方米）',
    `amenities`     varchar(500) NULL COMMENT '设施，逗号分隔',
    `description`   varchar(500) NULL COMMENT '描述',
    `status`        tinyint      NOT NULL DEFAULT 0 COMMENT '状态：0 开启 1 关闭',
    `sort`          int          NOT NULL DEFAULT 0 COMMENT '排序',
    `tenant_id`     bigint       NOT NULL DEFAULT 0 COMMENT '租户编号',
    `creator`       varchar(64)  NULL DEFAULT '' COMMENT '创建者',
    `create_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`       varchar(64)  NULL DEFAULT '' COMMENT '更新者',
    `update_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`       bit(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_code` (`code`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '酒店房型';

-- ----------------------------
-- 房间
-- ----------------------------
DROP TABLE IF EXISTS `hotel_room`;
CREATE TABLE `hotel_room` (
    `id`           bigint         NOT NULL AUTO_INCREMENT COMMENT '编号',
    `room_no`      varchar(20)    NOT NULL COMMENT '房间号',
    `room_type_id` bigint         NOT NULL COMMENT '房型编号',
    `floor`        int            NULL DEFAULT 0 COMMENT '楼层',
    `area`         int            NULL COMMENT '面积',
    `price`        decimal(10, 2) NOT NULL COMMENT '售价',
    `status`       tinyint        NOT NULL DEFAULT 10 COMMENT '房态：10 空闲 20 已入住 30 待清洁 40 维修中',
    `remark`       varchar(500)   NULL COMMENT '备注',
    `tenant_id`    bigint         NOT NULL DEFAULT 0 COMMENT '租户编号',
    `creator`      varchar(64)    NULL DEFAULT '' COMMENT '创建者',
    `create_time`  datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`      varchar(64)    NULL DEFAULT '' COMMENT '更新者',
    `update_time`  datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`      bit(1)         NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_room_no` (`room_no`),
    KEY `idx_status` (`status`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '酒店房间';

-- ----------------------------
-- 住客
-- ----------------------------
DROP TABLE IF EXISTS `hotel_guest`;
CREATE TABLE `hotel_guest` (
    `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '编号',
    `name`        varchar(50)  NOT NULL COMMENT '姓名',
    `mobile`      varchar(20)  NULL COMMENT '手机号',
    `id_card`     varchar(20)  NULL COMMENT '身份证号',
    `email`       varchar(100) NULL COMMENT '邮箱',
    `sex`         tinyint      NULL COMMENT '性别',
    `birthday`    date         NULL COMMENT '出生日期',
    `remark`      varchar(500) NULL COMMENT '备注',
    `tenant_id`   bigint       NOT NULL DEFAULT 0 COMMENT '租户编号',
    `creator`     varchar(64)  NULL DEFAULT '' COMMENT '创建者',
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`     varchar(64)  NULL DEFAULT '' COMMENT '更新者',
    `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     bit(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_mobile` (`mobile`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '酒店住客';

-- ----------------------------
-- 订单
-- ----------------------------
DROP TABLE IF EXISTS `hotel_order`;
CREATE TABLE `hotel_order` (
    `id`                    bigint         NOT NULL AUTO_INCREMENT COMMENT '编号',
    `order_no`              varchar(32)    NOT NULL COMMENT '订单号',
    `guest_id`              bigint         NOT NULL COMMENT '住客编号',
    `room_id`               bigint         NOT NULL COMMENT '房间编号',
    `check_in_date`         date           NOT NULL COMMENT '入住日期',
    `check_out_date`        date           NOT NULL COMMENT '离店日期',
    `nights`                int            NOT NULL DEFAULT 1 COMMENT '晚数',
    `amount`                decimal(10, 2) NOT NULL DEFAULT 0 COMMENT '订单金额',
    `paid_amount`           decimal(10, 2) NOT NULL DEFAULT 0 COMMENT '已收金额',
    `status`                tinyint        NOT NULL DEFAULT 10 COMMENT '订单状态：10 待确认 20 已确认 30 已入住 40 已退房 50 已取消',
    `payment_status`        tinyint        NOT NULL DEFAULT 10 COMMENT '支付状态：10 未支付 20 部分 30 已支付 40 已退款',
    `payment_method`        tinyint        NULL COMMENT '支付方式：1 现金 2 银行卡 3 微信 4 支付宝 5 在线',
    `invoice_no`            varchar(50)    NULL COMMENT '发票号',
    `invoice_requested`     bit(1)         NOT NULL DEFAULT b'0' COMMENT '是否需要发票',
    `remark`                varchar(500)   NULL COMMENT '备注',
    `actual_check_in_time`  datetime       NULL COMMENT '实际入住时间',
    `actual_check_out_time` datetime       NULL COMMENT '实际离店时间',
    `tenant_id`             bigint         NOT NULL DEFAULT 0 COMMENT '租户编号',
    `creator`               varchar(64)    NULL DEFAULT '' COMMENT '创建者',
    `create_time`           datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`               varchar(64)    NULL DEFAULT '' COMMENT '更新者',
    `update_time`           datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`               bit(1)         NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_order_no` (`order_no`),
    KEY `idx_room_status` (`room_id`, `status`),
    KEY `idx_dates` (`check_in_date`, `check_out_date`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '酒店订单';

-- ----------------------------
-- 清洁任务
-- ----------------------------
DROP TABLE IF EXISTS `hotel_cleaning_task`;
CREATE TABLE `hotel_cleaning_task` (
    `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '编号',
    `room_id`     bigint       NOT NULL COMMENT '房间编号',
    `type`        tinyint      NOT NULL DEFAULT 10 COMMENT '类型：10 退房清洁 20 日常清洁 30 维修后清洁',
    `status`      tinyint      NOT NULL DEFAULT 10 COMMENT '状态：10 待处理 20 清洁中 30 已完成 40 已验收',
    `assignee_id` bigint       NULL COMMENT '保洁人员编号',
    `plan_date`   date         NOT NULL COMMENT '计划日期',
    `finish_time` datetime     NULL COMMENT '完成时间',
    `remark`      varchar(500) NULL COMMENT '备注',
    `tenant_id`   bigint       NOT NULL DEFAULT 0 COMMENT '租户编号',
    `creator`     varchar(64)  NULL DEFAULT '' COMMENT '创建者',
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`     varchar(64)  NULL DEFAULT '' COMMENT '更新者',
    `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     bit(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_room_status` (`room_id`, `status`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '酒店清洁任务';

-- ============================================================
-- 演示数据（租户 1）
-- ============================================================
INSERT INTO `hotel_room_type` (`name`, `code`, `base_price`, `max_occupancy`, `area`, `amenities`, `description`, `status`, `sort`, `tenant_id`, `creator`, `updater`)
VALUES ('单人间', 'SINGLE', 268.00, 1, 22, 'WiFi,空调,电视,独立卫浴', '一张 1.5 米大床，适合单人差旅', 0, 1, 1, 'admin', 'admin'),
       ('标准双人间', 'DOUBLE', 398.00, 2, 30, 'WiFi,空调,电视,独立卫浴,早餐', '两张 1.2 米单床，含双份早餐', 0, 2, 1, 'admin', 'admin'),
       ('豪华双床房', 'TWIN', 528.00, 2, 38, 'WiFi,空调,电视,浴缸,早餐,迷你吧', '高层城景双床房，面积更大', 0, 3, 1, 'admin', 'admin'),
       ('行政套房', 'SUITE', 888.00, 3, 56, 'WiFi,空调,电视,浴缸,客厅,早餐,酒廊', '一室一厅，含行政酒廊权益', 0, 4, 1, 'admin', 'admin');

INSERT INTO `hotel_room` (`room_no`, `room_type_id`, `floor`, `area`, `price`, `status`, `remark`, `tenant_id`, `creator`, `updater`)
VALUES ('201', 1, 2, 22, 268.00, 10, NULL, 1, 'admin', 'admin'),
       ('202', 1, 2, 22, 268.00, 10, NULL, 1, 'admin', 'admin'),
       ('203', 2, 2, 30, 398.00, 10, NULL, 1, 'admin', 'admin'),
       ('204', 2, 2, 30, 398.00, 10, NULL, 1, 'admin', 'admin'),
       ('205', 3, 2, 38, 528.00, 10, NULL, 1, 'admin', 'admin'),
       ('301', 1, 3, 22, 268.00, 10, NULL, 1, 'admin', 'admin'),
       ('302', 2, 3, 30, 398.00, 10, NULL, 1, 'admin', 'admin'),
       ('303', 2, 3, 30, 398.00, 10, NULL, 1, 'admin', 'admin'),
       ('304', 3, 3, 38, 548.00, 10, NULL, 1, 'admin', 'admin'),
       ('305', 3, 3, 38, 548.00, 10, NULL, 1, 'admin', 'admin'),
       ('401', 2, 4, 30, 418.00, 10, NULL, 1, 'admin', 'admin'),
       ('402', 2, 4, 30, 418.00, 10, NULL, 1, 'admin', 'admin'),
       ('403', 3, 4, 38, 568.00, 10, NULL, 1, 'admin', 'admin'),
       ('404', 3, 4, 38, 568.00, 10, NULL, 1, 'admin', 'admin'),
       ('405', 4, 4, 56, 888.00, 10, '行政楼层', 1, 'admin', 'admin'),
       ('501', 3, 5, 38, 588.00, 10, NULL, 1, 'admin', 'admin'),
       ('502', 3, 5, 38, 588.00, 10, NULL, 1, 'admin', 'admin'),
       ('503', 4, 5, 56, 928.00, 10, '行政楼层', 1, 'admin', 'admin'),
       ('504', 4, 5, 56, 928.00, 10, '行政楼层', 1, 'admin', 'admin'),
       ('505', 4, 5, 56, 968.00, 10, '行政楼层 · 顶层', 1, 'admin', 'admin');

INSERT INTO `hotel_guest` (`name`, `mobile`, `id_card`, `email`, `sex`, `remark`, `tenant_id`, `creator`, `updater`)
VALUES ('李明月', '13900000001', '330281199203151234', 'limy@example.com', 2, '常客，偏好高楼层', 1, 'admin', 'admin'),
       ('王一鸣', '13900000002', '330281199507228765', 'wangym@example.com', 1, '协议公司客户', 1, 'admin', 'admin'),
       ('赵子期', '13900000003', '330281198811095432', 'zhaozq@example.com', 1, NULL, 1, 'admin', 'admin'),
       ('陈嘉禾', '13900000004', '330281199901301122', 'chenjh@example.com', 2, NULL, 1, 'admin', 'admin');

-- ============================================================
-- 菜单与权限（ID 段 20000+，避免与芋道内置菜单冲突）
-- ============================================================
DELETE FROM `system_menu` WHERE `id` BETWEEN 20000 AND 20199;

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`)
VALUES (20000, '酒店管理', '', 1, 5, 0, '/hotel', 'ep:office-building', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
       (20001, '房态看板', 'hotel:room:query', 2, 1, 20000, 'board', 'ep:grid', 'hotel/board/index', 'HotelBoard', 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
       (20002, '房型管理', 'hotel:room-type:query', 2, 2, 20000, 'room-type', 'ep:house', 'hotel/roomType/index', 'HotelRoomType', 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
       (20003, '房间管理', 'hotel:room:query', 2, 3, 20000, 'room', 'ep:key', 'hotel/room/index', 'HotelRoom', 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
       (20004, '住客管理', 'hotel:guest:query', 2, 4, 20000, 'guest', 'ep:user', 'hotel/guest/index', 'HotelGuest', 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
       (20005, '订单管理', 'hotel:order:query', 2, 5, 20000, 'order', 'ep:tickets', 'hotel/order/index', 'HotelOrder', 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
       (20006, '清洁任务', 'hotel:cleaning:query', 2, 6, 20000, 'cleaning', 'ep:brush', 'hotel/cleaning/index', 'HotelCleaning', 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
       (20007, '经营统计', 'hotel:statistics:query', 2, 7, 20000, 'statistics', 'ep:data-line', 'hotel/statistics/index', 'HotelStatistics', 0, b'1', b'1', b'1', 'admin', 'admin', b'0');

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`)
VALUES (20010, '房型查询', 'hotel:room-type:query', 3, 1, 20002, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
       (20011, '房型创建', 'hotel:room-type:create', 3, 2, 20002, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
       (20012, '房型更新', 'hotel:room-type:update', 3, 3, 20002, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
       (20013, '房型删除', 'hotel:room-type:delete', 3, 4, 20002, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),

       (20020, '房间查询', 'hotel:room:query', 3, 1, 20003, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
       (20021, '房间创建', 'hotel:room:create', 3, 2, 20003, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
       (20022, '房间更新', 'hotel:room:update', 3, 3, 20003, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
       (20023, '房间删除', 'hotel:room:delete', 3, 4, 20003, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),

       (20030, '住客查询', 'hotel:guest:query', 3, 1, 20004, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
       (20031, '住客创建', 'hotel:guest:create', 3, 2, 20004, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
       (20032, '住客更新', 'hotel:guest:update', 3, 3, 20004, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
       (20033, '住客删除', 'hotel:guest:delete', 3, 4, 20004, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),

       (20040, '订单查询', 'hotel:order:query', 3, 1, 20005, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
       (20041, '订单创建', 'hotel:order:create', 3, 2, 20005, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
       (20042, '订单更新', 'hotel:order:update', 3, 3, 20005, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
       (20043, '订单删除', 'hotel:order:delete', 3, 4, 20005, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),

       (20050, '清洁查询', 'hotel:cleaning:query', 3, 1, 20006, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
       (20051, '清洁创建', 'hotel:cleaning:create', 3, 2, 20006, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
       (20052, '清洁更新', 'hotel:cleaning:update', 3, 3, 20006, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
       (20053, '清洁删除', 'hotel:cleaning:delete', 3, 4, 20006, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),

       (20060, '统计查询', 'hotel:statistics:query', 3, 1, 20007, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');

-- ============================================================
-- P0「能开门」：表结构变更（只追加，不删改已有建表语句）
-- ============================================================

-- ----------------------------
-- 1.1 改表（一律 ADD COLUMN，禁止 DROP COLUMN）
-- ----------------------------
ALTER TABLE `hotel_room_type`
    ADD COLUMN `hourly_price` decimal(10,2) NULL COMMENT '钟点房价格（元/时段）' AFTER `base_price`,
    ADD COLUMN `hourly_hours` int NULL DEFAULT 3 COMMENT '钟点房时长（小时）' AFTER `hourly_price`,
    ADD COLUMN `hourly_enabled` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否开放钟点房' AFTER `hourly_hours`,
    ADD COLUMN `deposit` decimal(10,2) NULL DEFAULT 0 COMMENT '建议押金' AFTER `hourly_enabled`;

ALTER TABLE `hotel_room`
    ADD COLUMN `lock_status` tinyint NOT NULL DEFAULT 0 COMMENT '锁定状态：0 正常 1 维修锁定 2 预留锁定' AFTER `status`,
    ADD COLUMN `sort` int NOT NULL DEFAULT 0 COMMENT '排序' AFTER `lock_status`;

ALTER TABLE `hotel_order`
    ADD COLUMN `room_type_id`  bigint      NULL COMMENT '房型编号（冗余，便于统计）' AFTER `room_id`,
    ADD COLUMN `order_type`    tinyint     NOT NULL DEFAULT 10 COMMENT '订单类型：10 全日房 20 钟点房' AFTER `room_type_id`,
    ADD COLUMN `business_date` date        NULL COMMENT '所属营业日（入账归属日）' AFTER `order_type`,
    ADD COLUMN `deposit`       decimal(10,2) NOT NULL DEFAULT 0 COMMENT '押金' AFTER `amount`,
    ADD COLUMN `adult_count`   int         NULL DEFAULT 1 COMMENT '成人数' AFTER `nights`,
    ADD COLUMN `child_count`   int         NULL DEFAULT 0 COMMENT '儿童数' AFTER `adult_count`,
    ADD COLUMN `parent_order_id` bigint    NULL COMMENT '原订单号（续住/换房时关联）' AFTER `child_count`,
    ADD COLUMN `check_in_time`  datetime   NULL COMMENT '计划入住时间（钟点房精确到时段）' AFTER `check_in_date`,
    ADD COLUMN `check_out_time` datetime   NULL COMMENT '计划离店时间（钟点房精确到时段）' AFTER `check_out_date`,
    ADD KEY `idx_business_date` (`business_date`);

ALTER TABLE `hotel_guest`
    ADD COLUMN `guest_level` tinyint NOT NULL DEFAULT 0 COMMENT '客人类别：0 散客 1 会员 2 协议单位 3 中介' AFTER `sex`,
    ADD COLUMN `company`     varchar(100) NULL COMMENT '所属单位' AFTER `guest_level`,
    ADD COLUMN `address`     varchar(200) NULL COMMENT '联系地址' AFTER `company`;

-- ----------------------------
-- 1.2 新建表
-- ----------------------------
DROP TABLE IF EXISTS `hotel_room_daily_rate`;
CREATE TABLE `hotel_room_daily_rate` (
    `id`               bigint         NOT NULL AUTO_INCREMENT COMMENT '编号',
    `room_type_id`     bigint         NOT NULL COMMENT '房型编号',
    `biz_date`         date           NOT NULL COMMENT '业务日期',
    `price`            decimal(10,2)  NOT NULL COMMENT '当日房价',
    `total_inventory`  int            NOT NULL DEFAULT 0 COMMENT '总库存（可售房量）',
    `sold_inventory`   int            NOT NULL DEFAULT 0 COMMENT '已售房量',
    `status`           tinyint        NOT NULL DEFAULT 10 COMMENT '状态：10 开房 20 关房（不可售）',
    `remark`           varchar(500)   NULL COMMENT '备注',
    `tenant_id`        bigint         NOT NULL DEFAULT 0 COMMENT '租户编号',
    `creator`          varchar(64)    NULL DEFAULT '' COMMENT '创建者',
    `create_time`      datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`          varchar(64)    NULL DEFAULT '' COMMENT '更新者',
    `update_time`      datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`          bit(1)         NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_type_date` (`room_type_id`, `biz_date`, `tenant_id`, `deleted`),
    KEY `idx_date` (`biz_date`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '酒店房价日历';

DROP TABLE IF EXISTS `hotel_folio`;
CREATE TABLE `hotel_folio` (
    `id`             bigint         NOT NULL AUTO_INCREMENT COMMENT '编号',
    `folio_no`       varchar(32)    NOT NULL COMMENT '账页号 F + yyyyMMdd + 6位序号',
    `order_id`       bigint         NULL COMMENT '订单编号（可为空：非住客挂账）',
    `guest_id`       bigint         NOT NULL COMMENT '住客编号',
    `room_id`        bigint         NULL COMMENT '房间编号',
    `type`           tinyint        NOT NULL DEFAULT 10 COMMENT '账页类型：10 主账页 20 分账页',
    `status`         tinyint        NOT NULL DEFAULT 10 COMMENT '状态：10 未结 20 部分结账 30 已结账 40 已转出 50 已取消',
    `total_charge`   decimal(10,2)  NOT NULL DEFAULT 0 COMMENT '消费合计',
    `total_payment`  decimal(10,2)  NOT NULL DEFAULT 0 COMMENT '收款合计',
    `balance`        decimal(10,2)  NOT NULL DEFAULT 0 COMMENT '余额 = 消费 - 收款（正为应收）',
    `remark`         varchar(500)   NULL COMMENT '备注',
    `tenant_id`      bigint         NOT NULL DEFAULT 0 COMMENT '租户编号',
    `creator`        varchar(64)    NULL DEFAULT '' COMMENT '创建者',
    `create_time`    datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`        varchar(64)    NULL DEFAULT '' COMMENT '更新者',
    `update_time`    datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`        bit(1)         NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_folio_no` (`folio_no`),
    KEY `idx_order` (`order_id`),
    KEY `idx_guest` (`guest_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '酒店账务账页';

DROP TABLE IF EXISTS `hotel_folio_item`;
CREATE TABLE `hotel_folio_item` (
    `id`             bigint         NOT NULL AUTO_INCREMENT COMMENT '编号',
    `folio_id`       bigint         NOT NULL COMMENT '账页编号',
    `order_id`       bigint         NULL COMMENT '订单编号',
    `business_date`  date           NOT NULL COMMENT '营业日（入账归属）',
    `item_type`      tinyint        NOT NULL COMMENT '项目类型：10 房费 20 餐饮 30 迷你吧 40 洗衣 50 赔偿 60 押金 70 其他 80 退款',
    `item_name`      varchar(100)   NOT NULL COMMENT '项目名称',
    `direction`      tinyint        NOT NULL COMMENT '方向：1 借方（消费） 2 贷方（收款/冲减）',
    `quantity`       int            NOT NULL DEFAULT 1 COMMENT '数量',
    `unit_price`     decimal(10,2)  NOT NULL DEFAULT 0 COMMENT '单价',
    `amount`         decimal(10,2)  NOT NULL DEFAULT 0 COMMENT '金额（正数，方向由 direction 决定）',
    `payment_method` tinyint        NULL COMMENT '支付方式（direction=2 时必填），见 PaymentMethodEnum',
    `status`         tinyint        NOT NULL DEFAULT 10 COMMENT '状态：10 有效 20 已冲销 30 已转出',
    `void_id`        bigint         NULL COMMENT '被冲销时指向的冲销明细号',
    `transfer_to_folio_id` bigint   NULL COMMENT '转出的目标账页号',
    `operator_id`    bigint         NULL COMMENT '操作人（后台用户编号）',
    `remark`         varchar(500)   NULL COMMENT '备注',
    `tenant_id`      bigint         NOT NULL DEFAULT 0 COMMENT '租户编号',
    `creator`        varchar(64)    NULL DEFAULT '' COMMENT '创建者',
    `create_time`    datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`        varchar(64)    NULL DEFAULT '' COMMENT '更新者',
    `update_time`    datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`        bit(1)         NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_folio` (`folio_id`),
    KEY `idx_biz_date` (`business_date`),
    KEY `idx_order` (`order_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '酒店账务明细';

DROP TABLE IF EXISTS `hotel_payment_record`;
CREATE TABLE `hotel_payment_record` (
    `id`             bigint         NOT NULL AUTO_INCREMENT COMMENT '编号',
    `folio_id`       bigint         NOT NULL COMMENT '账页编号',
    `order_id`       bigint         NULL COMMENT '订单编号',
    `trade_no`       varchar(64)    NULL COMMENT '外部交易号（在线支付/银行卡凭证号）',
    `amount`         decimal(10,2)  NOT NULL COMMENT '金额（退款为负）',
    `method`         tinyint        NOT NULL COMMENT '支付方式，见 PaymentMethodEnum',
    `type`           tinyint        NOT NULL DEFAULT 10 COMMENT '类型：10 收款 20 退款',
    `status`         tinyint        NOT NULL DEFAULT 10 COMMENT '状态：10 成功 20 已退款 30 失败',
    `pay_time`       datetime       NOT NULL COMMENT '支付时间',
    `operator_id`    bigint         NULL COMMENT '操作人',
    `remark`         varchar(500)   NULL COMMENT '备注',
    `tenant_id`      bigint         NOT NULL DEFAULT 0 COMMENT '租户编号',
    `creator`        varchar(64)    NULL DEFAULT '' COMMENT '创建者',
    `create_time`    datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`        varchar(64)    NULL DEFAULT '' COMMENT '更新者',
    `update_time`    datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`        bit(1)         NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_trade_no` (`trade_no`, `tenant_id`, `deleted`),
    KEY `idx_folio` (`folio_id`),
    KEY `idx_order` (`order_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '酒店支付流水';

DROP TABLE IF EXISTS `hotel_business_date`;
CREATE TABLE `hotel_business_date` (
    `id`            bigint      NOT NULL AUTO_INCREMENT COMMENT '编号',
    `biz_date`      date        NOT NULL COMMENT '当前营业日',
    `status`        tinyint     NOT NULL DEFAULT 10 COMMENT '状态：10 进行中 20 已过账待审核 30 已结账',
    `audit_time`    datetime    NULL COMMENT '夜审完成时间',
    `operator_id`   bigint      NULL COMMENT '操作人',
    `remark`        varchar(500) NULL COMMENT '备注',
    `tenant_id`     bigint      NOT NULL DEFAULT 0 COMMENT '租户编号',
    `creator`       varchar(64) NULL DEFAULT '' COMMENT '创建者',
    `create_time`   datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`       varchar(64) NULL DEFAULT '' COMMENT '更新者',
    `update_time`   datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`       bit(1)      NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_tenant` (`tenant_id`, `deleted`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '酒店营业日';

DROP TABLE IF EXISTS `hotel_night_audit`;
CREATE TABLE `hotel_night_audit` (
    `id`               bigint         NOT NULL AUTO_INCREMENT COMMENT '编号',
    `biz_date`         date           NOT NULL COMMENT '被审计的营业日',
    `status`           tinyint        NOT NULL DEFAULT 10 COMMENT '状态：10 进行中 20 成功 30 失败',
    `start_time`       datetime       NOT NULL COMMENT '开始时间',
    `end_time`         datetime       NULL COMMENT '结束时间',
    `room_charge_total` decimal(12,2) NOT NULL DEFAULT 0 COMMENT '过账房费合计',
    `posted_room_count` int           NOT NULL DEFAULT 0 COMMENT '过账房数',
    `posted_item_count` int           NOT NULL DEFAULT 0 COMMENT '过账明细数',
    `fail_reason`      varchar(1000)  NULL COMMENT '失败原因',
    `operator_id`      bigint         NULL COMMENT '操作人',
    `tenant_id`        bigint         NOT NULL DEFAULT 0 COMMENT '租户编号',
    `creator`          varchar(64)    NULL DEFAULT '' COMMENT '创建者',
    `create_time`      datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`          varchar(64)    NULL DEFAULT '' COMMENT '更新者',
    `update_time`      datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`          bit(1)         NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_biz_date` (`biz_date`, `tenant_id`, `deleted`, `status`),
    KEY `idx_date` (`biz_date`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '酒店夜审记录';

DROP TABLE IF EXISTS `hotel_room_change_log`;
CREATE TABLE `hotel_room_change_log` (
    `id`            bigint      NOT NULL AUTO_INCREMENT COMMENT '编号',
    `order_id`      bigint      NOT NULL COMMENT '订单编号',
    `from_room_id`  bigint      NOT NULL COMMENT '原房间编号',
    `to_room_id`    bigint      NOT NULL COMMENT '新房间编号',
    `change_time`   datetime    NOT NULL COMMENT '换房时间',
    `reason`        varchar(500) NULL COMMENT '换房原因',
    `operator_id`   bigint      NULL COMMENT '操作人',
    `tenant_id`     bigint      NOT NULL DEFAULT 0 COMMENT '租户编号',
    `creator`       varchar(64) NULL DEFAULT '' COMMENT '创建者',
    `create_time`   datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`       varchar(64) NULL DEFAULT '' COMMENT '更新者',
    `update_time`   datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`       bit(1)      NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_order` (`order_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '酒店换房记录';

DROP TABLE IF EXISTS `hotel_shift_handover`;
CREATE TABLE `hotel_shift_handover` (
    `id`             bigint         NOT NULL AUTO_INCREMENT COMMENT '编号',
    `shift_no`       varchar(32)    NOT NULL COMMENT '班次号',
    `biz_date`       date           NOT NULL COMMENT '营业日',
    `start_time`     datetime       NOT NULL COMMENT '开班时间',
    `end_time`       datetime       NULL COMMENT '交班时间',
    `operator_id`    bigint         NULL COMMENT '收银员',
    `cash_amount`    decimal(10,2)  NOT NULL DEFAULT 0 COMMENT '现金',
    `card_amount`    decimal(10,2)  NOT NULL DEFAULT 0 COMMENT '银行卡',
    `wechat_amount`  decimal(10,2)  NOT NULL DEFAULT 0 COMMENT '微信',
    `alipay_amount`  decimal(10,2)  NOT NULL DEFAULT 0 COMMENT '支付宝',
    `other_amount`   decimal(10,2)  NOT NULL DEFAULT 0 COMMENT '其他',
    `total_amount`   decimal(10,2)  NOT NULL DEFAULT 0 COMMENT '合计',
    `status`         tinyint        NOT NULL DEFAULT 10 COMMENT '状态：10 进行中 20 已交班',
    `remark`         varchar(1000)  NULL COMMENT '交接备注',
    `tenant_id`      bigint         NOT NULL DEFAULT 0 COMMENT '租户编号',
    `creator`        varchar(64)    NULL DEFAULT '' COMMENT '创建者',
    `create_time`    datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`        varchar(64)    NULL DEFAULT '' COMMENT '更新者',
    `update_time`    datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`        bit(1)         NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_shift_no` (`shift_no`),
    KEY `idx_date` (`biz_date`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '酒店交班记录';

DROP TABLE IF EXISTS `hotel_invoice`;
CREATE TABLE `hotel_invoice` (
    `id`           bigint         NOT NULL AUTO_INCREMENT COMMENT '编号',
    `invoice_no`   varchar(50)    NOT NULL COMMENT '发票号码',
    `order_id`     bigint         NULL COMMENT '订单编号',
    `guest_id`     bigint         NOT NULL COMMENT '住客编号',
    `title`        varchar(200)   NOT NULL COMMENT '发票抬头',
    `tax_no`       varchar(50)    NULL COMMENT '税号',
    `type`         tinyint        NOT NULL DEFAULT 10 COMMENT '类型：10 增值税普通发票 20 增值税专用发票 30 电子发票',
    `amount`       decimal(10,2)  NOT NULL COMMENT '开票金额',
    `status`       tinyint        NOT NULL DEFAULT 10 COMMENT '状态：10 已开具 20 已红冲',
    `issue_time`   datetime       NOT NULL COMMENT '开票时间',
    `void_time`    datetime       NULL COMMENT '红冲时间',
    `operator_id`  bigint         NULL COMMENT '操作人',
    `remark`       varchar(500)   NULL COMMENT '备注',
    `tenant_id`    bigint         NOT NULL DEFAULT 0 COMMENT '租户编号',
    `creator`      varchar(64)    NULL DEFAULT '' COMMENT '创建者',
    `create_time`  datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`      varchar(64)    NULL DEFAULT '' COMMENT '更新者',
    `update_time`  datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`      bit(1)         NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_invoice_no` (`invoice_no`, `tenant_id`, `deleted`),
    KEY `idx_order` (`order_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '酒店发票';

-- ----------------------------
-- 演示数据补充：开放钟点房（体验用，可按门店实际情况关闭）
-- ----------------------------
UPDATE `hotel_room_type`
SET `hourly_enabled` = b'1',
    `hourly_price`   = ROUND(`base_price` * 0.45, 0),
    `hourly_hours`   = 3
WHERE `hourly_enabled` = b'0';

-- ============================================================
-- P0 菜单与权限（ID 段 20200–20299）
-- ============================================================
DELETE FROM `system_menu` WHERE `id` BETWEEN 20200 AND 20299;

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`)
VALUES (20201, '房价日历', 'hotel:rate:query', 2, 8, 20000, 'rate', 'ep:calendar', 'hotel/rate/index', 'HotelRate', 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
       (20202, '夜审管理', 'hotel:audit:query', 2, 9, 20000, 'audit', 'ep:moon-night', 'hotel/audit/index', 'HotelAudit', 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
       (20203, '账务账页', 'hotel:folio:query', 2, 10, 20000, 'folio', 'ep:wallet', 'hotel/folio/index', 'HotelFolio', 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
       (20204, '交班管理', 'hotel:shift:query', 2, 11, 20000, 'shift', 'ep:switch-button', 'hotel/shift/index', 'HotelShift', 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
       (20205, '发票管理', 'hotel:invoice:query', 2, 12, 20000, 'invoice', 'ep:document', 'hotel/invoice/index', 'HotelInvoice', 0, b'1', b'1', b'1', 'admin', 'admin', b'0');

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`)
VALUES (20210, '房价日历查询', 'hotel:rate:query', 3, 1, 20201, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
       (20211, '房价日历维护', 'hotel:rate:update', 3, 2, 20201, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),

       (20212, '夜审查询', 'hotel:audit:query', 3, 1, 20202, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
       (20213, '执行夜审', 'hotel:audit:audit', 3, 2, 20202, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),

       (20214, '账页查询', 'hotel:folio:query', 3, 1, 20203, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
       (20215, '账页创建', 'hotel:folio:create', 3, 2, 20203, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
       (20216, '账页更新', 'hotel:folio:update', 3, 3, 20203, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),

       (20217, '交班查询', 'hotel:shift:query', 3, 1, 20204, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
       (20218, '开班', 'hotel:shift:create', 3, 2, 20204, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
       (20219, '交班', 'hotel:shift:update', 3, 3, 20204, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),

       (20220, '发票查询', 'hotel:invoice:query', 3, 1, 20205, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
       (20221, '开票', 'hotel:invoice:create', 3, 2, 20205, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
       (20222, '发票红冲', 'hotel:invoice:update', 3, 3, 20205, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
