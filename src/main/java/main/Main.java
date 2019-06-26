package main;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.message.Message;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

import commands.Profile;
import commands.TestCommand;
import commands.TestReaction;
import commands.configuration.AddBannedWord;
import commands.configuration.Permissions;
import commands.configuration.RaidLock;
import commands.configuration.SetPrefix;
import commands.configuration.Settings;
import commands.fun.Hug;
import commands.moderation.Ban;
import commands.moderation.Clear;
import commands.moderation.ClearWarn;
import commands.moderation.Warn;
import commands.verification.VerificationSettings;
import commands.verification.Verify;
import listeners.AntiSpam;
import listeners.MemberJoinLeave;
import listeners.MentionListener;
import listeners.RoleChangedListener;
import listeners.ServerJoinLeaveLogger;
import listeners.UserBannedListener;
import logging.MessageDeleteLogger;
import managers.CommandManager;
import managers.ConsoleCommandsManager;
import managers.GarbageManager;
import managers.MessageGarbageThread;
import managers.ReactionManager;
import managers.SecurityManager;
import managers.VerifyManager;
import objects.Command;
import objects.PermissionLevels;
import objects.ReactionListener;
import objects.Server;
import objects.VerifyRequest;

public class Main {
	
    //private static Logger logger = LogManager.getLogger(Main.class);
    private static DiscordApi api;
    
    //Booleans
    public static boolean enableConsoleCommands = true;
    public static boolean showMessageDeleteCompletionTime = false;
    
    public static Map<Long, Server> servers = new ConcurrentHashMap<>();
    public static Map<Message, Long> messagesToDelete = new ConcurrentHashMap<>();
    private static Map<String, Command> commands = new ConcurrentHashMap<>();
    private static Map<Message, ReactionListener> reactionListeners = new ConcurrentHashMap<>();
    private static Map<Long, VerifyRequest> verifyRequests = new ConcurrentHashMap<>();
    
    static MessageGarbageThread messageGarbageThread = new MessageGarbageThread(messagesToDelete);
    static SecurityManager securityManager;
    static CommandManager commandManager;
    static ReactionManager reactionManager;
    static VerifyManager verifyManager;
    static GarbageManager garbageManager;
    static ConsoleCommandsManager consoleManager;
    
    public static final String DEFAULT_PREFIX = "!";
    
    static String databaseName, databasePassword;
    static String botToken;
    
	static MongoClientURI uri;
	public static MongoClient mongoClient;

	private static MessageDeleteLogger messageDeleteLogger;

	private static int addedCommands;

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
        
        new Init(api, mongoClient, servers, verifyRequests);
        
        messageDeleteLogger = new MessageDeleteLogger(servers);
        
        //Create command associations
        addCommand(new TestCommand(PermissionLevels.REGULAR));
        addCommand(new Permissions(PermissionLevels.MANAGER));
        addCommand(new TestReaction(PermissionLevels.REGULAR));
        addCommand(new Clear(PermissionLevels.MODERATOR, messageDeleteLogger));
        addCommand(new Verify(PermissionLevels.REGULAR, verifyRequests, mongoClient));
        addCommand(new VerificationSettings(PermissionLevels.ADMINISTRATOR));
        addCommand(new RaidLock(PermissionLevels.MANAGER, securityManager));
        addCommand(new Ban(PermissionLevels.ADMINISTRATOR));
        addCommand(new SetPrefix(PermissionLevels.ADMINISTRATOR));
        addCommand(new Settings(PermissionLevels.ADMINISTRATOR));
        addCommand(new AddBannedWord(PermissionLevels.ADMINISTRATOR));
        addCommand(new Warn(PermissionLevels.MODERATOR));
        addCommand(new ClearWarn(PermissionLevels.MODERATOR));
        addCommand(new Profile(PermissionLevels.REGULAR));
        addCommand(new Hug(PermissionLevels.REGULAR));
        
        System.out.println("Added " + addedCommands + " commands.");
        
        //Start message garbage thread to remove messages set to be deleted.
        securityManager = new SecurityManager(mongoClient, servers);
        commandManager = new CommandManager(servers, api, commands);
        reactionManager = new ReactionManager(reactionListeners);
        verifyManager = new VerifyManager(verifyRequests, mongoClient);
        garbageManager = new GarbageManager(verifyRequests, mongoClient);
        consoleManager = new ConsoleCommandsManager();
        
        //Start threads
        messageGarbageThread.start();
        garbageManager.start();
        consoleManager.start();
        
        //Add listeners
        api.addMessageCreateListener(new MentionListener(servers));
        api.addListener(new ServerJoinLeaveLogger(mongoClient, servers));
        api.addMessageCreateListener(verifyManager);
        api.addListener(new MemberJoinLeave());
        api.addListener(new UserBannedListener());
        api.addMessageCreateListener(commandManager);
        api.addReactionAddListener(reactionManager);
        api.addListener(messageDeleteLogger);
        api.addListener(new RoleChangedListener());
        
        api.addMessageCreateListener(new AntiSpam(mongoClient));
        //api.addMessageCreateListener(new Kick(mongoClient, prefixes, DEFAULT_PREFIX, messagesToDelete));
        //api.addMessageCreateListener(new SetLogChannel(mongoClient, messagesToDelete, servers));
        //api.addMessageCreateListener(new Investrigate(mongoClient, prefixes, DEFAULT_PREFIX, messagesToDelete));
        
    }
    
    public static DiscordApi getAPI(){
        return api;
    }
    
    public static void addCommand(Command command) {
    	for(String prefix : command.prefix) commands.put(prefix, command);
    	addedCommands++;
    }
    
    public static void addReactionListener(Message message, ReactionListener reactionListener) {
    	reactionListeners.put(message, reactionListener);
    }
    
    public static void skipMessage(Message message, Long... timer) {
    	if(timer.length<1) {
			timer = new Long[1];
			timer[0] = 1000 * 60 * 5l;
		}
    	messageDeleteLogger.skipLogging(message);
    }
    
	public static Map<Long, Server> getServerList() {
		return servers;
	}
}