package com.boot.jx.chat;

import java.util.HashMap;
import java.util.Map;

public class ChatCommands {

	public static Map<String, String> strMapping = new HashMap<String, String>();

	public static void registerCommand(String command) {
		strMapping.put(command, command);
	}

	public static void init() {
		registerCommand("/exit_chat");
	}

	public static boolean isCommand(String msg) {
		return strMapping.containsKey(msg);
	}

	static {
		init();
	}
}
