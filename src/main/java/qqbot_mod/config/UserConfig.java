
package com.github.zyxgad.qqbot_mod.config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

import com.mojang.authlib.GameProfile;
import net.minecraft.util.UserCache;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import com.github.zyxgad.qqbot_mod.QQBotMod;

public final class UserConfig{
	public static final UserConfig INSTANCE = new UserConfig();

	static final class Item{
		GameProfile player;
		long qqid;

		Item(GameProfile player, long qqid){
			this.player = player;
			this.qqid = qqid;
		}
		Item(JsonReader jreader) throws IOException{
			this.fromJson(jreader);
		}

		public void fromJson(JsonReader jreader) throws IOException{
			jreader.nextName();
			jreader.beginObject();
			while(jreader.hasNext()){
				final String name = jreader.nextName();
				if(name.equals("qq")){
					this.qqid = jreader.nextLong();
				}else if(name.equals("uuid")){
					this.player = QQBotMod.INSTANCE.getServer().getUserCache().getByUuid(UUID.fromString(jreader.nextString()));
				}else{
					jreader.skipValue();
				}
			}
			jreader.endObject();
		}

		public void toJson(JsonWriter jwriter) throws IOException{
			jwriter.name(player.getId().toString());
			jwriter.beginObject();
			jwriter.name("qq");
			jwriter.value(qqid);
			jwriter.name("uuid");
			jwriter.value(player.getId().toString());
			jwriter.endObject();
		}
	}

	private Map<UUID, Item> storage;

	private UserConfig(){
		this.storage = new HashMap<>();
	}

	public void setPlayerQQ(GameProfile player, long qqid){
		this.storage.put(player.getId(), new Item(player, qqid));
	}

	public GameProfile getQQPlayer(long qqid){
		Item item = null;
		for(Item it: this.storage.values()){
			if(it.qqid == qqid){
				item = it;
				break;
			}
		}
		if(item == null){
			return null;
		}
		return item.player;
	}

	public void reload(){
		final File file = new File(QQBotMod.INSTANCE.getDataFolder(), "userconfig.json");
		this.storage.clear();
		if(!file.exists()){
			return;
		}
		try(
			FileReader filer = new FileReader(file);
			JsonReader jreader = new JsonReader(filer)
		){
			Item item;
			jreader.beginObject();
			while(jreader.hasNext()){
				item = new Item(jreader);
				this.storage.put(item.player.getId(), item);
			}
			jreader.endObject();
		}catch(IOException e){
			QQBotMod.LOGGER.error("Read userconfig json error:\n", e);
		}
	}

	public void save(){
		final File file = new File(QQBotMod.INSTANCE.getDataFolder(), "userconfig.json");
		if(!file.exists()){
			try{
				file.createNewFile();
			}catch(IOException e){
				QQBotMod.LOGGER.error("Create userconfig.json error:\n", e);
				return;
			}
		}

		try(
			FileWriter filew = new FileWriter(file);
			JsonWriter jwriter = new JsonWriter(filew)
		){
			jwriter.beginObject();
			for(Item item: this.storage.values()){
				item.toJson(jwriter);
			}
			jwriter.endObject();
		}catch(IOException e){
			QQBotMod.LOGGER.error("Write userconfig json error:\n", e);
		}
	}

}