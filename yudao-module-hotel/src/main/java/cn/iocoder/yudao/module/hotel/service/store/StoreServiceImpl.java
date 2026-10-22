package cn.iocoder.yudao.module.hotel.service.store;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hotel.controller.admin.store.vo.StorePageReqVO;
import cn.iocoder.yudao.module.hotel.controller.admin.store.vo.StoreRespVO;
import cn.iocoder.yudao.module.hotel.controller.admin.store.vo.StoreSaveReqVO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.store.StoreDO;
import cn.iocoder.yudao.module.hotel.dal.mysql.order.OrderMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.room.RoomMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.store.StoreMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.hotel.enums.T5ErrorCodeConstants.*;

/**
 * 门店 Service
 */
@Service
@Validated
public class StoreServiceImpl {

    @Resource
    private StoreMapper storeMapper;
    @Resource
    private RoomMapper roomMapper;
    @Resource
    private OrderMapper orderMapper;

    public Long createStore(StoreSaveReqVO reqVO) {
        validateCodeUnique(null, reqVO.getCode());
        StoreDO store = BeanUtils.toBean(reqVO, StoreDO.class);
        if (store.getStatus() == null) {
            store.setStatus(0);
        }
        if (store.getSort() == null) {
            store.setSort(0);
        }
        storeMapper.insert(store);
        return store.getId();
    }

    public void updateStore(StoreSaveReqVO reqVO) {
        validateExists(reqVO.getId());
        validateCodeUnique(reqVO.getId(), reqVO.getCode());
        storeMapper.updateById(BeanUtils.toBean(reqVO, StoreDO.class));
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteStore(Long id) {
        validateExists(id);
        Long rooms = roomMapper.selectCount("store_id", id);
        Long orders = orderMapper.selectCount("store_id", id);
        if ((rooms != null && rooms > 0) || (orders != null && orders > 0)) {
            throw exception(ERROR_STORE_HAS_DATA);
        }
        storeMapper.deleteById(id);
    }

    public StoreDO getStore(Long id) {
        return storeMapper.selectById(id);
    }

    public List<StoreRespVO> getStoreList() {
        return storeMapper.selectListAll().stream()
                .map(store -> BeanUtils.toBean(store, StoreRespVO.class))
                .collect(Collectors.toList());
    }

    public PageResult<StoreRespVO> getStorePage(StorePageReqVO reqVO) {
        PageResult<StoreDO> page = storeMapper.selectPage(reqVO);
        List<StoreRespVO> list = page.getList().stream()
                .map(store -> BeanUtils.toBean(store, StoreRespVO.class))
                .collect(Collectors.toList());
        return new PageResult<>(list, page.getTotal());
    }

    private void validateExists(Long id) {
        if (id == null || storeMapper.selectById(id) == null) {
            throw exception(ERROR_STORE_NOT_EXIST);
        }
    }

    private void validateCodeUnique(Long id, String code) {
        if (code == null) {
            return;
        }
        StoreDO exists = storeMapper.selectByCode(code.trim());
        if (exists != null && !Objects.equals(exists.getId(), id)) {
            throw exception(ERROR_STORE_CODE_DUPLICATE);
        }
    }

}
