package objects;

import java.util.Map;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.user.User;

public class Server {
	org.javacord.api.entity.server.Server server;
	String serverID;
	Map<User, Long> lastJoinedUsers;
	String prefix;
	TextChannel logChannel;
	
	
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
	public TextChannel getLogChannel() {
		return logChannel;
	}
	public void setLogChannel(TextChannel logChannel) {
		this.logChannel = logChannel;
	}
	
}
