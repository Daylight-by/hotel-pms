package cn.iocoder.yudao.module.hotel.service.invoice;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hotel.controller.admin.invoice.vo.InvoicePageReqVO;
import cn.iocoder.yudao.module.hotel.controller.admin.invoice.vo.InvoiceRespVO;
import cn.iocoder.yudao.module.hotel.controller.admin.invoice.vo.InvoiceSaveReqVO;

/**
 * 发票 Service
 */
public interface InvoiceService {

    /**
     * 开票：金额不得超过该订单账页消费合计 - 已开金额
     */
    Long issueInvoice(InvoiceSaveReqVO reqVO);

    /**
     * 红冲
     */
    Long voidInvoice(Long id, String reason);

    /**
     * 发票分页
     */
    PageResult<InvoiceRespVO> getInvoicePage(InvoicePageReqVO reqVO);
}
