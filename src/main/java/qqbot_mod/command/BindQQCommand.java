
package com.github.zyxgad.qqbot_mod.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import static net.minecraft.server.command.CommandManager.literal;

import com.github.zyxgad.qqbot_mod.QQBotMod;
import com.github.zyxgad.qqbot_mod.config.UserConfig;

public final class BindQQCommand{
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher){
		dispatcher.register(literal("bindqq")
			.then(
				RequiredArgumentBuilder.<ServerCommandSource, Long>argument("qqid", LongArgumentType.longArg(0))
				.executes((CommandContext<ServerCommandSource> context)->{
					final ServerCommandSource source = context.getSource();
					final ServerPlayerEntity player = source.getPlayer();
					final GameProfile profile = player.getGameProfile();
					final long qqid = context.getArgument("qqid", Long.class).longValue();
					UserConfig.INSTANCE.setPlayerQQ(profile, qqid);
					source.sendFeedback(QQBotMod.createText("Bind qq(" + qqid + ") SUCCESS"), true);
					return Command.SINGLE_SUCCESS;
				})
			)
		);
	}
}
