package com.test

import groovy.transform.CompileStatic
import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.runtime.server.event.ServerStartupEvent

import javax.inject.Inject
import javax.inject.Singleton

@CompileStatic
@Singleton
class ApplicationLoader implements ApplicationEventListener<ServerStartupEvent> {

    @Inject
    PublisherRepository publisherRepository

    @Override
    void onApplicationEvent(ServerStartupEvent event) {
        publisherRepository.initializeHibernateSearch()
    }
}
