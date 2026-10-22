package cn.iocoder.yudao.module.hotel.service.invoiceApply;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hotel.controller.admin.invoice.vo.InvoiceSaveReqVO;
import cn.iocoder.yudao.module.hotel.controller.admin.invoiceApply.vo.InvoiceApplyPageReqVO;
import cn.iocoder.yudao.module.hotel.controller.admin.invoiceApply.vo.InvoiceApplyRespVO;
import cn.iocoder.yudao.module.hotel.controller.app.member.vo.AppMemberInvoiceApplyReqVO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.invoice.InvoiceApplyDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.order.OrderDO;
import cn.iocoder.yudao.module.hotel.dal.mysql.invoice.InvoiceApplyMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.order.OrderMapper;
import cn.iocoder.yudao.module.hotel.enums.InvoiceApplyStatusEnum;
import cn.iocoder.yudao.module.hotel.enums.InvoiceTypeEnum;
import cn.iocoder.yudao.module.hotel.enums.OrderStatusEnum;
import cn.iocoder.yudao.module.hotel.service.invoice.InvoiceService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.hotel.enums.ErrorCodeConstants.ORDER_NOT_EXISTS;
import static cn.iocoder.yudao.module.hotel.enums.HotelT2ErrorCodeConstants.*;

/**
 * 电子发票申请 Service（C 端申请 → 管理端开具 → 生成 hotel_invoice）。
 */
@Service
@Validated
@Slf4j
public class InvoiceApplyService {

    private static final DateTimeFormatter APPLY_NO_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Resource
    private InvoiceApplyMapper invoiceApplyMapper;
    @Resource
    private OrderMapper orderMapper;
    @Resource
    private InvoiceService invoiceService;

    /**
     * C 端提交发票申请。
     */
    @Transactional(rollbackFor = Exception.class)
    public Long apply(Long memberId, AppMemberInvoiceApplyReqVO reqVO) {
        OrderDO order = reqVO.getOrderId() == null ? null : orderMapper.selectById(reqVO.getOrderId());
        if (order == null) {
            throw exception(ORDER_NOT_EXISTS);
        }
        BigDecimal paid = order.getPaidAmount() == null ? BigDecimal.ZERO : order.getPaidAmount();
        boolean paidEnough = paid.compareTo(BigDecimal.ZERO) > 0
                || Objects.equals(order.getStatus(), OrderStatusEnum.CHECKED_OUT.getStatus());
        if (!paidEnough) {
            throw exception(ERROR_ORDER_NOT_PAID_FOR_INVOICE);
        }
        BigDecimal base = paid.compareTo(BigDecimal.ZERO) > 0 ? paid
                : (order.getAmount() == null ? BigDecimal.ZERO : order.getAmount());
        BigDecimal used = invoiceApplyMapper.selectListByOrderId(order.getId()).stream()
                .filter(item -> !Objects.equals(item.getStatus(), InvoiceApplyStatusEnum.REJECTED.getStatus()))
                .map(item -> item.getAmount() == null ? BigDecimal.ZERO : item.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal amount = base.subtract(used);
        if (amount.compareTo(new BigDecimal("0.01")) < 0) {
            throw exception(ERROR_INVOICE_APPLY_AMOUNT_EXCEED);
        }

        InvoiceApplyDO apply = new InvoiceApplyDO();
        apply.setApplyNo(generateApplyNo());
        apply.setOrderId(order.getId());
        apply.setMemberId(memberId);
        apply.setTitle(reqVO.getTitle());
        apply.setTaxNo(reqVO.getTaxNo());
        apply.setEmail(reqVO.getEmail());
        apply.setType(reqVO.getType() == null ? InvoiceTypeEnum.ELECTRONIC.getType() : reqVO.getType());
        apply.setAmount(amount);
        apply.setStatus(InvoiceApplyStatusEnum.PENDING.getStatus());
        invoiceApplyMapper.insert(apply);
        return apply.getId();
    }

    /**
     * 管理端处理申请：20 开具 / 30 驳回。
     */
    @Transactional(rollbackFor = Exception.class)
    public void handle(Long id, Integer status, String rejectReason) {
        InvoiceApplyDO apply = id == null ? null : invoiceApplyMapper.selectById(id);
        if (apply == null) {
            throw exception(ERROR_INVOICE_APPLY_NOT_EXIST);
        }
        if (!Objects.equals(apply.getStatus(), InvoiceApplyStatusEnum.PENDING.getStatus())) {
            throw exception(ERROR_INVOICE_APPLY_STATUS_INVALID);
        }
        if (Objects.equals(status, InvoiceApplyStatusEnum.ISSUED.getStatus())) {
            InvoiceSaveReqVO invoiceReq = new InvoiceSaveReqVO();
            invoiceReq.setOrderId(apply.getOrderId());
            invoiceReq.setTitle(apply.getTitle());
            invoiceReq.setTaxNo(apply.getTaxNo());
            invoiceReq.setType(apply.getType());
            invoiceReq.setAmount(apply.getAmount());
            invoiceReq.setRemark("由发票申请 " + apply.getApplyNo() + " 开具");
            Long invoiceId = invoiceService.issueInvoice(invoiceReq);

            InvoiceApplyDO update = new InvoiceApplyDO();
            update.setId(apply.getId());
            update.setStatus(InvoiceApplyStatusEnum.ISSUED.getStatus());
            update.setInvoiceId(invoiceId);
            update.setHandleTime(LocalDateTime.now());
            invoiceApplyMapper.updateById(update);
            return;
        }
        if (Objects.equals(status, InvoiceApplyStatusEnum.REJECTED.getStatus())) {
            InvoiceApplyDO update = new InvoiceApplyDO();
            update.setId(apply.getId());
            update.setStatus(InvoiceApplyStatusEnum.REJECTED.getStatus());
            update.setRejectReason(rejectReason == null ? "信息不完整" : rejectReason);
            update.setHandleTime(LocalDateTime.now());
            invoiceApplyMapper.updateById(update);
            return;
        }
        throw exception(ERROR_INVOICE_APPLY_STATUS_INVALID);
    }

    public PageResult<InvoiceApplyRespVO> getPage(InvoiceApplyPageReqVO reqVO) {
        PageResult<InvoiceApplyDO> page = invoiceApplyMapper.selectPage(reqVO);
        List<InvoiceApplyRespVO> list = page.getList().stream().map(this::buildResp).collect(Collectors.toList());
        return new PageResult<>(list, page.getTotal());
    }

    public List<InvoiceApplyRespVO> getMyApplies(Long memberId) {
        return invoiceApplyMapper.selectListByMemberId(memberId).stream()
                .map(this::buildResp).collect(Collectors.toList());
    }

    private InvoiceApplyRespVO buildResp(InvoiceApplyDO apply) {
        InvoiceApplyRespVO resp = BeanUtils.toBean(apply, InvoiceApplyRespVO.class);
        resp.setStatusName(InvoiceApplyStatusEnum.nameOf(apply.getStatus()));
        resp.setTypeName(invoiceTypeName(apply.getType()));
        return resp;
    }

    private String invoiceTypeName(Integer type) {
        for (InvoiceTypeEnum value : InvoiceTypeEnum.values()) {
            if (value.getType().equals(type)) {
                return value.getName();
            }
        }
        return "未知";
    }

    private String generateApplyNo() {
        return "FA" + LocalDateTime.now().format(APPLY_NO_FORMAT)
                + String.format("%03d", new Random().nextInt(1000));
    }
}
