//package com.shopify.shadowenv.products.rubymine;
//
//import com.intellij.execution.*;
//import com.intellij.execution.configurations.GeneralCommandLine;
//import com.intellij.execution.configurations.RunnerSettings;
//import com.intellij.openapi.diagnostic.Logger;
//import com.intellij.openapi.projectRoots.ProjectJdkTable;
//import com.intellij.openapi.projectRoots.Sdk;
//import com.shopify.shadowenv.utils.Shadowenv;
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//import org.jetbrains.plugins.ruby.ruby.RModuleUtil;
//import org.jetbrains.plugins.ruby.ruby.run.configuration.AbstractRubyRunConfiguration;
//import org.jetbrains.plugins.ruby.ruby.run.configuration.RubyRunConfigurationExtension;
//
//import java.util.HashMap;
//import java.util.Map;
//
//public class RubyMineRunConfigurationExtension extends RubyRunConfigurationExtension {
//
//    @Override
//    protected void patchCommandLine(@NotNull AbstractRubyRunConfiguration configuration, @Nullable RunnerSettings runnerSettings, @NotNull GeneralCommandLine cmdLine, @NotNull String runnerId) throws ExecutionException {
//        Logger l = Logger.getInstance(RubyMineRunConfigurationExtension.class);
//
//        Map<String, String> gc = new GeneralCommandLine()
//                .withEnvironment(configuration.getEnvs())
//                .withParentEnvironmentType(
//                        configuration.isPassParentEnvs() ? GeneralCommandLine.ParentEnvironmentType.CONSOLE : GeneralCommandLine.ParentEnvironmentType.NONE
//                )
//                .getEffectiveEnvironment();
//
//        updateConfig(configuration, gc);
//        String version = gc.get("RUBY_VERSION");
//        if (version == null) {
//            l.error("could not find RUBY_VERSION in parsed output");
//            return;
//        }
//        l.warn("name: " + configuration.getModule().getName());
//        l.warn("version: " + configuration.getSdk().getVersionString());
//        // TODO(tb): we probably don't want to patch the command line like this. It'll run
//        // the correct ruby, but something else down the line seems to be resetting the env vars
//        cmdLine.setExePath(cmdLine.getExePath().replaceAll("[0-9]+\\.[0-9]+\\.[0-9]+", version));
//        cmdLine.withEnvironment(gc);
//    }
//
//    @Override
//    public boolean isApplicableFor(@NotNull AbstractRubyRunConfiguration configuration) {
//        return true;
//    }
//
//    @Override
//    public boolean isEnabledFor(@NotNull AbstractRubyRunConfiguration applicableConfiguration, @Nullable RunnerSettings runnerSettings) {
//        return true;
//    }
//
//    @Override
//    protected void extendCreatedConfiguration(@NotNull AbstractRubyRunConfiguration<?> configuration, @NotNull Location location) {
//        updateConfig(configuration, new HashMap<>());
//        super.extendCreatedConfiguration(configuration, location);
//    }
//
//    private void updateConfig(@NotNull AbstractRubyRunConfiguration<?> configuration, Map<String, String> env) {
//        Logger l = Logger.getInstance(RubyMineRunConfigurationExtension.class);
//        Map<String, String> m = new HashMap<>(env);
//        try {
//            Shadowenv.evaluate(configuration.getProject().getBasePath(), m);
//        } catch (Exception e) { l.error(e.getMessage()); }
//        env.clear();
//        env.putAll(m);
//
//        String curName = configuration.getSdk().getName();
//        String rv = m.get("RUBY_VERSION");
//        if (curName.contains(rv)) {
//            return;
//        }
//        l.info("found new ruby version: " + rv);
//        l.info("current sdk: " + curName);
//        Sdk sdk = findSdk("chruby: " + rv);
//        if (sdk == null) {
//            l.error("couldn't find sdk");
//            return;
//        }
//        RModuleUtil.getInstance().changeModuleSdk(sdk, configuration.getModule());
//        configuration.setShouldUseAlternativeSdk(true);
//        configuration.setAlternativeSdkName(sdk.getName());
//        l.info("set sdk to: " + configuration.getSdk().getName());
//    }
//
//    @Nullable
//    private Sdk findSdk(String name) {
//        Sdk sdk = null;
//        for (Sdk s : ProjectJdkTable.getInstance().getAllJdks()) {
//            if (!s.getName().contains(name)) {
//                continue;
//            }
//            sdk = s;
//            break;
//        }
//        return sdk;
//    }
//}
