package com.sonatype.nexus.repository.nuget.internal.security;

import org.sonatype.nexus.common.entity.Entity;

import org.apache.shiro.subject.PrincipalCollection;

/**
 * An Orient-stored object representing the association between a {@link PrincipalCollection} and a nuget api key
 * (char[]).
 *
 * @since 3.0
 */
public class NugetApiKeyEntity
    extends Entity
{
  private PrincipalCollection principals;

  private char[] apiKey;

  public void setPrincipals(final PrincipalCollection principals) {
    this.principals = principals;
  }

  public void setApiKey(final char[] apiKey) {
    this.apiKey = apiKey;
  }

  public PrincipalCollection getPrincipals() {
    return principals;
  }

  public char[] getApiKey() {
    return apiKey;
  }
}
