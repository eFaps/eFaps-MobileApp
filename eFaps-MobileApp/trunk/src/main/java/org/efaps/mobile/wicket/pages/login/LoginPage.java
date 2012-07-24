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

package org.efaps.mobile.wicket.pages.login;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.efaps.mobile.wicket.MobileSession;
import org.efaps.mobile.wicket.pages.main.MainPage;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class LoginPage
    extends WebPage
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;


    private String username;

    private String password;

    public LoginPage()
    {
        final CompoundPropertyModel<LoginPage> model = new CompoundPropertyModel<LoginPage>(this);
        final Form<LoginPage> form = new Form<LoginPage>("form", model)
        {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit()
            {
                super.onSubmit();
                final MobileSession session = (MobileSession) getSession();


                if (session.authenticate(LoginPage.this.username, LoginPage.this.password)) {
                    getRequestCycle().setResponsePage(MainPage.class);
                } else {
                    final LoginPage page = new LoginPage();
                    getRequestCycle().setResponsePage(page);
                }
            }
        };
        add(form);
        form.add(new RequiredTextField<String>("username"));
        form.add(new PasswordTextField("password"));
    }

    @Override
    public void renderHead(final IHeaderResponse _response)
    {
        super.renderHead(_response);
        _response.render(CssHeaderItem.forReference(new CssResourceReference(LoginPage.class, "LoginPage.css")));
    }
}
