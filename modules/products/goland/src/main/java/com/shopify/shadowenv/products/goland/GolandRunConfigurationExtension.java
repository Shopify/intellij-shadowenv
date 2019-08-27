package com.shopify.shadowenv.products.goland;

import com.goide.execution.GoRunConfigurationBase;
import com.goide.execution.extension.GoRunConfigurationExtension;
import com.goide.project.GoModuleBuilder;
import com.goide.sdk.GoSdkUtil;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.RunnerSettings;
import com.shopify.shadowenv.utils.Shadowenv;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class GolandRunConfigurationExtension extends GoRunConfigurationExtension {
    @Override
    protected void patchCommandLine(@NotNull GoRunConfigurationBase goRunConfigurationBase, @Nullable RunnerSettings runnerSettings, @NotNull GeneralCommandLine generalCommandLine, @NotNull String s) throws ExecutionException {
        Shadowenv.evaluate(goRunConfigurationBase.getWorkingDirectory(), generalCommandLine.getEnvironment());
    }


    @Override
    public boolean isApplicableFor(@NotNull GoRunConfigurationBase goRunConfigurationBase) {
        return true;
    }

    @Override
    public boolean isEnabledFor(@NotNull GoRunConfigurationBase goRunConfigurationBase, @Nullable RunnerSettings runnerSettings) {
        return true;
    }
}
