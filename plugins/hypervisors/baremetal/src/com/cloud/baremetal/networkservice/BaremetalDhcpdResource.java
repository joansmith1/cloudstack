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
// Apache License, Version 2.0 (the "License"); you may not use this
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// 
// Automatically generated by addcopyright.py at 04/03/2012
package com.cloud.baremetal.networkservice;

import java.util.HashMap;
import java.util.Map;

import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.PingCommand;
import com.cloud.agent.api.PingRoutingCommand;
import com.cloud.agent.api.routing.DhcpEntryCommand;
import com.cloud.utils.script.Script;
import com.cloud.utils.ssh.SSHCmdHelper;
import com.cloud.vm.VirtualMachine.State;
import com.trilead.ssh2.SCPClient;

public class BaremetalDhcpdResource extends BaremetalDhcpResourceBase {
	private static final Logger s_logger = Logger.getLogger(BaremetalDhcpdResource.class);
	
	public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
		com.trilead.ssh2.Connection sshConnection = null;
		try {
			super.configure(name, params);
			s_logger.debug(String.format("Trying to connect to DHCP server(IP=%1$s, username=%2$s, password=%3$s)", _ip, _username, "******"));
			sshConnection = SSHCmdHelper.acquireAuthorizedConnection(_ip, _username, _password);
			if (sshConnection == null) {
				throw new ConfigurationException(
						String.format("Cannot connect to DHCP server(IP=%1$s, username=%2$s, password=%3$s", _ip, _username, "******"));
			}

			if (!SSHCmdHelper.sshExecuteCmd(sshConnection, "[ -f '/usr/sbin/dhcpd' ]")) {
				throw new ConfigurationException("Cannot find dhcpd.conf /etc/dhcpd.conf at  on " + _ip);
			}

			SCPClient scp = new SCPClient(sshConnection);

			String editHosts = "scripts/network/exdhcp/dhcpd_edithosts.py";
			String editHostsPath = Script.findScript("", editHosts);
			if (editHostsPath == null) {
				throw new ConfigurationException("Can not find script dnsmasq_edithosts.sh at " + editHosts);
			}
			scp.put(editHostsPath, "/usr/bin/", "0755");
			
			String prepareDhcpdScript = "scripts/network/exdhcp/prepare_dhcpd.sh";
			String prepareDhcpdScriptPath = Script.findScript("", prepareDhcpdScript);
			if (prepareDhcpdScriptPath == null) {
				throw new ConfigurationException("Can not find prepare_dhcpd.sh at " + prepareDhcpdScriptPath);
			}
			scp.put(prepareDhcpdScriptPath, "/usr/bin/", "0755");
			
			//TODO: tooooooooooooooo ugly here!!!
			String[] ips = _ip.split("\\.");
			ips[3] = "0";
			StringBuffer buf = new StringBuffer();
			int i;
			for (i=0;i<ips.length-1;i++) {
				buf.append(ips[i]).append(".");
			}
			buf.append(ips[i]);
			String subnet = buf.toString();
			String cmd = String.format("sh /usr/bin/prepare_dhcpd.sh %1$s", subnet);
			if (!SSHCmdHelper.sshExecuteCmd(sshConnection, cmd)) {
				throw new ConfigurationException("prepare Dhcpd at " + _ip + " failed, command:" + cmd);
			}	
			
			s_logger.debug("Dhcpd resource configure successfully");
			return true;
		} catch (Exception e) {
			s_logger.debug("Dhcpd resorce configure failed", e);
			throw new ConfigurationException(e.getMessage());
		} finally {
			SSHCmdHelper.releaseSshConnection(sshConnection);
		}
	}
	
	@Override
	public PingCommand getCurrentStatus(long id) {
		com.trilead.ssh2.Connection sshConnection = SSHCmdHelper.acquireAuthorizedConnection(_ip, _username, _password);
		if (sshConnection == null) {
			return null;
		} else {
			SSHCmdHelper.releaseSshConnection(sshConnection);
			return new PingRoutingCommand(getType(), id, new HashMap<String, State>());
		}
	}
	
	Answer execute(DhcpEntryCommand cmd) {
		com.trilead.ssh2.Connection sshConnection = null;
		try {
			sshConnection = SSHCmdHelper.acquireAuthorizedConnection(_ip, _username, _password);
			if (sshConnection == null) {
				return new Answer(cmd, false, "ssh authenticate failed");
			}
			String addDhcp = String.format("python /usr/bin/dhcpd_edithosts.py %1$s %2$s %3$s %4$s %5$s %6$s",
					cmd.getVmMac(), cmd.getVmIpAddress(), cmd.getVmName(), cmd.getDns(), cmd.getGateway(), cmd.getNextServer());
			if (!SSHCmdHelper.sshExecuteCmd(sshConnection, addDhcp)) {
				return new Answer(cmd, false, "add Dhcp entry failed");
			} else {
				return new Answer(cmd);
			}
		} finally {
			SSHCmdHelper.releaseSshConnection(sshConnection);
		}
	}
	
	@Override
	public Answer executeRequest(Command cmd) {
		if (cmd instanceof DhcpEntryCommand) {
			return execute((DhcpEntryCommand)cmd);
		} else {
			return super.executeRequest(cmd);
		}
	}
}
