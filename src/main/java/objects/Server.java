package objects;

import java.util.HashMap;
import java.util.Map;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.user.User;

import main.Main;

public class Server {
	org.javacord.api.entity.server.Server server;
	Long serverID;
	Map<User, Long> lastJoinedUsers;
	String prefix = Main.DEFAULT_PREFIX;
	TextChannel logChannel;
	
	/*
	 * 		-1 	=	Banned from using commands
	 * 		0 	=	No permissions, regular user
	 * 		1 	=	Moderator on server
	 * 		2	=	Administrator on server
	 * 		3	=	All permissions
	 */
	Map<Long, PermissionLevels> permissions = new HashMap<Long, PermissionLevels>();
	
	
	public Long getServerID() {
		return serverID;
	}
	public void setServerID(Long serverID) {
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
	public boolean hasLogChannel() {
		return logChannel!=null;
	}
	public Map<Long, PermissionLevels> getPermissions() {
		return permissions;
	}
	public void addPermission(User user, PermissionLevels permissionLevel) {
		if(permissions.containsKey(user.getId())) permissions.replace(user.getId(), permissionLevel);
		else permissions.put(user.getId(), permissionLevel);
	}
	/**
	 * 
	 * @param user User to check permissions for
	 * @param permissionLevel Lowest permission level the user is allowed
	 * @return Returns true if the user has the specified permission level or above, assumes permission level REGULAR if user has no specific permission
	 */
	public boolean hasPermissions(User user, PermissionLevels permissionLevel) {
		if(permissions.getOrDefault(user.getId(), PermissionLevels.REGULAR).compareTo(permissionLevel) >= 0) return true;
		else return false;
	}
	public boolean hasHigherPermissions(User user, PermissionLevels permissionLevels) {
		if(permissions.getOrDefault(user.getId(), PermissionLevels.REGULAR).compareTo(permissionLevels) >= 1) return true;
		else return false;
	}
	public PermissionLevels getUserPermissions(User user) {
		return permissions.get(user.getId());
	}
	public PermissionLevels getUserPermissionsOrDefault(User user, PermissionLevels defaultPermissionLevel) {
		return permissions.getOrDefault(user.getId(), defaultPermissionLevel);
	}
}

