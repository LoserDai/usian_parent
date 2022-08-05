package com.usian.config;

import com.usian.job.OrderJob;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

/**
 * @author Loser
 * @date 2021年12月04日 14:53
 */
@Configuration
public class QuartzConfig{

    /**
     *干什么事
     */
    @Bean
    public MethodInvokingJobDetailFactoryBean methodInvokingJobDetailFactoryBean(OrderJob orderJob){
        MethodInvokingJobDetailFactoryBean jobDetailFactoryBean = new MethodInvokingJobDetailFactoryBean();
        jobDetailFactoryBean.setTargetObject(orderJob);
        jobDetailFactoryBean.setTargetMethod("closeTimeOutOrder");
        return jobDetailFactoryBean;
    }

    /**什么时间
     * @param jobDetail
     * @return
     */
    @Bean
    public CronTriggerFactoryBean cronTriggerFactoryBean(MethodInvokingJobDetailFactoryBean jobDetail){
        CronTriggerFactoryBean cronTriggerFactoryBean = new CronTriggerFactoryBean();
        cronTriggerFactoryBean.setJobDetail(jobDetail.getObject());
        //每十秒定时任务删除一次
        cronTriggerFactoryBean.setCronExpression("0/10 * * * * ?");
        return cronTriggerFactoryBean;
    }

    /**
     * 什么时间做什么事
     * @param triggerFactoryBean
     * @return
     */
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(CronTriggerFactoryBean triggerFactoryBean){
        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
        schedulerFactoryBean.setTriggers(triggerFactoryBean.getObject());
        return schedulerFactoryBean;
    }
}
