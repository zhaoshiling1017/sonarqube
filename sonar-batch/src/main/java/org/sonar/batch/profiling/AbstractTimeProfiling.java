/*
 * SonarQube, open source software quality management tool.
 * Copyright (C) 2008-2013 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * SonarQube is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * SonarQube is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.batch.profiling;

import org.sonar.api.utils.TimeUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractTimeProfiling {

  private final long startTime;

  private long totalTime;

  private Clock clock;

  public AbstractTimeProfiling(Clock clock) {
    this.clock = clock;
    this.startTime = clock.now();
  }

  public long startTime() {
    return startTime;
  }

  public void stop() {
    this.totalTime = clock.now() - startTime;
  }

  public long totalTime() {
    return totalTime;
  }

  public String totalTimeAsString() {
    return TimeUtils.formatDuration(totalTime);
  }

  public void setTotalTime(long totalTime) {
    this.totalTime = totalTime;
  }

  protected void add(AbstractTimeProfiling other) {
    this.setTotalTime(this.totalTime() + other.totalTime());
  }

  static <G extends AbstractTimeProfiling> Map<Object, G> sortByDescendingTotalTime(Map<?, G> unsorted) {
    List<Map.Entry<?, G>> entries =
        new ArrayList<Map.Entry<?, G>>(unsorted.entrySet());
    Collections.sort(entries, new Comparator<Map.Entry<?, G>>() {
      @Override
      public int compare(Map.Entry<?, G> o1, Map.Entry<?, G> o2) {
        return Long.valueOf(o2.getValue().totalTime()).compareTo(o1.getValue().totalTime());
      }
    });
    Map<Object, G> sortedMap = new LinkedHashMap<Object, G>();
    for (Map.Entry<?, G> entry : entries) {
      sortedMap.put(entry.getKey(), entry.getValue());
    }
    return sortedMap;
  }

  static <G extends AbstractTimeProfiling> List<G> truncate(Collection<G> sortedList) {
    int maxSize = 10;
    List<G> result = new ArrayList<G>(maxSize);
    int i = 0;
    for (G item : sortedList) {
      if (i++ >= maxSize || item.totalTime() == 0) {
        return result;
      }
      else {
        result.add(item);
      }
    }
    return result;
  }

  protected void println(String msg) {
    PhasesSumUpTimeProfiler.println(msg);
  }

  protected void println(String text, Double percent, AbstractTimeProfiling phaseProfiling) {
    PhasesSumUpTimeProfiler.println(text, percent, phaseProfiling);
  }

  protected void println(String text, AbstractTimeProfiling phaseProfiling) {
    println(text, null, phaseProfiling);
  }

}
