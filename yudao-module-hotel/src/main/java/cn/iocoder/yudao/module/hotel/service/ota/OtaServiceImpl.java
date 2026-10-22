package cn.iocoder.yudao.module.hotel.service.ota;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hotel.controller.admin.ota.vo.OtaConfigPageReqVO;
import cn.iocoder.yudao.module.hotel.controller.admin.ota.vo.OtaConfigRespVO;
import cn.iocoder.yudao.module.hotel.controller.admin.ota.vo.OtaConfigSaveReqVO;
import cn.iocoder.yudao.module.hotel.controller.admin.ota.vo.OtaOrderPageReqVO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.ota.OtaConfigDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.ota.OtaOrderDO;
import cn.iocoder.yudao.module.hotel.dal.mysql.ota.OtaConfigMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.ota.OtaOrderMapper;
import cn.iocoder.yudao.module.hotel.framework.store.StoreContext;
import cn.iocoder.yudao.module.hotel.service.ota.client.OtaClient;
import cn.iocoder.yudao.module.hotel.service.ota.client.OtaOrderDTO;
import cn.iocoder.yudao.module.hotel.service.ota.client.OtaPushResult;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.hotel.enums.T5ErrorCodeConstants.*;

/**
 * OTA 直连 Service（Mock）：配置、拉单、推价。
 */
@Service
@Validated
@Slf4j
public class OtaServiceImpl {

    @Resource
    private OtaConfigMapper otaConfigMapper;
    @Resource
    private OtaOrderMapper otaOrderMapper;
    @Resource
    private OtaClient otaClient;

    public Long createConfig(OtaConfigSaveReqVO reqVO) {
        OtaConfigDO config = BeanUtils.toBean(reqVO, OtaConfigDO.class);
        if (config.getPushRate() == null) {
            config.setPushRate(true);
        }
        if (config.getPullOrder() == null) {
            config.setPullOrder(true);
        }
        if (config.getMockEnabled() == null) {
            config.setMockEnabled(true);
        }
        if (config.getSyncInterval() == null) {
            config.setSyncInterval(300);
        }
        if (config.getStatus() == null) {
            config.setStatus(0);
        }
        otaConfigMapper.insert(config);
        return config.getId();
    }

    public void updateConfig(OtaConfigSaveReqVO reqVO) {
        validateConfig(reqVO.getId());
        otaConfigMapper.updateById(BeanUtils.toBean(reqVO, OtaConfigDO.class));
    }

    public PageResult<OtaConfigRespVO> getConfigPage(OtaConfigPageReqVO reqVO) {
        PageResult<OtaConfigDO> page = otaConfigMapper.selectPage(reqVO);
        List<OtaConfigRespVO> list = page.getList().stream()
                .map(config -> BeanUtils.toBean(config, OtaConfigRespVO.class))
                .collect(Collectors.toList());
        return new PageResult<>(list, page.getTotal());
    }

    /**
     * 拉取 OTA 订单：Mock 每天生成一个固定单号，重复同步幂等跳过。
     *
     * @return 本次新增订单数
     */
    @Transactional(rollbackFor = Exception.class)
    public int syncOrder(Long configId) {
        OtaConfigDO config = validateConfig(configId);
        if (config.getMockEnabled() != null && !config.getMockEnabled()) {
            // 真实模式未接厂商，按同步失败返回，避免假成功误导演示
            throw exception(ERROR_OTA_SYNC_FAILED);
        }
        List<OtaOrderDTO> orders = otaClient.pullOrders(LocalDateTime.now().minusDays(1));
        int created = 0;
        Long storeId = config.getStoreId() == null ? StoreContext.getStoreId() : config.getStoreId();
        for (OtaOrderDTO dto : orders) {
            if (otaOrderMapper.selectByOtaOrderNo(config.getOtaCode(), dto.getOtaOrderNo()) != null) {
                continue;
            }
            OtaOrderDO order = new OtaOrderDO();
            order.setStoreId(storeId == null ? 0L : storeId);
            order.setOtaCode(config.getOtaCode());
            order.setOtaOrderNo(dto.getOtaOrderNo());
            order.setSyncStatus(20);
            order.setRawData(dto.getRawData());
            order.setSyncTime(LocalDateTime.now());
            otaOrderMapper.insert(order);
            created++;
        }
        OtaConfigDO update = new OtaConfigDO();
        update.setId(config.getId());
        update.setLastSyncTime(LocalDateTime.now());
        otaConfigMapper.updateById(update);
        return created;
    }

    public boolean pushRate(Long configId) {
        OtaConfigDO config = validateConfig(configId);
        OtaPushResult result = otaClient.pushRate(null, LocalDate.now(), LocalDate.now().plusDays(30));
        OtaConfigDO update = new OtaConfigDO();
        update.setId(config.getId());
        update.setLastSyncTime(LocalDateTime.now());
        otaConfigMapper.updateById(update);
        if (!result.isSuccess()) {
            throw exception(ERROR_OTA_SYNC_FAILED);
        }
        return true;
    }

    public PageResult<OtaOrderDO> getOrderPage(OtaOrderPageReqVO reqVO) {
        return otaOrderMapper.selectPage(reqVO);
    }

    private OtaConfigDO validateConfig(Long id) {
        OtaConfigDO config = id == null ? null : otaConfigMapper.selectById(id);
        if (config == null) {
            throw exception(ERROR_OTA_CONFIG_NOT_EXIST);
        }
        return config;
    }

}
