package com.shopify.shadowenv.products.idea;

import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.openapi.diagnostic.Logger;
import org.apache.log4j.Level;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class IdeaRunConfigurationExtensionTest {
    private static final String SECRET = "supersecret-value-for-test";

    @Rule
    public TemporaryFolder projectFolder = new TemporaryFolder();

    private final List<String> logRecords = Collections.synchronizedList(new ArrayList<>());
    private Logger.Factory previousFactory;

    @Before
    public void setUp() throws Exception {
        previousFactory = Logger.getFactory();
        Logger.setFactory(category -> new CapturingLogger(logRecords));

        File shadowenvDir = projectFolder.newFolder(".shadowenv.d");
        try (FileWriter fw = new FileWriter(new File(shadowenvDir, "test.lisp"))) {
            fw.write("(env/set \"FOO\" \"BAR\")\n");
        }
        String[] envp = {"HOME=" + System.getenv("HOME")};
        Runtime.getRuntime().exec("shadowenv trust", envp, projectFolder.getRoot()).waitFor();
    }

    @After
    public void tearDown() {
        Logger.setFactory(previousFactory);
    }

    @Test
    public void doesNotLogRunConfigurationEnvironmentValues() throws Exception {
        Map<String, String> env = new HashMap<>();
        env.put("API_TOKEN", SECRET);

        JavaParameters params = new JavaParameters();
        params.setWorkingDirectory(projectFolder.getRoot().getAbsolutePath());
        params.setPassParentEnvs(false);
        params.setEnv(env);

        new IdeaRunConfigurationExtension().<RunConfigurationBase>updateJavaParameters(null, params, null);

        // The environment is still injected into the run configuration.
        assertEquals(SECRET, params.getEnv().get("API_TOKEN"));
        assertEquals("BAR", params.getEnv().get("FOO"));

        // But no value from it reaches the IDE log.
        synchronized (logRecords) {
            for (String record : logRecords) {
                assertFalse("IDE log contains a run-configuration secret: " + record, record.contains(SECRET));
            }
        }
    }

    private static final class CapturingLogger extends Logger {
        private final List<String> records;

        CapturingLogger(List<String> records) {
            this.records = records;
        }

        private void record(String level, String message) {
            records.add(level + " " + message);
        }

        // Default IDE log settings: debug is off, so only unconditional records are captured.
        @Override public boolean isDebugEnabled() { return false; }
        @Override public void debug(String message) { record("DEBUG", message); }
        @Override public void debug(Throwable t) { record("DEBUG", String.valueOf(t)); }
        @Override public void debug(String message, Throwable t) { record("DEBUG", message); }
        @Override public void info(String message) { record("INFO", message); }
        @Override public void info(String message, Throwable t) { record("INFO", message); }
        @Override public void warn(String message, Throwable t) { record("WARN", message); }
        @Override public void error(String message, Throwable t, String... details) { record("ERROR", message); }
        @Override public void setLevel(Level level) { }
    }
}
