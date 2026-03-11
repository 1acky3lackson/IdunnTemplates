package com.jackyblackson.idunntemplates.backend;

import com.jackyblackson.idunntemplates.backend.commercial.service.NeteaseCrawlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@SpringBootApplication
@EnableScheduling
@RequiredArgsConstructor
@EntityScan({
        "com.jackyblackson.idunntemplates.core.domain",
        "com.jackyblackson.idunntemplates.backend.domain",
        "com.jackyblackson.idunntemplates.backend.commercial.entity"
})
public class BackendApplication {

    private final NeteaseCrawlerService crawlerService;

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}
