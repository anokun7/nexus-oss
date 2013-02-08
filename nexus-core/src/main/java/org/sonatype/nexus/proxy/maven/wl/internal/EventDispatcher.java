/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.proxy.maven.wl.internal;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.ApplicationStatusSource;
import org.sonatype.nexus.configuration.Configurable;
import org.sonatype.nexus.configuration.ConfigurationChangeEvent;
import org.sonatype.nexus.proxy.events.NexusStartedEvent;
import org.sonatype.nexus.proxy.events.RepositoryConfigurationUpdatedEvent;
import org.sonatype.nexus.proxy.events.RepositoryGroupMembersChangedEvent;
import org.sonatype.nexus.proxy.events.RepositoryItemEvent;
import org.sonatype.nexus.proxy.events.RepositoryItemEventCache;
import org.sonatype.nexus.proxy.events.RepositoryItemEventDelete;
import org.sonatype.nexus.proxy.events.RepositoryItemEventDeleteRoot;
import org.sonatype.nexus.proxy.events.RepositoryItemEventStore;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventAdd;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.uid.IsHiddenAttribute;
import org.sonatype.nexus.proxy.maven.MavenGroupRepository;
import org.sonatype.nexus.proxy.maven.MavenHostedRepository;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.maven.wl.WLManager;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.utils.RepositoryStringUtils;
import org.sonatype.nexus.util.SystemPropertiesHelper;

import com.google.common.eventbus.Subscribe;

/**
 * Internal class routing various Nexus events to {@link WLManager}.
 * <p>
 * Note: This component was initially marked as {@code @EagerSingleton}, but it did not play well with rest of plexus
 * components, as it broke everything. Seems like this component was created "too early", also, some UTs does not
 * prepare environment properly (like DefaultPasswordGeneratorTest, that does not set even the minimal properties
 * needed). Hence, this component is made a "plain" singleton (not eager), and {@link WLManager} implementation will
 * pull it up, to have it created and to start ticking.
 * 
 * @author cstamas
 * @since 2.4
 */
public class EventDispatcher
{
    /**
     * System properties key that is used to read up boolean controlling is WL event dispatcher active or not. Main use
     * case is to disable this in "legacy" ITs, but might serve too as troubleshooting in some cases. Event dispatcher
     * is active by default, to deactivate it, specify a system property like this:
     * 
     * <pre>
     * org.sonatype.nexus.proxy.maven.wl.internal.EventDispatcher.active = false
     * </pre>
     */
    public static final String ACTIVE_KEY = EventDispatcher.class.getName() + ".active";

    private final Logger logger;

    private final ApplicationStatusSource applicationStatusSource;

    private final WLManager wlManager;

    private final boolean active;

    /**
     * Da constructor.
     * 
     * @param applicationStatusSource
     * @param wlManager
     */
    public EventDispatcher( final ApplicationStatusSource applicationStatusSource, final WLManager wlManager )
    {
        this.logger = LoggerFactory.getLogger( getClass() );
        this.applicationStatusSource = checkNotNull( applicationStatusSource );
        this.wlManager = checkNotNull( wlManager );
        this.active = SystemPropertiesHelper.getBoolean( ACTIVE_KEY, true );
    }

    protected Logger getLogger()
    {
        return logger;
    }

    // actual work is done here

    protected void handleNexusStarted()
    {
        wlManager.initializeAllWhitelists();
    }

    protected void handleRepositoryAdded( final MavenRepository mavenRepository )
    {
        try
        {
            wlManager.initializeWhitelist( mavenRepository );
        }
        catch ( IOException e )
        {
            getLogger().warn(
                "Problem while updating published WL for newly added repository "
                    + RepositoryStringUtils.getHumanizedNameString( mavenRepository ), e );
        }
    }

    protected void handleRepositoryModified( final MavenRepository mavenRepository )
    {
        wlManager.updateWhitelist( mavenRepository );
    }

    protected void handlePrefixFileUpdate( final RepositoryItemEvent evt )
    {
        final MavenRepository mavenRepository = (MavenRepository) evt.getRepository();
        try
        {
            wlManager.republish( mavenRepository );
        }
        catch ( IOException e )
        {
            getLogger().warn(
                "Problem while updating published WL for repository "
                    + RepositoryStringUtils.getHumanizedNameString( mavenRepository ) + " in response to " + evt, e );
        }
    }

    protected void handlePrefixFileRemoval( final RepositoryItemEvent evt )
    {
        final MavenRepository mavenRepository = (MavenRepository) evt.getRepository();
        try
        {
            wlManager.unpublish( mavenRepository );
        }
        catch ( IOException e )
        {
            getLogger().warn(
                "Problem while updating published WL for repository "
                    + RepositoryStringUtils.getHumanizedNameString( mavenRepository ) + " in response to " + evt, e );
        }
    }

    protected void offerPaths( final MavenHostedRepository mavenHostedRepository, String... paths )
    {
        try
        {
            wlManager.offerWLEntries( mavenHostedRepository, paths );
        }
        catch ( IOException e )
        {
            getLogger().warn(
                "Problem while maintaining WL for hosted repository "
                    + RepositoryStringUtils.getHumanizedNameString( mavenHostedRepository )
                    + ", unable to remove paths: " + paths, e );
        }
    }

    protected void revokePaths( final MavenHostedRepository mavenHostedRepository, String... paths )
    {
        try
        {
            wlManager.revokeWLEntries( mavenHostedRepository, paths );
        }
        catch ( IOException e )
        {
            getLogger().warn(
                "Problem while maintaining WL for hosted repository "
                    + RepositoryStringUtils.getHumanizedNameString( mavenHostedRepository )
                    + ", unable to remove paths: " + paths, e );
        }
    }

    // == Filters

    protected boolean isActive()
    {
        return active;
    }

    protected boolean isActiveAndStarted()
    {
        // we handle repository events only if this is Active and after nexus is started
        return isActive() && applicationStatusSource.getSystemStatus().isNexusStarted();
    }

    protected boolean isRepositoryHandled( final Repository repository )
    {
        // we handle repository events after this isActiveAndStarted, and only for repository that are Maven reposes
        return isActiveAndStarted() && repository != null
            && repository.getRepositoryKind().isFacetAvailable( MavenRepository.class );
    }

    protected boolean isPrefixFileEvent( final RepositoryItemEvent evt )
    {
        // is not fired as side effect of Publisher publishing this
        return isRepositoryHandled( evt.getRepository() )
            && !wlManager.isRequestContextMarked( evt.getItem().getItemContext() )
            && wlManager.isEventAboutWLFile( evt );
    }

    protected boolean isPlainItemEvent( final RepositoryItemEvent evt )
    {
        // is not fired as side effect of Publisher publishing this
        return isRepositoryHandled( evt.getRepository() )
            && !wlManager.isRequestContextMarked( evt.getItem().getItemContext() )
            && !evt.getItem().getRepositoryItemUid().getBooleanAttributeValue( IsHiddenAttribute.class );
    }

    protected boolean isPlainFileItemEvent( final RepositoryItemEvent evt )
    {
        // is not fired as side effect of Publisher publishing this
        return isPlainItemEvent( evt ) && evt.getItem() instanceof StorageFileItem;
    }

    // == handlers for item events (to maintain WL file)

    /**
     * Event handler.
     * 
     * @param evt
     */
    @Subscribe
    public void onRepositoryItemEventStore( final RepositoryItemEventStore evt )
    {
        if ( isPrefixFileEvent( evt ) )
        {
            handlePrefixFileUpdate( evt );
        }
        else if ( isPlainFileItemEvent( evt ) )
        {
            // we maintain WL for hosted reposes only!
            final MavenHostedRepository mavenHostedRepository =
                evt.getRepository().adaptToFacet( MavenHostedRepository.class );
            if ( mavenHostedRepository != null )
            {
                offerPaths( mavenHostedRepository, evt.getItem().getPath() );
            }
        }
    }

    /**
     * Event handler.
     * 
     * @param evt
     */
    @Subscribe
    public void onRepositoryItemEventCache( final RepositoryItemEventCache evt )
    {
        if ( isPrefixFileEvent( evt ) )
        {
            handlePrefixFileUpdate( evt );
        }
        else if ( isPlainFileItemEvent( evt ) )
        {
            // we maintain WL for hosted reposes only!
            final MavenHostedRepository mavenHostedRepository =
                evt.getRepository().adaptToFacet( MavenHostedRepository.class );
            if ( mavenHostedRepository != null )
            {
                offerPaths( mavenHostedRepository, evt.getItem().getPath() );
            }
        }
    }

    /**
     * Event handler.
     * 
     * @param evt
     */
    @Subscribe
    public void onRepositoryItemEventDelete( final RepositoryItemEventDelete evt )
    {
        if ( isPrefixFileEvent( evt ) )
        {
            handlePrefixFileRemoval( evt );
        }
        else if ( evt instanceof RepositoryItemEventDeleteRoot && isPlainItemEvent( evt ) )
        {
            // we maintain WL for hosted reposes only!
            final MavenHostedRepository mavenHostedRepository =
                evt.getRepository().adaptToFacet( MavenHostedRepository.class );
            if ( mavenHostedRepository != null )
            {
                revokePaths( mavenHostedRepository, evt.getItem().getPath() );
            }
        }
    }

    // == Handler for WL initialization

    /**
     * Event handler.
     * 
     * @param evt
     */
    @Subscribe
    public void onNexusStartedEvent( final NexusStartedEvent evt )
    {
        if ( isActive() )
        {
            handleNexusStarted();
        }
    }

    /**
     * Event handler.
     * 
     * @param evt
     */
    @Subscribe
    public void onRepositoryRegistryEventAdd( final RepositoryRegistryEventAdd evt )
    {
        if ( isRepositoryHandled( evt.getRepository() ) )
        {
            final MavenRepository mavenRepository = evt.getRepository().adaptToFacet( MavenRepository.class );
            handleRepositoryAdded( mavenRepository );
        }
    }

    // == Handlers for Proxy remote URL changes

    /**
     * Event handler.
     * 
     * @param evt
     */
    @Subscribe
    public void on( final RepositoryConfigurationUpdatedEvent evt )
    {
        if ( isRepositoryHandled( evt.getRepository() ) && ( evt.isLocalUrlChanged() || evt.isRemoteUrlChanged() ) )
        {
            final MavenRepository mavenRepository = evt.getRepository().adaptToFacet( MavenRepository.class );
            handleRepositoryModified( mavenRepository );
        }
    }

    // == Handlers for Group changes (WL of group and groups of groups needs to be updated)

    /**
     * This subscription is disabled, as this event is fired BEFORE configuration is committed, so group members are NOT
     * discovered properly and leading to bad WL content!
     * 
     * @param evt
     */
    // @Subscribe
    public void onRepositoryGroupMembersChangedEvent( final RepositoryGroupMembersChangedEvent evt )
    {
        if ( isRepositoryHandled( evt.getRepository() ) )
        {
            final MavenRepository mavenRepository = evt.getRepository().adaptToFacet( MavenRepository.class );
            handleRepositoryModified( mavenRepository );
        }
    }

    /**
     * Workaround for method above! This tricky subscriber actually listens for group member changes, but it does it by
     * hooking to {@link ConfigurationChangeEvent} instead of {@link RepositoryGroupMembersChangedEvent}, since former
     * is fired AFTER configuration is committed, while latter is fired BEFORE.
     * 
     * @param evt
     */
    @Subscribe
    public void onConfigurationChangeEvent( final ConfigurationChangeEvent evt )
    {
        for ( Configurable configurable : evt.getChanges() )
        {
            if ( configurable instanceof Repository )
            {
                final Repository repository = (Repository) configurable;
                final MavenGroupRepository mavenGroupRepository = repository.adaptToFacet( MavenGroupRepository.class );
                if ( mavenGroupRepository != null && isRepositoryHandled( mavenGroupRepository ) )
                {
                    handleRepositoryModified( mavenGroupRepository );
                }
            }
        }
    }
}
