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
package org.sonatype.nexus.formfields;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.sonatype.sisu.goodies.i18n.I18N;
import org.sonatype.sisu.goodies.i18n.MessageBundle;

import com.google.common.collect.Maps;

/**
 * A repository combo box {@link FormField}.
 *
 * @since 2.7
 */
public class RepositoryCombobox
    extends Combobox<String>
{

  public static final String REGARDLESS_VIEW_PERMISSIONS = "regardlessViewPermissions";

  public static final String FACET = "facet";

  public static final String CONTENT_CLASS = "contentClass";

  public static final String ALL_REPOS_ENTRY = "allReposEntry";

  private List<Class<?>> includingFacets;

  private List<Class<?>> excludingFacets;

  private boolean regardlessViewPermissions;

  private List<String> includingContentClasses;

  private List<String> excludingContentClasses;

  private boolean generateAllRepositoriesEntry;

  private static interface Messages
      extends MessageBundle
  {

    @DefaultMessage("Repository")
    String label();

    @DefaultMessage("Select the repository.")
    String helpText();

  }

  private static final Messages messages = I18N.create(Messages.class);

  public RepositoryCombobox(String id, String label, String helpText, boolean required, String regexValidation) {
    super(id, label, helpText, required, regexValidation);
  }

  public RepositoryCombobox(String id, String label, String helpText, boolean required) {
    super(id, label, helpText, required);
  }

  public RepositoryCombobox(String id, boolean required) {
    super(id, messages.label(), messages.helpText(), required);
  }

  public RepositoryCombobox(String id) {
    super(id, messages.label(), messages.helpText(), false);
  }

  /**
   * Repository will be present if implements any of specified facets.
   */
  public RepositoryCombobox includingAnyOfFacets(final Class<?>... facets) {
    this.includingFacets = Arrays.asList(facets);
    return this;
  }

  /**
   * Repository will not be present if implements any of specified facets.
   */
  public RepositoryCombobox excludingAnyOfFacets(final Class<?>... facets) {
    this.excludingFacets = Arrays.asList(facets);
    return this;
  }

  /**
   * Repository will be present if has any of specified content classes.
   */
  public RepositoryCombobox includingAnyOfContentClasses(final String... contentClasses) {
    this.includingContentClasses = Arrays.asList(contentClasses);
    return this;
  }

  /**
   * Repository will not be present if has any of specified content classes.
   */
  public RepositoryCombobox excludingAnyOfContentClasses(final String... contentClasses) {
    this.excludingContentClasses = Arrays.asList(contentClasses);
    return this;
  }

  /**
   * Repository will be present regardless if current user has rights to view the repository.
   */
  public RepositoryCombobox regardlessViewPermissions() {
    this.regardlessViewPermissions = true;
    return this;
  }

  /**
   * Will add an entry for "All repositories". The value will be "*".
   */
  public RepositoryCombobox includeAnEntryForAllRepositories() {
    this.generateAllRepositoriesEntry = true;
    return this;
  }

  /**
   * @since 3.0
   */
  @Override
  public String getStoreApi() {
    return "coreui_legacy_Repository." + (generateAllRepositoriesEntry ? "readReferencesAddingEntryForAll" : "readReferences");
  }

  /**
   * @since 3.0
   */
  @Override
  public Map<String, String> getStoreFilters() {
    Map<String, String> storeFilters = Maps.newHashMap();
    StringBuilder types = new StringBuilder();
    if (includingFacets != null) {
      for (Class<?> facet : includingFacets) {
        if (types.length() > 0) {
          types.append(',');
        }
        types.append(facet.getName());
      }
    }
    if (excludingFacets != null) {
      for (Class<?> facet : excludingFacets) {
        if (types.length() > 0) {
          types.append(',');
        }
        types.append('!').append(facet.getName());
      }
    }
    if (types.length() > 0) {
      storeFilters.put("type", types.toString());
    }
    StringBuilder contentClasses = new StringBuilder();
    if (includingContentClasses != null) {
      for (String contentClass : includingContentClasses) {
        if (contentClasses.length() > 0) {
          contentClasses.append(',');
        }
        contentClasses.append(contentClass);
      }
    }
    if (excludingContentClasses != null) {
      for (String contentClass : excludingContentClasses) {
        if (contentClasses.length() > 0) {
          contentClasses.append(',');
        }
        contentClasses.append("!").append(contentClass);
      }
    }
    if (contentClasses.length() > 0) {
      storeFilters.put("format", contentClasses.toString());
    }
    return storeFilters.isEmpty() ? null : storeFilters;
  }

}
