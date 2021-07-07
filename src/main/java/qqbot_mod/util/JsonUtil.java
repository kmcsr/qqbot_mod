
package com.github.zyxgad.qqbot_mod.util;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

public final class JsonUtil{
	public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

	public static JsonObject fromJson(JsonReader reader) throws IOException{
		return (JsonObject)(GSON.fromJson(reader, JsonObject.class));
	}
}
