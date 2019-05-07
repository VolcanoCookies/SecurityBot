package listeners;

import java.util.concurrent.ExecutionException;

import org.javacord.api.event.server.member.ServerMemberBanEvent;
import org.javacord.api.listener.server.member.ServerMemberBanListener;

import mysqlhandler.Connector;

public class UserBanned implements ServerMemberBanListener {
	
	Connector connector = new Connector();
	
	@Override
	public void onServerMemberBan(ServerMemberBanEvent event) {
		try {
			connector.ExecutePreparedStatement("REPLACE INTO Banned (user_id,server_id,reason,time)\n" + 
											   "    VALUES\n" + 
											   "    (\"" + event.getUser().getIdAsString() + "\",\"" + event.getServer().getIdAsString() + "\",\"" + event.requestReason().get() + "\")");
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
