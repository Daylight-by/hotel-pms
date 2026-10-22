package cn.iocoder.yudao.module.hotel.service.demo;

import java.util.Map;

/**
 * 演示数据生成器 Service
 *
 * <p>一次性生成 6 个月（可配 1-12 个月）约 400-600 笔演示订单：含住客、账务明细、支付流水、
 * 房价日历、清洁任务、夜审记录；固定随机种子保证可复现；数据全部打标（remark=T4-demo），
 * 一键清理只删本包数据，不触碰其它 agent 数据。</p>
 */
public interface DemoDataService {

    /**
     * 生成演示数据
     *
     * @param months 月数（1-12，默认 6）
     * @return 批次摘要
     */
    Map<String, Object> generate(int months);

    /**
     * 清理演示数据（幂等，可反复执行）
     */
    Map<String, Object> clean();

    /**
     * 演示数据状态
     */
    Map<String, Object> status();
}
