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

import org.agilewiki.pactor.Actor;

import com.blockwithme.tactors.TActor;
import com.blockwithme.tactors.TMailbox;
import com.google.common.base.Preconditions;

/**
 * Base class to use for most TActor.
 *
 * @author monster
 */
public abstract class TActorBase<M extends TMailbox> implements TActor<M> {

    /** The actor Mailbox. */
    protected final M mailbox;

    /** The actor ID. */
    protected final long id;

    /** The actor name. If defined, it will be unique within the mailbox. */
    protected final String name;

    /** The actor parent, if any. */
    protected final TActor<?> parent;

    /** The listeners support. */
    protected final TActorListenerSupport support;

    /**
     * Initialize the actor with a Mailbox.
     */
    protected TActorBase(final M theMailbox, final boolean pin) {
        this(theMailbox, null, null, pin);
    }

    /**
     * Initialize the actor with a Mailbox.
     */
    protected TActorBase(final M theMailbox, final String theName,
            final boolean pin) {
        this(theMailbox, theName, null, pin);
    }

    /**
     * Initialize the actor with a Mailbox.
     * Null is a valid parent.
     */
    protected TActorBase(final M theMailbox, final String theName,
            final TActor<?> theParent, final boolean pin) {
        this(theMailbox, theName, theParent, new TActorListenerSupportImpl(
                theMailbox), pin);
    }

    /**
     * Initialize the actor with a Mailbox.
     * Null is a valid parent.
     */
    protected TActorBase(final M theMailbox, final String theName,
            final TActor<?> theParent, final TActorListenerSupport theSupport,
            final boolean pin) {
        mailbox = Preconditions.checkNotNull(theMailbox);
        support = Preconditions.checkNotNull(theSupport);
        name = theName;
        parent = theParent;
        id = mailbox.nextActorID(this, pin);
    }

    /* (non-Javadoc)
     * @see org.agilewiki.pactor.Actor#sameMailbox(org.agilewiki.pactor.Actor)
     */
    @Override
    public final boolean sameMailbox(final Actor other) {
        return (other != null) && (other.getMailbox() == mailbox);
    }

    /* (non-Javadoc)
     * @see com.blockwithme.tactors.TActor#getMailbox()
     */
    @Override
    public final M getMailbox() {
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
     * @see org.agilewiki.pautil.Named#getActorName()
     */
    @Override
    public final String getActorName() {
        return name;
    }

    /* (non-Javadoc)
     * @see com.blockwithme.tactors.TActor#getParent()
     */
    @Override
    public final TActor<?> getParent() {
        return parent;
    }
}
