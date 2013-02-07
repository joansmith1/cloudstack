// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
// 
//   http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
// 
// Automatically generated by addcopyright.py at 01/29/2013
package com.cloud.baremetal.networkservice;

import java.util.List;

import javax.inject.Inject;

import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseCmd;
import org.apache.cloudstack.api.BaseCmd.CommandType;
import org.apache.cloudstack.api.BaseListCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.ListResponse;
import org.apache.log4j.Logger;

import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.NetworkRuleConflictException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;

public class ListBaremetalDhcpCmd extends BaseListCmd {
    private static final Logger s_logger = Logger.getLogger(ListBaremetalDhcpCmd.class);
    private static final String s_name = "listexternaldhcpresponse";
    @Inject BaremetalDhcpManager _dhcpMgr;
    
    // ///////////////////////////////////////////////////
    // ////////////// API parameters /////////////////////
    // ///////////////////////////////////////////////////
    @Parameter(name = ApiConstants.ID, type = CommandType.LONG, description = "DHCP server device ID")
    private Long id;
    
    @Parameter(name = ApiConstants.POD_ID, type = CommandType.LONG, description = "Pod ID where pxe server is in")
    private Long podId;
    
    @Parameter(name = ApiConstants.DHCP_SERVER_TYPE, type = CommandType.STRING, description = "Type of DHCP device")
    private String deviceType;
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPodId() {
        return podId;
    }

    public void setPodId(Long podId) {
        this.podId = podId;
    }
    
    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    @Override
    public void execute() throws ResourceUnavailableException, InsufficientCapacityException, ServerApiException, ConcurrentOperationException,
            ResourceAllocationException, NetworkRuleConflictException {
    	try {
            ListResponse<BaremetalDhcpResponse> response = new ListResponse<BaremetalDhcpResponse>();
            List<BaremetalDhcpResponse> dhcpResponses = _dhcpMgr.listBaremetalDhcps(this);
            response.setResponses(dhcpResponses);
            response.setResponseName(getCommandName());
            this.setResponseObject(response);
    	} catch (Exception e) {
    		s_logger.debug("Exception happend while executing ListBaremetalDhcpCmd");
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, e.getMessage());
    	}

    }

    @Override
    public String getCommandName() {
        return s_name;
    }

}
