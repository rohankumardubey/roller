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

package org.apache.roller.weblogger.ui.rendering.pagers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.planet.business.PlanetManager;
import org.apache.roller.planet.pojos.Planet;
import org.apache.roller.planet.pojos.SubscriptionEntry;
import org.apache.roller.planet.pojos.PlanetGroup;
import org.apache.roller.planet.pojos.Subscription;
import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.business.WebloggerFactory;


/**
 * Paging through a collection of planet entries.
 */
public class PlanetEntriesPager extends AbstractPager<SubscriptionEntry> {
    
    private static final Log log = LogFactory.getLog(PlanetEntriesPager.class);
    
    private String feedURL = null;
    private String groupHandle = null;
    private int sinceDays = -1;
    private int length = 0;
    
    // the collection for the pager
    private List<SubscriptionEntry> entries = null;
    
    // are there more items?
    private boolean more = false;
    
    
    public PlanetEntriesPager(
            URLStrategy    strat,
            String         feedURL,
            String         groupHandle,
            String         baseUrl,
            int            sinceDays,
            int            page,
            int            length) {
        
        super(strat, baseUrl, page);
        
        this.feedURL = feedURL;
        this.groupHandle = groupHandle;
        this.sinceDays = sinceDays;
        this.length = length;
        
        // initialize the collection
        getItems();
    }
    
    
    @Override
    public List<SubscriptionEntry> getItems() {
        
        if (entries == null) {
            // calculate offset
            int offset = getPage() * length;
            
            Date startDate = null;
            if(sinceDays > 0) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(new Date());
                cal.add(Calendar.DATE, -1 * sinceDays);
                startDate = cal.getTime();
            }
            
            List<SubscriptionEntry> results = new ArrayList<>();
            try {
                PlanetManager planetManager = WebloggerFactory.getWeblogger().getPlanetManager();
                Planet planet = planetManager.getWeblogger("default");
                
                List<SubscriptionEntry> subEntries;
                if (feedURL != null) {
                    Subscription sub = planetManager.getSubscription(feedURL);
                    subEntries = planetManager.getEntries(sub, offset, length+1);
                } else if (groupHandle != null) {
                    PlanetGroup group = planetManager.getGroup(planet, groupHandle);
                    subEntries = planetManager.getEntries(group, startDate, null, offset, length + 1);
                } else {
                    PlanetGroup group = planetManager.getGroup(planet, "all");
                    subEntries = planetManager.getEntries(group, startDate, null, offset, length + 1);
                }
                
                // wrap 'em
                int count = 0;
                for (SubscriptionEntry entry : subEntries) {
                    // TODO needs pojo wrapping from planet
                    if (count++ < length) { 
                        results.add(entry);
                    } else {
                        more = true;
                    }
                }
                
            } catch (Exception e) {
                log.error("ERROR: get aggregation", e);
            }
            
            entries = results;
        }
        
        return entries;
    }
    

    @Override
    public boolean hasMoreItems() {
        return more;
    }
}


