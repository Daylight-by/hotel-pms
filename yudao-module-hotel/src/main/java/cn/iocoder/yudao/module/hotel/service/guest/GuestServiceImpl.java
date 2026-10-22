package cn.iocoder.yudao.module.hotel.service.guest;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hotel.controller.admin.guest.vo.GuestPageReqVO;
import cn.iocoder.yudao.module.hotel.controller.admin.guest.vo.GuestSaveReqVO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.guest.GuestDO;
import cn.iocoder.yudao.module.hotel.dal.mysql.guest.GuestMapper;
import cn.iocoder.yudao.module.hotel.service.psb.IdCardUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.hotel.enums.ErrorCodeConstants.GUEST_NOT_EXISTS;
import static cn.iocoder.yudao.module.hotel.enums.T5ErrorCodeConstants.ERROR_PSB_ID_CARD_INVALID;

@Service
@Validated
@Slf4j
public class GuestServiceImpl {

    @Resource
    private GuestMapper guestMapper;

    public Long createGuest(GuestSaveReqVO reqVO) {
        validateIdCard(reqVO.getIdCard());
        GuestDO guest = BeanUtils.toBean(reqVO, GuestDO.class);
        guestMapper.insert(guest);
        return guest.getId();
    }

    public void updateGuest(GuestSaveReqVO reqVO) {
        validateExists(reqVO.getId());
        validateIdCard(reqVO.getIdCard());
        guestMapper.updateById(BeanUtils.toBean(reqVO, GuestDO.class));
    }

    public void deleteGuest(Long id) {
        validateExists(id);
        guestMapper.deleteById(id);
    }

    public GuestDO getGuest(Long id) {        return guestMapper.selectById(id);
    }

    public PageResult<GuestDO> getGuestPage(GuestPageReqVO reqVO) {
        return guestMapper.selectPage(reqVO);
    }

    public List<GuestDO> getGuestList() {
        return guestMapper.selectListAll();
    }

    /**
     * 按手机号获取住客，不存在则创建（前台快速建档）
     */
    public GuestDO getOrCreateByMobile(String name, String mobile) {
        if (mobile != null && !mobile.isBlank()) {
            GuestDO exists = guestMapper.selectByMobile(mobile.trim());
            if (exists != null) {
                return exists;
            }
        }
        GuestDO guest = new GuestDO();
        guest.setName(name);
        guest.setMobile(mobile);
        guestMapper.insert(guest);
        return guest;
    }

    public List<GuestDO> getGuestListByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return guestMapper.selectBatchIds(ids);
    }

    private void validateExists(Long id) {
        if (id == null || guestMapper.selectById(id) == null) {
            throw exception(GUEST_NOT_EXISTS);
        }
    }

    /**
     * 身份证号非空时必须通过 18 位校验（T5 合规要求）。
     */
    private void validateIdCard(String idCard) {
        if (idCard != null && !idCard.isBlank() && !IdCardUtils.isCreatable(idCard)) {
            throw exception(ERROR_PSB_ID_CARD_INVALID);
        }
    }

    public boolean exists(Long id) {
        return id != null && Objects.nonNull(guestMapper.selectById(id));
    }
}
