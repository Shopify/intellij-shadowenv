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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
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
        ProcessBuilder p = new ProcessBuilder("shadowenv", "hook", "--json", "");
        p.directory(new File(params.getWorkingDirectory()));
        BufferedReader er, fr;
        StringBuilder txt = new StringBuilder();
        StringBuilder err = new StringBuilder();
        String line;
        try {
            Process proc = p.start();
            fr = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            er = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            while ((line = fr.readLine()) != null) {
                txt.append(line);
            }
            while ((line = er.readLine()) != null) {
                err.append(line);
            }
        } catch (Exception e) {
            throw new ExecutionException(e);
        }

        String errTxt = err.toString();

        if (!err.toString().isEmpty()) {
            if (errTxt.contains("untrusted")) {
                throw new ExecutionException("untrusted shadowenv program: run shadowenv help trust for more info");
            } else {
                throw new ExecutionException(errTxt);
            }
        }
        Logger.getInstance(IdeaRunConfigurationExtension.class).debug("shadowenv output: " + txt.toString());
        try {
            JsonObject parsed = new JsonParser().parse(txt.toString()).getAsJsonObject();
            JsonObject evs = parsed.getAsJsonObject("exported");
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

    @Override
    public boolean isApplicableFor(@NotNull RunConfigurationBase configuration) {
        return true;
    }
}
