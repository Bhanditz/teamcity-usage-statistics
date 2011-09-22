/*
 * Copyright 2000-2011 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.usageStatistics.impl.providers;

import jetbrains.buildServer.serverSide.SBuildRunnerDescriptor;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

abstract class BaseCoverageEngineUsageStatisticsProvider extends BaseExtensionUsageStatisticsProvider {
  @NotNull private final SBuildServer myServer;
  @NotNull private final Map<String, String> myEngineName2DisplayName = new HashMap<String, String>();

  protected BaseCoverageEngineUsageStatisticsProvider(@NotNull final SBuildServer server) {
    myServer = server;
  }

  protected void registerCoverageEngine(@NotNull final String engineName, @NotNull final String engineDisplayName) {
    myEngineName2DisplayName.put(engineName, engineDisplayName);
  }

  @Nullable
  protected abstract String getSelectedEngineName(@NotNull Map<String, String> parameters);

  @Override
  protected void collectUsages(@NotNull final UsagesCollectorCallback callback) {
    for (final SBuildType buildType : myServer.getProjectManager().getActiveBuildTypes()) {
      final Set<String> collectedEngines = new HashSet<String>();
      for (final SBuildRunnerDescriptor runner : buildType.getBuildRunners()) {
        final String engineName = getSelectedEngineName(runner.getParameters());
        if (engineName != null && !collectedEngines.contains(engineName)) {
          callback.addUsage(engineName, myEngineName2DisplayName.get(engineName));
          collectedEngines.add(engineName);
        }
      }
    }
  }

  @NotNull
  @Override
  protected String getValueTooltip() {
    return "Build configuration count (% of active build configurations)";
  }

  @Override
  protected int getTotalUsagesCount(@NotNull final Map<ExtensionType, Integer> extensionUsages) {
    return myServer.getProjectManager().getActiveBuildTypes().size();
  }
}