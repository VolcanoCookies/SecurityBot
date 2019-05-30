package objects;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bson.Document;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.user.User;

import com.mongodb.MongoClient;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.operation.UpdateOperation;

import main.Main;

public class Server {
	org.javacord.api.entity.server.Server server;
	Long serverID;
	Map<User, Long> lastJoinedUsers;
	String prefix = Main.DEFAULT_PREFIX;
	TextChannel logChannel;
	RaidLock raidLock = null;
	public boolean verificationEnabled = true;
	TextChannel verificationChannel;
	MongoClient mongoClient = Main.mongoClient;
	
	/*
	 * 		-1 	=	Banned from using commands
	 * 		0 	=	No permissions, regular user
	 * 		1 	=	Moderator on server
	 * 		2	=	Administrator on server
	 * 		3	=	All permissions
	 */
	Map<Long, PermissionLevels> permissions = new ConcurrentHashMap<>();
	Map<Command, PermissionLevels> commandPermissions = new ConcurrentHashMap<>();
	
	public Map<Command, PermissionLevels> getCommandPermissions() {
		return commandPermissions;
	}
	public void setCommandPermissions(Map<Command, PermissionLevels> commandPermissions) {
		this.commandPermissions = commandPermissions;
	}
	public PermissionLevels getCommandPermission(Command command) {
		return commandPermissions.getOrDefault(command, command.defaultPermission);
	}
	public boolean canExecute(User user, Command command) {
		if(hasPermissions(user, commandPermissions.getOrDefault(command, command.getDefaultPermission())) || server.isOwner(user)) return true;
		else return false;
	}
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
	public void setRaidLock(RaidLock raidLock) {
		this.raidLock = raidLock;
	}
	public boolean isRaidLock() {
		if(this.raidLock==null) return false;
		else return true;
	}
	public RaidLock getRaidLock() {
		return this.raidLock;
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
	public boolean isVerificationEnabled() {
		return verificationEnabled;
	}
	public void setVerificationEnabled(boolean verificationEnabled) {
		this.verificationEnabled = verificationEnabled;
	}
	public TextChannel getVerificationChannel() {
		return verificationChannel;
	}
	public void setVerificationChannel(TextChannel verificationChannel) {
		this.verificationChannel = verificationChannel;
	}
	public void setPermissions(Map<Long, PermissionLevels> permissions) {
		this.permissions = permissions;
	}
	public boolean hasVerificationChannel() {
		return verificationChannel!=null;
	}
	public void updateMongoDatabase() {
		Document filter = new Document("server_id", serverID);
		
		Document data = new Document();
		
		Document update = new Document("$set", data);
		
		UpdateOptions updateOptions = new UpdateOptions();
		updateOptions.upsert(true);
	}
}

