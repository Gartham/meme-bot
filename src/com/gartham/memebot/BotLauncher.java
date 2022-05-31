package com.gartham.memebot;

import java.util.HashMap;
import java.util.Map;

import javax.security.auth.login.LoginException;

import org.alixia.javalibrary.parsers.cli.CLIParams;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class BotLauncher {

	public static void main(String[] args) throws LoginException {
		BotConfiguration config = new BotConfiguration(new CLIParams(args));

		JDA jda = JDABuilder.createDefault(config.getToken(), GatewayIntent.getIntents(GatewayIntent.ALL_INTENTS))
				.build();

		Map<String, Integer> score = new HashMap<>();

		jda.addEventListener((EventListener) event -> {
			if (event instanceof MessageReactionAddEvent) {
				var mre = (MessageReactionAddEvent) event;
				if (mre.getChannel().getId().equals(config.getMemesChannel())
						&& mre.getUserId().equals(config.getPicker()) && mre.getReactionEmote().isEmoji()
						&& mre.getReactionEmote().getEmoji().equals("\u2B50")) {
					Message msg = mre.retrieveMessage().complete();
					if (msg.getAttachments().size() == 1) {
						Utilities.sendAsCreature(msg.getMember(),
								new EmbedBuilder().setDescription("Meme by " + msg.getAuthor().getAsMention() + '.')
										.setImage(msg.getAttachments().get(0).getUrl()),
								jda.getTextChannelById(config.getSelectedMemesChannel()));
						inc(score, msg.getAuthor().getId());
					}
				}
			}
		});

		// TODO Write out whatever code you need your bot to do after it's logged in.
	}

	private static <T> void inc(Map<? super T, Integer> map, T val) {
		map.put(val, map.containsKey(val) ? map.get(val) + 1 : 1);
	}
}
