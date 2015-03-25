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
package com.sonatype.nexus.repository.nuget.internal.security;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.shiro.subject.PrincipalCollection;

/**
 * Default {@link NugetApiKeyStore} implementation.
 *
 * TODO: Implement using Orient
 *
 * @since 3.0
 */
@Named
@Singleton
public class NugetApiKeyStoreImpl
    implements NugetApiKeyStore
{
  @Override
  public char[] createApiKey(final PrincipalCollection principals) {
    return new char[0];
  }

  @Override
  public char[] getApiKey(final PrincipalCollection principals) {
    return new char[0];
  }

  @Override
  public PrincipalCollection getPrincipals(final char[] apiKey) {
    return null;
  }

  @Override
  public void deleteApiKey(final PrincipalCollection principals) {

  }

  @Override
  public void purgeApiKeys() {

  }
}
