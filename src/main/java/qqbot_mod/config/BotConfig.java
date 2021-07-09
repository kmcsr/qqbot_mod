
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

import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import com.github.zyxgad.qqbot_mod.QQBotMod;
import com.github.zyxgad.qqbot_mod.util.Util;
import com.github.zyxgad.qqbot_mod.util.JsonUtil;

public final class BotConfig{
	public static final BotConfig INSTANCE = new BotConfig();

	static class DataType{
		@SerializedName("qqid")
		private long qqid = 0;
		@SerializedName("qqpassword")
		private String qqpassword = "";
		@SerializedName("groupid")
		private List<Long> groupid = new ArrayList<>();

		public DataType(){}

		public long getQQID(){
			return this.qqid;
		}
		public void setQQID(long qqid){
			this.qqid = qqid;
		}
		public String getQQPassword(){
			return this.qqpassword;
		}
		public void setQQPassword(String qqpwd){
			this.qqpassword = qqpwd;
		}
		public List<Long> getGroupIDs(){
			return this.groupid;
		}
	}

	private File file;
	private DataType data;
	private BotConfig(){
		this.file = new File(QQBotMod.INSTANCE.getDataFolder(), "botconfig.json");
		this.data = new DataType();
	}

	public long getQQID(){
		return this.data.getQQID();
	}

	public void setQQID(long qqid){
		this.data.setQQID(qqid);
	}

	public byte[] getQQPassword(){
		return Util.hexToBytes(this.data.getQQPassword());
	}

	public void setQQPassword(String password){
		this.data.setQQPassword(Util.bytesToHex(Util.md5(Util.stringToBytes(password))));
	}

	public boolean addGroupID(long gid){
		final Long ogid = Long.valueOf(gid);
		final List<Long> gidlist = this.data.getGroupIDs();
		if(!gidlist.contains(ogid)){
			gidlist.add(ogid);
			return true;
		}
		return false;
	}

	public boolean hasGroupID(long gid){
		return this.data.getGroupIDs().contains(Long.valueOf(gid));
	}

	public boolean removeGroupID(long gid){
		final Long ogid = Long.valueOf(gid);
		final List<Long> gidlist = this.data.getGroupIDs();
		if(gidlist.contains(ogid)){
			gidlist.remove(ogid);
			return true;
		}
		return false;
	}

	public List<Long> getGroupIDs(){
		return this.data.getGroupIDs();
	}

	public void reload(){
		if(!this.file.exists()){
			return;
		}
		try(
			FileReader filer = new FileReader(file);
			JsonReader jreader = new JsonReader(filer)
		){
			this.data = JsonUtil.GSON.fromJson(jreader, DataType.class);
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
			JsonUtil.GSON.toJson(this.data, DataType.class, jwriter);
		}catch(IOException e){
			QQBotMod.LOGGER.error("save bot config file error:\n", e);
		}
	}
}