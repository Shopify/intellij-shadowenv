package com.shopify.shadowenv.products.rubymine;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.RunnerSettings;
import com.shopify.shadowenv.utils.Shadowenv;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.ruby.run.configuration.AbstractRubyRunConfiguration;
import org.jetbrains.plugins.ruby.ruby.run.configuration.RubyRunConfigurationExtension;

import java.util.Map;

public class RubyMineRunConfigurationExtension extends RubyRunConfigurationExtension {

    @Override
    protected void patchCommandLine(@NotNull AbstractRubyRunConfiguration configuration, @Nullable RunnerSettings runnerSettings, @NotNull GeneralCommandLine cmdLine, @NotNull String runnerId) throws ExecutionException {
        Map<String, String> currentEnv = cmdLine.getEnvironment();
        Shadowenv.evaluate(configuration.getWorkingDirectory(), currentEnv);
        currentEnv.putAll(currentEnv);
    }

    @Override
    public boolean isApplicableFor(@NotNull AbstractRubyRunConfiguration configuration) {
        return true;
    }

    @Override
    public boolean isEnabledFor(@NotNull AbstractRubyRunConfiguration applicableConfiguration, @Nullable RunnerSettings runnerSettings) {
        return true;
    }
}
