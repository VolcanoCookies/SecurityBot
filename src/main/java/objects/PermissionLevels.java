package objects;

public enum PermissionLevels {
	NOCOMMANDS(-1),
	REGULAR(0),
	MODERATOR(1),
	ADMINISTRATOR(2),
	MANAGER(3);
	
	int permissionLevel;
	
	private PermissionLevels(int permissionLevel) {
		this.permissionLevel = permissionLevel;
	}
	
	
}
