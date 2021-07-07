
package com.github.zyxgad.qqbot_mod.event;

import com.mojang.authlib.GameProfile;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.Listener;
import net.mamoe.mirai.event.ListeningStatus;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.QuoteReply;

import com.github.zyxgad.qqbot_mod.QQBotMod;
import com.github.zyxgad.qqbot_mod.util.Util;
import com.github.zyxgad.qqbot_mod.config.UserConfig;
import com.github.zyxgad.qqbot_mod.command.QQCommandExecuter;

public final class QQMessageListener extends SimpleListenerHost{
	public QQMessageListener(){}

	@EventHandler
	public ListeningStatus onMessage(GroupMessageEvent event){
		final MessageChain chain = event.getMessage();
		final String command = chain.contentToString();
		if(command.length() < 3 || command.charAt(0) != '!'){
			return ListeningStatus.LISTENING;
		}
		final Member sender = event.getSender();
		final long qqID = sender.getId();
		String message = command.substring(2);
		switch(command.charAt(1)){
			case '!': return ListeningStatus.LISTENING;
			case '#':{
				final GameProfile player = UserConfig.INSTANCE.getQQPlayer(qqID);
				if(player == null){
					event.getSubject().sendMessage(new MessageChainBuilder()
						.append(new QuoteReply(chain))
						.append("[McBot]")
						.append("[错误]此QQ未绑定游戏账号")
						.build());
					break;
				}
				QQBotMod.INSTANCE.sendMessage(player, "QQ_WEB", message);
				break;
			}
			case '/':{
				final GameProfile player = UserConfig.INSTANCE.getQQPlayer(qqID);
				if(player == null){
					event.getSubject().sendMessage(new MessageChainBuilder()
						.append(new QuoteReply(chain))
						.append("[McBot]")
						.append("[错误]此QQ未绑定游戏账号")
						.build());
					break;
				}
				ServerTickHandler.INSTANCE.newTickTask(()->{
					try{
						final String rmsg = new QQCommandExecuter(player, message).run();
						event.getSubject().sendMessage(new MessageChainBuilder()
							.append(new QuoteReply(chain))
							.append("[McBot]指令执行结果:\n")
							.append(rmsg)
							.build());
					}catch(Exception e){
						QQBotMod.LOGGER.error("QQBot on command '" + message + "'' error:\n", e);
					}
					return;
				});
				break;
			}
			case ':':{
				event.getSubject().sendMessage(new MessageChainBuilder()
					.append(new QuoteReply(chain))
					.append("[McBot]")
					.append("功能开发中")
					.build());
				break;
			}
		}
		return ListeningStatus.LISTENING;
	}
}
