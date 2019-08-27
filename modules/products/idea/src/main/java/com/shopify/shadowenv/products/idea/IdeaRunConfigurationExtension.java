package com.shopify.shadowenv.products.idea;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.RunConfigurationExtension;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.externalSystem.service.execution.ExternalSystemRunConfiguration;
import com.shopify.shadowenv.utils.ReadOnceMap;
import com.shopify.shadowenv.utils.Shadowenv;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class IdeaRunConfigurationExtension extends RunConfigurationExtension {
    /**
     * Unlike other extensions the IDEA extension
     * calls this method instead of RunConfigurationExtensionBase#patchCommandLine method
     * that we could have used to update environment variables.
     */
    @Override
    public <T extends RunConfigurationBase> void updateJavaParameters(T configuration, JavaParameters params, RunnerSettings runnerSettings) throws ExecutionException {
        String workingDirectory = Shadowenv.getWorkingDirectory(configuration, params.getWorkingDirectory());
        if (workingDirectory == null) return;

        Map<String, String> sourceEnv = getSourceEnv(params);
        Shadowenv.evaluate(workingDirectory, sourceEnv);
        sourceEnv.forEach((k, v) -> Logger.getInstance(IdeaRunConfigurationExtension.class).warn(k + ": " + v));
        params.setEnv(sourceEnv);

        // The code below works based on assumptions about internal implementation of
        // ExternalSystemExecuteTaskTask and ExternalSystemExecutionSettings and therefore may break any time may it change
        // It seems to be the only way to get things working for run configurations such as Gradle, at least for now
        if (configuration instanceof ExternalSystemRunConfiguration) {
            ExternalSystemRunConfiguration ext = (ExternalSystemRunConfiguration) configuration;
            ext.getSettings().setEnv(new ReadOnceMap<>(sourceEnv, ext.getSettings().getEnv()));
        }
    }

    @NotNull
    private Map<String, String> getSourceEnv(JavaParameters params) {
        // Borrowed from com.intellij.openapi.projectRoots.JdkUtil
        return new GeneralCommandLine()
            .withEnvironment(params.getEnv())
            .withParentEnvironmentType(
                params.isPassParentEnvs() ? GeneralCommandLine.ParentEnvironmentType.CONSOLE : GeneralCommandLine.ParentEnvironmentType.NONE
            )
            .getEffectiveEnvironment();
    }

    @Override
    public boolean isApplicableFor(@NotNull RunConfigurationBase configuration) {
        return true;
    }
}
