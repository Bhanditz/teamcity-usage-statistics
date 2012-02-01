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

import java.util.HashMap;
import java.util.Map;
import jetbrains.buildServer.issueTracker.IssuePluginsManager;
import jetbrains.buildServer.issueTracker.IssueProviderEx;
import jetbrains.buildServer.issueTracker.IssueProviderFactory;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsGroupPosition;
import jetbrains.buildServer.util.positioning.PositionAware;
import org.jetbrains.annotations.NotNull;

public class IssueTrackerUsageStatisticsProvider extends BaseExtensionUsageStatisticsProvider {
  @NotNull private final IssuePluginsManager myIssuePluginsManager;

  public IssueTrackerUsageStatisticsProvider(@NotNull final IssuePluginsManager issuePluginsManager) {
    myIssuePluginsManager = issuePluginsManager;
  }

  @NotNull
  @Override
  protected PositionAware getGroupPosition() {
    return UsageStatisticsGroupPosition.ISSUE_TRACKERS;
  }

  @Override
  protected void collectUsages(@NotNull final UsagesCollectorCallback callback) {
    final Map<String, String> issueProviderFactoryType2Name = new HashMap<String, String>();
    for (final IssueProviderEx issueProvider : myIssuePluginsManager.getProviders()) {
      final String type = issueProvider.getType();
      String name = issueProviderFactoryType2Name.get(type);
      if (name == null) {
        final IssueProviderFactory factory = myIssuePluginsManager.findFactoryByType(type);
        name = factory == null ? type : factory.getType();
        issueProviderFactoryType2Name.put(type, name);
      }
      callback.addUsage(type, name);
    }
  }

  @NotNull
  @Override
  protected String getValueTooltip() {
    return "Issue tracker connection count (% of all issue tracker connections)";
  }
}
