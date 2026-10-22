-- ============================================================
-- T4「数据与演示」：表结构 + 菜单权限（只写本包，号段 20400-20449）
-- 执行：mariadb -uroot -p123456 --default-character-set=utf8mb4 ruoyi-vue-pro < sql/mysql/hotel-t4.sql
-- 归属：操作审计日志 / 演示数据批次 + 大屏/报表/审计菜单
-- ============================================================

-- ----------------------------
-- 操作审计日志（P2 §1.2 规格，T4 负责实现）
-- ----------------------------
DROP TABLE IF EXISTS `hotel_operation_log`;
CREATE TABLE `hotel_operation_log` (
    `id`            bigint        NOT NULL AUTO_INCREMENT COMMENT '编号',
    `store_id`      bigint        NULL COMMENT '门店编号',
    `module`        varchar(50)   NOT NULL COMMENT '模块：order/folio/rate/audit/...',
    `biz_id`        varchar(64)   NULL COMMENT '业务单据号',
    `operation`     varchar(100)  NOT NULL COMMENT '操作：如 入账/冲销/改价/夜审',
    `before_data`   varchar(4000) NULL COMMENT '变更前（JSON）',
    `after_data`    varchar(4000) NULL COMMENT '变更后（JSON）',
    `operator_id`   bigint        NULL COMMENT '操作人',
    `operator_name` varchar(50)   NULL COMMENT '操作人姓名',
    `ip`            varchar(50)   NULL COMMENT 'IP',
    `tenant_id`     bigint        NOT NULL DEFAULT 0 COMMENT '租户编号',
    `creator`       varchar(64)   NULL DEFAULT '' COMMENT '创建者',
    `create_time`   datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`       varchar(64)   NULL DEFAULT '' COMMENT '更新者',
    `update_time`   datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`       bit(1)        NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_module_biz` (`module`, `biz_id`),
    KEY `idx_time` (`create_time`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '酒店操作审计日志';

-- ----------------------------
-- 演示数据批次（T4 自己记录，用于 demo/clean 精确清理）
-- ----------------------------
DROP TABLE IF EXISTS `hotel_demo_seed`;
CREATE TABLE `hotel_demo_seed` (
    `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '编号',
    `batch_no`    varchar(32)  NOT NULL COMMENT '批次号',
    `months`      int          NOT NULL DEFAULT 6 COMMENT '生成月数',
    `start_date`  date         NULL COMMENT '数据窗口起',
    `end_date`    date         NULL COMMENT '数据窗口止',
    `order_count` int          NOT NULL DEFAULT 0 COMMENT '订单数',
    `guest_count` int          NOT NULL DEFAULT 0 COMMENT '住客数',
    `rate_count`  int          NOT NULL DEFAULT 0 COMMENT '房价日历行数',
    `status`      tinyint      NOT NULL DEFAULT 10 COMMENT '状态：10 进行中 20 已清理',
    `remark`      varchar(500) NULL COMMENT '备注',
    `tenant_id`   bigint       NOT NULL DEFAULT 0 COMMENT '租户编号',
    `creator`     varchar(64)  NULL DEFAULT '' COMMENT '创建者',
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`     varchar(64)  NULL DEFAULT '' COMMENT '更新者',
    `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     bit(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_batch` (`batch_no`, `tenant_id`, `deleted`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '酒店演示数据批次';

-- ============================================================
-- 菜单与权限（ID 段 20400-20449，T4 专用）
-- ============================================================
DELETE FROM `system_menu` WHERE `id` BETWEEN 20400 AND 20449;

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`)
VALUES (20401, '经营数据大屏', 'hotel:dashboard:query', 2, 13, 20000, 'dashboard', 'ep:monitor', 'hotel/dashboard/index', 'HotelDashboard', 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
       (20402, '报表中心', 'hotel:report:query', 2, 14, 20000, 'report', 'ep:data-analysis', 'hotel/report/index', 'HotelReport', 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
       (20403, '操作审计', 'hotel:log:query', 2, 15, 20000, 'operation-log', 'ep:document-copy', 'hotel/operationLog/index', 'HotelOperationLog', 0, b'1', b'1', b'1', 'admin', 'admin', b'0');

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`)
VALUES (20411, '大屏查询', 'hotel:dashboard:query', 3, 1, 20401, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
       (20412, '演示数据生成', 'hotel:demo:generate', 3, 2, 20401, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
       (20413, '演示数据清理', 'hotel:demo:clean', 3, 3, 20401, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
       (20414, '演示数据状态', 'hotel:demo:query', 3, 4, 20401, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),

       (20421, '报表查询', 'hotel:report:query', 3, 1, 20402, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
       (20422, '报表导出', 'hotel:report:export', 3, 2, 20402, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),

       (20431, '审计查询', 'hotel:log:query', 3, 1, 20403, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
