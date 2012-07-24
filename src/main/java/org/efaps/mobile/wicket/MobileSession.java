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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.pages.InternalErrorPage;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.string.StringValue;
import org.efaps.admin.user.Person;
import org.efaps.admin.user.UserAttributesSet;
import org.efaps.db.Context;
import org.efaps.jaas.LoginHandler;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class MobileSession
    extends WebSession
{
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(MobileSession.class);



    private String userName;

    /**
     * @param _request
     */
    public MobileSession(final Request _request)
    {
        super(_request);
    }

    /**
     *
     */
    public void openContext()
    {
        if (isAuthenticated()) {
            try {
                if (!Context.isTMActive()) {
                    final ServletWebRequest request = (ServletWebRequest) RequestCycle.get().getRequest();

                    final Map<String, String[]> parameters = new HashMap<String, String[]>();
                    final IRequestParameters reqPara = request.getRequestParameters();
                    for (final String name : reqPara.getParameterNames()) {
                        final List<StringValue> values = reqPara.getParameterValues(name);
                        final String[] valArray = new String[values.size()];
                        int i = 0;
                        for (final StringValue value : values) {
                            valArray[i] = value.toString();
                            i++;
                        }
                        parameters.put(name, valArray);
                    }
                    final Map<String, Object> sessionAttributes = new HashMap<String, Object>();
                    for (final String attribute: getAttributeNames()) {
                        sessionAttributes.put(attribute, getAttribute(attribute));
                    }
                    Context.begin(this.userName, super.getLocale(), sessionAttributes, parameters, null,
                                    true);
                    // set the locale in the context and in the session
                    setLocale(Context.getThreadContext().getLocale());
                    setAttribute(UserAttributesSet.CONTEXTMAPKEY, Context.getThreadContext().getUserAttributes());
                    Context.getThreadContext().setPath(request.getContextPath());
                }
            } catch (final EFapsException e) {
                MobileSession.LOG.error("could not initialise the context", e);
                throw new RestartResponseException(new InternalErrorPage());
            }
        }
    }

    /**
     * Method that closes the opened Context {@link #openContext()}, by
     * committing or rollback it.
     *
     * @see #detach()
     */
    public void closeContext()
    {
        if (isAuthenticated()) {
            try {
                if (!Context.isTMNoTransaction()) {
                    if (Context.isTMActive()) {
                        Context.commit();
                    } else {
                        Context.rollback();
                    }
                }
            } catch (final SecurityException e) {
                throw new RestartResponseException(new InternalErrorPage());
            } catch (final IllegalStateException e) {
                throw new RestartResponseException(new InternalErrorPage());
            } catch (final EFapsException e) {
                throw new RestartResponseException(new InternalErrorPage());
            }
        }
    }

    /**
     * Method to check if a user is checked in.
     *
     * @return true if a user is checked in, else false
     * @see #userName
     */
    public boolean isAuthenticated()
    {
        boolean ret = false;
        if (this.userName != null) {
            ret = true;
        }
        return ret;
    }

    /* (non-Javadoc)
     * @see org.apache.wicket.protocol.http.WebSession#authenticate(java.lang.String, java.lang.String)
     */
    @Override
    public boolean authenticate(final String _username,
                                final String _password)
    {
        if (checkLogin(_username, _password)) {
            this.userName = _username;
            // on login a valid Context for the User must be opened to ensure that the
            // session attributes that depend on the user are set correctly before any
            // further requests are made (e.g. setting the current company
            openContext();
        } else {
            this.userName = null;
            invalidate();
        }
        return isAuthenticated();
    }


    /**
     * Logs a user out and stores the UserAttribues in the eFaps database.
     */
    public final void logout()
    {
        closeContext();
        this.userName = null;
    }


    /**
     * method to check the LoginInformation (Name and Password) against the
     * eFapsDatabase. To check the Information a Context is opened an afterwards
     * closed. It also puts a new Instance of UserAttributes into the instance
     * map {@link #sessionAttributes}. The person returned must have at least one
     * role asigned to be confirmed as value.
     *
     * @param _name Name of the User to be checked in
     * @param _passwd Password of the User to be checked in
     * @return true if LoginInformation was valid, else false
     */
    private boolean checkLogin(final String _name,
                               final String _passwd)
    {

        boolean loginOk = false;
        try {
            Context context = null;

            if (Context.isTMActive()) {
                context = Context.getThreadContext();
            } else {
                context = Context.begin();
            }
            boolean ok = false;

            try {
                // on a new login the cache for Person is reseted
                Person.initialize();
                final MobileApplication app = (MobileApplication) getApplication();
                final LoginHandler loginHandler = new LoginHandler(app.getApplicationKey());
                final Person person = loginHandler.checkLogin(_name, _passwd);
                if (person != null && !person.getRoles().isEmpty()) {
                    loginOk = true;
                    setAttribute(UserAttributesSet.CONTEXTMAPKEY, new UserAttributesSet(_name));
                }
                ok = true;
            } finally {
                if (ok && context.allConnectionClosed() && Context.isTMActive()) {
                    Context.commit();
                } else {
                    if (Context.isTMMarkedRollback()) {
                        MobileSession.LOG.error("transaction is marked to roll back");
                    } else if (!context.allConnectionClosed()) {
                        MobileSession.LOG.error("not all connection to database are closed");
                    } else {
                        MobileSession.LOG.error("transaction manager in undefined status");
                    }
                    Context.rollback();
                }
            }
        } catch (final EFapsException e) {
            MobileSession.LOG.error("could not check name and password", e);
        }
        return loginOk;
    }
}
