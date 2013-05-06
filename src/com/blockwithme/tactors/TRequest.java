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

import org.agilewiki.jactor.api.Request;

import com.blockwithme.time.Time;

/**
 * A temporal Request. It is associated with the time at which it was created.
 *
 * @author monster
 */
public interface TRequest<RESPONSE_TYPE> extends Request<RESPONSE_TYPE> {

    /** @see org.agilewiki.jactor.api.Request#getMailbox() */
    @Override
    TMailbox getMailbox();

    /** Time at creation. */
    Time creationTime();
}
