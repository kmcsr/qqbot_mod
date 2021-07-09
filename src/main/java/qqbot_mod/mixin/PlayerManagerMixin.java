package com.github.zyxgad.qqbot_mod.mixin;

import java.util.UUID;

import net.minecraft.server.PlayerManager;
import net.minecraft.text.Text;
import net.minecraft.network.MessageType;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.zyxgad.qqbot_mod.QQBotMod;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
	@Inject(at=@At("HEAD"),
		method="broadcastChatMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/MessageType;Ljava/util/UUID;)V",
		cancellable=false)
	private void broadcastChatMessage(Text text, MessageType mtype, UUID uuid, CallbackInfo info){
		String msg = text.getString();
		QQBotMod.INSTANCE.broadcastMessage(msg);
	}
}
