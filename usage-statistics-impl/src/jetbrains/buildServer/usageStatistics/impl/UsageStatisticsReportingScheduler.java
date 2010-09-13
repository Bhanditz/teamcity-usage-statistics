/*
 * Copyright 2000-2010 JetBrains s.r.o.
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

package jetbrains.buildServer.usageStatistics.impl;

import java.util.Date;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.TeamCityProperties;
import jetbrains.buildServer.usageStatistics.UsageStatisticsReporter;
import jetbrains.buildServer.util.Dates;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class UsageStatisticsReportingScheduler extends BuildServerAdapter implements Runnable {
  @NotNull private static final Logger LOG = Logger.getLogger(UsageStatisticsReportingScheduler.class);

  private static final long CHECKING_PERIOD = Dates.ONE_HOUR;

  @NotNull public static final String USAGE_STATISTICS_REPORTING_PERIOD = "teamcity.usageStatistics.reporting.period";
  private static final long DEFAULT_USAGE_STATISTICS_REPORTING_PERIOD = Dates.ONE_DAY;

  @NotNull private final UsageStatisticsSettingsPersistor mySettingsPersistor;
  @NotNull private final UsageStatisticsReporter myStatisticsReporter;
  @NotNull private final ScheduledFuture<?> myTask;
  private final long myReportingPeriod;

  public UsageStatisticsReportingScheduler(@NotNull final SBuildServer server,
                                           @NotNull final ScheduledExecutorService executor,
                                           @NotNull final UsageStatisticsSettingsPersistor settingsPersistor,
                                           @NotNull final UsageStatisticsReporter statisticsReporter) {
    mySettingsPersistor = settingsPersistor;
    myStatisticsReporter = statisticsReporter;
    myTask = executor.scheduleAtFixedRate(this, CHECKING_PERIOD, CHECKING_PERIOD, TimeUnit.MILLISECONDS);
    myReportingPeriod = TeamCityProperties.getLong(USAGE_STATISTICS_REPORTING_PERIOD, DEFAULT_USAGE_STATISTICS_REPORTING_PERIOD);
    server.addListener(this);
  }

  @Override
  public void serverShutdown() {
    myTask.cancel(true);
  }

  public void run() {
    try {
      final UsageStatisticsSettings settings = mySettingsPersistor.loadSettings();
      if (settings.isReportingEnabled()) {
        final Date lastReportingDate = settings.getLastReportingDate();
        if (lastReportingDate == null || Dates.now().after(Dates.after(lastReportingDate, myReportingPeriod))) {
          if (myStatisticsReporter.reportStatistics()) {
            settings.setLastReportingDate(Dates.now());
            mySettingsPersistor.saveSettings(settings);
          }
        }
      }
    }
    catch (final Throwable e) {
      LOG.error("Cannot report usage statistics: ", e);
    }
  }
}