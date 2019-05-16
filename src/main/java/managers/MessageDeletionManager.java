package managers;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import org.javacord.api.entity.message.Message;

public class MessageDeletionManager extends Thread {
	
	Map<Message, Long> messages;
	
	public MessageDeletionManager(Map<Message, Long> messages) {
		this.messages = messages;
	}
	
	@Override
	public void run() {
		while(true) {
			if(!messages.isEmpty()) {
				messages.forEach(DeleteNow());
			}
			try {
				TimeUnit.MILLISECONDS.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public BiConsumer<Message,Long> DeleteNow() {
		return (m,l) -> {if(m.getCreationTimestamp().toEpochMilli() + l < Instant.now().toEpochMilli()) {m.delete(); messages.remove(m);}};
	}
	
	public void addMessage(Message message, long timer) {
		messages.put(message, timer);
	}
}
