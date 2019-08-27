package com.shopify.shadowenv.products.pycharm;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.RunnerSettings;
import com.jetbrains.python.run.AbstractPythonRunConfiguration;
import com.jetbrains.python.run.PythonRunConfigurationExtension;
import com.shopify.shadowenv.utils.Shadowenv;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class PyCharmRunConfigurationExtension extends PythonRunConfigurationExtension {
    @Override
    protected void patchCommandLine(@NotNull AbstractPythonRunConfiguration configuration, @Nullable RunnerSettings runnerSettings, @NotNull GeneralCommandLine cmdLine, @NotNull String runnerId) throws ExecutionException {
        Shadowenv.evaluate(configuration.getWorkingDirectorySafe(), cmdLine.getEnvironment());
    }

    @Override
    public boolean isApplicableFor(@NotNull AbstractPythonRunConfiguration configuration) {
        return true;
    }

    @Override
    public boolean isEnabledFor(@NotNull AbstractPythonRunConfiguration applicableConfiguration, @Nullable RunnerSettings runnerSettings) {
        return true;
    }
}
