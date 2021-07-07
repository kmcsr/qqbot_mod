
package com.github.zyxgad.qqbot_mod.config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import com.github.zyxgad.qqbot_mod.QQBotMod;
import com.github.zyxgad.qqbot_mod.util.Util;
import com.github.zyxgad.qqbot_mod.util.JsonUtil;

public final class BotConfig{
	public static final BotConfig INSTANCE = new BotConfig();

	private File file;
	private JsonObject data;
	private BotConfig(){
		this.file = new File(QQBotMod.INSTANCE.getDataFolder(), "botconfig.json");
		this.data = new JsonObject();
		this.data.addProperty("qqid", Long.valueOf(0));
		this.data.add("groupid", new JsonArray());
	}

	public long getQQID(){
		return this.data.getAsJsonPrimitive("qqid").getAsNumber().longValue();
	}

	public void setQQID(long qqid){
		this.data.addProperty("qqid", Long.valueOf(qqid));
	}

	public byte[] getQQPassword(){
		return Util.hexToBytes(this.data.getAsJsonPrimitive("qqpassword").getAsString());
	}

	public void setQQPassword(String password){
		this.data.addProperty("qqpassword", Util.bytesToHex(Util.md5(Util.stringToBytes(password))));
	}

	public void addGroupID(long gid){
		final JsonPrimitive ogid = new JsonPrimitive(Long.valueOf(gid));
		final JsonArray gidlist = this.data.getAsJsonArray("groupid");
		if(!gidlist.contains(ogid)){
			gidlist.add(ogid);
		}
	}

	public boolean hasGroupID(long gid){
		return this.data.getAsJsonArray("groupid").contains(new JsonPrimitive(gid));
	}

	public boolean removeGroupID(long gid){
		final JsonPrimitive ogid = new JsonPrimitive(Long.valueOf(gid));
		final JsonArray gidlist = this.data.getAsJsonArray("groupid");
		if(gidlist.contains(ogid)){
			gidlist.remove(ogid);
			return true;
		}
		return false;
	}

	public List<Long> getGroupIDs(){
		final JsonArray jarr = this.data.getAsJsonArray("groupid");
		final List<Long> gids = new ArrayList<Long>(jarr.size());
		Iterator<JsonElement> iter = jarr.iterator();
		while(iter.hasNext()){
			gids.add(Long.valueOf(iter.next().getAsNumber().longValue()));
		}
		return gids;
	}

	public void reload(){
		if(!this.file.exists()){
			return;
		}
		try(
			FileReader filer = new FileReader(file);
			JsonReader jreader = new JsonReader(filer)
		){
			this.data = JsonUtil.fromJson(jreader);
		}catch(IOException e){
			QQBotMod.LOGGER.error("load bot config file error:\n", e);
		}
	}

	public void save(){
		if(!this.file.exists()){
			try{
				File dir = this.file.getParentFile();
				if(!dir.exists()){
					dir.mkdirs();
				}
				this.file.createNewFile();
			}catch(IOException e){
				QQBotMod.LOGGER.error("Create bot config file error:\n", e);
			}
		}
		try(
			FileWriter filew = new FileWriter(file);
			JsonWriter jwriter = new JsonWriter(filew)
		){
			JsonUtil.GSON.toJson(this.data, jwriter);
		}catch(IOException e){
			QQBotMod.LOGGER.error("save bot config file error:\n", e);
		}
	}
}