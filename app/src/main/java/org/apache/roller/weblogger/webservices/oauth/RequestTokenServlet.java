/*
 * Copyright 2007 AOL, LLC.
 * Portions Copyright 2009 Apache Software Foundation
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
 */

package org.apache.roller.weblogger.webservices.oauth;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthMessage;
import net.oauth.server.OAuthServlet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.OAuthManager;
import org.apache.roller.weblogger.business.WebloggerFactory;


/**
 * Request token request handler
 * 
 * @author Praveen Alavilli
 * @author Dave Johnson (adapted for Roller)
 */
public class RequestTokenServlet extends HttpServlet {
    protected static final Log log = LogFactory.getFactory().getInstance(RequestTokenServlet.class);
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws IOException, ServletException {
        processRequest(request, response);
    }
    
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        processRequest(request, response);
    }
        
    public void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        try {
            OAuthMessage requestMessage = OAuthServlet.getMessage(request, null);
            OAuthManager omgr = WebloggerFactory.getWeblogger().getOAuthManager();            
            OAuthAccessor accessor = omgr.getAccessor(requestMessage);

            if (accessor == null) {

                OAuthConsumer consumer = omgr.getConsumer(requestMessage);
                accessor = new OAuthAccessor(consumer);
                omgr.getValidator().validateMessage(requestMessage, accessor);

                {
                    // Support the 'Variable Accessor Secret' extension
                    // described in http://oauth.pbwiki.com/AccessorSecret
                    String secret = requestMessage.getParameter("oauth_accessor_secret");
                    if (secret != null) {
                        accessor.setProperty(OAuthConsumer.ACCESSOR_SECRET, secret);
                    }
                }

                // generate request_token and secret
                omgr.generateRequestToken(accessor);
                WebloggerFactory.getWeblogger().flush();
            }

            response.setContentType("text/plain");
            try (OutputStream out = response.getOutputStream()) {
                String token = accessor.requestToken != null ? accessor.requestToken: accessor.accessToken;
                OAuth.formEncode(OAuth.newList(
                        "oauth_token", token,
                        "oauth_token_secret", accessor.tokenSecret), out);
            }
            
        } catch (Exception e){
            handleException(e, request, response, true);
        }
        
    }

    public void handleException(Exception e, HttpServletRequest request,
            HttpServletResponse response, boolean sendBody)
            throws IOException, ServletException {
        log.debug("ERROR authorizing token", e);
        String realm = (request.isSecure())?"https://":"http://";
        realm += request.getLocalName();
        OAuthServlet.handleException(response, e, realm, sendBody);
    }
}
