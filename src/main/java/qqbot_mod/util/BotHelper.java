
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
	private volatile int bot_offline_tick = 0;
	private volatile int bot_offline_tick_max = 20 * 5;

	public BotHelper(){
		super("BotHelper");
	}

	public Bot getBot(){
		return this.robot;
	}

	public void loginBotSync(){
		if(this.robot != null){
			throw new RuntimeException("need close bot first");
		}
		final long qqID = BotConfig.INSTANCE.getQQID();
		final byte[] qqPassword = BotConfig.INSTANCE.getQQPassword();
		QQBotMod.LOGGER.info("QQID: " + qqID);
		QQBotMod.LOGGER.info("QQPassword: " + Util.bytesToHex(qqPassword));
		this.robot = BotFactory.INSTANCE.newBot(qqID, qqPassword, new BotConfiguration(){{
			this.setWorkingDir(QQBotMod.INSTANCE.getDataFolder());
			this.fileBasedDeviceInfo(String.format("qqinfo_%d.json", qqID));
			this.noNetworkLog();
			this.noBotLog();
		}});
		QQBotMod.LOGGER.info("logging QQ bot...");
		this.robot.login();
		this.bot_offline_tick = 0;
		QQBotMod.LOGGER.info("logging QQ bot SUCCESS");
		this.broadcastMessage("QQ bot is logged");
	}

	public void loginBot(){
		if(this.robot != null){
			throw new RuntimeException("need close bot first");
		}
		final long qqID = BotConfig.INSTANCE.getQQID();
		final byte[] qqPassword = BotConfig.INSTANCE.getQQPassword();
		QQBotMod.LOGGER.info("QQID: " + qqID);
		QQBotMod.LOGGER.info("QQPassword: " + Util.bytesToHex(qqPassword));
		this.robot = BotFactory.INSTANCE.newBot(qqID, qqPassword, new BotConfiguration(){{
			this.setWorkingDir(QQBotMod.INSTANCE.getDataFolder());
			this.fileBasedDeviceInfo(String.format("qqinfo_%d.json", qqID));
			this.noNetworkLog();
			this.noBotLog();
		}});
		queue.add(()->{
			QQBotMod.LOGGER.info("logging QQ bot...");
			this.robot.login();
			this.bot_offline_tick = 0;
			QQBotMod.LOGGER.info("logging QQ bot SUCCESS");
			this.broadcastMessage("QQ bot is logged");
		});
	}

	public void closeBot(){
		if(this.robot == null){
			return;
		}
		queue.add(()->{
			if(this.robot != null){
				this.broadcastMessage("QQ bot is logging out");
				QQBotMod.LOGGER.info("logging out QQ bot...");
				Bot robot = this.robot;
				this.robot = null;
				robot.closeAndJoin(null);
				QQBotMod.LOGGER.info("logout QQ bot SUCCESS");
			}
		});
	}

	public void checkBot(){
		if(this.robot != null && !this.robot.isOnline()){
			this.bot_offline_tick += 1;
			if(this.bot_offline_tick >= this.bot_offline_tick_max){
				queue.add(()->{
					QQBotMod.LOGGER.info("re logging QQ bot...");
					this.robot.login();
					this.bot_offline_tick = 0;
					QQBotMod.LOGGER.info("re logging QQ bot SUCCESS");
					this.broadcastMessage("QQ bot is re logged");
				});
			}
		}
	}

	public void broadcastMessage(final String msg){
		if(this.robot == null){
			return;
		}
		BotConfig.INSTANCE.getGroupIDs().forEach((Long gid)->{
			Group group = BotHelper.this.robot.getGroup(gid.longValue());
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
