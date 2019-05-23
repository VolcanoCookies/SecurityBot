package objects;

public class VerifyRequest {
	public long serverID;
	public long userID;
	public long date;
	public long expiration;
	
	public VerifyRequest(long serverID, long userID, long date, long expiraton) {
		this.serverID = serverID;
		this.userID = userID;
		this.date = date;
		this.expiration = expiraton;
	}
}
