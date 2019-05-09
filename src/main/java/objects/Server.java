package objects;

import java.util.Map;

import org.javacord.api.entity.user.User;

public class Server {
	org.javacord.api.entity.server.Server server;
	String serverID;
	Map<User, Long> lastJoinedUsers;
	String prefix;
	String verifyChannelID;
	
	
	public String getServerID() {
		return serverID;
	}
	public void setServerID(String serverID) {
		this.serverID = serverID;
	}
	public Map<User, Long> getLastJoinedUsers() {
		return lastJoinedUsers;
	}
	public void setLastJoinedUsers(Map<User, Long> lastJoinedUsers) {
		this.lastJoinedUsers = lastJoinedUsers;
	}
	public org.javacord.api.entity.server.Server getServer() {
		return server;
	}
	public void setServer(org.javacord.api.entity.server.Server server) {
		this.server = server;
	}
	public String getPrefix() {
		return prefix;
	}
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	public String getVerifyChannelID() {
		return verifyChannelID;
	}
	public void setVerifyChannelID(String verifyChannelID) {
		this.verifyChannelID = verifyChannelID;
	}
}
