package listeners;

import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.server.ServerJoinEvent;
import org.javacord.api.event.server.ServerLeaveEvent;
import org.javacord.api.listener.server.ServerJoinListener;
import org.javacord.api.listener.server.ServerLeaveListener;

import mysqlhandler.Connector;

public class ServerJoinLeave implements ServerJoinListener,ServerLeaveListener {

	Connector connector = new Connector();
	
	@Override
	public void onServerJoin(ServerJoinEvent event) {
		connector.ExecutePreparedStatement("REPLACE INTO Servers (server_id,server_name,connected)\n    VALUES\n    (\"" + event.getServer().getIdAsString() + "\",\"" + event.getServer().getName() + "\",\"1\")");
		
		ProfileServerUsers(event.getServer());
	}

	@Override
	public void onServerLeave(ServerLeaveEvent event) {
		connector.ExecutePreparedStatement("REPLACE INTO Servers (server_id,server_name,connected)\n    VALUES\n    (\"" + event.getServer().getIdAsString() + "\",\"" + event.getServer().getName() + "\",\"0\")");
	}
	
	private void ProfileServerUsers(Server server) {
		String statement = "";
		for(User user : server.getMembers()) {
			String isBot = "0";
			if(user.isBot()) isBot = "1";
			String timestamp = user.getCreationTimestamp().toString();
			if(timestamp.contains(".")) timestamp = timestamp.substring(0, timestamp.indexOf("."));
			else timestamp = timestamp.replaceAll("Z", "");
			timestamp = timestamp.replaceAll("T", " ");
			statement += "\n    (\"" + user.getIdAsString() + "\",\"" + user.getDiscriminatedName() + "\",\"" + timestamp + "\",\"" + isBot + "\"),";
		}
		connector.ExecutePreparedStatement("REPLACE INTO Users (user_id,user_name,creation_date,is_bot)\n" + 
				   "    VALUES" + statement.substring(0, statement.lastIndexOf(",")) + ";");
	}
	
}
