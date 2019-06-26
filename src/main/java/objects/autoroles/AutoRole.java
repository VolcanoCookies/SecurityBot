package objects.autoroles;

import org.javacord.api.entity.permission.Role;

public class AutoRole {
	
	Role role;
	AutoRoleConditions condition;
	Long afterTime;
	
	public boolean isAfterTime() {
		return condition.equals(AutoRoleConditions.AfterSetTime);
	}
	public boolean isOnJoin() {
		return condition.equals(AutoRoleConditions.OnJoin);
	}
	public boolean isOnverification() {
		return condition.equals(AutoRoleConditions.OnVerification);
	}
	//Getters and setters
	public Role getRole() {
		return role;
	}
	public void setRole(Role role) {
		this.role = role;
	}
	public AutoRoleConditions getCondition() {
		return condition;
	}
	public void setCondition(AutoRoleConditions condition) {
		this.condition = condition;
	}
	public Long getAfterTime() {
		return afterTime;
	}
	public void setAfterTime(Long afterTime) {
		this.afterTime = afterTime;
	}
}
