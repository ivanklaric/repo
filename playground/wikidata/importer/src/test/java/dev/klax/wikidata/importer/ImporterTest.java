package dev.klax.wikidata.importer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.test.ShellAssertions;
import org.springframework.shell.test.autoconfigure.ShellTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.shell.test.ShellTestClient;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

@ShellTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ImporterTest {

    @Autowired
    ShellTestClient client;
    @Test
    void testSingleEntityImport() {
        var session  = client.nonInterative(
                "import",
                "src/test/resources/single_entity.json")
                .run();

        await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            var screen = session.screen();
            ShellAssertions.assertThat(screen).containsText("Found sport");
            ShellAssertions.assertThat(screen).containsText("judo");
        });
    }
}
