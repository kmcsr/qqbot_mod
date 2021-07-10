
package com.github.zyxgad.qqbot_mod.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.BotFactory;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.utils.BotConfiguration;

import com.github.zyxgad.qqbot_mod.QQBotMod;
import com.github.zyxgad.qqbot_mod.config.BotConfig;
import com.github.zyxgad.qqbot_mod.config.UserConfig;

public final class BotHelper extends Thread{
	private BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
	private volatile Bot robot = null;

	public BotHelper(){
		super("BotHelper");
	}

	public Bot getBot(){
		return this.robot;
	}

	public static Bot createBot(long qqID, byte[] qqPassword){
		return BotFactory.INSTANCE.newBot(qqID, qqPassword, new BotConfiguration(){{
			this.setWorkingDir(QQBotMod.INSTANCE.getDataFolder());
			this.fileBasedDeviceInfo(String.format("qqinfo_%d.json", qqID));
			this.noNetworkLog();
			this.noBotLog();
			this.setAutoReconnectOnForceOffline(true);
		}});
	}

	public void loginBotSync(){
		if(this.robot == null){
			final long qqID = BotConfig.INSTANCE.getQQID();
			final byte[] qqPassword = BotConfig.INSTANCE.getQQPassword();
			QQBotMod.LOGGER.info("QQID: " + qqID);
			QQBotMod.LOGGER.info("QQPassword: " + Util.bytesToHex(qqPassword));
			this.robot = BotHelper.createBot(qqID, qqPassword);
		}
		QQBotMod.LOGGER.info("logging QQ bot...");
		this.robot.login();
		QQBotMod.LOGGER.info("logging QQ bot SUCCESS");
		this.broadcastMessage("QQ bot is logged");
	}

	public void loginBot(){
		final long qqID = BotConfig.INSTANCE.getQQID();
		final byte[] qqPassword = BotConfig.INSTANCE.getQQPassword();
		this.robot = BotHelper.createBot(qqID, qqPassword);
		queue.add(this::loginBotSync);
	}

	public void closeBotSync(){
		if(this.robot != null){
			this.broadcastMessage("QQ bot is logging out");
			QQBotMod.LOGGER.info("logging out QQ bot...");
			Bot robot = this.robot;
			this.robot = null;
			robot.closeAndJoin(null);
			QQBotMod.LOGGER.info("logout QQ bot SUCCESS");
		}
	}

	public void closeBot(){
		if(this.robot == null){
			return;
		}
		queue.add(this::closeBotSync);
	}

	public void broadcastMessage(final String msg){
		if(this.robot == null){
			return;
		}
		BotConfig.INSTANCE.getGroupIDs().forEach((Long gid)->{
			Group group = this.robot.getGroup(gid.longValue());
			if(group != null){
				group.sendMessage(msg);
			}
		});
	}

	@Override
	public void run(){
		try{
			Runnable task;
			while(true){
				task = queue.take();
				if(task != null){
					task.run();
				}
			}
		}catch(InterruptedException e){
			//
		}
	}
}
