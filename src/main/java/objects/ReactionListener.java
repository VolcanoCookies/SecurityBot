package objects;

import java.util.ArrayList;
import java.util.List;

import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.message.Message;
import org.javacord.api.event.message.reaction.ReactionAddEvent;

public abstract class ReactionListener {
	
	Message message;
	List<Emoji> emojis = new ArrayList<>();
	
	long creationDate;
	long removeDate;
	
	public ReactionListener(Message message) {
		this.message = message;
	}
	
	public abstract void onClick(ReactionAddEvent event);
	
	public List<Emoji> getEmojis() {
		return emojis;
	}
	public void removeAllReactions(){
		message.removeAllReactions();
	}
	public void removeUsersReactions() {
		message.getReactions().forEach(r -> {
			r.getUsers().thenAccept(ul -> {
				ul.forEach(u -> {
					if(!u.isYourself()) r.removeUser(u);
				});
			});
		});
	}
	public Message getMessage() {
		return message;
	}
	public void setMessage(Message message) {
		this.message = message;
	}
	public long getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(long creationDate) {
		this.creationDate = creationDate;
	}
	public long getRemoveDate() {
		return removeDate;
	}
	public void setRemoveDate(long removeDate) {
		this.removeDate = removeDate;
	}
	public void setEmojis(List<Emoji> emojis) {
		this.emojis = emojis;
	}
}
