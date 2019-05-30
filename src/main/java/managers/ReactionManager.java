package managers;

import java.util.Map;

import org.javacord.api.entity.message.Message;
import org.javacord.api.event.message.reaction.ReactionAddEvent;
import org.javacord.api.listener.message.reaction.ReactionAddListener;

import objects.ReactionListener;

public class ReactionManager implements ReactionAddListener {

	private Map<Message, ReactionListener> reactionListeners;

	public ReactionManager(Map<Message, ReactionListener> reactionListeners) {
		this.reactionListeners = reactionListeners;
	}
	
	@Override
	public void onReactionAdd(ReactionAddEvent event) {
		
		if(event.getUser().isYourself()) return;
		
		Message message = event.getMessage().get();
		
		if(reactionListeners.containsKey(message)) reactionListeners.get(message).onClick(event);
		
	}
	
}
