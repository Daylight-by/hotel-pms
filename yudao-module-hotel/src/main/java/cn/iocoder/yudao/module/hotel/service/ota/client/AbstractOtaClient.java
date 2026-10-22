package cn.iocoder.yudao.module.hotel.service.ota.client;

/**
 * 真实 OTA 适配器骨架：携程/美团/飞猪/去哪儿分别实现签名与报文。
 */
public abstract class AbstractOtaClient implements OtaClient {

    protected abstract String sign(String appKey, String appSecret, String body);

    @Override
    public String otaCode() {
        return getClass().getSimpleName().replace("Client", "").toUpperCase();
    }

}
