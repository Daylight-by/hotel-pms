package cn.iocoder.yudao.module.hotel.service.folio;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hotel.controller.admin.folio.vo.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 账务账页 Service
 *
 * <p>余额恒等式：balance = total_charge - total_payment（正数为酒店应收，负数为客人预存）。</p>
 */
public interface FolioService {

    /**
     * 开账页
     */
    Long createFolio(FolioCreateReqVO reqVO);

    /**
     * 获得账页（含明细、余额）
     */
    FolioRespVO getFolio(Long id);

    /**
     * 获得某订单的主账页（含明细）
     */
    FolioRespVO getFolioByOrder(Long orderId);

    /**
     * 入账消费（direction=1）
     */
    Long postCharge(FolioPostReqVO reqVO);

    /**
     * 收款（direction=2 + 写支付流水）
     */
    Long postPayment(FolioPaymentReqVO reqVO);

    /**
     * 冲销明细：原明细置为已冲销，新增反向明细，重算余额
     */
    Long voidItem(Long itemId, String reason);

    /**
     * 转账：原明细置为已转出，目标账页新增同额明细
     */
    Long transferItem(Long itemId, Long targetFolioId);

    /**
     * 拆账页：把选定明细转入新建的分账页
     */
    Long splitFolio(Long folioId, List<Long> itemIds, String remark);

    /**
     * 结账：余额必须为 0
     */
    Long settleFolio(Long folioId);

    /**
     * 账务明细分页
     */
    PageResult<FolioItemRespVO> getFolioItemPage(FolioItemPageReqVO reqVO);

    /**
     * 确保订单存在主账页（订单确认时调用，幂等）
     */
    Long ensureFolioForOrder(Long orderId, Long guestId, Long roomId);

    /**
     * 重算账页的消费 / 收款 / 余额
     */
    void recalculate(Long folioId);

    /**
     * 账页余额
     */
    BigDecimal getBalance(Long folioId);
}
