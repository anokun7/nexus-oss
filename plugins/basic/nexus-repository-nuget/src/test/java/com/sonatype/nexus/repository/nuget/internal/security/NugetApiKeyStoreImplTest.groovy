package com.sonatype.nexus.repository.nuget.internal.security

import com.google.inject.util.Providers
import org.apache.shiro.subject.PrincipalCollection
import org.apache.shiro.subject.SimplePrincipalCollection
import org.hamcrest.MatcherAssert
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.sonatype.nexus.orient.DatabaseInstanceRule
import org.sonatype.nexus.security.UserPrincipalsHelper
import org.sonatype.sisu.litmus.testsupport.TestSupport

import static MatcherAssert.assertThat
import static org.hamcrest.Matchers.equalTo
import static org.mockito.Mockito.mock

/**
 * Tests {@link NugetApiKeyStoreImpl}
 */
class NugetApiKeyStoreImplTest
    extends TestSupport
{
  @Rule
  public DatabaseInstanceRule database = new DatabaseInstanceRule('test')

  private NugetApiKeyStoreImpl underTest

  @Before
  void setup() {
    underTest = new NugetApiKeyStoreImpl(
        Providers.of(database.instance),
        new NugetApiKeyEntityAdapter(),
        mock(UserPrincipalsHelper.class)
    )
    underTest.start()
  }

  @After
  void tearDown() {
    if (underTest) {
      underTest.stop()
      underTest = null
    }
  }

  @Test
  void 'Can create and read an API key'() {
    PrincipalCollection p = makePrincipals("name")
    byte[] key = underTest.createApiKey(p)
    byte[] fetchedKey = underTest.getApiKey(p)

    assertThat(fetchedKey, equalTo(key))
  }

  @Test
  void 'Can create and delete an API Key'() {
    PrincipalCollection p = makePrincipals("name")

    underTest.createApiKey(p)
    underTest.deleteApiKey(p)

    char[] key = underTest.getApiKey(p)
    assertThat(key, equalTo(null))
  }

  @Test
  void 'Find by principal name'() {
    char[] alphakey = underTest.createApiKey(makePrincipals("alpha"))
    underTest.createApiKey(makePrincipals("beta"))
    underTest.createApiKey(makePrincipals("gamma"))

    char[] key = underTest.getApiKey(makePrincipals("alpha"))

    assertThat(key, equalTo(alphakey))
  }

  @Test
  void 'Find by api key'() {
    char[] key = underTest.createApiKey(makePrincipals("alpha"))
    underTest.createApiKey(makePrincipals("beta"))
    underTest.createApiKey(makePrincipals("gamma"))

    PrincipalCollection principals = underTest.getPrincipals(key)

    System.err.println(principals)
    //    assertThat(principals, isNotNull())
    assertThat(principals.primaryPrincipal, equalTo("alpha"))
  }

  private PrincipalCollection makePrincipals(String name) {
    return new SimplePrincipalCollection(name, "nuget")
  }
}

