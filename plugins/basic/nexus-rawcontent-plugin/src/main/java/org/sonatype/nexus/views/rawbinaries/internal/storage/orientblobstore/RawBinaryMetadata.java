/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.views.rawbinaries.internal.storage.orientblobstore;

/**
 * I suppose this is a fledgling prototypical component metadata.
 *
 * @since 3.0
 */
public class RawBinaryMetadata
{
  private String path;

  private String blobId;

  private String contentType;

  public RawBinaryMetadata(final String path, final String blobId, final String contentType) {
    this.path = path;
    this.blobId = blobId;
    this.contentType = contentType;
  }

  public String getPath() {
    return path;
  }

  public String getBlobId() {
    return blobId;
  }

  public String getContentType() {
    return contentType;
  }
}