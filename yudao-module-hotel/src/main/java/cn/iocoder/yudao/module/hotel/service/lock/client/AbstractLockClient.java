package cn.iocoder.yudao.module.hotel.service.lock.client;

/**
 * 真实门锁适配器骨架：厂商通常走 TCP/HTTP 网关，按型号补协议。
 */
public abstract class AbstractLockClient implements HotelLockClient {

    protected abstract String buildRequest(String action, Object payload);

    @Override
    public String name() {
        return getClass().getSimpleName();
    }

}
