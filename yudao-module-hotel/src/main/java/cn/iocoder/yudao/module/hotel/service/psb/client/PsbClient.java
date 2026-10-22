package cn.iocoder.yudao.module.hotel.service.psb.client;

import cn.iocoder.yudao.module.hotel.dal.dataobject.psb.PsbUploadLogDO;

/**
 * 公安旅馆业治安系统客户端。
 *
 * <p>本机没有真实公安专网，默认使用 {@link MockPsbClient}；
 * 真实厂商适配继承 {@link AbstractPsbClient} 填写签名与报文模板。</p>
 */
public interface PsbClient {

    String name();

    PsbUploadResult uploadCheckIn(PsbUploadLogDO data);

    PsbUploadResult uploadCheckOut(PsbUploadLogDO data);

    PsbUploadResult uploadChange(PsbUploadLogDO data);

}
