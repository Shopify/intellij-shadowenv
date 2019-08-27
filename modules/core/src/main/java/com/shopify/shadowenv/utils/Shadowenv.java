package com.shopify.shadowenv.utils;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Map;

import com.intellij.execution.ExecutionException;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.execution.configurations.SimpleProgramParameters;
import com.intellij.openapi.diagnostic.Logger;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

public class Shadowenv {
    public static class UntrustedException extends ExecutionException {
        UntrustedException() {
            super("untrusted shadowenv program: run shadowenv help trust for more info");
        }
    }

    public static void evaluate(String pwd, Map<String, String> env) throws ExecutionException {
        GeneralCommandLine gc = new GeneralCommandLine();
        gc = gc.withEnvironment(env).
                withCharset(Charset.defaultCharset()).
                withExePath("shadowenv").
                withWorkDirectory(pwd);

        gc.addParameters("hook", "--json", "");
        Process proc = gc.createProcess();
        String out, err;
        try {
            out = IOUtils.toString(proc.getInputStream(), Charset.defaultCharset());
            err = IOUtils.toString(proc.getErrorStream(), Charset.defaultCharset());
        } catch (Exception e) {
            throw new ExecutionException(e);
        }

        if (!err.isEmpty()) {
            if (err.contains("untrusted")) {
                throw new UntrustedException();
            } else {
                throw new ExecutionException(err);
            }
        }
        try {
            JsonObject parsed = new JsonParser().parse(out).getAsJsonObject();
            JsonObject evs = parsed.getAsJsonObject("exported");
            for (Map.Entry<String, JsonElement> e : evs.entrySet()) {
                if (e.getValue() == null) {
                    env.remove(e.getKey());
                } else {
                    Logger.getInstance(Shadowenv.class).warn(e.getKey() + ": " + e.getValue());
                    env.put(e.getKey(), e.getValue().getAsString());
                }
            }
        } catch (Exception e) {
            throw new ExecutionException(e);
        }
    }

    public static <T extends RunConfigurationBase> String getWorkingDirectory(T configuration) {
        String bp = configuration.getProject().getBasePath();
        if (bp != null) {
            return bp;
        }

        return new File(".").getAbsolutePath();
    }

    public static <T extends RunConfigurationBase> String getWorkingDirectory(T configuration, String dir) {
        if (dir != null && dir.length() > 0) {
            return dir;
        }

        String bp = configuration.getProject().getBasePath();
        if (bp != null) {
            return bp;
        }

        return new File(".").getAbsolutePath();
    }

    @NotNull
    public static <T extends SimpleProgramParameters> Map<String, String> getSourceEnv(T params) {
        // Borrowed from com.intellij.openapi.projectRoots.JdkUtil
        return new GeneralCommandLine()
                .withEnvironment(params.getEnv())
                .withParentEnvironmentType(
                        params.isPassParentEnvs() ? GeneralCommandLine.ParentEnvironmentType.CONSOLE : GeneralCommandLine.ParentEnvironmentType.NONE
                )
                .getEffectiveEnvironment();
    }

    @NotNull
    public static Map<String, String> getSourceEnv(Map<String, String> existingEnv, boolean passParent) {
        // Borrowed from com.intellij.openapi.projectRoots.JdkUtil
        return new GeneralCommandLine()
                .withEnvironment(existingEnv)
                .withParentEnvironmentType(
                        passParent ? GeneralCommandLine.ParentEnvironmentType.CONSOLE : GeneralCommandLine.ParentEnvironmentType.NONE
                )
                .getEffectiveEnvironment();
    }
}
