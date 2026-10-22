package cn.iocoder.yudao.module.hotel.service.ar;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.datapermission.core.util.DataPermissionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hotel.controller.admin.ar.vo.*;
import cn.iocoder.yudao.module.hotel.dal.dataobject.ar.ArAccountDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.ar.ArLedgerDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.ar.ArSettlementDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.store.StoreDO;
import cn.iocoder.yudao.module.hotel.dal.mysql.ar.ArAccountMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.ar.ArLedgerMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.ar.ArSettlementMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.store.StoreMapper;
import cn.iocoder.yudao.module.hotel.framework.store.StoreContext;
import cn.iocoder.yudao.module.hotel.service.operationlog.annotation.HotelOperationLog;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.module.hotel.enums.T5ErrorCodeConstants.*;

/**
 * 协议单位 AR Service：挂账 / 结算 / 收款。
 */
@Service
@Validated
public class ArServiceImpl {

    private static final DateTimeFormatter NO_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Resource
    private ArAccountMapper arAccountMapper;
    @Resource
    private ArLedgerMapper arLedgerMapper;
    @Resource
    private ArSettlementMapper arSettlementMapper;
    @Resource
    private StoreMapper storeMapper;

    // ==================== 协议单位 ====================

    public Long createArAccount(ArAccountSaveReqVO reqVO) {
        validateCodeUnique(null, reqVO.getCode());
        ArAccountDO account = BeanUtils.toBean(reqVO, ArAccountDO.class);
        account.setUsedAmount(BigDecimal.ZERO);
        if (account.getCreditLimit() == null) {
            account.setCreditLimit(BigDecimal.ZERO);
        }
        if (account.getSettleCycle() == null) {
            account.setSettleCycle(30);
        }
        if (account.getStatus() == null) {
            account.setStatus(0);
        }
        arAccountMapper.insert(account);
        return account.getId();
    }

    public void updateArAccount(ArAccountSaveReqVO reqVO) {
        validateAccount(reqVO.getId());
        validateCodeUnique(reqVO.getId(), reqVO.getCode());
        ArAccountDO update = BeanUtils.toBean(reqVO, ArAccountDO.class);
        update.setUsedAmount(null);
        arAccountMapper.updateById(update);
    }

    public PageResult<ArAccountRespVO> getArAccountPage(ArAccountPageReqVO reqVO) {
        PageResult<ArAccountDO> page = arAccountMapper.selectPage(reqVO);
        Map<Long, String> storeNames = getStoreNames(page.getList().stream()
                .map(ArAccountDO::getStoreId).filter(Objects::nonNull).collect(Collectors.toSet()));
        List<ArAccountRespVO> list = page.getList().stream().map(account -> {
            ArAccountRespVO vo = BeanUtils.toBean(account, ArAccountRespVO.class);
            vo.setStoreName(storeNames.get(account.getStoreId()));
            vo.setAvailableAmount(nullToZero(account.getCreditLimit())
                    .subtract(nullToZero(account.getUsedAmount())));
            return vo;
        }).collect(Collectors.toList());
        return new PageResult<>(list, page.getTotal());
    }

    public List<ArAccountRespVO> getArAccountList() {
        List<ArAccountDO> accounts = arAccountMapper.selectListAll();
        Map<Long, String> storeNames = getStoreNames(accounts.stream()
                .map(ArAccountDO::getStoreId).filter(Objects::nonNull).collect(Collectors.toSet()));
        return accounts.stream().map(account -> {
            ArAccountRespVO vo = BeanUtils.toBean(account, ArAccountRespVO.class);
            vo.setStoreName(storeNames.get(account.getStoreId()));
            vo.setAvailableAmount(nullToZero(account.getCreditLimit())
                    .subtract(nullToZero(account.getUsedAmount())));
            return vo;
        }).collect(Collectors.toList());
    }

    // ==================== 挂账 ====================

    @Transactional(rollbackFor = Exception.class)
    @HotelOperationLog(module = "ar", operation = "挂账")
    public Long postToAr(ArPostReqVO reqVO) {
        ArAccountDO account = validateAccount(reqVO.getArAccountId());
        if (!Objects.equals(account.getStatus(), 0)) {
            throw exception(ERROR_AR_ACCOUNT_DISABLED);
        }
        validateStorePermission(account.getStoreId());
        BigDecimal amount = reqVO.getAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw exception(ERROR_AR_AMOUNT_INVALID);
        }
        BigDecimal used = nullToZero(account.getUsedAmount());
        BigDecimal limit = nullToZero(account.getCreditLimit());
        if (used.add(amount).compareTo(limit) > 0) {
            throw exception(ERROR_AR_CREDIT_EXCEED);
        }

        Long storeId = StoreContext.getStoreId() > 0 ? StoreContext.getStoreId() : account.getStoreId();
        ArLedgerDO ledger = new ArLedgerDO();
        ledger.setStoreId(storeId == null ? 0L : storeId);
        ledger.setArAccountId(account.getId());
        ledger.setOrderId(reqVO.getOrderId());
        ledger.setFolioId(reqVO.getFolioId());
        ledger.setAmount(amount);
        ledger.setType(10);
        ledger.setBizDate(LocalDate.now());
        ledger.setStatus(10);
        ledger.setOperatorId(getLoginUserId());
        ledger.setRemark(reqVO.getRemark());
        arLedgerMapper.insert(ledger);

        arAccountMapper.addUsedAmount(account.getId(), amount);
        return ledger.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    @HotelOperationLog(module = "ar", operation = "生成结算单")
    public Long generateSettlement(ArSettlementGenerateReqVO reqVO) {
        ArAccountDO account = validateAccount(reqVO.getArAccountId());
        validateStorePermission(account.getStoreId());
        LocalDate begin = reqVO.getBeginDate();
        LocalDate end = reqVO.getEndDate();
        if (begin == null || end == null || end.isBefore(begin)) {
            throw exception(ERROR_AR_AMOUNT_INVALID);
        }
        List<ArLedgerDO> ledgers = arLedgerMapper.selectUnsettled(account.getId(), begin, end);
        BigDecimal total = ledgers.stream().map(ArLedgerDO::getAmount)
                .map(this::nullToZero).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (ledgers.isEmpty() || total.compareTo(BigDecimal.ZERO) <= 0) {
            throw exception(ERROR_AR_SETTLEMENT_EMPTY);
        }

        Long storeId = account.getStoreId() == null ? 0L : account.getStoreId();
        ArSettlementDO settlement = new ArSettlementDO();
        settlement.setStoreId(storeId);
        settlement.setSettlementNo(buildSettlementNo());
        settlement.setArAccountId(account.getId());
        settlement.setBeginDate(begin);
        settlement.setEndDate(end);
        settlement.setTotalAmount(total);
        settlement.setPaidAmount(BigDecimal.ZERO);
        settlement.setStatus(10);
        settlement.setRemark(reqVO.getRemark());
        arSettlementMapper.insert(settlement);

        arLedgerMapper.markSettled(ledgers.stream().map(ArLedgerDO::getId).collect(Collectors.toList()),
                settlement.getId());
        return settlement.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    @HotelOperationLog(module = "ar", operation = "结算收款")
    public void receiveSettlement(ArSettlementReceiveReqVO reqVO) {
        ArSettlementDO settlement = reqVO.getSettlementId() == null ? null
                : DataPermissionUtils.executeIgnore(() -> arSettlementMapper.selectById(reqVO.getSettlementId()));
        if (settlement == null) {
            throw exception(ERROR_AR_SETTLEMENT_NOT_EXIST);
        }
        validateStorePermission(settlement.getStoreId());
        if (Objects.equals(settlement.getStatus(), 30)) {
            throw exception(ERROR_AR_RECEIVE_EXCEED);
        }
        BigDecimal amount = reqVO.getAmount();
        BigDecimal remain = nullToZero(settlement.getTotalAmount()).subtract(nullToZero(settlement.getPaidAmount()));
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0 || amount.compareTo(remain) > 0) {
            throw exception(ERROR_AR_RECEIVE_EXCEED);
        }

        BigDecimal paid = nullToZero(settlement.getPaidAmount()).add(amount);
        ArSettlementDO update = new ArSettlementDO();
        update.setId(settlement.getId());
        update.setPaidAmount(paid);
        if (paid.compareTo(nullToZero(settlement.getTotalAmount())) >= 0) {
            update.setStatus(30);
            update.setSettleTime(LocalDateTime.now());
        } else {
            update.setStatus(20);
        }
        arSettlementMapper.updateById(update);

        // 已收款部分回退占用额度
        arAccountMapper.addUsedAmount(settlement.getArAccountId(), amount.negate());
    }

    // ==================== 查询 ====================

    public PageResult<ArLedgerRespVO> getArLedgerPage(ArLedgerPageReqVO reqVO) {
        PageResult<ArLedgerDO> page = arLedgerMapper.selectPage(reqVO);
        Map<Long, String> names = getAccountNames(page.getList().stream()
                .map(ArLedgerDO::getArAccountId).filter(Objects::nonNull).collect(Collectors.toSet()));
        List<ArLedgerRespVO> list = page.getList().stream().map(ledger -> {
            ArLedgerRespVO vo = BeanUtils.toBean(ledger, ArLedgerRespVO.class);
            vo.setArAccountName(names.get(ledger.getArAccountId()));
            return vo;
        }).collect(Collectors.toList());
        return new PageResult<>(list, page.getTotal());
    }

    public PageResult<ArSettlementRespVO> getArSettlementPage(ArSettlementPageReqVO reqVO) {
        PageResult<ArSettlementDO> page = arSettlementMapper.selectPage(reqVO);
        Map<Long, String> names = getAccountNames(page.getList().stream()
                .map(ArSettlementDO::getArAccountId).filter(Objects::nonNull).collect(Collectors.toSet()));
        List<ArSettlementRespVO> list = page.getList().stream().map(settlement -> {
            ArSettlementRespVO vo = BeanUtils.toBean(settlement, ArSettlementRespVO.class);
            vo.setArAccountName(names.get(settlement.getArAccountId()));
            vo.setRemainAmount(nullToZero(settlement.getTotalAmount())
                    .subtract(nullToZero(settlement.getPaidAmount())));
            return vo;
        }).collect(Collectors.toList());
        return new PageResult<>(list, page.getTotal());
    }

    // ==================== 内部 ====================

    private ArAccountDO validateAccount(Long id) {
        ArAccountDO account = id == null ? null
                : DataPermissionUtils.executeIgnore(() -> arAccountMapper.selectById(id));
        if (account == null) {
            throw exception(ERROR_AR_ACCOUNT_NOT_EXIST);
        }
        return account;
    }

    private void validateStorePermission(Long storeId) {
        Long contextStoreId = StoreContext.getStoreId();
        if (contextStoreId != null && contextStoreId > 0
                && storeId != null && storeId > 0 && !Objects.equals(contextStoreId, storeId)) {
            throw exception(ERROR_STORE_NO_PERMISSION);
        }
    }

    private void validateCodeUnique(Long id, String code) {
        if (code == null) {
            return;
        }
        ArAccountDO exists = arAccountMapper.selectByCode(code.trim());
        if (exists != null && !Objects.equals(exists.getId(), id)) {
            throw exception(ERROR_AR_CODE_DUPLICATE);
        }
    }

    private Map<Long, String> getStoreNames(Set<Long> ids) {
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return storeMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(StoreDO::getId, StoreDO::getName, (a, b) -> a));
    }

    private Map<Long, String> getAccountNames(Set<Long> ids) {
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return arAccountMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(ArAccountDO::getId, ArAccountDO::getName, (a, b) -> a));
    }

    private BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String buildSettlementNo() {
        return "ST" + LocalDateTime.now().format(NO_FORMAT)
                + String.format("%03d", ThreadLocalRandom.current().nextInt(1000));
    }

}
