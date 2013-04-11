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
package com.blockwithme.tactors;

import org.agilewiki.pactor.Actor;
import org.agilewiki.pautil.Ancestor;

/**
 * Base interface for all temporal actors.
 *
 * TODO Now that all TActor are weakly referenced, we could add lifecycles to them.
 *
 * @author monster
 */
public interface TActor<M extends TMailbox> extends Actor, Ancestor {
    /** @see org.agilewiki.pactor.Actor#getMailbox() */
    @Override
    public M getMailbox();

    /**
     * Returns the Actor ID. It will be within [Long.MIN_VALUE,Long.MAX_VALUE], but not 0.
     * It is derived from the Mailbox ID.
     */
    long id();

    /** The parent of a TActor is always another TActor, or null. */
    @Override
    TActor<?> getParent();

    /**
     * Creates a copy of this actor, with the given new Mailbox.
     * If the mailbox is null, the current mailbox is used.
     */
    TActor<M> copy(M mailbox);
}
