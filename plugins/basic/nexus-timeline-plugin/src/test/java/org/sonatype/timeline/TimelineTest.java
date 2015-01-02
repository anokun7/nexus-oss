/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.timeline;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class TimelineTest
    extends AbstractTimelineTestCase
{

  protected File persistDirectory;

  protected File indexDirectory;

  @Override
  public void setUp()
      throws Exception
  {
    super.setUp();

    persistDirectory = new File(getBasedir(), "target/persist");
    cleanDirectory(persistDirectory);
    indexDirectory = new File(getBasedir(), "target/index");
    cleanDirectory(indexDirectory);
  }

  @Test
  public void testConfigureTimeline()
      throws Exception
  {
    timeline.start(new TimelineConfiguration(persistDirectory, indexDirectory));
  }

  @Test
  public void testSimpleAddAndRetrieve()
      throws Exception
  {
    timeline.start(new TimelineConfiguration(persistDirectory, indexDirectory));

    Map<String, String> data = new HashMap<String, String>();
    data.put("k1", "v1");
    data.put("k2", "v2");
    data.put("k3", "v3");
    timeline.add(createTimelineRecord(System.currentTimeMillis(), "typeA", "subType", data));

    Set<String> types = new HashSet<String>();
    types.add("typeA");
    AsList cb = new AsList();
    timeline.retrieve(0, 10, types, null, null, cb);
    List<TimelineRecord> results = cb.getRecords();

    assertEquals(1, results.size());
    assertEquals(data, results.get(0).getData());
  }

  @Test
  public void testRepairIndexCouldNotRead()
      throws Exception
  {
    File crashedPersistDir = new File(getBasedir(), "target/test-classes/crashed-could-not-read/persist");
    File carshedIndexDir = new File(getBasedir(), "target/test-classes/crashed-could-not-read/index");
    FileUtils.copyDirectory(crashedPersistDir, persistDirectory);
    FileUtils.copyDirectory(carshedIndexDir, indexDirectory);

    // as time passes, the timestamps in test persist files will fall out of default 30 day, so we say restore all
    timeline.start(new TimelineConfiguration(persistDirectory, indexDirectory,
        TimelineConfiguration.DEFAULT_ROLLING_INTERVAL_MILLIS,
        Integer.MAX_VALUE));

    {
      // all records have this type, so we should have them as many as we asked for but max 100
      // as many records are in test persist directory
      AsList cb = new AsList();
      timeline.retrieve(0, 1000, Collections.singleton("type"), null, null, cb);
      List<TimelineRecord> results = cb.getRecords();
      assertEquals(100, results.size());
    }
    {
      // no records have this type, so we should have 0
      AsList cb = new AsList();
      timeline.retrieve(0, 10, Collections.singleton("badtype"), null, null, cb);
      List<TimelineRecord> results = cb.getRecords();
      assertEquals(0, results.size());
    }
  }

  @Test
  public void testRepairIndexCouldNotRetrieve()
      throws Exception
  {
    File crashedPersistDir = new File(getBasedir(), "target/test-classes/crashed-could-not-retrieve/persist");
    File carshedIndexDir = new File(getBasedir(), "target/test-classes/crashed-could-not-retrieve/index");
    FileUtils.copyDirectory(crashedPersistDir, persistDirectory);
    FileUtils.copyDirectory(carshedIndexDir, indexDirectory);

    // as time passes, the timestamps in test persist files will fall out of default 30 day, so we say restore all
    timeline.start(new TimelineConfiguration(persistDirectory, indexDirectory,
        TimelineConfiguration.DEFAULT_ROLLING_INTERVAL_MILLIS,
        Integer.MAX_VALUE));

    {
      // all records have this type, so we should have them as many as we asked for but max 100
      // as many records are in test persist directory
      AsList cb = new AsList();
      timeline.retrieve(0, 1000, Collections.singleton("type"), null, null, cb);
      List<TimelineRecord> results = cb.getRecords();
      assertEquals(100, results.size());
    }
    {
      // no records have this type, so we should have 0
      AsList cb = new AsList();
      timeline.retrieve(0, 10, Collections.singleton("badtype"), null, null, cb);
      List<TimelineRecord> results = cb.getRecords();
      assertEquals(0, results.size());
    }
  }

  @Test
  public void testRepairIndexCouldNotAdd()
      throws Exception
  {
    File persistDir = new File(getBasedir(), "target/test-classes/crashed-could-not-add/persist");
    File goodIndexDir = new File(getBasedir(), "target/test-classes/crashed-could-not-add/index-good");
    File crashedIndexDir = new File(getBasedir(), "target/test-classes/crashed-could-not-add/index-broken");
    FileUtils.copyDirectory(persistDir, persistDirectory);
    FileUtils.copyDirectory(goodIndexDir, indexDirectory);

    // as time passes, the timestamps in test persist files will fall out of default 30 day, so we say restore all
    timeline.start(new TimelineConfiguration(persistDirectory, indexDirectory,
        TimelineConfiguration.DEFAULT_ROLLING_INTERVAL_MILLIS,
        Integer.MAX_VALUE));

    {
      // add, this should pass without any exception
      Map<String, String> data = new HashMap<String, String>();
      data.put("k1", "v1");
      data.put("k2", "v2");
      data.put("k3", "v3");
      timeline.add(createTimelineRecord(System.currentTimeMillis(), "typeA", "subType", data));
    }

    // pretend that when timeline is running, the index is manually changed
    timeline.stop();
    cleanDirectory(indexDirectory);
    FileUtils.copyDirectory(crashedIndexDir, indexDirectory);
    // as time passes, the timestamps in test persist files will fall out of default 30 day, so we say restore all
    timeline.start(new TimelineConfiguration(persistDirectory, indexDirectory,
        TimelineConfiguration.DEFAULT_ROLLING_INTERVAL_MILLIS,
        Integer.MAX_VALUE));

    {
      // add again, this should also pass without any exception
      Map<String, String> data = new HashMap<String, String>();
      data.put("k1", "v1");
      data.put("k2", "v2");
      data.put("k3", "v3");
      timeline.add(createTimelineRecord(System.currentTimeMillis(), "typeA", "subType", data));
    }
  }

  @Test
  public void testRepairIndexCouldNotPurge()
      throws Exception
  {
    File persistDir = new File(getBasedir(), "target/test-classes/crashed-could-not-purge/persist");
    File goodIndexDir = new File(getBasedir(), "target/test-classes/crashed-could-not-purge/index-good");
    File crashedIndexDir = new File(getBasedir(), "target/test-classes/crashed-could-not-purge/index-broken");
    FileUtils.copyDirectory(persistDir, persistDirectory);
    FileUtils.copyDirectory(goodIndexDir, indexDirectory);

    // as time passes, the timestamps in test persist files will fall out of default 30 day, so we say restore all
    timeline.start(new TimelineConfiguration(persistDirectory, indexDirectory,
        TimelineConfiguration.DEFAULT_ROLLING_INTERVAL_MILLIS,
        Integer.MAX_VALUE));

    // here, the "good" index really contains 6 records only
    assertEquals(6, timeline.purgeOlderThan(0));

    // pretend that when timeline is running, the index is manually changed
    timeline.stop();
    cleanDirectory(indexDirectory);
    FileUtils.copyDirectory(crashedIndexDir, indexDirectory);
    // as time passes, the timestamps in test persist files will fall out of default 30 day, so we say restore all
    timeline.start(new TimelineConfiguration(persistDirectory, indexDirectory,
        TimelineConfiguration.DEFAULT_ROLLING_INTERVAL_MILLIS,
        Integer.MAX_VALUE));
    assertEquals(47, timeline.purgeOlderThan(0));
  }
}
