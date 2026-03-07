package cn.taixue.comstats;

import cn.taixue.comstats.service.NeteaseCrawlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@EnableScheduling
@SpringBootApplication
@RequiredArgsConstructor
public class ComStatsApplication {

    private final NeteaseCrawlerService crawlerService;

    public static void main(String[] args) {
        SpringApplication.run(ComStatsApplication.class, args);
    }

    @Scheduled(fixedDelayString = "${app.crawler.interval-ms}")
    public void scheduleCrawl() {
        log.info("Starting scheduled crawl");
        try {
            crawlerService.crawlAndSave();
        } catch (Exception e) {
            log.error("Scheduled crawl failed", e);
        }
        log.info("Scheduled crawl finished");
    }
}
