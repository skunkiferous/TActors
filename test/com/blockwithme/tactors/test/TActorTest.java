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
package com.blockwithme.tactors.test;

import junit.framework.TestCase;

import org.agilewiki.jactor.api.Actor;
import org.agilewiki.jactor.api.Transport;

import com.blockwithme.tactors.MBOwner;
import com.blockwithme.tactors.TActor;
import com.blockwithme.tactors.TMailbox;
import com.blockwithme.tactors.TMailboxFactory;
import com.blockwithme.tactors.TRequest;
import com.blockwithme.tactors.internal.TActorBase;
import com.blockwithme.tactors.internal.TActorsImplModule;
import com.blockwithme.tactors.internal.TRequestBase;
import com.blockwithme.time.Timeline;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Test code.
 */
public class TActorTest extends TestCase {
    private static class MyActorNonMBOwner extends TActorBase {
        public final TRequest<String> hi1;

        public MyActorNonMBOwner(final TMailbox theMailbox, final String name,
                final Timeline timeline, final boolean pin) {
            super(theMailbox, name, pin, timeline);

            hi1 = new TRequestBase<String>(mailbox, timeline) {
                @Override
                public void processRequest(
                        final Transport<String> responseProcessor)
                        throws Exception {
                    responseProcessor.processResponse("Hello world!");
                }
            };
        }

        @Override
        public TActor copy(final TMailbox mailbox) {
            throw new UnsupportedOperationException();
        }
    };

    private static class MyActor extends MyActorNonMBOwner implements MBOwner {
        public MyActor(final TMailbox theMailbox, final String name,
                final Timeline timeline, final boolean pin) {
            super(theMailbox, name, timeline, pin);
        }
    }

    private static class DummyTActor implements TActor {
        public long id;

        @Override
        public String getName() {
            return null;
        }

        @Override
        public boolean sameMailbox(final Actor other) {
            return false;
        }

        @Override
        public TMailbox getMailbox() {
            return null;
        }

        @Override
        public long id() {
            return id;
        }

        @Override
        public String name() {
            return null;
        }

        @Override
        public TActor getParent() {
            return null;
        }

        @Override
        public TActor copy(final TMailbox mailbox) {
            return null;
        }

        @Override
        public Timeline timeline() {
            return null;
        }
    };

    protected static void sleep(final long sleep) {
        try {
            Thread.sleep(sleep);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected static void close(final AutoCloseable ac) {
        try {
            ac.close();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    TMailboxFactory mailboxFactory;

    @Override
    protected void setUp() {
        final Injector injector = Guice.createInjector(new TActorsImplModule());
        mailboxFactory = injector.getInstance(TMailboxFactory.class);
    }

    @Override
    protected void tearDown() throws Exception {
        mailboxFactory.close();
        mailboxFactory = null;
    }

    public void testCall() throws Exception {
        final TMailbox mailbox = mailboxFactory.createMailbox();
        final MyActor actor1 = new MyActor(mailbox, null, mailboxFactory
                .clockService().coreTimeline(), false);
        final String result = actor1.hi1.call();
        assertEquals("Hello world!", result);
    }

    public void testID() throws Exception {
        final TMailbox mailbox = mailboxFactory.createMailbox();
        final MyActor actor1 = new MyActor(mailbox, null, mailboxFactory
                .clockService().coreTimeline(), false);
        final long id = actor1.id();
        assertEquals(actor1, mailboxFactory.findActor(id));
    }

    public void testName() throws Exception {
        TMailbox mailbox = mailboxFactory.createMailbox();
        MyActor actor1 = new MyActor(mailbox, "testName", mailboxFactory
                .clockService().coreTimeline(), false);
        mailbox = null;
        assertEquals(actor1, mailboxFactory.findActor("testName"));
        actor1 = null;
        // No reference to actor1 or mailbox in the method anymore ...
        final long start = System.currentTimeMillis();
        System.gc();
        while (System.currentTimeMillis() - start < 3000) {
            Thread.sleep(200);
            System.gc();
        }
        assertTrue(mailboxFactory.findActor("testName") != null);
    }

    public void testOwner() throws Exception {
        final TMailbox mailbox = mailboxFactory.createMailbox();
        final MyActor actor1 = new MyActor(mailbox, null, mailboxFactory
                .clockService().coreTimeline(), false);
        assertEquals(actor1, mailbox.owner());
    }

    public void testPin() throws Exception {
        TMailbox mailbox = mailboxFactory.createMailbox();
        MyActor actor1 = new MyActor(mailbox, null, mailboxFactory
                .clockService().coreTimeline(), true);
        mailbox = null;
        final long id = actor1.id();
        assertEquals(actor1, mailboxFactory.findActor(id));
        actor1 = null;
        // No reference to actor1 or mailbox in the method anymore ...
        final long start = System.currentTimeMillis();
        System.gc();
        while (System.currentTimeMillis() - start < 3000) {
            Thread.sleep(200);
            System.gc();
        }
        assertTrue(mailboxFactory.findActor(id) != null);
    }

    public void testGC() throws Exception {
        final TMailbox mailbox = mailboxFactory.createMailbox();
        // actor1 is MB owner
        final MyActor actor1 = new MyActor(mailbox, null, mailboxFactory
                .clockService().coreTimeline(), false);
        final long id1 = actor1.id();
        MyActorNonMBOwner actor2 = new MyActorNonMBOwner(mailbox, null,
                mailboxFactory.clockService().coreTimeline(), false);
        final long id2 = actor2.id();
        assertTrue(id1 != id2);
        // Now GC can work ...
        actor2 = null;
        final long start = System.currentTimeMillis();
        System.gc();
        while ((mailboxFactory.findActor(id2) != null)
                && (System.currentTimeMillis() - start < 5000)) {
            Thread.sleep(100);
            System.gc();
        }
        assertEquals(null, mailboxFactory.findActor(id2));
    }

    public void testNextActorID() throws Exception {
        final TMailbox mailbox = mailboxFactory.createMailbox();
        // actor1 is MB owner
        @SuppressWarnings("unused")
        final MyActor actor1 = new MyActor(mailbox, "testNextActorID",
                mailboxFactory.clockService().coreTimeline(), false);

        boolean failed = false;
        try {
            // Must not accept null
            mailbox.getMailboxFactory().nextActorID(null, false);
        } catch (final Exception e) {
            failed = true;
        }
        assertTrue(failed);

        failed = false;
        try {
            final DummyTActor dummy = new DummyTActor();
            dummy.id = 123;
            // Must not accept actors with ID
            mailbox.getMailboxFactory().nextActorID(dummy, false);
        } catch (final Exception e) {
            failed = true;
        }
        assertTrue(failed);

        failed = false;
        try {
            // Must not accept duplicate names
            new MyActor(mailbox, "testNextActorID", mailboxFactory
                    .clockService().coreTimeline(), false);
        } catch (final Exception e) {
            failed = true;
        }
        assertTrue(failed);

        failed = false;
        try {
            final DummyTActor dummy = new DummyTActor();
            // Must not accept non-MBOwner actors as owner
            mailboxFactory.createMailbox().nextActorID(dummy, false);
        } catch (final Exception e) {
            failed = true;
        }
        assertTrue(failed);
    }
}
