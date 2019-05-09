package listeners;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.server.member.ServerMemberJoinEvent;
import org.javacord.api.event.user.UserChangeMutedEvent;
import org.javacord.api.listener.server.member.ServerMemberJoinListener;
import org.javacord.api.listener.user.UserChangeMutedListener;
import org.javacord.api.util.event.ListenerManager;

public class AntiRaid implements ServerMemberJoinListener {

	@Override
	public void onServerMemberJoin(ServerMemberJoinEvent event) {
		event.time
	}

}
