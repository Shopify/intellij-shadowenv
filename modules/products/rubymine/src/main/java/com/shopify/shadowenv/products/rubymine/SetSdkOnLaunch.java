package com.shopify.shadowenv.products.rubymine;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.startup.StartupActivity;
import com.shopify.shadowenv.utils.Shadowenv;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.ruby.ruby.sdk.LocalRubySdkSystemAccessor;
import org.jetbrains.plugins.ruby.ruby.sdk.RubySdkType;

import java.util.HashMap;
import java.util.Map;

public class SetSdkOnLaunch implements StartupActivity {

    private Logger logger;

    public SetSdkOnLaunch() {
        logger = Logger.getInstance(RubyMineRunConfigurationExtension.class);
    }

    @Override
    public void runActivity(@NotNull Project project) {
        // TODO: This looks like the right method
        // Also while this API takes a path; it looks like RubyMine looks up existing SDKs before adding one.
        Sdk sdk = SdkConfigurationUtil.createAndAddSDK("", RubySdkType.getInstance());

        // TODO: Pretty sure this should _set_ the SDK but idk if it will work
        ProjectRootManager.getInstance(project).setProjectSdk(sdk);

        // TODO: Consider watching .shadowenv.d to 👀 when it changes
        // TODO: Consider polling the Shadowenv class to watch for environment changes (e.g. if nix changes a path)
    }
}
