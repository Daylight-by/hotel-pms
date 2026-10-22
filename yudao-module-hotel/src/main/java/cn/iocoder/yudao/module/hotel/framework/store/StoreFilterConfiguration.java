package cn.iocoder.yudao.module.hotel.framework.store;

import cn.iocoder.yudao.framework.datapermission.core.rule.DataPermissionRule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 门店上下文装配：Web 拦截器 + 数据权限规则 + INSERT 自动填充。
 */
@Configuration
@EnableScheduling
public class StoreFilterConfiguration implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new StoreContextInterceptor())
                .addPathPatterns("/**")
                .order(-100);
    }

    @Bean
    @ConditionalOnMissingBean(name = "storeDataPermissionRule")
    public DataPermissionRule storeDataPermissionRule() {
        return new StoreDataPermissionRule();
    }

    @Bean
    public StoreInsertInterceptor storeInsertInterceptor() {
        return new StoreInsertInterceptor();
    }

}
