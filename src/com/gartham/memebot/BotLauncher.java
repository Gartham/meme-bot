package com.gartham.memebot;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.alixia.javalibrary.json.JSONNumber;
import org.alixia.javalibrary.json.JSONObject;
import org.alixia.javalibrary.json.JSONParser;
import org.alixia.javalibrary.parsers.cli.CLIParams;
import org.alixia.javalibrary.streams.CharacterStream;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class BotLauncher {

	private static final File SCORE_FILE = new File("user-scores.log");

	public static void main(String[] args) throws Throwable {
		Map<String, Integer> score;
		if (!SCORE_FILE.exists()) {
			if (SCORE_FILE.getParentFile() != null)
				SCORE_FILE.getParentFile().mkdirs();
			score = new HashMap<>();
		} else
			try (InputStreamReader reader = new InputStreamReader(new FileInputStream(SCORE_FILE),
					StandardCharsets.UTF_8)) {
				JSONObject o = (JSONObject) new JSONParser().parse(CharacterStream.from(reader));
				score = new HashMap<>();
				for (var v : o.entrySet())
					score.put(v.getKey(), ((JSONNumber) v.getValue()).intValue());
			}

		BotConfiguration config = new BotConfiguration(new CLIParams(args));

		JDA jda = JDABuilder.createDefault(config.getToken(), GatewayIntent.getIntents(GatewayIntent.ALL_INTENTS))
				.build();

		jda.addEventListener((EventListener) event -> {
			if (event instanceof MessageReactionAddEvent) {
				var mre = (MessageReactionAddEvent) event;
				if (mre.getChannel().getId().equals(config.getMemesChannel())
						&& mre.getUserId().equals(config.getPicker()) && mre.getReactionEmote().isEmoji()
						&& mre.getReactionEmote().getEmoji().equals("\u2B50")) {
					Message msg = mre.retrieveMessage().complete();
					if (msg.getAttachments().size() == 1) {
						Utilities.sendAsCreature(msg.getAuthor(),
								new EmbedBuilder().setDescription("Meme by " + msg.getAuthor().getAsMention() + '.')
										.setColor(new Color(0)).setTitle("[Link]", msg.getJumpUrl())
										.setImage(msg.getAttachments().get(0).getUrl()),
								jda.getTextChannelById(config.getSelectedMemesChannel()));
						jda.getTextChannelById(config.getLogChannel()).sendMessage("meme-bot.v1: "
								+ msg.getAuthor().getAsMention() + " - " + msg.getJumpUrl() + " - " + msg.getId())
								.queue();
						mre.getReaction().removeReaction(mre.getUser()).complete();
						inc(score, msg.getAuthor().getId());
						write(score);
					}
				}
			}
		});

	}

	private static <T> void inc(Map<? super T, Integer> map, T val) {
		map.put(val, map.containsKey(val) ? map.get(val) + 1 : 1);
	}

	private static void write(Map<String, Integer> v) {
		JSONObject o = new JSONObject();
		for (var e : v.entrySet())
			o.put(e.getKey(), e.getValue());
		try (PrintWriter pw = new PrintWriter(SCORE_FILE)) {
			pw.println(o.toString());
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
	}
}
