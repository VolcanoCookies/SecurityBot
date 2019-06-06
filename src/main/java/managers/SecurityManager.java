package managers;

import java.awt.Color;
import java.time.Instant;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;

import org.bson.Document;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.ExplicitContentFilterLevel;
import org.javacord.api.entity.server.ServerUpdater;
import org.javacord.api.entity.server.VerificationLevel;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;

import main.Init;
import objects.RaidLock;
import objects.Server;

public class SecurityManager {
	
	public Map<Long, Long> raidTimer;
	private Thread unlockerThread;
	private MongoCollection<Document> serverCollection;

	public SecurityManager(MongoClient mongoClient, Map<Long, Server> servers) {
		this.serverCollection = mongoClient.getDatabase("index").getCollection("servers");
		
		unlockerThread = new Thread(() -> {
			while(true) {
				if(raidTimer.isEmpty()) {
					try {
						wait();
					} catch (InterruptedException e) {
						
					}
				} else {
					long lowestValue = Long.MAX_VALUE;
					for(Entry<Long, Long> l : raidTimer.entrySet()) {
						if(l.getValue() - 5000l < Instant.now().toEpochMilli()) {
							RaidUnlock(servers.get(l.getKey()));
							if(raidTimer.containsKey(l.getKey())) {
								raidTimer.remove(l.getKey());
							}
						} else {
							if(l.getValue() < lowestValue) {
								lowestValue = l.getValue();
							}
						}
					}
					
					try {
						Thread.sleep(lowestValue);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
			}
		});
	}
	
	public void RaidLockdown(Server server, long time) {
		RaidLockdown(server, time, "");
	}
	
	/**
	 * 
	 * @param server
	 * Server to lockdown
	 * @param time
	 * For how long the server should be locked, in milliseconds.
	 * @param cause
	 * The reason for the lockdown, can be left empty.
	 */
	public void RaidLockdown(Server server, long time, String cause) {
		
		raidTimer.put(server.getServerID(), Instant.now().plusMillis(time).toEpochMilli());
		unlockerThread.notify();

		org.javacord.api.entity.server.Server currentServer = server.getServer();
		
		ServerUpdater serverUpdater = new ServerUpdater(currentServer);
		RaidLock raidLock = new RaidLock(time, currentServer.getVerificationLevel(), currentServer.getExplicitContentFilterLevel());
		server.setRaidLock(raidLock);
		
		serverUpdater.setVerificationLevel(VerificationLevel.HIGH);
		serverUpdater.setExplicitContentFilterLevel(ExplicitContentFilterLevel.MEMBERS_WITHOUT_ROLES);
		serverUpdater.update();
		
		long millis = time % 1000;
		long second = (time / 1000) % 60;
		long minute = (time / (1000 * 60)) % 60;
		long hour = (time / (1000 * 60 * 60)) % 24;

		String timeString = String.format("%02d:%02d:%02d.%d", hour, minute, second, millis);
		
		//Send DM to server owner
		try {
			new MessageBuilder()
			.setEmbed(new EmbedBuilder()
					.setTitle("Server RaidLockdown Alert")
					.addField("Server", currentServer.getName())
					.addInlineField("ID", currentServer.getIdAsString())
					.setColor(new Color(255, 50, 50))
					.addField("Raid Lockdown", "One of your servers has been put in raid lockdown mode, this means the verification level has been set to high and all messages from members without roles will be scanned by discord.\n\n" + 
					"The server will automatically exit raid lockdown mode in approximately " + timeString + " from this moment. No further action is required, to exit raid lockdown mode manually use the command [exitlockdown], this command can by default only be executed by the server owner and administrators.")
					.setTimestampToNow()
					.setThumbnail(Init.nostoppingIcon))
			.send(currentServer.getOwner().openPrivateChannel().get());
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Send message to log channel
		if(server.hasLogChannel()) {
			new MessageBuilder()
			.setEmbed(new EmbedBuilder()
					.setTitle("Server is now in RaidLockdown Mode")
					.setColor(new Color(255, 50, 50))
					.addField("Raid Lockdown", "The server is currently in raid lockdown mode, this means the verification level has been set to high and all messages from members without roles will be scanned by discord.\n\n" + 
					"The server will automatically exit raid lockdown mode in 10 minutes. No further action is required, to exit raid lockdown mode manually use the command [exitlockdown], this command can by default only be executed by the server owner and administrators.")
					.setTimestampToNow()
					.setThumbnail(Init.nostoppingIcon))
			.send(server.getLogChannel());
		} else { //Send a message to the System Channel if present.
			try { 
				ServerTextChannel systemChannel = currentServer.getSystemChannel().get();
				new MessageBuilder()
				.setEmbed(new EmbedBuilder()
						.setTitle("Server is now in RaidLockdown Mode")
						.setColor(new Color(255, 50, 50))
						.addField("Raid Lockdown", "The server is currently in raid lockdown mode, this means the verification level has been set to high and all messages from members without roles will be scanned by discord.\n\n" + 
						"The server will automatically exit raid lockdown mode in 10 minutes. No further action is required, to exit raid lockdown mode manually use the command [exitlockdown], this command can by default only be executed by the server owner and administrators.")
						.setTimestampToNow()
						.setThumbnail(Init.nostoppingIcon))
				.send(systemChannel);
			} catch (NoSuchElementException e) {}
		}
		
		/*
		 * Log to MongoDB so server can exit raid lockdown after a bot restart.
		 */
		
		Document filter = new Document("server_id", currentServer.getId());
		
		Document data = new Document();
		data.append("raidlock.timer", time);
		data.append("raidlock.unlock_date", Instant.now().plusMillis(time).toString());
		data.append("raidlock.date", Instant.now().toString());
		data.append("raidlock.normal_verification_level", currentServer.getVerificationLevel());
		data.append("raidlock.normal_explicit_content_level", currentServer.getExplicitContentFilterLevel());
		data.append("raidlock.cause", cause);
		
		Document update = new Document("$set", data);
		
		UpdateOptions updateOptions = new UpdateOptions();
		updateOptions.upsert(true);
		
		serverCollection.updateOne(filter, update, updateOptions);
		
	}
	
	public void RaidUnlock(Server server) {
		
		raidTimer.remove(server.getServerID());
		unlockerThread.notify();
		
		RaidLock raidLock = server.getRaidLock();
		server.setRaidLock(null);
		
		org.javacord.api.entity.server.Server currentServer = server.getServer();
		ServerUpdater serverUpdater = new ServerUpdater(currentServer);
		
		serverUpdater.setVerificationLevel(raidLock.getNormalVerificationLevel());
		serverUpdater.setExplicitContentFilterLevel(raidLock.getNormalExplicitContentFilterLevel());
		
		serverUpdater.update();
		
		//Send DM to server owner
		try {
			new MessageBuilder()
			.setEmbed(new EmbedBuilder()
					.setTitle("Server RaidLockdown Alert")
					.addField("Server", currentServer.getName())
					.addInlineField("ID", currentServer.getIdAsString())
					.setColor(new Color(50, 255, 50))
					.addField("Raid Lockdown", "One of your servers has exited raid lockdown mode, verification and content scanning levels have been set to their respective pre-raid values.\n\n" + 
					"No further action is required, to manually enter raid lockdown mode use the command [lockdown <duration>(In seconds)], this command can by default only be executed by the server owner and administrators.")
					.setTimestampToNow()
					.setThumbnail(Init.checkdocumentIcon))
			.send(currentServer.getOwner().openPrivateChannel().get());
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Send message to log channel
		if(server.hasLogChannel()) {
			new MessageBuilder()
			.setEmbed(new EmbedBuilder()
					.setTitle("RaidLockdown mode disabled")
					.setColor(new Color(50, 255, 50))
					.addField("Raid Lockdown", "RaidLockdown mode has now been disabled on the server, verification level and content scanning levels have been reduced to their respective pre-raid values.\n\n" + 
					"No further action is required, to manually enter raid lockdown mode use the command [lockdown <duration(In seconds)>], this command can by default only be executed by the server owner and administrators.")
					.setTimestampToNow()
					.setThumbnail(Init.checkdocumentIcon))
			.send(server.getLogChannel());
		} else { //Send a message to the System Channel if present.
			try { 
				ServerTextChannel systemChannel = currentServer.getSystemChannel().get();
				new MessageBuilder()
				.setEmbed(new EmbedBuilder()
						.setTitle("RaidLockdown mode disabled")
						.setColor(new Color(50, 255, 50))
						.addField("Raid Lockdown", "RaidLockdown mode has now been disabled on the server, verification level and content scanning levels have been reduced to their respective pre-raid values.\n\n" + 
						"No further action is required, to manually enter raid lockdown mode use the command [lockdown <duration>(In seconds)], this command can by default only be executed by the server owner and administrators.")
						.setTimestampToNow()
						.setThumbnail(Init.checkdocumentIcon))
				.send(systemChannel);
			} catch (NoSuchElementException e) {}
		}
		
		//Remove MongoDB entry
		
		Document filter = new Document("server_id", server.getServerID());
		
		Document update = serverCollection.find(filter).first();
		update.remove("raidlock");
		
		serverCollection.replaceOne(filter, update);
		
	}
	
}
