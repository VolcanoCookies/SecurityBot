package listeners;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.javacord.api.entity.user.User;
import org.javacord.api.event.server.member.ServerMemberJoinEvent;
import org.javacord.api.listener.server.member.ServerMemberJoinListener;

import objects.Server;

public class AntiMassJoin implements ServerMemberJoinListener {
	
	List<Server> servers = new ArrayList<>();
	
	@Override
	public void onServerMemberJoin(ServerMemberJoinEvent event) {
		for(Server server : servers) {
			if(server.getServerID().equals(event.getServer().getIdAsString())) {
				server.getLastJoinedUsers().put(event.getUser(), Instant.now().toEpochMilli());
				server.getLastJoinedUsers().values().removeIf(isOld());
				if(server.getLastJoinedUsers().size() >= 10) {
					//Anti raid mode for server
					//10 or more users joined within the last 15 seconds
					for(User user : server.getLastJoinedUsers().keySet()) {
						
					}
				}
			} else {
				Server s = new Server();
				s.setServerID(event.getServer().getIdAsString());
				s.getLastJoinedUsers().put(event.getUser(), Instant.now().toEpochMilli());
				servers.add(s);
			}
		}
	}
	public static Predicate<Long> isOld() {
		return p -> p.longValue()+15000 < Instant.now().toEpochMilli();
	}
}
