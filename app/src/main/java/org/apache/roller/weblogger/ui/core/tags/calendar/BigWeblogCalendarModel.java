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

package org.apache.roller.weblogger.ui.core.tags.calendar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntrySearchCriteria;
import org.apache.roller.weblogger.ui.rendering.util.WeblogPageRequest;
import org.apache.roller.util.DateUtil;


/**
 * Model for big calendar that displays titles for each day.
 */
public class BigWeblogCalendarModel extends WeblogCalendarModel {
    
    private static final Log mLogger = LogFactory.getLog(BigWeblogCalendarModel.class);

    protected final SimpleDateFormat starDateFormat =
            DateUtil.get8charDateFormat();

    protected final SimpleDateFormat singleDayFormat =
            new SimpleDateFormat("dd");


    public BigWeblogCalendarModel(WeblogPageRequest pRequest, String cat) {
        super(pRequest, cat);
        TimeZone tz = weblog.getTimeZoneInstance();
        starDateFormat.setTimeZone(tz);
        singleDayFormat.setTimeZone(tz);
    }
    
    
    @Override
    protected void loadWeblogEntries(Date startDate, Date endDate, String catName) {
        try {
            WeblogEntryManager mgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
            WeblogEntrySearchCriteria wesc = new WeblogEntrySearchCriteria();
            wesc.setWeblog(weblog);
            wesc.setStartDate(startDate);
            wesc.setEndDate(endDate);
            wesc.setCatName(catName);
            wesc.setStatus(WeblogEntry.PubStatus.PUBLISHED);
            wesc.setLocale(locale);
            monthMap = mgr.getWeblogEntryObjectMap(wesc);
        } catch (WebloggerException e) {
            mLogger.error(e);
            monthMap = new HashMap<>();
        }
    }
    
    
    @Override
    public String getContent(Date day) {
        String content = null;
        try {
            StringBuilder sb = new StringBuilder();
            
            // get the 8 char YYYYMMDD datestring for day, returns null
            // if no weblog entry on that day
            String dateString;
            List<?> entries = (List<?>)monthMap.get(day);
            if ( entries != null ) {
                dateString = starDateFormat.format(
                        ((WeblogEntry)entries.get(0)).getPubTime());
                
                // append 8 char date string on end of selfurl
                String dayUrl = WebloggerFactory.getWeblogger().getUrlStrategy().getWeblogCollectionURL(weblog, locale, cat, dateString, null, -1, false);
                              
                sb.append("<div class=\"hCalendarDayTitleBig\">");
                sb.append("<a href=\"");
                sb.append( dayUrl );
                sb.append("\">");
                sb.append(singleDayFormat.format(day));
                sb.append("</a></div>");
                
                for ( int i=0; i<entries.size(); i++ ) {
                    sb.append("<div class=\"bCalendarDayContentBig\">");
                    sb.append("<a href=\"");
                    sb.append(((WeblogEntry)entries.get(i)).getPermalink());
                    sb.append("\">");
                    
                    String title = ((WeblogEntry)entries.get(i)).getTitle().trim();
                    if ( title.length()==0 ) {
                        title = ((WeblogEntry)entries.get(i)).getAnchor();
                    }
                    if ( title.length() > 20 ) {
                        title = title.substring(0,20)+"...";
                    }
                    
                    sb.append( title );
                    sb.append("</a></div>");
                }
                
            } else {
                sb.append("<div class=\"hCalendarDayTitleBig\">");
                sb.append(singleDayFormat.format(day));
                sb.append("</div>");
                sb.append("<div class=\"bCalendarDayContentBig\"/>");
            }
            content = sb.toString();
        } catch (Exception e) {
            mLogger.error("ERROR: creating URL", e);
        }
        return content;
    }
    
    /**
     * Create URL for use on view-weblog page
     * @param day              Day for URL or null if no entries on that day
     * @param nextPrevMonthURL True to create next/prev month URL
     * @param alwaysURL        Always return a URL, never return null
     * @return URL for day, or null if no weblog entry on that day
     */
    @Override
    public String computeUrl(Date day, boolean nextPrevMonthURL, boolean alwaysURL) {
        String url = null;
        // get the 8 char YYYYMMDD datestring for day, returns null
        // if no weblog entry on that day
        String dateString = null;
        List<?> entries = (List<?>)monthMap.get( day );
        if ( entries != null && day != null ) {
            WeblogEntry entry = (WeblogEntry)entries.get(0);
            dateString = starDateFormat.format(entry.getPubTime());
        }
        if (dateString == null && !alwaysURL) {
            return null;
        }
        else if (dateString == null && !nextPrevMonthURL) {
            dateString = DateUtil.format8chars(day);
        } else if (dateString == null) {
            dateString = DateUtil.format6chars(day);
        }
        try {
            if (nextPrevMonthURL && pageLink != null) { 
                // next/prev month URLs point to current page
                url = WebloggerFactory.getWeblogger().getUrlStrategy().getWeblogPageURL(weblog, locale, pageLink, null, cat, dateString, null, -1, false);
            } else { 
                // all other URLs point back to main weblog page
                url = WebloggerFactory.getWeblogger().getUrlStrategy().getWeblogCollectionURL(weblog, locale, cat, dateString, null, -1, false);
            }
        } catch (Exception e) {
            mLogger.error("ERROR: creating URL",e);
        }
        return url;
    }
}
