
package com.github.zyxgad.qqbot_mod.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import static net.minecraft.server.command.CommandManager.literal;

import com.github.zyxgad.qqbot_mod.QQBotMod;
import com.github.zyxgad.qqbot_mod.config.BotConfig;

public final class QQBotCommand{
	private static final String FINISH_RELOAD_MSG = "reload QQBot config SUCCESS";
	private static final String FINISH_SAVE_MSG   = "save QQBot config SUCCESS";

	private static LiteralCommandNode<ServerCommandSource> makeGroupCmd(){
		return literal("group")
			.then(literal("list").executes((context)->{
				StringBuilder builder = new StringBuilder();
				builder.append("QQ GROUPS:").append('\n');
				BotConfig.INSTANCE.getGroupIDs().forEach((Long ogid)->{
					builder.append("- ").append(ogid.toString()).append('\n');
				});
				context.getSource().sendFeedback(QQBotMod.createText(builder.toString()), true);
				return Command.SINGLE_SUCCESS;
			}))
			.then(literal("add").then(
				RequiredArgumentBuilder.<ServerCommandSource, Long>argument("groupid", LongArgumentType.longArg(0)).executes((context)->{
					final long groupid = context.getArgument("groupid", Long.class).longValue();
					if(!BotConfig.INSTANCE.addGroupID(groupid)){
						context.getSource().sendFeedback(QQBotMod.createText("group " + groupid + " is already exists"), true);
						return Command.SINGLE_SUCCESS;
					}
					context.getSource().sendFeedback(QQBotMod.createText("append " + groupid + " SUCCESS"), true);
					return Command.SINGLE_SUCCESS;
				})
			))
			.then(literal("remove").then(
				RequiredArgumentBuilder.<ServerCommandSource, Long>argument("groupid", LongArgumentType.longArg(0)).executes((context)->{
					final long groupid = context.getArgument("groupid", Long.class).longValue();
					if(!BotConfig.INSTANCE.removeGroupID(groupid)){
						context.getSource().sendFeedback(QQBotMod.createText("group " + groupid + " is not exists"), true);
						return Command.SINGLE_SUCCESS;
					}
					context.getSource().sendFeedback(QQBotMod.createText("remove " + groupid + " SUCCESS"), true);
					return Command.SINGLE_SUCCESS;
				})
			))
			.then(literal("query").then(
				RequiredArgumentBuilder.<ServerCommandSource, Long>argument("groupid", LongArgumentType.longArg(0)).executes((context)->{
					final long groupid = context.getArgument("groupid", Long.class).longValue();
					context.getSource().sendFeedback(QQBotMod.createText(new StringBuilder()
						.append("group ").append(groupid).append(' ')
						.append((BotConfig.INSTANCE.hasGroupID(groupid)) ?"is" :"isn't")
						.append(" exists")
						.toString()), true);
					return Command.SINGLE_SUCCESS;
				})
			))
			.build();
	}

	public static LiteralCommandNode<ServerCommandSource> makeBotCmd(){
		return literal("bot")
			.then(literal("login").executes((context)->{
				if(QQBotMod.INSTANCE.getBot() != null){
					context.getSource().sendFeedback(QQBotMod.createText("QQ bot is logged"), true);
					return Command.SINGLE_SUCCESS;
				}
				QQBotMod.INSTANCE.loginBot();
				context.getSource().sendFeedback(QQBotMod.createText("login QQ bot"), true);
				return Command.SINGLE_SUCCESS;
			}))
			.then(literal("logout").executes((context)->{
				if(QQBotMod.INSTANCE.getBot() == null){
					context.getSource().sendFeedback(QQBotMod.createText("QQ bot isn't logged"), true);
					return Command.SINGLE_SUCCESS;
				}
				QQBotMod.INSTANCE.closeBot();
				context.getSource().sendFeedback(QQBotMod.createText("logout QQ bot"), true);
				return Command.SINGLE_SUCCESS;
			}))
			.then(literal("query").executes((context)->{
				context.getSource().sendFeedback(QQBotMod.createText(
					"QQ bot " + ((QQBotMod.INSTANCE.getBot() == null) ?"isn't" :"is") + " login"), true);
				return Command.SINGLE_SUCCESS;
			}))
			.build();
	}

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher){
		dispatcher.register(literal("qqbot")
			.requires((ServerCommandSource source)->{
				return source.hasPermissionLevel(4);
			})
			.then(literal("reload")
				.executes((context)->{
					context.getSource().sendFeedback(QQBotMod.createText("reloading QQBot config..."), true);
					QQBotMod.INSTANCE.onReload();
					context.getSource().sendFeedback(QQBotMod.createText(FINISH_RELOAD_MSG), true);
					return Command.SINGLE_SUCCESS;
				})
			)
			.then(literal("save")
				.executes((context)->{
					context.getSource().sendFeedback(QQBotMod.createText("saving QQBot config..."), true);
					QQBotMod.INSTANCE.onReload();
					context.getSource().sendFeedback(QQBotMod.createText(FINISH_SAVE_MSG), true);
					return Command.SINGLE_SUCCESS;
				})
			)
			.then(makeGroupCmd())
			.then(makeBotCmd())
		);
	}
}
