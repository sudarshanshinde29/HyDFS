package java.org.example;

import org.example.service.Log.ClientComponent;
import org.example.service.Log.LogServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class GrepTest {
    private static final Logger logger = LoggerFactory.getLogger(GrepTest.class);

    @BeforeAll
    static void setUp() throws IOException {

    }

    @AfterAll
    static void tearDown() {

    }

    /**
     * This test case Tests the end to end flow of distributed log querier.
     * @throws InterruptedException
     */
    @Test
    public void endToEndTest() throws InterruptedException {

        Thread serverThread = new Thread(() -> {
            try {
                LogServer s = new LogServer();
                logger.info("Starting Server in Grep Test");
                s.start();
            } catch (Exception e) {
               throw new RuntimeException("Error while running Server in GrepTest", e);
            }
        });
        serverThread.start();

        Thread.sleep(1000);
        ClientComponent c = new ClientComponent("127.0.0.1", 5001, "junitMachine", "grep -n \"GET\"");
        logger.info("Starting Client in Grep Test");
        c.run();
        logger.info("End to end flow successfully tested");

    }

}