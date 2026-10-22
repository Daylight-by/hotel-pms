package cn.iocoder.yudao.module.hotel.framework.store;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.datapermission.core.rule.DataPermissionRule;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;

import java.util.Set;

/**
 * 门店维度数据权限规则。
 *
 * <p>挂在芋道数据权限插件上，对所有 hotel_* 表的 SELECT / UPDATE / DELETE
 * 自动追加 {@code store_id = 当前门店}，避免每个 Mapper 手写条件漏过滤。</p>
 *
 * <p>storeId = 0 表示集团视角，返回 null 表示不追加条件。</p>
 */
public class StoreDataPermissionRule implements DataPermissionRule {

    /**
     * 参与门店隔离的表（hotel_store 本身是门店主数据，不隔离）
     */
    private static final Set<String> TABLE_NAMES = Set.of(
            "hotel_room_type", "hotel_room", "hotel_guest", "hotel_order",
            "hotel_cleaning_task", "hotel_room_daily_rate", "hotel_folio", "hotel_folio_item",
            "hotel_payment_record", "hotel_shift_handover", "hotel_invoice",
            "hotel_business_date", "hotel_night_audit", "hotel_room_change_log",
            "hotel_channel", "hotel_rate_plan", "hotel_booking_hold", "hotel_member",
            "hotel_member_points_log", "hotel_pay_order", "hotel_invoice_apply",
            "hotel_price_suggestion", "hotel_price_backtest", "hotel_demo_seed",
            "hotel_psb_upload_log", "hotel_lock_card_log", "hotel_ar_account",
            "hotel_ar_ledger", "hotel_ar_settlement", "hotel_ota_config",
            "hotel_ota_order", "hotel_operation_log"
    );

    @Override
    public Set<String> getTableNames() {
        return TABLE_NAMES;
    }

    @Override
    public Expression getExpression(String tableName, Alias tableAlias) {
        Long storeId = StoreContext.getStoreId();
        if (storeId == null || storeId <= 0) {
            return null;
        }
        String qualifier = StrUtil.isNotBlank(tableAlias == null ? null : tableAlias.getName())
                ? tableAlias.getName() : tableName;
        Column column = new Column(new Table(qualifier), "store_id");
        return new EqualsTo(column, new LongValue(storeId));
    }

}
