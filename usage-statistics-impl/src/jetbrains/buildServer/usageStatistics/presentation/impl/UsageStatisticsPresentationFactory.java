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

package jetbrains.buildServer.usageStatistics.presentation.impl;

import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticPresentation;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsFormatter;
import jetbrains.buildServer.usageStatistics.presentation.formatters.DefaultFormatter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class UsageStatisticsPresentationFactory {
  @NotNull private static final String MISCELLANEOUS = "Miscellaneous";
  @NotNull private static final UsageStatisticsFormatter ourDefaultFormatter = new DefaultFormatter();

  @NotNull private final String myId;
  @Nullable private final String myDisplayName;
  @Nullable private final String myGroupName;
  @Nullable private final UsageStatisticsFormatter myFormatter;

  public UsageStatisticsPresentationFactory(@NotNull final String id,
                                            @Nullable final String displayName,
                                            @Nullable final String groupName,
                                            @Nullable final UsageStatisticsFormatter formatter) {
    myId = id;
    myDisplayName = displayName;
    myGroupName = groupName;
    myFormatter = formatter;
  }

  @NotNull
  public UsageStatisticPresentation createFor(@Nullable final Object value) {
    return new UsageStatisticsPresentationImpl(
      getNotNull(myDisplayName, myId),
      getNotNull(myGroupName, MISCELLANEOUS),
      getNotNull(myFormatter, ourDefaultFormatter).format(value)
    );
  }

  @NotNull
  private static <T> T getNotNull(@Nullable final T value, @NotNull final T defaultValue) {
    return value == null ? defaultValue : value;
  }
}