package com.gartham.memebot;

import org.alixia.javalibrary.parsers.cli.CLIParams;

public class BotConfiguration {

	public BotConfiguration(CLIParams cmdLineArguments) {
		token = cmdLineArguments.readString("", "--token");
		selectedMemesChannel = cmdLineArguments.readString("682566378514939927", "--selected-memes-channel", "-smc");
		memesChannel = cmdLineArguments.readString("682621944943738900", "--normal-channel", "--memes-channel", "-nc",
				"-mc");
		picker = cmdLineArguments.readString("682427503272525862", "--picker", "-p");
	}

	private final String token, selectedMemesChannel, memesChannel, picker;

	public String getToken() {
		return token;
	}

	public String getSelectedMemesChannel() {
		return selectedMemesChannel;
	}

	public String getMemesChannel() {
		return memesChannel;
	}

	public String getPicker() {
		return picker;
	}

}
