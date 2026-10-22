package cn.iocoder.yudao.module.hotel.service.psb.client;

/**
 * 真实公安上传适配器骨架。
 *
 * <p>厂商差异集中在 {@link #buildPayload} 与 {@link #sign}；
 * 真实模式需要公安专网/边界接入，本机不启用。</p>
 */
public abstract class AbstractPsbClient implements PsbClient {

    protected abstract String buildPayload(Object data);

    protected abstract String sign(String payload, String secret);

    @Override
    public String name() {
        return getClass().getSimpleName();
    }

}
