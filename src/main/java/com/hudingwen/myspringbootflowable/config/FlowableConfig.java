package com.hudingwen.myspringbootflowable.config;

import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;

/**
 * ClassName:FlowableConfig
 * Package:com.hudingwen.myspringbootflowable.config
 * Description:描述
 *
 * @Date:2022/12/22 16:09
 * @Author:胡丁文
 * @E-mail:admin@aiwanyun.cn
 **/
@Configuration
public class FlowableConfig implements EngineConfigurationConfigurer<SpringProcessEngineConfiguration> {
    @Override
    public void configure(SpringProcessEngineConfiguration engineConfiguration) {
        //设置Flowable画图不乱吗
        engineConfiguration.setActivityFontName("微软雅黑");
        engineConfiguration.setLabelFontName("微软雅黑");
        engineConfiguration.setAnnotationFontName("微软雅黑");
    }
}
