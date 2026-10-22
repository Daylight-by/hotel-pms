-- ============================================================
-- T2 收钱与客（在线支付 + 会员积分 + 自助入住退房 + 电子发票申请）
-- 号段：菜单 20350-20399 / 错误码 1-003-004-4235~4260
-- 说明：本文件独立于 sql/mysql/hotel.sql，由派发者按 T1→T5 顺序合并导入
-- 幂等：只 DROP 本包自己的表，不动任何既有表数据
-- ============================================================

SET NAMES utf8mb4;

-- ------------------------------------------------------------
-- 一、hotel_order 加字段（T2 只加这 7 个，T1 的 3 个字段不在本文件）
-- ------------------------------------------------------------
ALTER TABLE `hotel_order`
    ADD COLUMN `pay_deadline`   datetime     NULL COMMENT '支付截止时间（超时自动关单）' AFTER `business_date`,
    ADD COLUMN `cancel_policy`  varchar(500) NULL COMMENT '取消政策快照' AFTER `pay_deadline`,
    ADD COLUMN `contact_name`   varchar(50)  NULL COMMENT '联系人' AFTER `adult_count`,
    ADD COLUMN `contact_mobile` varchar(20)  NULL COMMENT '联系手机' AFTER `contact_name`,
    ADD COLUMN `arrival_time`   varchar(20)  NULL COMMENT '预计到店时间' AFTER `contact_mobile`,
    ADD COLUMN `cancel_time`    datetime     NULL COMMENT '取消时间' AFTER `actual_check_out_time`,
    ADD COLUMN `cancel_reason`  varchar(500) NULL COMMENT '取消原因' AFTER `cancel_time`;

-- ------------------------------------------------------------
-- 二、会员
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `hotel_member`;
CREATE TABLE `hotel_member` (
    `id`            bigint         NOT NULL AUTO_INCREMENT COMMENT '编号',
    `member_no`     varchar(32)    NOT NULL COMMENT '会员卡号',
    `guest_id`      bigint         NULL COMMENT '关联住客档案编号',
    `name`          varchar(50)    NOT NULL COMMENT '姓名',
    `mobile`        varchar(20)    NOT NULL COMMENT '手机号（登录账号）',
    `password`      varchar(100)   NULL COMMENT '密码（BCrypt，可为空=免密/验证码登录）',
    `level`         tinyint        NOT NULL DEFAULT 10 COMMENT '等级：10 普通 20 银卡 30 金卡 40 铂金',
    `points`        int            NOT NULL DEFAULT 0 COMMENT '积分',
    `balance`       decimal(10,2)  NOT NULL DEFAULT 0 COMMENT '储值余额',
    `total_nights`  int            NOT NULL DEFAULT 0 COMMENT '累计间夜',
    `total_amount`  decimal(12,2)  NOT NULL DEFAULT 0 COMMENT '累计消费',
    `status`        tinyint        NOT NULL DEFAULT 0 COMMENT '状态：0 正常 1 冻结',
    `register_time` datetime       NULL COMMENT '注册时间',
    `tenant_id`     bigint         NOT NULL DEFAULT 0 COMMENT '租户编号',
    `creator`       varchar(64)    NULL DEFAULT '' COMMENT '创建者',
    `create_time`   datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`       varchar(64)    NULL DEFAULT '' COMMENT '更新者',
    `update_time`   datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`       bit(1)         NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_member_no` (`member_no`),
    UNIQUE KEY `uk_mobile` (`mobile`, `tenant_id`, `deleted`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '酒店会员';

-- ------------------------------------------------------------
-- 三、会员积分流水
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `hotel_member_points_log`;
CREATE TABLE `hotel_member_points_log` (
    `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '编号',
    `member_id`   bigint       NOT NULL COMMENT '会员编号',
    `type`        tinyint      NOT NULL COMMENT '类型：10 消费获得 20 兑换使用 30 手动调整 40 退款扣回',
    `points`      int          NOT NULL COMMENT '积分（正为增加，负为扣减）',
    `balance`     int          NOT NULL COMMENT '变动后积分',
    `order_id`    bigint       NULL COMMENT '关联订单',
    `remark`      varchar(500) NULL COMMENT '备注',
    `tenant_id`   bigint       NOT NULL DEFAULT 0 COMMENT '租户编号',
    `creator`     varchar(64)  NULL DEFAULT '' COMMENT '创建者',
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`     varchar(64)  NULL DEFAULT '' COMMENT '更新者',
    `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     bit(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_member` (`member_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '酒店会员积分流水';

-- ------------------------------------------------------------
-- 四、在线支付单
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `hotel_pay_order`;
CREATE TABLE `hotel_pay_order` (
    `id`               bigint         NOT NULL AUTO_INCREMENT COMMENT '编号',
    `pay_order_no`     varchar(64)    NOT NULL COMMENT '本站支付单号',
    `biz_order_id`     bigint         NOT NULL COMMENT '业务订单编号（hotel_order.id）',
    `biz_type`         tinyint        NOT NULL DEFAULT 10 COMMENT '业务类型：10 房费预付 20 押金 30 储值充值',
    `amount`           decimal(10,2)  NOT NULL COMMENT '支付金额（元）',
    `channel`          varchar(32)    NOT NULL COMMENT '支付渠道：wxpay/alipay/mock',
    `channel_trade_no` varchar(128)   NULL COMMENT '第三方交易号',
    `status`           tinyint        NOT NULL DEFAULT 10 COMMENT '状态：10 待支付 20 已支付 30 已关闭 40 已退款 50 退款中',
    `expire_time`      datetime       NULL COMMENT '支付单过期时间',
    `pay_time`         datetime       NULL COMMENT '支付成功时间',
    `refund_amount`    decimal(10,2)  NOT NULL DEFAULT 0 COMMENT '已退金额',
    `notify_data`      varchar(2000)  NULL COMMENT '回调原始报文',
    `tenant_id`        bigint         NOT NULL DEFAULT 0 COMMENT '租户编号',
    `creator`          varchar(64)    NULL DEFAULT '' COMMENT '创建者',
    `create_time`      datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`          varchar(64)    NULL DEFAULT '' COMMENT '更新者',
    `update_time`      datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`          bit(1)         NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_pay_order_no` (`pay_order_no`),
    UNIQUE KEY `uk_channel_trade` (`channel_trade_no`),
    KEY `idx_biz` (`biz_order_id`, `biz_type`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '酒店在线支付单';

-- ------------------------------------------------------------
-- 五、电子发票申请
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `hotel_invoice_apply`;
CREATE TABLE `hotel_invoice_apply` (
    `id`            bigint         NOT NULL AUTO_INCREMENT COMMENT '编号',
    `apply_no`      varchar(32)    NOT NULL COMMENT '申请单号',
    `order_id`      bigint         NOT NULL COMMENT '订单编号',
    `member_id`     bigint         NULL COMMENT '会员编号（H5 自助申请）',
    `title`         varchar(200)   NOT NULL COMMENT '发票抬头',
    `tax_no`        varchar(50)    NULL COMMENT '税号',
    `email`         varchar(100)   NULL COMMENT '接收邮箱（电子发票）',
    `type`          tinyint        NOT NULL DEFAULT 30 COMMENT '类型：10 增值税普通 20 增值税专用 30 电子发票',
    `amount`        decimal(10,2)  NOT NULL COMMENT '开票金额',
    `status`        tinyint        NOT NULL DEFAULT 10 COMMENT '状态：10 待开具 20 已开具 30 已驳回',
    `invoice_id`    bigint         NULL COMMENT '开具后关联的发票编号',
    `reject_reason` varchar(500)   NULL COMMENT '驳回原因',
    `handle_time`   datetime       NULL COMMENT '处理时间',
    `tenant_id`     bigint         NOT NULL DEFAULT 0 COMMENT '租户编号',
    `creator`       varchar(64)    NULL DEFAULT '' COMMENT '创建者',
    `create_time`   datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`       varchar(64)    NULL DEFAULT '' COMMENT '更新者',
    `update_time`   datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`       bit(1)         NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_apply_no` (`apply_no`),
    KEY `idx_order` (`order_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '酒店电子发票申请';

-- ------------------------------------------------------------
-- 六、菜单与权限（T2 号段 20350-20399）
-- ------------------------------------------------------------
DELETE FROM `system_menu` WHERE `id` BETWEEN 20350 AND 20399;

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`)
VALUES (20350, '会员管理', 'hotel:member:query', 2, 13, 20000, 'member', 'ep:user-filled', 'hotel/member/index', 'HotelMember', 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
       (20351, '支付单', 'hotel:pay:query', 2, 14, 20000, 'pay-order', 'ep:money', 'hotel/payOrder/index', 'HotelPayOrder', 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
       (20352, '发票申请', 'hotel:invoice-apply:query', 2, 15, 20000, 'invoice-apply', 'ep:document-checked', 'hotel/invoiceApply/index', 'HotelInvoiceApply', 0, b'1', b'1', b'1', 'admin', 'admin', b'0');

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`)
VALUES (20360, '会员查询', 'hotel:member:query', 3, 1, 20350, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
       (20361, '会员创建', 'hotel:member:create', 3, 2, 20350, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
       (20362, '会员更新', 'hotel:member:update', 3, 3, 20350, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
       (20370, '支付单查询', 'hotel:pay:query', 3, 1, 20351, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
       (20371, '支付退款', 'hotel:pay:refund', 3, 2, 20351, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
       (20380, '发票申请查询', 'hotel:invoice-apply:query', 3, 1, 20352, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
       (20381, '发票申请处理', 'hotel:invoice-apply:update', 3, 2, 20352, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
