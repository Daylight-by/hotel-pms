package cn.iocoder.yudao.module.hotel.controller.admin.device;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.hotel.service.psb.IdCardUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 身份证读卡：真实读卡由本机 script/idreader/agent.js 通过 WebSocket 回填，
 * 后端只做解析/校验，Mock 模式返回固定测试身份证。
 */
@Tag(name = "管理后台 - 身份证读卡")
@RestController
@RequestMapping("/hotel/device")
@Validated
public class DeviceController {

    private static final String MOCK_ID_CARD = "330281199203151234";

    @PostMapping("/id-card/parse")
    @Operation(summary = "解析身份证读卡结果")
    @PreAuthorize("@ss.hasPermission('hotel:guest:create')")
    public CommonResult<Map<String, Object>> parse(@RequestBody(required = false) Map<String, Object> body) {
        String idCard = body == null ? null : (String) body.get("idCard");
        boolean mock = body == null || body.get("idCard") == null;
        String value = mock ? MOCK_ID_CARD : idCard;
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("mock", mock);
        result.put("idCard", value);
        result.put("name", mock ? "张三" : null);
        result.put("sex", IdCardUtils.parseSex(value));
        result.put("birthday", IdCardUtils.parseBirthday(value));
        result.put("nation", mock ? "汉" : null);
        result.put("address", mock ? "浙江省杭州市西湖区文三路 100 号" : null);
        result.put("valid", IdCardUtils.isValid(value));
        return success(result);
    }

}
