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

package org.efaps.mobile.wicket;

import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.authorization.strategies.page.SimplePageAuthorizationStrategy;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.efaps.mobile.wicket.pages.ISecuredPage;
import org.efaps.mobile.wicket.pages.login.LoginPage;
import org.efaps.mobile.wicket.request.MobileRequestCycleListener;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id: MobileApplication.java 7821 2012-07-23 23:40:46Z jan@moxter.net
 *          $
 */
public class MobileApplication
    extends WebApplication
{

    @Override
    public Class<? extends Page> getHomePage()
    {
        return LoginPage.class;
    }


    /**
     * @see org.apache.wicket.protocol.http.WebApplication#newSession(org.apache.wicket.Request,
     *      org.apache.wicket.Response)
     * @param _request the request
     * @param _response the response
     * @return a new Session for the request
     */
    @Override
    public Session newSession(final Request _request,
                              final Response _response)
    {
        return new MobileSession(_request);
    }



    @Override
    protected void init()
    {
        getMarkupSettings().setStripWicketTags(true);
        getMarkupSettings().setStripComments(true);
        getMarkupSettings().setCompressWhitespace(true);
        getMarkupSettings().setAutomaticLinking(false);

        getRequestCycleListeners().add(new MobileRequestCycleListener());

        getSecuritySettings().setAuthorizationStrategy(
                        new SimplePageAuthorizationStrategy(ISecuredPage.class, LoginPage.class)
                        {
                            @Override
                            protected boolean isAuthorized()
                            {
                                final MobileSession session = (MobileSession) WebSession.get();
                                return session.isAuthenticated();
                            }
                        });
    }
}
