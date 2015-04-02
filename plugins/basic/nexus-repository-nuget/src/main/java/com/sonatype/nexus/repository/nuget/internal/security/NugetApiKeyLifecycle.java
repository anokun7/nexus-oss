package com.sonatype.nexus.repository.nuget.internal.security;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.sonatype.nexus.proxy.events.NexusStartedEvent;
import org.sonatype.nexus.proxy.events.NexusStoppedEvent;
import org.sonatype.sisu.goodies.eventbus.EventBus;
import org.sonatype.sisu.goodies.lifecycle.LifecycleManagerImpl;

import com.google.common.eventbus.Subscribe;
import org.eclipse.sisu.EagerSingleton;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Manages the lifecycle for the {@link NugetApiKeyStoreImpl}.
 *
 * @since 3.0
 */
@Named
@EagerSingleton
public class NugetApiKeyLifecycle
    extends LifecycleManagerImpl
{
  private final EventBus eventBus;

  private final Provider<NugetApiKeyStore> keyStore;

  @Inject
  public NugetApiKeyLifecycle(final EventBus eventBus, final Provider<NugetApiKeyStore> keyStore)
  {
    this.eventBus = checkNotNull(eventBus);
    this.keyStore = checkNotNull(keyStore);

    eventBus.register(this);
  }

  @Subscribe
  public void on(final NexusStartedEvent event) throws Exception {
    add(keyStore.get());

    start();
  }

  @Subscribe
  public void on(final NexusStoppedEvent event) throws Exception {
    eventBus.unregister(this);

    stop();
    clear();
  }
}
