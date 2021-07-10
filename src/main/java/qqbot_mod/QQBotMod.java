
package com.github.zyxgad.qqbot_mod;

import java.io.File;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.event.EventChannel;
import net.mamoe.mirai.event.events.GroupMessageEvent;

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
import com.github.zyxgad.qqbot_mod.command.QQBotCommand;
import com.github.zyxgad.qqbot_mod.event.ServerTickHandler;
import com.github.zyxgad.qqbot_mod.event.QQMessageListener;
import com.github.zyxgad.qqbot_mod.util.Util;
import com.github.zyxgad.qqbot_mod.util.BotHelper;

public final class QQBotMod implements ModInitializer{
	public static final UUID SERVER_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
	public static QQBotMod INSTANCE = null;
	public static final Logger LOGGER = LogManager.getLogger("QQBotMod");

	private MinecraftServer server = null;

	private File folder;
	private BotHelper helper;
	private EventChannel eventChannel = null;

	public QQBotMod(){
		this.folder = new File("qqbot_mod");
		if(!this.folder.exists()){
			this.folder.mkdirs();
		}
		this.helper = new BotHelper();
		this.helper.start();
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
		ServerLifecycleEvents.START_DATA_PACK_RELOAD.register(this::onReloadCall);
		ServerLifecycleEvents.SERVER_STOPPING.register(this::onStopping);
		ServerTickEvents.START_SERVER_TICK.register(ServerTickHandler.INSTANCE::onTick);
	}

	public void onStarting(MinecraftServer server){
		this.server = server;
		this.onReload();
		this.loginBotSync();
	}

	public void onStarted(MinecraftServer server){
		this.broadcastMessage("Minecraft server started");
	}

	public void onRegisterCommands(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated){
		BindQQCommand.register(dispatcher);
		QQBotCommand.register(dispatcher);
	}

	public void onReloadCall(MinecraftServer server, ServerResourceManager serverResourceManager){
		LOGGER.info("QQBotMod is reloading...");
		this.closeBot();
		this.onReload();
		this.loginBot();
	}

	public void onReload(){
		BotConfig.INSTANCE.reload();
		UserConfig.INSTANCE.reload();
	}

	public void onSave(){
		BotConfig.INSTANCE.save();
		UserConfig.INSTANCE.save();
	}

	public void onStopping(MinecraftServer server){
		this.broadcastMessage("Minecraft server is stopping");
		this.closeBotSync();
		this.onSave();
		this.server = null;
		this.helper.interrupt();
	}

	public Bot getBot(){
		return this.helper.getBot();
	}

	public void loginBotSync(){
		this.helper.loginBotSync();
		this.eventChannel = this.helper.getBot().getEventChannel()
				.filterIsInstance(GroupMessageEvent.class)
				.filter((GroupMessageEvent event)->{ return BotConfig.INSTANCE.hasGroupID(event.getSubject().getId()); });
			this.eventChannel.registerListenerHost(new QQMessageListener());
	}

	public void loginBot(){
		this.helper.loginBot();
		this.eventChannel = this.helper.getBot().getEventChannel()
				.filterIsInstance(GroupMessageEvent.class)
				.filter((GroupMessageEvent event)->{ return BotConfig.INSTANCE.hasGroupID(event.getSubject().getId()); });
			this.eventChannel.registerListenerHost(new QQMessageListener());
	}

	public void closeBotSync(){
		this.eventChannel = null;
		this.helper.closeBotSync();
	}

	public void closeBot(){
		this.eventChannel = null;
		this.helper.closeBot();
	}

	public void broadcastMessage(final String msg){
		this.helper.broadcastMessage(msg);
	}

	public void sendMessage(final GameProfile player, final String world, final String msg){
		final String message = new StringBuilder()
			.append('[').append(world).append('/').append(player.getName()).append("]: ")
			.append(' ').append(msg)
			.toString();
		this.broadcastMessage(message);
		QQBotMod.LOGGER.info(message);
		for(ServerPlayerEntity p: this.server.getPlayerManager().getPlayerList()){
			QQBotMod.tellPlayer(p, message, player.getId());
		}
	}

	public static void tellPlayer(final ServerPlayerEntity player, final String message){
		tellPlayer(player, message, SERVER_UUID);
	}

	public static void tellPlayer(final ServerPlayerEntity player, final String message, final UUID uuid){
		player.sendMessage(createText(message), MessageType.byId((byte)(0)), uuid);
	}

	public static Text createText(final String text){
		return new LiteralText(text);
	}
}
