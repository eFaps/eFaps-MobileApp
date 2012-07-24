/*
 * Copyright 2003 - 2012 The eFaps Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.mobile.wicket.request;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.AbstractRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.session.ISessionStore;
import org.efaps.mobile.wicket.MobileSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class extends the
 * {@link org.apache.wicket.protocol.http.WebRequestCycle} to throw a own
 * ErrorPage and to open/close the Context on begin/end of a Request.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class MobileRequestCycleListener
    extends AbstractRequestCycleListener
{

    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(MobileRequestCycleListener.class);

    /**
     * Method to get the EFapsSession.
     *
     * @param _request Request the Session is wanted for
     * @return EFapsSession
     */
    private MobileSession getEFapsSession(final Request _request)
    {
        final ISessionStore sessionStore = WebApplication.get().getSessionStore();
        final MobileSession session = (MobileSession) sessionStore.lookup(_request);
        return session;
    }

    /**
     * Called when the request cycle object is beginning its response.
     *
     * @param _cycle    RequestCycle this Listener belongs to
     */
    @Override
    public void onBeginRequest(final RequestCycle _cycle)
    {
        final MobileSession session = getEFapsSession(_cycle.getRequest());
        if (session != null) {
            session.openContext();
        }
        MobileRequestCycleListener.LOG.debug("Begin of Request.");
    }

    /**
     * Called when the request cycle object has finished its response.
     *
     * @param _cycle    RequestCycle this Listener belongs to
     */
    @Override
    public void onEndRequest(final RequestCycle _cycle)
    {
        final MobileSession session = getEFapsSession(_cycle.getRequest());
        if (session != null) {
            session.closeContext();
        }
        MobileRequestCycleListener.LOG.debug("End of Request.");
    }
}
