/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.blobstore.file.internal;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.annotation.Nullable;

/**
 * Helper methods for externalizing primitives to {@link ObjectOutput} instances.
 *
 * @since 3.0
 */
public class ExternalizationHelper
{
  /**
   * Writes a possibly null {@link Long} to an {@link ObjectOutput}. Read the value using {@link
   * #readNullableLong(ObjectInput)}.
   */
  public static void writeNullableLong(ObjectOutput out, Long value) throws IOException {
    out.writeBoolean(value != null);
    if (value != null) {
      out.writeLong(value);
    }
  }

  @Nullable
  public static Long readNullableLong(ObjectInput in) throws IOException {
    if (in.readBoolean()) {
      return in.readLong();
    }
    return null;
  }

  /**
   * Writes a possibly null {@link String} to an {@link ObjectOutput}. Read the value using {@link
   * #readNullableString(ObjectInput)}.
   */
  public static void writeNullableString(ObjectOutput out, String value) throws IOException {
    out.writeBoolean(value != null);
    if (value != null) {
      out.writeUTF(value);
    }
  }

  @Nullable
  public static String readNullableString(ObjectInput in) throws IOException {
    if (in.readBoolean()) {
      return in.readUTF();
    }
    return null;
  }
}
