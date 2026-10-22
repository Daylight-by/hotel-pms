package cn.iocoder.yudao.module.hotel.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 账务方向枚举
 */
@Getter
@AllArgsConstructor
public enum FolioDirectionEnum {

    DEBIT(1, "借方（消费）"),
    CREDIT(2, "贷方（收款/冲减）");

    private final Integer direction;
    private final String name;
}
