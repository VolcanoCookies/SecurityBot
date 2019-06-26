package commands.fun;

import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;

import objects.Command;
import objects.PermissionLevels;
import objects.Server;

public class Music extends Command {

	public Music(PermissionLevels defaultPermission) {
		super(defaultPermission);
		setPrefix("music");
	}

	@Override
	public void execute(MessageCreateEvent event) {
		
		User user = getUser(event);
		Server server = getServer(event);
		ServerVoiceChannel voiceChannel = user.getConnectedVoiceChannel(server.getServer()).orElseGet(null);
		if(voiceChannel!=null) {
			if(getApi().getYourself().isConnected(voiceChannel)) {
				
			} else if(voiceChannel.canYouConnect()) {
				voiceChannel.connect
			}
		}
		
	}

	@Override
	public EmbedBuilder help() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean canUse(MessageCreateEvent event) {
		// TODO Auto-generated method stub
		return false;
	}
	
}
