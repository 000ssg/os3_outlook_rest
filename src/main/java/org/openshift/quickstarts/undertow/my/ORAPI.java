/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openshift.quickstarts.undertow.my;

import com.ssg.common.annotations.XMethod;
import com.ssg.common.annotations.XParameter;
import com.ssg.common.annotations.XType;
import java.io.IOException;
import org.openshift.quickstarts.undertow.servlet.OutlookAuth;
import org.openshift.quickstarts.undertow.servlet.OutlookServlet;

/**
 *
 * @author sesidoro
 */
@XType
public class ORAPI {

    OutlookAuth oauth;
    String token;
    OutlookAuth.RoomLists rls;
    long timestamp;
    long expire = System.currentTimeMillis();

    
    OutlookAuth getOA() {
        if(oauth==null) oauth=OutlookServlet.oa;
        return oauth;
    }
    
    @XMethod
    public boolean isLoggedIn() {
        return oauth != null && token != null;
    }

    @XMethod
    public void setToken(@XParameter(name = "token") String token) {
        this.token = token;
    }

    @XMethod
    public OutlookAuth.RoomLists rooms(@XParameter(name = "force") Boolean force, @XParameter(name = "async") Boolean async) throws IOException {
        if (force == null) {
            force = false;
        }
        if (async == null) {
            async = false;
        }

        if (force || rls == null || expire <= System.currentTimeMillis()) {
            rls = getOA().fetchRooms(token, OutlookAuth.TIME_PERIOD.today);
            timestamp = System.currentTimeMillis();
            expire = timestamp + 1000 * 60 * 60 * 5;
        }

        if (!async && rls.fetching) {
            long timeout = System.currentTimeMillis() + 1000 * 60 * 3;
            while (rls.fetching && System.currentTimeMillis() < timeout) {
                try {
                    Thread.sleep(100);
                } catch (Throwable th) {
                    break;
                }
            }
        }

        return rls;
    }

}
