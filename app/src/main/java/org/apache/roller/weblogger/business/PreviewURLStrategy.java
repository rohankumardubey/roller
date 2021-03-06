/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 */

package org.apache.roller.weblogger.business;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogTheme;
import org.apache.roller.weblogger.util.URLUtilities;


/**
 * A URLStrategy used by the preview rendering system.
 */
public class PreviewURLStrategy extends MultiWeblogURLStrategy {
    
    private final String previewTheme;
    private static final String PREVIEW_URL_SEGMENT = "/roller-ui/authoring/preview/";
    
    public PreviewURLStrategy(String theme) {
        previewTheme = theme;
    }
    
    
    /**
     * Get root url for a given *preview* weblog.  
     * Optionally for a certain locale.
     */
    @Override
    public String getWeblogURL(Weblog weblog, String locale, boolean absolute) {
        
        if(weblog == null) {
            return null;
        }
        
        StringBuilder url = new StringBuilder(URL_BUFFER_SIZE);
        
        if(absolute) {
            url.append(WebloggerRuntimeConfig.getAbsoluteContextURL());
        } else {
            url.append(WebloggerRuntimeConfig.getRelativeContextURL());
        }
        
        url.append(PREVIEW_URL_SEGMENT).append(weblog.getHandle()).append('/');
        
        if(locale != null) {
            url.append(locale).append('/');
        }
        
        Map<String, String> params = Collections.emptyMap();
        if(previewTheme != null) {
            params = Map.of("theme", URLUtilities.encode(previewTheme));
        }
        
        return url.append(URLUtilities.getQueryString(params)).toString();
    }
    
    
    /**
     * Get url for a given *preview* weblog entry.  
     * Optionally for a certain locale.
     */
    @Override
    public String getWeblogEntryURL(Weblog weblog,
                                    String locale,
                                    String previewAnchor,
                                    boolean absolute) {
        
        if(weblog == null) {
            return null;
        }

        StringBuilder url = new StringBuilder(URL_BUFFER_SIZE);
        
        if(absolute) {
            url.append(WebloggerRuntimeConfig.getAbsoluteContextURL());
        } else {
            url.append(WebloggerRuntimeConfig.getRelativeContextURL());
        }
        
        url.append(PREVIEW_URL_SEGMENT).append(weblog.getHandle()).append('/');
        
        if(locale != null) {
            url.append(locale).append('/');
        }
        
        Map<String, String> params = new HashMap<>();
        if(previewTheme != null) {
            params.put("theme", URLUtilities.encode(previewTheme));
        }
        if(previewAnchor != null) {
            params.put("previewEntry", URLUtilities.encode(previewAnchor));
        }
        
        return url.append(URLUtilities.getQueryString(params)).toString();
    }
    
    
    /**
     * Get url for a collection of entries on a given weblog.
     */
    @Override
    public String getWeblogCollectionURL(Weblog weblog,
                                                      String locale,
                                                      String category,
                                                      String dateString,
                                                      List<String> tags,
                                                      int pageNum,
                                                      boolean absolute) {
        
        if(weblog == null) {
            return null;
        }

        StringBuilder pathinfo = new StringBuilder(URL_BUFFER_SIZE);
        Map<String, String> params = new HashMap<>();
        
        if(absolute) {
        	pathinfo.append(WebloggerRuntimeConfig.getAbsoluteContextURL());
        } else {
        	pathinfo.append(WebloggerRuntimeConfig.getRelativeContextURL());
        }
        
        pathinfo.append(PREVIEW_URL_SEGMENT).append(weblog.getHandle()).append('/');
        
        if(locale != null) {
        	pathinfo.append(locale).append('/');
        }

        String cat;
        if("root".equals(category)) {
            cat = null;
        } else {
            cat = category;
        }
        
        if(cat != null && dateString == null) {
            pathinfo.append("category/").append(URLUtilities.encodePath(cat));
            
        } else if(dateString != null && cat == null) {
            pathinfo.append("date/").append(dateString);  
        
        } else if(tags != null && !tags.isEmpty()) {
            pathinfo.append("tags/").append(URLUtilities.getEncodedTagsString(tags));
        } else {
            if (dateString != null) {
                params.put("date", dateString);
            }
            if (cat != null) {
                params.put("cat", URLUtilities.encode(cat));
            }
        }

        if(pageNum > 0) {
            params.put("page", Integer.toString(pageNum));
        }
        
        if(previewTheme != null) {
            params.put("theme", URLUtilities.encode(previewTheme));
        }

        return pathinfo.append(URLUtilities.getQueryString(params)).toString();
    }
    

    /**
     * Get url for a custom page on a given weblog.
     */
    @Override
    public String getWeblogPageURL(Weblog weblog,
                                                String locale,
                                                String pageLink,
                                                String entryAnchor,
                                                String category,
                                                String dateString,
                                                List<String> tags,
                                                int pageNum,
                                                boolean absolute) {
        
        if(weblog == null) {
            return null;
        }
        
        StringBuilder pathinfo = new StringBuilder(URL_BUFFER_SIZE);
        Map<String, String> params = new HashMap<>();
        
        if(absolute) {
            pathinfo.append(WebloggerRuntimeConfig.getAbsoluteContextURL());
        } else {
            pathinfo.append(WebloggerRuntimeConfig.getRelativeContextURL());
        }
        
        pathinfo.append(PREVIEW_URL_SEGMENT).append(weblog.getHandle()).append('/');
        
        if(locale != null) {
            pathinfo.append(locale).append('/');
        }
        
        if(previewTheme != null) {
            params.put("theme", URLUtilities.encode(previewTheme));
        }
        
        if(pageLink != null) {
            pathinfo.append("page/").append(pageLink);
            
            // for custom pages we only allow query params
            if(dateString != null) {
                params.put("date", dateString);
            }
            if(category != null) {
                params.put("cat", URLUtilities.encode(category));
            }
            if(tags != null && !tags.isEmpty()) {
                params.put("tags", URLUtilities.getEncodedTagsString(tags));
            }
            if(pageNum > 0) {
                params.put("page", Integer.toString(pageNum));
            }
        } else {
            // if there is no page link then this is just a typical collection url
            return getWeblogCollectionURL(weblog, locale, category, dateString, tags, pageNum, absolute);
        }
        
        return pathinfo.append(URLUtilities.getQueryString(params)).toString();
    }
    
    
    /**
     * Get a url to a *preview* resource on a given weblog.
     */
    @Override
    public String getWeblogResourceURL(Weblog weblog, String filePath, boolean absolute) {
        
        if(weblog == null) {
            return null;
        }
        
        StringBuilder url = new StringBuilder(URL_BUFFER_SIZE);
        
        if(absolute) {
            url.append(WebloggerRuntimeConfig.getAbsoluteContextURL());
        } else {
            url.append(WebloggerRuntimeConfig.getRelativeContextURL());
        }
        
        url.append("/roller-ui/authoring/previewresource/").append(weblog.getHandle()).append('/');
        
        if(filePath.startsWith("/")) {
            url.append(filePath.substring(1));
        } else {
            url.append(filePath);
        }
        
        Map<String, String> params = Collections.emptyMap();
        if(previewTheme != null && !WeblogTheme.CUSTOM.equals(previewTheme)) {
            params = Map.of("theme", URLUtilities.encode(previewTheme));
        }
        
        return url.append(URLUtilities.getQueryString(params)).toString();
    }
    
}
