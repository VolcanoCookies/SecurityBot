package main;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.message.Message;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

import commands.Ban;
import commands.Clear;
import commands.Investrigate;
import commands.Kick;
import commands.Permissions;
import commands.SetLogChannel;
import commands.SetPrefix;
import commands.TestCommand;
import listeners.AntiSpam;
import listeners.MessageChanged;
import listeners.RoleChangedListener;
import listeners.ServerJoinLeaveLogger;
import listeners.UserBannedListener;
import managers.MessageGarbageThread;
import objects.Server;

public class Main {
	
    //private static Logger logger = LogManager.getLogger(Main.class);
    private static DiscordApi api;
    
    static Map<Long, Server> servers = new ConcurrentHashMap<>();
    static Map<String, String> prefixes = new HashMap<>();
    static Map<Message, Long> messagesToDelete = new ConcurrentHashMap<>();
    static MessageGarbageThread messageGarbageThread = new MessageGarbageThread(messagesToDelete);
    
    public static final String DEFAULT_PREFIX = "!";
    
    static String databaseName, databasePassword;
    static String botToken;
    
	static MongoClientURI uri;
	static MongoClient mongoClient;

    public static void main(String[] args) {
    	
    	botToken = args[0];
    	databaseName = args[1];
    	databasePassword = args[2];
    	
    	//Login into mongoDB
    	uri = new MongoClientURI("mongodb://" + databaseName + ":" + databasePassword + "@securitybot-shard-00-00-nmzyu.mongodb.net:27017,securitybot-shard-00-01-nmzyu.mongodb.net:27017,securitybot-shard-00-02-nmzyu.mongodb.net:27017/test?ssl=true&replicaSet=SecurityBot-shard-0&authSource=admin&retryWrites=true");
    	mongoClient = new MongoClient(uri);
    	
    	//Login into discord
        api = new DiscordApiBuilder().setToken(botToken).login().join();
        
        api.getThreadPool().getExecutorService().execute(() -> {
        	int connectedServers = api.getServers().size();
        	int totalMembers = 0;
        	for(org.javacord.api.entity.server.Server server : api.getServers()) {
        		totalMembers += server.getMembers().size();
        	}
        	
        	System.out.println("<Bootup> Successfull login\n" + 
        					   "<Bootup> Connected to servers\t[" + connectedServers + "]\n" + 
        					   "<Bootup> Total members       \t[" + totalMembers + "]\n" + 
        					   "<Bootup> Invite code: " + api.createBotInvite());
        	
        });
        
        new Init(api, mongoClient, servers).run();
        
        //Start message garbage thread to remove messages set to be deleted.
        messageGarbageThread.start();
        
        // Add listeners
        api.addListener(new ServerJoinLeaveLogger(mongoClient));
        //api.addListener(new MemberJoinLeave(mongoClient, servers));
        api.addListener(new UserBannedListener(mongoClient, servers));
        
        api.addListener(new MessageChanged(servers));
        api.addListener(new RoleChangedListener(servers));
        
        api.addMessageCreateListener(new TestCommand(mongoClient));
        api.addMessageCreateListener(new AntiSpam(mongoClient));
        api.addMessageCreateListener(new Clear(servers, messagesToDelete));
        api.addMessageCreateListener(new SetPrefix(mongoClient, servers, messagesToDelete));
        api.addMessageCreateListener(new Permissions(mongoClient, servers, messagesToDelete));
        api.addMessageCreateListener(new Ban(prefixes, DEFAULT_PREFIX, messagesToDelete));
        api.addMessageCreateListener(new Kick(mongoClient, prefixes, DEFAULT_PREFIX, messagesToDelete));
        api.addMessageCreateListener(new SetLogChannel(mongoClient, messagesToDelete, servers));
        api.addMessageCreateListener(new Investrigate(mongoClient, prefixes, DEFAULT_PREFIX, messagesToDelete));
        
        // Log a message, if the bot joined or left a server
//        api.addServerJoinListener(event -> logger.info("Joined server " + event.getServer().getName()));
//        api.addServerLeaveListener(event -> logger.info("Left server " + event.getServer().getName()));
        api.addServerLeaveListener(e -> System.out.println("Bot left " + e.getServer().getName() + "."));
        api.addServerJoinListener(e -> System.out.println("Bot joined " + e.getServer().getName() + " with " + e.getServer().getMemberCount() + " members."));
    }
    public static DiscordApi getAPI(){
        return api;
    }
    public Map<String, String> getPrefixes() {
		return prefixes;
	}
}