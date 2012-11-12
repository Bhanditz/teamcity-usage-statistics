/*
 * Copyright 2000-2012 JetBrains s.r.o.
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

import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.usageStatistics.impl.GetRequestDetector;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsGroupPosition;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.util.positioning.PositionAware;
import jetbrains.buildServer.web.util.WebUtil;
import nl.bitwalker.useragentutils.*;
import org.jetbrains.annotations.NotNull;

public class BrowserUsageStatisticsProvider extends BaseToolUsersUsageStatisticsProvider implements WebUsersProvider, GetRequestDetector.Listener {
  public BrowserUsageStatisticsProvider(@NotNull final SBuildServer server,
                                        @NotNull final ServerPaths serverPaths,
                                        @NotNull final GetRequestDetector getRequestDetector) {
    super(server, serverPaths, createDWMPeriodDescriptions());
    getRequestDetector.addListener(this);
  }

  @NotNull
  @Override
  protected PositionAware getGroupPosition() {
    return UsageStatisticsGroupPosition.WEB_BROWSERS;
  }

  @NotNull
  public Set<String> getWebUsers(final long fromTimestamp) {
    return getUsers(fromTimestamp);
  }

  public void onGetRequest(@NotNull final HttpServletRequest request, @NotNull final SUser user) {
    final String userAgentString = WebUtil.getUserAgent(request);
    if (userAgentString == null) return;

    final UserAgent userAgent = UserAgent.parseUserAgentString(userAgentString);

    final Browser browser = userAgent.getBrowser();
    final BrowserType browserType = browser.getBrowserType();
    if (!isBrowser(browserType)) return;

    String name = getBrowserGroupIfNeeded(browser, browserType).getName(); // do not report version for non-IE web browsers

    final OperatingSystem operatingSystem = userAgent.getOperatingSystem();
    if (operatingSystem.getDeviceType() != DeviceType.COMPUTER) { // do not specify OS for desktop browsers
      name += " (" + operatingSystem.getGroup().getName() + ")";
    }

    addUsage(name, user.getId());
  }

  private static boolean isBrowser(@NotNull final BrowserType browserType) {
    return browserType == BrowserType.WEB_BROWSER || browserType == BrowserType.MOBILE_BROWSER || browserType == BrowserType.TEXT_BROWSER;
  }

  @NotNull
  private static Browser getBrowserGroupIfNeeded(@NotNull final Browser browser, @NotNull final BrowserType browserType) {
    final Browser group = browser.getGroup();
    return group == Browser.IE || browserType != BrowserType.WEB_BROWSER ? browser : group;
  }

  @NotNull
  @Override
  protected String getExternalId() {
    return "browsersUsage";
  }

  @NotNull
  @Override
  protected String getToolName() {
    return "browser";
  }

  @NotNull
  @Override
  protected String getToolIdName() {
    return "name";
  }

  @Override
  protected boolean publishToolUsages(@NotNull final String path) {
    return true;
  }

  @NotNull
  @Override
  protected String getValueTooltip() {
    return "User count (% of web users)";
  }
}