package cn.iocoder.yudao.module.hotel.service.member;

import cn.hutool.crypto.digest.BCrypt;
import cn.iocoder.yudao.framework.common.biz.system.oauth2.OAuth2TokenCommonApi;
import cn.iocoder.yudao.framework.common.biz.system.oauth2.dto.OAuth2AccessTokenCreateReqDTO;
import cn.iocoder.yudao.framework.common.biz.system.oauth2.dto.OAuth2AccessTokenRespDTO;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hotel.controller.admin.member.vo.*;
import cn.iocoder.yudao.module.hotel.controller.app.member.vo.AppMemberInfoRespVO;
import cn.iocoder.yudao.module.hotel.controller.app.member.vo.AppMemberLoginReqVO;
import cn.iocoder.yudao.module.hotel.controller.app.member.vo.AppMemberLoginRespVO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.member.MemberDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.member.MemberPointsLogDO;
import cn.iocoder.yudao.module.hotel.dal.mysql.member.MemberMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.member.MemberPointsLogMapper;
import cn.iocoder.yudao.module.hotel.enums.MemberLevelEnum;
import cn.iocoder.yudao.module.hotel.enums.PointsTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.hotel.enums.HotelT2ErrorCodeConstants.*;

/**
 * 酒店会员 Service：注册登录、积分、储值、等级。
 */
@Service("hotelMemberService")
@Validated
@Slf4j
public class MemberService {

    /** 演示环境的固定短信验证码（未接入真实短信通道） */
    private static final String DEMO_SMS_CODE = "123456";

    private static final DateTimeFormatter MEMBER_NO_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Resource
    private MemberMapper memberMapper;
    @Resource
    private MemberPointsLogMapper memberPointsLogMapper;
    @Resource
    private OAuth2TokenCommonApi oauth2TokenApi;

    /** 每消费 1 元获得积分，默认 1 分 */
    @Value("${hotel.member.points-per-yuan:1}")
    private BigDecimal pointsPerYuan;

    /** 会员令牌使用的 OAuth2 客户端编号，默认复用系统内置的 default 客户端 */
    @Value("${hotel.member.token-client-id:default}")
    private String tokenClientId;

    // ============================================================ App 端：登录 / 信息

    /**
     * 手机号 + 验证码登录。未注册的手机号自动注册（会员免密注册）。
     */
    @Transactional(rollbackFor = Exception.class)
    public AppMemberLoginRespVO login(AppMemberLoginReqVO reqVO) {
        String mobile = reqVO.getMobile() == null ? "" : reqVO.getMobile().trim();
        if (mobile.isEmpty()) {
            throw exception(ERROR_MEMBER_LOGIN_FAIL);
        }
        MemberDO member = memberMapper.selectByMobile(mobile);
        if (member == null) {
            // 免密注册：P1 要求手机号即可成为会员
            member = new MemberDO();
            member.setMemberNo(generateMemberNo());
            member.setName("会员" + mobile.substring(Math.max(0, mobile.length() - 4)));
            member.setMobile(mobile);
            member.setLevel(MemberLevelEnum.NORMAL.getLevel());
            member.setPoints(0);
            member.setBalance(BigDecimal.ZERO);
            member.setTotalNights(0);
            member.setTotalAmount(BigDecimal.ZERO);
            member.setStatus(0);
            member.setRegisterTime(LocalDateTime.now());
            memberMapper.insert(member);
        }
        // 校验凭证：设置过密码的会员可用密码登录；否则校验验证码
        if (member.getPassword() != null && !member.getPassword().isEmpty()
                && reqVO.getPassword() != null && !reqVO.getPassword().isEmpty()) {
            if (!BCrypt.checkpw(reqVO.getPassword(), member.getPassword())) {
                throw exception(ERROR_MEMBER_PASSWORD_ERROR);
            }
        } else {
            String code = reqVO.getCode();
            if (code == null || !DEMO_SMS_CODE.equals(code.trim())) {
                throw exception(ERROR_MEMBER_LOGIN_FAIL);
            }
        }
        if (Objects.equals(member.getStatus(), 1)) {
            throw exception(ERROR_MEMBER_FROZEN);
        }
        OAuth2AccessTokenCreateReqDTO tokenReq = new OAuth2AccessTokenCreateReqDTO();
        tokenReq.setUserId(member.getId());
        tokenReq.setUserType(UserTypeEnum.MEMBER.getValue());
        tokenReq.setClientId(tokenClientId);
        tokenReq.setScopes(List.of("hotel.member"));
        OAuth2AccessTokenRespDTO token = oauth2TokenApi.createAccessToken(tokenReq);

        AppMemberLoginRespVO resp = new AppMemberLoginRespVO();
        resp.setAccessToken(token.getAccessToken());
        resp.setExpiresTime(token.getExpiresTime() == null ? null
                : token.getExpiresTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        resp.setMemberId(member.getId());
        resp.setMemberNo(member.getMemberNo());
        resp.setName(member.getName());
        resp.setMobile(member.getMobile());
        resp.setLevel(member.getLevel());
        resp.setLevelName(MemberLevelEnum.nameOf(member.getLevel()));
        resp.setPoints(member.getPoints());
        resp.setBalance(member.getBalance());
        resp.setRegisterTime(member.getRegisterTime());
        return resp;
    }

    public MemberDO validateMember(Long memberId) {
        MemberDO member = memberId == null ? null : memberMapper.selectById(memberId);
        if (member == null) {
            throw exception(ERROR_MEMBER_NOT_EXIST);
        }
        return member;
    }

    public AppMemberInfoRespVO getMemberInfo(Long memberId) {
        MemberDO member = validateMember(memberId);
        AppMemberInfoRespVO resp = new AppMemberInfoRespVO();
        resp.setMemberId(member.getId());
        resp.setMemberNo(member.getMemberNo());
        resp.setName(member.getName());
        resp.setMobile(member.getMobile());
        resp.setLevel(member.getLevel());
        resp.setLevelName(MemberLevelEnum.nameOf(member.getLevel()));
        resp.setPoints(member.getPoints());
        resp.setBalance(member.getBalance());
        resp.setTotalNights(member.getTotalNights());
        resp.setTotalAmount(member.getTotalAmount());
        resp.setRegisterTime(member.getRegisterTime());
        return resp;
    }

    public List<MemberPointsLogRespVO> getPointsLog(Long memberId) {
        return memberPointsLogMapper.selectListByMemberId(memberId).stream().map(log -> {
            MemberPointsLogRespVO vo = BeanUtils.toBean(log, MemberPointsLogRespVO.class);
            vo.setTypeName(PointsTypeEnum.nameOf(log.getType()));
            return vo;
        }).collect(Collectors.toList());
    }

    // ============================================================ 管理端：CRUD

    @Transactional(rollbackFor = Exception.class)
    public Long createMember(MemberSaveReqVO reqVO) {
        String mobile = reqVO.getMobile().trim();
        if (memberMapper.selectByMobile(mobile) != null) {
            throw exception(ERROR_MEMBER_MOBILE_EXIST);
        }
        MemberDO member = new MemberDO();
        member.setMemberNo(generateMemberNo());
        member.setName(reqVO.getName());
        member.setMobile(mobile);
        member.setPassword(reqVO.getPassword() == null || reqVO.getPassword().isEmpty()
                ? null : BCrypt.hashpw(reqVO.getPassword(), BCrypt.gensalt()));
        member.setLevel(reqVO.getLevel() == null ? MemberLevelEnum.NORMAL.getLevel() : reqVO.getLevel());
        member.setPoints(0);
        member.setBalance(BigDecimal.ZERO);
        member.setTotalNights(0);
        member.setTotalAmount(BigDecimal.ZERO);
        member.setStatus(reqVO.getStatus() == null ? 0 : reqVO.getStatus());
        member.setGuestId(reqVO.getGuestId());
        member.setRegisterTime(LocalDateTime.now());
        memberMapper.insert(member);
        return member.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateMember(MemberSaveReqVO reqVO) {
        MemberDO member = validateMember(reqVO.getId());
        if (!Objects.equals(member.getMobile(), reqVO.getMobile().trim())) {
            MemberDO exists = memberMapper.selectByMobile(reqVO.getMobile().trim());
            if (exists != null && !Objects.equals(exists.getId(), member.getId())) {
                throw exception(ERROR_MEMBER_MOBILE_EXIST);
            }
        }
        MemberDO update = new MemberDO();
        update.setId(member.getId());
        update.setName(reqVO.getName());
        update.setMobile(reqVO.getMobile().trim());
        update.setLevel(reqVO.getLevel());
        update.setStatus(reqVO.getStatus());
        update.setGuestId(reqVO.getGuestId());
        if (reqVO.getPassword() != null && !reqVO.getPassword().isEmpty()) {
            update.setPassword(BCrypt.hashpw(reqVO.getPassword(), BCrypt.gensalt()));
        }
        memberMapper.updateById(update);
    }

    public MemberRespVO getMember(Long id) {
        return buildResp(validateMember(id));
    }

    public PageResult<MemberRespVO> getMemberPage(MemberPageReqVO reqVO) {
        PageResult<MemberDO> page = memberMapper.selectPage(reqVO);
        List<MemberRespVO> list = page.getList().stream().map(this::buildResp).collect(Collectors.toList());
        return new PageResult<>(list, page.getTotal());
    }

    private MemberRespVO buildResp(MemberDO member) {
        MemberRespVO resp = BeanUtils.toBean(member, MemberRespVO.class);
        resp.setLevelName(MemberLevelEnum.nameOf(member.getLevel()));
        resp.setStatusName(Objects.equals(member.getStatus(), 1) ? "冻结" : "正常");
        return resp;
    }

    // ============================================================ 积分 / 储值

    /**
     * 调整积分（正加负减），并记录流水。
     */
    @Transactional(rollbackFor = Exception.class)
    public void adjustPoints(Long memberId, Integer points, Integer type, Long orderId, String remark) {
        MemberDO member = validateMember(memberId);
        int current = member.getPoints() == null ? 0 : member.getPoints();
        int newPoints = current + points;
        if (newPoints < 0) {
            throw exception(ERROR_MEMBER_POINTS_NOT_ENOUGH);
        }
        MemberDO update = new MemberDO();
        update.setId(memberId);
        update.setPoints(newPoints);
        memberMapper.updateById(update);

        MemberPointsLogDO log = new MemberPointsLogDO();
        log.setMemberId(memberId);
        log.setType(type);
        log.setPoints(points);
        log.setBalance(newPoints);
        log.setOrderId(orderId);
        log.setRemark(remark);
        memberPointsLogMapper.insert(log);
    }

    /**
     * 按订单实付金额累计积分（1 元 = N 分，可配），退房时调用。
     */
    @Transactional(rollbackFor = Exception.class)
    public int accruePointsForOrder(Long memberId, Long orderId, BigDecimal paidAmount) {
        if (memberId == null || paidAmount == null || paidAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        BigDecimal rate = pointsPerYuan == null ? BigDecimal.ONE : pointsPerYuan;
        int points = paidAmount.multiply(rate).setScale(0, RoundingMode.DOWN).intValue();
        if (points <= 0) {
            return 0;
        }
        adjustPoints(memberId, points, PointsTypeEnum.EARN.getType(), orderId,
                "订单实付 " + paidAmount + " 元，按 1 元=" + rate + " 分累计");
        // 累计消费与间夜，并按消费额升级
        MemberDO member = memberMapper.selectById(memberId);
        BigDecimal totalAmount = (member.getTotalAmount() == null ? BigDecimal.ZERO : member.getTotalAmount())
                .add(paidAmount);
        MemberDO update = new MemberDO();
        update.setId(memberId);
        update.setTotalAmount(totalAmount);
        update.setLevel(resolveLevel(totalAmount));
        memberMapper.updateById(update);
        return points;
    }

    @Transactional(rollbackFor = Exception.class)
    public void adjustBalance(Long memberId, BigDecimal amount, String remark) {
        MemberDO member = validateMember(memberId);
        BigDecimal current = member.getBalance() == null ? BigDecimal.ZERO : member.getBalance();
        BigDecimal newBalance = current.add(amount);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw exception(ERROR_MEMBER_BALANCE_NOT_ENOUGH);
        }
        MemberDO update = new MemberDO();
        update.setId(memberId);
        update.setBalance(newBalance);
        memberMapper.updateById(update);
        log.info("[adjustBalance] 会员 {} 余额 {} -> {}，{}", memberId, current, newBalance, remark);
    }

    private Integer resolveLevel(BigDecimal totalAmount) {
        if (totalAmount == null) {
            return MemberLevelEnum.NORMAL.getLevel();
        }
        if (totalAmount.compareTo(new BigDecimal("20000")) >= 0) {
            return MemberLevelEnum.PLATINUM.getLevel();
        }
        if (totalAmount.compareTo(new BigDecimal("5000")) >= 0) {
            return MemberLevelEnum.GOLD.getLevel();
        }
        if (totalAmount.compareTo(new BigDecimal("1000")) >= 0) {
            return MemberLevelEnum.SILVER.getLevel();
        }
        return MemberLevelEnum.NORMAL.getLevel();
    }

    private String generateMemberNo() {
        return "M" + LocalDateTime.now().format(MEMBER_NO_FORMAT)
                + String.format("%03d", new Random().nextInt(1000));
    }
}
