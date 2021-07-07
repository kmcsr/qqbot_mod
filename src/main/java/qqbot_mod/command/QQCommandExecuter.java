
package com.github.zyxgad.qqbot_mod.command;

import java.util.UUID;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.OperatorList;
import net.minecraft.server.OperatorEntry;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.text.Text;

import com.github.zyxgad.qqbot_mod.QQBotMod;

public final class QQCommandExecuter{
	private static final String NO_OPER_ERROR_MSG = "[错误]您没有权限执行该指令";

	private final GameProfile player;
	private final String source;
	public QQCommandExecuter(GameProfile player, String source){
		this.player = player;
		this.source = source;
	}
	
	class CommandOutputImpl implements CommandOutput{
		private StringBuilder buffer = new StringBuilder();
		public CommandOutputImpl(){};
		@Override
		public boolean shouldBroadcastConsoleToOps(){ return true; }
		@Override
		public boolean shouldReceiveFeedback(){ return true; }
		@Override
		public boolean shouldTrackOutput(){ return true; }
		@Override
		public void sendSystemMessage(Text text, UUID uuid){
			this.buffer.append(text.getString()).append('\n');
		}
		public String getString(){
			return this.buffer.toString();
		}
	}

	public String run(){
		final MinecraftServer server = QQBotMod.INSTANCE.getServer();
		final OperatorEntry entry = server.getPlayerManager().getOpList().get(this.player);
		final CommandOutputImpl out = new CommandOutputImpl();
		final ServerCommandSource cmdsrc = new ServerCommandSource(
			out, // command output
			null, // Vec3d(null)
			null, // Vec2f(null)
			null, // world(null)
			entry == null ?0 :entry.getPermissionLevel(), // op level
			this.player.getName(), // name
			QQBotMod.createText(this.player.getName()), // name
			server, //server
			null  // entity(null)
		);
		server.getCommandManager().execute(cmdsrc, this.source);
		return out.getString();
	}
}
