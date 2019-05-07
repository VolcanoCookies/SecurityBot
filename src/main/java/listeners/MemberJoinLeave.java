package listeners;

import org.javacord.api.event.server.member.ServerMemberJoinEvent;
import org.javacord.api.listener.server.member.ServerMemberJoinListener;

import mysqlhandler.Connector;

public class MemberJoinLeave implements ServerMemberJoinListener {
	Connector connector = new Connector();
	@Override
	public void onServerMemberJoin(ServerMemberJoinEvent event) {
		connector.ExecutePreparedStatement("REPLACE INTO Users (user_id,user_name,creation_date)\n" + 
										   "    VALUES\n" + 
										   "    (\"" + event.getUser().getIdAsString() + "\",\"" + event.getUser().getDiscriminatedName() + "\",\"" + event.getUser().getCreationTimestamp().toString() + "\",\"" + event.getUser().isBot() + "\");");
	}

}
