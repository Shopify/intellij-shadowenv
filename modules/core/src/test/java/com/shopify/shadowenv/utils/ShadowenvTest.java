package com.shopify.shadowenv.utils;

import com.google.gson.JsonObject;
import org.junit.Rule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

class ShadowenvTest {

    @Rule
    public TemporaryFolder projectFolder = new TemporaryFolder();

    @BeforeEach
    void setUp() throws IOException {
        projectFolder.create();
        projectFolder.newFolder(".shadowenv.d");
    }

    @AfterEach
    void tearDown() {
        projectFolder.delete();
    }

    @Test
    void modifyEnv() throws IOException, Shadowenv.ExecutionException, InterruptedException {
        File file = projectFolder.newFile(".shadowenv.d/test.lisp");
        try (FileWriter fw = new FileWriter(file)) {
            fw.write("(env/set \"FOO\" \"BAR\")\n");
            fw.write("(env/set \"HOME\" ())\n");
        }
        trust();


        Map<String, String> env = new HashMap<>();
        env.put("HOME", System.getenv("HOME"));
        env.put("BAZ", "123");
        Shadowenv.modifyEnv(projectFolder.getRoot().getAbsolutePath(), env);
        Assertions.assertEquals(2, env.entrySet().size());
        Assertions.assertEquals("BAR", env.get("FOO"));
        Assertions.assertEquals("123", env.get("BAZ"));
    }

    private void trust() throws IOException, InterruptedException {
        String[] envp = {"HOME=" + System.getenv("HOME")};
        Runtime.getRuntime().exec("shadowenv trust", envp, projectFolder.getRoot()).waitFor();
    }
}
