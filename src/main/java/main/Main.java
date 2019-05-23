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
import listeners.ServerJoinLeaveLogger;
import listeners.UserBanned;
import managers.MessageDeletionManager;
import objects.Server;

public class Main {
	
    //Add check for valid bot token, error print if no valid toker found

    //private static Logger logger = LogManager.getLogger(Main.class);
    private static DiscordApi api;
    
    static Map<Long, Server> servers = new ConcurrentHashMap<>();
    static Map<String, String> prefixes = new HashMap<>();
    static Map<Message, Long> messagesToDelete = new ConcurrentHashMap<>();
    static MessageDeletionManager messageDM = new MessageDeletionManager(messagesToDelete);
    
    public static final String DEFAULT_PREFIX = "!";
    
    //MongoDB Credentials
    //User: security_bot
    //Pass: vXFCVnSC1NvuT6nz
    
	static MongoClientURI uri = new MongoClientURI(
	    "mongodb://security_bot:vXFCVnSC1NvuT6nz@securitybot-shard-00-00-nmzyu.mongodb.net:27017,securitybot-shard-00-01-nmzyu.mongodb.net:27017,securitybot-shard-00-02-nmzyu.mongodb.net:27017/test?ssl=true&replicaSet=SecurityBot-shard-0&authSource=admin&retryWrites=true");
	static MongoClient mongoClient = new MongoClient(uri);

    public static void main(String[] args) {
        // Insert your bot's token here
        String token = "NTc1MjgzMjc3MDI0MTk4Njk2.XNFsUg.gkJ-5O1QKoAJXhXCBblC_NyE720";
        
        api = new DiscordApiBuilder().setToken(token).login().join();
        
        // Print the invite url of your bot
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
        
        messageDM.start();
        
        // Add listeners
        api.addListener(new ServerJoinLeaveLogger(mongoClient));
        //api.addListener(new MemberJoinLeave(mongoClient, servers));
        api.addListener(new UserBanned(mongoClient));
        
        //api.addListener(new MessageChanged(servers));
        //api.addListener(new RoleChanged(servers));
        
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
    }
    public static DiscordApi getAPI(){
        return api;
    }
    public Map<String, String> getPrefixes() {
		return prefixes;
	}
}