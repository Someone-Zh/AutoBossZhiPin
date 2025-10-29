package com.someone.auto.process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.someone.auto.config.RequireConfig;
import com.someone.auto.service.PlaywrightWebDrive;

@Component
public class Delivery {
    private static final Logger log = LoggerFactory.getLogger(Delivery.class);

    private PlaywrightWebDrive playwrightWebDrive;
    private RequireConfig requireConfig;
    
    @Autowired
    public Delivery(PlaywrightWebDrive playwrightWebDrive, RequireConfig requireConfig) {
        this.playwrightWebDrive = playwrightWebDrive;
        this.requireConfig = requireConfig;
    }

    public void deliver() {
        log.info("开始投递职位...");
        playwrightWebDrive.refreshJob(requireConfig);
        // 投递逻辑实现
        // playwrightWebDrive.search(ProcessConfig.BossUrl+ ProcessConfig.JobUri, requireConfig);
        // while(true){
        //     playwrightWebDrive.getJobs().forEach(job -> {
        //         playwrightWebDrive.applyJob(job);
        //     });
        // }
    }

    
}
