package io.qameta.allure.history;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.AllureUtils;
import io.qameta.allure.Main;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import static io.qameta.allure.history.HistoryReader.HISTORY_TYPE;
import static io.qameta.allure.testdata.TestData.unpackDummyResources;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static ru.yandex.qatools.matchers.nio.PathMatchers.contains;

/**
 * @author Egor Borisov ehborisov@gmail.com
 */
public class HistoryFileTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private final ObjectMapper mapper = AllureUtils.createMapper();

    @Test
    public void generateHistoryForTestCaseWithoutId() throws Exception {
        String testName = "noIdTest";

        Path plugins = folder.newFolder().toPath();
        Main main = new Main(plugins, null);
        Path resultsDirectory = folder.newFolder().toPath();
        Path output = folder.newFolder().toPath();
        unpackDummyResources("allure2/", resultsDirectory);
        main.generate(output, resultsDirectory);
        assertThat(output, contains("index.html"));
        assertThat(output, contains("data"));
        Path data = output.resolve("data");
        assertThat(data, contains("history.json"));

        try (InputStream is = Files.newInputStream(data.resolve("history.json"))){
            Map<String, HistoryData> history = mapper.readValue(is, HISTORY_TYPE);
            Optional<Map.Entry<String, HistoryData>> historyEntry = history.entrySet().stream()
                    .filter(entry -> entry.getValue().getName().equals(testName)).findFirst();
            assertThat("Corresponding entry for test case without id is not found in history file",
                    historyEntry.isPresent(), equalTo(true));
        } catch (IOException e) {
            fail("Cannot read history.json file");
        }
    }
}
