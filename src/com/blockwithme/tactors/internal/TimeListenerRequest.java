package com.blockwithme.tactors.internal;

import org.agilewiki.jactor.api.Transport;

import com.blockwithme.tactors.TMailbox;
import com.blockwithme.time.Time;

public class TimeListenerRequest extends TRequestBase<Void> {

    private final TActorBase target;

    public TimeListenerRequest(final TMailbox targetMailbox,
            final TActorBase _target, final Time time) {
        super(targetMailbox, time);
        target = _target;
    }

    @Override
    public void processRequest(final Transport<Void> responseProcessor)
            throws Exception {
        target.onAsyncTimeChange(creationTime());
        responseProcessor.processResponse(null);
    }
}
