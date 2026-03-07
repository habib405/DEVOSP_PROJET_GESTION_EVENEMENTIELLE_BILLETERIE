package com.projet.gestion.evenementielle;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.mail.host=localhost",
        "spring.mail.port=3025",
        "jwt.secret=dGVzdFNlY3JldEtleVRoYXRJc0xvbmdFbm91Z2hGb3JITUFDMjU2QWxnb3JpdGht"
})
class ApplicationTests {

    @Test
    void contextLoads() {
    }

}
