package com.shopify.shadowenv.products.idea;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.RunConfigurationExtension;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.externalSystem.service.execution.ExternalSystemRunConfiguration;
import com.shopify.shadowenv.utils.ReadOnceMap;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Map;

public class IdeaRunConfigurationExtension extends RunConfigurationExtension {
    /**
     * Unlike other extensions the IDEA extension
     * calls this method instead of RunConfigurationExtensionBase#patchCommandLine method
     * that we could have used to update environment variables.
     */
    @Override
    public <T extends RunConfigurationBase> void updateJavaParameters(T configuration, JavaParameters params, RunnerSettings runnerSettings) throws ExecutionException {
        // Borrowed from com.intellij.openapi.projectRoots.JdkUtil
        Map<String, String> sourceEnv = new GeneralCommandLine()
            .withEnvironment(params.getEnv())
            .withParentEnvironmentType(
                params.isPassParentEnvs() ? GeneralCommandLine.ParentEnvironmentType.CONSOLE : GeneralCommandLine.ParentEnvironmentType.NONE
            )
            .getEffectiveEnvironment();
        try {
            JsonObject evs = getJsonEnv(params.getWorkingDirectory());
            for (Map.Entry<String, JsonElement> e : evs.entrySet()) {
                sourceEnv.put(e.getKey(), e.getValue().getAsString());
            }

        } catch (Exception e) {
            throw new ExecutionException(e);
        }

        params.setEnv(sourceEnv);



        // The code below works based on assumptions about internal implementation of
        // ExternalSystemExecuteTaskTask and ExternalSystemExecutionSettings and therefore may break any time may it change
        // It seems to be the only way to get things working for run configurations such as Gradle, at least for now
        if (configuration instanceof ExternalSystemRunConfiguration) {
            ExternalSystemRunConfiguration ext = (ExternalSystemRunConfiguration) configuration;

            ext.getSettings().setEnv(new ReadOnceMap<>(sourceEnv, ext.getSettings().getEnv()));
        }
    }

    private JsonObject getJsonEnv(String workingDirectory) throws ExecutionException {
        try {
            ProcessBuilder p = new ProcessBuilder("shadowenv", "hook", "--json", "");
            p.directory(new File(workingDirectory));
            Process proc = p.start();

            String out = IOUtils.toString(proc.getInputStream(), Charset.defaultCharset());
            String err = IOUtils.toString(proc.getErrorStream(), Charset.defaultCharset());

            if (!err.isEmpty()) {
                if (err.contains("untrusted")) {
                    throw new ExecutionException("untrusted shadowenv program: run shadowenv help trust for more info");
                } else {
                    throw new ExecutionException(err);
                }
            }

            Logger.getInstance(IdeaRunConfigurationExtension.class).debug("shadowenv output: " + out);

            JsonObject parsed = new JsonParser().parse(out).getAsJsonObject();
            return parsed.getAsJsonObject("exported");
        } catch (IOException e) {
            throw new ExecutionException(e);
        }
    }

    @Override
    public boolean isApplicableFor(@NotNull RunConfigurationBase configuration) {
        return true;
    }
}
