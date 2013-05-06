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

import org.agilewiki.jactor.api.Actor;

import com.blockwithme.tactors.TActor;
import com.blockwithme.tactors.TMailbox;
import com.blockwithme.time.Time;
import com.blockwithme.time.Timeline;
import com.google.common.base.Preconditions;

/**
 * Base class to use for most TActor.
 *
 * @author monster
 */
public abstract class TActorBase implements TActor {

    /** The actor Mailbox. */
    protected final TMailbox mailbox;

    /** The actor ID. */
    protected final long id;

    /** The actor name. If defined, it will be unique within the mailbox. */
    protected final String name;

    /** The actor parent, if any. */
    protected final TActor parent;

    /** The listeners support. */
    protected final TActorListenerSupport support;

    /** The timeline. */
    protected final Timeline timeline;

    /**
     * Initialize the actor with a Mailbox.
     * Null is a valid Timeline, if you are not the future mailbox owner.
     */
    protected TActorBase(final TMailbox theMailbox, final Timeline theTimeline) {
        this(theMailbox, null, null, false, theTimeline);
    }

    /**
     * Initialize the actor with a Mailbox.
     * Null is a valid name.
     * Null is a valid Timeline, if you are not the future mailbox owner.
     */
    protected TActorBase(final TMailbox theMailbox, final String theName,
            final Timeline theTimeline) {
        this(theMailbox, theName, null, false, theTimeline);
    }

    /**
     * Initialize the actor with a Mailbox.
     * Null is a valid name.
     * Null is a valid parent.
     * Null is a valid Timeline, if you are not the future mailbox owner.
     */
    protected TActorBase(final TMailbox theMailbox, final String theName,
            final TActor theParent, final Timeline theTimeline) {
        this(theMailbox, theName, theParent, new TActorListenerSupportImpl(
                theMailbox), false, theTimeline);
    }

    /**
     * Initialize the actor with a Mailbox.
     * Null is a valid Timeline, if you are not the future mailbox owner.
     */
    protected TActorBase(final TMailbox theMailbox, final boolean pin,
            final Timeline theTimeline) {
        this(theMailbox, null, null, pin, theTimeline);
    }

    /**
     * Initialize the actor with a Mailbox.
     * Null is a valid name.
     * Null is a valid Timeline, if you are not the future mailbox owner.
     */
    protected TActorBase(final TMailbox theMailbox, final String theName,
            final boolean pin, final Timeline theTimeline) {
        this(theMailbox, theName, null, pin, theTimeline);
    }

    /**
     * Initialize the actor with a Mailbox.
     * Null is a valid name.
     * Null is a valid parent.
     * Null is a valid Timeline, if you are not the future mailbox owner.
     */
    protected TActorBase(final TMailbox theMailbox, final String theName,
            final TActor theParent, final boolean pin,
            final Timeline theTimeline) {
        this(theMailbox, theName, theParent, new TActorListenerSupportImpl(
                theMailbox), pin, theTimeline);
    }

    /**
     * Initialize the actor with a Mailbox.
     * Should not be used by the mailbox owner.
     */
    protected TActorBase(final TMailbox theMailbox) {
        this(theMailbox, null, null, false);
    }

    /**
     * Initialize the actor with a Mailbox.
     * Null is a valid name.
     * Should not be used by the mailbox owner.
     */
    protected TActorBase(final TMailbox theMailbox, final String theName) {
        this(theMailbox, theName, null, false);
    }

    /**
     * Initialize the actor with a Mailbox.
     * Null is a valid name.
     * Null is a valid parent.
     * Should not be used by the mailbox owner.
     */
    protected TActorBase(final TMailbox theMailbox, final String theName,
            final TActor theParent) {
        this(theMailbox, theName, theParent, new TActorListenerSupportImpl(
                theMailbox), false);
    }

    /**
     * Initialize the actor with a Mailbox.
     * Null is a valid name.
     * Null is a valid parent.
     * Should not be used by the mailbox owner.
     */
    protected TActorBase(final TMailbox theMailbox, final String theName,
            final TActor theParent, final TActorListenerSupport theSupport) {
        this(theMailbox, theName, theParent, theSupport, false, null);
    }

    /**
     * Initialize the actor with a Mailbox.
     * Should not be used by the mailbox owner.
     */
    protected TActorBase(final TMailbox theMailbox, final boolean pin) {
        this(theMailbox, null, null, pin);
    }

    /**
     * Initialize the actor with a Mailbox.
     * Null is a valid name.
     * Should not be used by the mailbox owner.
     */
    protected TActorBase(final TMailbox theMailbox, final String theName,
            final boolean pin) {
        this(theMailbox, theName, null, pin);
    }

    /**
     * Initialize the actor with a Mailbox.
     * Null is a valid name.
     * Null is a valid parent.
     * Should not be used by the mailbox owner.
     */
    protected TActorBase(final TMailbox theMailbox, final String theName,
            final TActor theParent, final boolean pin) {
        this(theMailbox, theName, theParent, new TActorListenerSupportImpl(
                theMailbox), pin);
    }

    /**
     * Initialize the actor with a Mailbox.
     * Null is a valid name.
     * Null is a valid parent.
     * Should not be used by the mailbox owner.
     */
    protected TActorBase(final TMailbox theMailbox, final String theName,
            final TActor theParent, final TActorListenerSupport theSupport,
            final boolean pin) {
        this(theMailbox, theName, theParent, theSupport, pin, null);
    }

    /**
     * Initialize the actor with a Mailbox.
     * Null is a valid name.
     * Null is a valid parent.
     * Null is a valid Timeline, if you are not the future mailbox owner.
     */
    protected TActorBase(final TMailbox theMailbox, final String theName,
            final TActor theParent, final TActorListenerSupport theSupport,
            final boolean pin, final Timeline theTimeline) {
        mailbox = Preconditions.checkNotNull(theMailbox);
        support = Preconditions.checkNotNull(theSupport);
        timeline = Preconditions
                .checkNotNull((theTimeline == null) ? theMailbox.owner()
                        .timeline() : theTimeline);
        name = theName;
        parent = theParent;
        id = mailbox.nextActorID(this, pin);
    }

    /* (non-Javadoc)
     * @see com.blockwithme.tactors.TActor#sameMailbox(org.agilewiki.jactor.api.Actor)
     */
    @Override
    public final boolean sameMailbox(final Actor other) {
        return (other != null) && (other.getMailbox() == mailbox);
    }

    /* (non-Javadoc)
     * @see com.blockwithme.tactors.TActor#getMailbox()
     */
    @Override
    public final TMailbox getMailbox() {
        return mailbox;
    }

    /* (non-Javadoc)
     * @see com.blockwithme.tactors.TActor#id()
     */
    @Override
    public final long id() {
        return id;
    }

    /* (non-Javadoc)
     * @see com.blockwithme.tactors.TActor#name()
     */
    @Override
    public final String name() {
        return name;
    }

    /* (non-Javadoc)
     * @see org.agilewiki.jactor.util.Named#getName()
     */
    @Override
    public final String getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see com.blockwithme.tactors.TActor#getParent()
     */
    @Override
    public final TActor getParent() {
        return parent;
    }

    /* (non-Javadoc)
     * @see com.blockwithme.time.Timed#timeline()
     */
    @Override
    public final Timeline timeline() {
        return timeline;
    }

    /** What's the time? */
    protected final Time time() {
        return timeline.lastTick();
    }
}
