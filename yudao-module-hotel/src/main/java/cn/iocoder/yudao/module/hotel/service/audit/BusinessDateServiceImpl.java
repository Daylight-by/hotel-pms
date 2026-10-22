package cn.iocoder.yudao.module.hotel.service.audit;

import cn.iocoder.yudao.module.hotel.dal.dataobject.audit.BusinessDateDO;
import cn.iocoder.yudao.module.hotel.dal.mysql.audit.BusinessDateMapper;
import cn.iocoder.yudao.module.hotel.enums.BusinessDateStatusEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 营业日 Service 实现
 *
 * <p>酒店一天的账以「营业日」为界，而不是自然日：夜审把 D 日的房费过账完成后，营业日推进到 D+1。</p>
 */
@Service
@Slf4j
public class BusinessDateServiceImpl implements BusinessDateService {

    @Resource
    private BusinessDateMapper businessDateMapper;

    @Override
    public LocalDate getCurrentBusinessDate() {
        BusinessDateDO current = getCurrentBusinessDateDO();
        return current == null ? LocalDate.now() : current.getBizDate();
    }

    @Override
    public BusinessDateDO getCurrentBusinessDateDO() {
        BusinessDateDO current = businessDateMapper.selectCurrent();
        if (current == null) {
            initIfAbsent();
            current = businessDateMapper.selectCurrent();
        }
        return current;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initIfAbsent() {
        BusinessDateDO current = businessDateMapper.selectCurrent();
        if (current != null) {
            return;
        }
        BusinessDateDO create = new BusinessDateDO();
        create.setBizDate(LocalDate.now());
        create.setStatus(BusinessDateStatusEnum.PROCESSING.getStatus());
        create.setRemark("系统初始化");
        businessDateMapper.insert(create);
        log.info("[initIfAbsent][初始化营业日为 {}]", create.getBizDate());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long rollBusinessDate() {
        BusinessDateDO current = getCurrentBusinessDateDO();
        if (current == null) {
            initIfAbsent();
            current = businessDateMapper.selectCurrent();
        }
        BusinessDateDO update = new BusinessDateDO();
        update.setId(current.getId());
        update.setBizDate(current.getBizDate().plusDays(1));
        update.setStatus(BusinessDateStatusEnum.PROCESSING.getStatus());
        update.setAuditTime(LocalDateTime.now());
        businessDateMapper.updateById(update);
        return update.getBizDate().toEpochDay();
    }
}
