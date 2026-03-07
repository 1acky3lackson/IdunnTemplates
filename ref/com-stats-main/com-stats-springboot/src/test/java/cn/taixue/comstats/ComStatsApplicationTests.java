package cn.taixue.comstats;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:postgresql://localhost:5432/com_stats",
        "spring.jpa.hibernate.ddl-auto=none"
})
class ComStatsApplicationTests {

    @Test
    void contextLoads() {
    }

}
