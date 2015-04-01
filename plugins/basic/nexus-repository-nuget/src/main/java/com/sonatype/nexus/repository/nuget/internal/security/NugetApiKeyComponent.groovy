package com.sonatype.nexus.repository.nuget.internal.security

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider
import javax.inject.Singleton

import org.sonatype.nexus.common.validation.Validate
import org.sonatype.nexus.extdirect.DirectComponentSupport
import org.sonatype.nexus.security.SecurityHelper
import org.sonatype.nexus.wonderland.AuthTicketService

import com.softwarementors.extjs.djn.config.annotations.DirectAction
import com.softwarementors.extjs.djn.config.annotations.DirectMethod
import org.apache.shiro.authz.annotation.RequiresAuthentication
import org.apache.shiro.authz.annotation.RequiresPermissions
import org.hibernate.validator.constraints.NotEmpty

/**
 * Managing per-user NuGet Api Keys.
 * 
 * @since 3.0
 */
@Named
@Singleton
@DirectAction(action = 'nuget_NuGetApiKey')
class NugetApiKeyComponent
    extends DirectComponentSupport
{

    @Inject
    Provider<NugetApiKeyStore> keyStore

    @Inject
    AuthTicketService authTokens

    @Inject
    SecurityHelper securityHelper

    /**
     * Read NuGet API Key for current signed on user.
     */
    @DirectMethod
    @RequiresPermissions('apikey:access:read')
    @Validate
    String readKey(final @NotEmpty(message = '[authToken] may not be empty') String authToken) {
        validateAuthToken(authToken)

        def principals = securityHelper.subject().principals
        char[] apiKey = keyStore.get().getApiKey(principals)
        if (!apiKey) {
            apiKey = keyStore.get().createApiKey(principals)
        }
        return new String(apiKey)
    }

    /**
     * Resets NuGet API Key for current signed on user.
     */
    @DirectMethod
    @RequiresAuthentication
    @RequiresPermissions('apikey:access:delete')
    @Validate
    String resetKey(final @NotEmpty(message = '[authToken] may not be empty') String authToken) {
        validateAuthToken(authToken)

        def principals = securityHelper.subject().principals
        def keyStore = keyStore.get()
        keyStore.deleteApiKey(principals)
        char[] apiKey = keyStore.createApiKey(principals)
        return new String(apiKey)
    }

    private void validateAuthToken(final String authToken) {
        assert authToken, 'Missing authentication ticket'

        log.debug 'Validating authentication ticket: {}', authToken

        if (!authTokens.redeemTicket(authToken)) {
            throw new IllegalAccessException('Invalid authentication ticket')
        }
    }
}
