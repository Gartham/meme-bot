package com.gartham.memebot;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

import org.alixia.javalibrary.strings.StringTools;
import org.jetbrains.annotations.NotNull;

import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.external.JDAWebhookClient;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessage;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.Webhook;

public class Utilities {
	private static final Map<String, JDAWebhookClient> clients = new HashMap<>();

	/**
	 * Tries to provide a feasible webhook for use. This method iterates over all
	 * the webhooks it retrieves from the specified channel. If any one of them is
	 * created by the bot user (more specifically, if its owner is the bot user), it
	 * is returned. Otherwise, an attempt is made to create a new webhook. This
	 * function will throw exceptions if it does not have the appropriate
	 * permissions or if an error occurs during the retrieval or creation of a
	 * webhook.
	 * 
	 * @param channel The channel that the webhook will belong to.
	 * @return The {@link Webhook} that was found or newly created.
	 */
	public static Webhook getFeasibleWebhook(TextChannel channel) {
		for (Webhook wb : channel.retrieveWebhooks().complete())
			if (wb.getOwner() != null && wb.getOwner().getId().equals(channel.getJDA().getSelfUser().getId()))
				return wb;
		byte[] b = new byte[5];
		new Random().nextBytes(b);// 1/2^40 collision chance.
		return channel.createWebhook(StringTools.toHexString(b)).complete();
	}

	public static void queueWithFeasibleWebhook(TextChannel channel, Consumer<Webhook> consumer) {
		channel.retrieveWebhooks().queue(t -> {
			for (Webhook wb : t)
				if (wb.getOwner().getId().equals(channel.getJDA().getSelfUser().getId())) {
					consumer.accept(wb);
					return;
				}
			byte[] b = new byte[5];
			new Random().nextBytes(b);// 1/2^40 collision chance.
			channel.createWebhook(StringTools.toHexString(b)).queue(t1 -> consumer.accept(t1));
		});
	}

	public static void queueWithClient(TextChannel channel, Consumer<JDAWebhookClient> consumer) {
		if (clients.containsKey(channel.getId()))
			consumer.accept(clients.get(channel.getId()));
		else {
			queueWithFeasibleWebhook(channel, t -> {
				var cl = WebhookClientBuilder.fromJDA(t).buildJDA();
				clients.put(channel.getId(), cl);
				consumer.accept(cl);
			});
		}
	}

	public static JDAWebhookClient getClient(TextChannel channel) {
		if (clients.containsKey(channel.getId()))
			return clients.get(channel.getId());
		else {
			var cl = WebhookClientBuilder.fromJDA(getFeasibleWebhook(channel)).buildJDA();
			clients.put(channel.getId(), cl);
			return cl;
		}
	}

	public static void sendAsCreature(User creature, String message, EmbedBuilder embed, TextChannel channel) {
		if (message == null && embed == null)
			throw null;

		var wmb = new WebhookMessageBuilder();
		wmb.setAvatarUrl(creature.getEffectiveAvatarUrl());
		wmb.setUsername(creature.getName());
		if (message != null)
			wmb.setContent(message);
		if (embed != null)
			wmb.addEmbeds(WebhookEmbedBuilder.fromJDA(embed.build()).build());
		JDAWebhookClient client = getClient(channel);
		@NotNull
		WebhookMessage builtmsg = wmb.build();
		client.send(builtmsg).exceptionally(t -> {
			System.err.println(
					"An error occurred while sending a webhook message over a webhook (hook URL: " + client.getUrl()
							+ ").\nPerhaps the webhook was deleted. Retrying with a refreshed webhook client.");
			t.printStackTrace();
			clients.remove(channel.getId());
			return getClient(channel).send(builtmsg).join();
		});
	}

	public static void sendAsCreature(User creature, EmbedBuilder embed, TextChannel channel) {
		sendAsCreature(creature, null, embed, channel);
	}

	public static void sendAsCreature(User creature, String message, TextChannel channel) {
		sendAsCreature(creature, message, null, channel);
	}

}
