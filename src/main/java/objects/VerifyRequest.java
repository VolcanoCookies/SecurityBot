package objects;

import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

public class VerifyRequest {
	
	public User user;
	public int attemptsLeft = 3;
	public String token;
	public long expiration;
	public Server server;
	
	public VerifyRequest(User user, Server server, String token, long expiraton) {
		this.user = user;
		this.token = token;
		this.expiration = expiraton;
		this.server = server;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public int getAttemptsLeft() {
		return attemptsLeft;
	}
	public void setAttemptsLeft(int attemptsLeft) {
		this.attemptsLeft = attemptsLeft;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public long getExpiration() {
		return expiration;
	}
	public void setExpiration(long expiration) {
		this.expiration = expiration;
	}
	public Server getServer() {
		return server;
	}
	public void setServer(Server server) {
		this.server = server;
	}	
}
