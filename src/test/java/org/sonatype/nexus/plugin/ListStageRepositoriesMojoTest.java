/*
 * Nexus Plugin for Maven
 * Copyright (C) 2009 Sonatype, Inc.                                                                                                                          
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.plugin;

import static junit.framework.Assert.fail;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.util.FileUtils;
import org.jdom.JDOMException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sonatype.nexus.restlight.common.RESTLightClientException;
import org.sonatype.nexus.restlight.stage.StageClient;
import org.sonatype.nexus.restlight.testharness.AbstractRESTTest;
import org.sonatype.nexus.restlight.testharness.ConversationalFixture;
import org.sonatype.nexus.restlight.testharness.GETFixture;
import org.sonatype.nexus.restlight.testharness.RESTTestFixture;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class ListStageRepositoriesMojoTest
    extends AbstractRESTTest
{

    private final ConversationalFixture fixture = new ConversationalFixture( getExpectedUser(), getExpectedPassword() );

    private final Set<File> toDelete = new HashSet<File>();

    private Log log;

    @Before
    public void setupMojoLog()
    {
        log = new SystemStreamLog()
        {
            @Override
            public boolean isDebugEnabled()
            {
                return true;
            }
        };
    }

    @After
    public void cleanupFiles()
    {
        if ( toDelete != null )
        {
            for ( File f : toDelete )
            {
                try
                {
                    FileUtils.forceDelete( f );
                }
                catch ( IOException e )
                {
                    System.out.println( "Failed to delete test file/dir: " + f + ". Reason: " + e.getMessage() );
                }
            }
        }
    }

    @Test
    public void simplestUseCase()
        throws JDOMException, IOException, RESTLightClientException, MojoExecutionException
    {
        printTestName();
        
        ListStageRepositoriesMojo mojo = new ListStageRepositoriesMojo();
        
        mojo.setUsername( getExpectedUser() );
        mojo.setPassword( getExpectedPassword() );
        mojo.setNexusUrl( getBaseUrl() );

        runMojo( mojo );
    }

    @Test
    public void baseUrlWithTrailingSlash()
        throws JDOMException, IOException, RESTLightClientException, MojoExecutionException
    {
        printTestName();

        ListStageRepositoriesMojo mojo = new ListStageRepositoriesMojo();

        mojo.setUsername( getExpectedUser() );
        mojo.setPassword( getExpectedPassword() );
        mojo.setNexusUrl( getBaseUrl() + "/" );

        mojo.setVerboseDebug( true );
        fixture.setDebugEnabled( true );

        runMojo( mojo );
    }

    @Test
    public void badPassword()
        throws JDOMException, IOException, RESTLightClientException, MojoExecutionException
    {
        printTestName();

        ListStageRepositoriesMojo mojo = new ListStageRepositoriesMojo();

        mojo.setUsername( getExpectedUser() );
        mojo.setPassword( "wrong" );
        mojo.setNexusUrl( getBaseUrl() );

        try
        {
            runMojo( mojo );
            fail( "should fail to connect due to bad password" );
        }
        catch ( MojoExecutionException e )
        {
            // expected.
        }
    }

    @Test
    public void promptForPassword()
        throws JDOMException, IOException, RESTLightClientException, MojoExecutionException
    {
        printTestName();
        
        ListStageRepositoriesMojo mojo = new ListStageRepositoriesMojo();
        
        ExpectPrompter prompter = new ExpectPrompter();
        
        prompter.addExpectation( "Password", getExpectedPassword() );
        
        mojo.setPrompter( prompter );
        
        mojo.setUsername( getExpectedUser() );
        mojo.setNexusUrl( getBaseUrl() );
        
        runMojo( mojo );
    }

    @Test
    public void promptForNexusURL()
        throws JDOMException, IOException, RESTLightClientException, MojoExecutionException
    {
        printTestName();
        
        ListStageRepositoriesMojo mojo = new ListStageRepositoriesMojo();
        
        ExpectPrompter prompter = new ExpectPrompter();
        
        prompter.addExpectation( "Nexus URL", getBaseUrl() );

        mojo.setPrompter( prompter );
        
        mojo.setUsername( getExpectedUser() );
        mojo.setPassword( getExpectedPassword() );
        
        runMojo( mojo );
    }

    @Test
    public void authUsingSettings()
        throws JDOMException, IOException, RESTLightClientException, MojoExecutionException
    {
        printTestName();
        
        ListStageRepositoriesMojo mojo = new ListStageRepositoriesMojo();
        
        String serverId = "server";
        
        Server server = new Server();
        server.setId( serverId );
        server.setUsername( getExpectedUser() );
        server.setPassword( getExpectedPassword() );
        
        Settings settings = new Settings();
        settings.addServer( server );
        
        mojo.setSettings( settings );
        mojo.setServerAuthId( serverId );
        
        mojo.setNexusUrl( getBaseUrl() );
        
        runMojo( mojo );
    }

    private void runMojo( final ListStageRepositoriesMojo mojo )
        throws JDOMException, IOException, MojoExecutionException
    {
        mojo.setLog( log );
        
        List<RESTTestFixture> conversation = new ArrayList<RESTTestFixture>();

        conversation.add( getVersionCheckFixture() );

        GETFixture repoListGet = new GETFixture( getExpectedUser(), getExpectedPassword() );
        repoListGet.setExactURI( StageClient.PROFILES_PATH );
        repoListGet.setResponseDocument( readTestDocumentResource( "list/profile-list.xml" ) );

        conversation.add( repoListGet );

        GETFixture reposGet = new GETFixture( getExpectedUser(), getExpectedPassword() );
        reposGet.setExactURI( StageClient.PROFILE_REPOS_PATH_PREFIX + "112cc490b91265a1" );
        reposGet.setResponseDocument( readTestDocumentResource( "list/profile-repo-list.xml" ) );

        conversation.add( reposGet );

        repoListGet = new GETFixture( getExpectedUser(), getExpectedPassword() );
        repoListGet.setExactURI( StageClient.PROFILES_PATH );
        repoListGet.setResponseDocument( readTestDocumentResource( "list/profile-list-closed.xml" ) );

        conversation.add( repoListGet );

        reposGet = new GETFixture( getExpectedUser(), getExpectedPassword() );
        reposGet.setExactURI( StageClient.PROFILE_REPOS_PATH_PREFIX + "112cc490b91265a1" );
        reposGet.setResponseDocument( readTestDocumentResource( "list/profile-repo-list-closed.xml" ) );

        conversation.add( reposGet );

        fixture.setConversation( conversation );
        
        mojo.execute();
    }

    @Override
    protected RESTTestFixture getTestFixture()
    {
        return fixture;
    }

    protected void printTestName()
    {
        StackTraceElement e = new Throwable().getStackTrace()[1];
        System.out.println( "\n\nRunning: '"
            + ( getClass().getName().substring( getClass().getPackage().getName().length() + 1 ) ) + "#"
            + e.getMethodName() + "'\n\n" );
    }
    
}
