
package com.github.zyxgad.qqbot_mod;

import java.io.File;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.BotFactory;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.EventChannel;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.utils.BotConfiguration;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.network.MessageType;
import net.minecraft.resource.ServerResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import com.github.zyxgad.qqbot_mod.config.BotConfig;
import com.github.zyxgad.qqbot_mod.config.UserConfig;
import com.github.zyxgad.qqbot_mod.command.BindQQCommand;
import com.github.zyxgad.qqbot_mod.event.ServerTickHandler;
import com.github.zyxgad.qqbot_mod.event.QQMessageListener;


public final class QQBotMod implements ModInitializer{
	public static final UUID SERVER_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
	public static QQBotMod INSTANCE = null;
	public static final Logger LOGGER = LogManager.getLogger("QQBotMod");

	private MinecraftServer server = null;

	private File folder;
	private volatile Bot robot = null;
	private volatile EventChannel eventChannel = null;
	private int bot_offline_tick = 0;
	private int bot_offline_tick_max = 20 * 5;

	public QQBotMod(){
		this.folder = new File("qqbot_mod");
		if(!this.folder.exists()){
			this.folder.mkdirs();
		}
		INSTANCE = this;
	}

	public MinecraftServer getServer(){
		return this.server;
	}

	public File getDataFolder(){
		return this.folder;
	}

	@Override
	public void onInitialize(){
		LOGGER.info("QQBotMod is onInitialize");
		ServerLifecycleEvents.SERVER_STARTING.register(this::onStarting);
		ServerLifecycleEvents.SERVER_STARTED.register(this::onStarted);
		CommandRegistrationCallback.EVENT.register(this::onRegisterCommands);
		ServerLifecycleEvents.START_DATA_PACK_RELOAD.register(this::onReload);
		ServerLifecycleEvents.SERVER_STOPPING.register(this::onStopping);
		ServerTickEvents.START_SERVER_TICK.register(ServerTickHandler.INSTANCE::onTick);

		ServerTickHandler.INSTANCE.addTickHandler(this::checkBot);
	}

	public void onStarting(MinecraftServer server){
		this.server = server;
		BotConfig.INSTANCE.reload();
		UserConfig.INSTANCE.reload();
		this.loginBot();
	}

	public void onStarted(MinecraftServer server){
		this.broadcastMessage("Minecraft server started");
	}

	public void onRegisterCommands(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated){
		BindQQCommand.register(dispatcher);
	}

	public void onReload(MinecraftServer server, ServerResourceManager serverResourceManager){
		LOGGER.info("QQBotMod is reloading...");
		this.closeBot();
		this.loginBot();
	}

	public void onStopping(MinecraftServer server){
		this.broadcastMessage("Minecraft server is stopping");
		this.closeBot();
		UserConfig.INSTANCE.save();
		BotConfig.INSTANCE.save();
		this.server = null;
	}

	public Bot getBot(){
		return this.robot;
	}

	public void loginBot(){
		LOGGER.info("logging QQ bot...");
		final long qqID = BotConfig.INSTANCE.getQQID();
		final byte[] qqPassword = BotConfig.INSTANCE.getQQPassword();
		this.robot = BotFactory.INSTANCE.newBot(qqID, qqPassword, new BotConfiguration(){{
			this.fileBasedDeviceInfo(new File(QQBotMod.this.getDataFolder(), String.format("qqinfo_%d.json", qqID)).getAbsolutePath());
			this.noNetworkLog();
			this.noBotLog();
		}});
		this.robot.login();
		this.bot_offline_tick = 0;
		this.eventChannel = this.robot.getEventChannel()
			.filterIsInstance(GroupMessageEvent.class)
			.filter((GroupMessageEvent event)->{ return BotConfig.INSTANCE.hasGroupID(event.getSubject().getId()); });
		this.eventChannel.registerListenerHost(new QQMessageListener());
		LOGGER.info("logging QQ bot SUCCESS");
		this.broadcastMessage("QQ bot is logged");
	}

	public void closeBot(){
		if(this.robot != null){
			this.broadcastMessage("QQ bot is logging out");
			LOGGER.info("logging out QQ bot...");
			Bot robot = this.robot;
			this.robot = null;
			this.eventChannel = null;
			robot.closeAndJoin(null);
			LOGGER.info("logout QQ bot SUCCESS");
		}
	}

	public void checkBot(){
		if(this.robot == null){
			return;
		}
		if(!this.robot.isOnline()){
			this.bot_offline_tick += 1;
			if(this.bot_offline_tick >= this.bot_offline_tick_max){
				LOGGER.info("re logging QQ bot...");
				this.robot.login();
				this.bot_offline_tick = 0;
				LOGGER.info("re logging QQ bot SUCCESS");
				this.broadcastMessage("QQ bot is re logged");
			}
		}
	}

	public void broadcastMessage(final String msg){
		if(this.robot == null){
			return;
		}
		BotConfig.INSTANCE.getGroupIDs().forEach((Long gid)->{
			Group group = this.robot.getGroup(gid.longValue());
			if(group == null){
				return;
			}
			group.sendMessage(msg);
		});
	}

	public void sendMessage(final GameProfile player, final String world, final String msg){
		final String message = new StringBuilder()
			.append('[').append(world).append('/').append(player.getName()).append("]: ")
			.append(' ').append(msg)
			.toString();
		QQBotMod.LOGGER.info(message);
		for(ServerPlayerEntity p: this.server.getPlayerManager().getPlayerList()){
			QQBotMod.tellPlayer(p, message);
		}
	}

	public static void tellPlayer(final ServerPlayerEntity player, final String message){
		player.sendMessage(createText(message), MessageType.byId((byte)(0)), SERVER_UUID);
	}

	public static Text createText(final String text){
		return new LiteralText(text);
	}
}
