/*
 * Copyright (C) 2013 Sebastien Diot.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.blockwithme.tactors.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blockwithme.tactors.TActor;
import com.blockwithme.tactors.TMailboxFactory;
import com.blockwithme.time.internal.TimeImplModule;
import com.blockwithme.util.LongObjectCache;
import com.blockwithme.util.LongObjectCacheImpl;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

/**
 * Guice module for TActors implementation.
 *
 * @author monster
 */
public class TActorsImplModule extends AbstractModule {

    /** Logger. */
    private static final Logger LOG = LoggerFactory
            .getLogger(TActorsImplModule.class);

    @Override
    protected void configure() {
        install(new TimeImplModule());

        bind(Long.class).annotatedWith(Names.named("MailboxFactoryID"))
                .toInstance(1L);
        bind(new TypeLiteral<LongObjectCache<TActor>>() {
        }).to(new TypeLiteral<LongObjectCacheImpl<TActor>>() {
        });
        bind(TMailboxFactory.class).to(TMailboxFactoryImpl.class);

        LOG.info("TActorsImplModule initialized");
    }
}
