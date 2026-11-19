package com.vanillah.client.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.ChatFormatting;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(ChatComponent.class)
public class ChatComponentMixin {
	private static final boolean LOOSE_DETECTION = false;
	private static final Set<String> PLAYER_KEYS = Set.of(
		"chat.type.text",
		"chat.type.team.text",
		"chat.type.team.sent",
		"chat.type.emote"
	);

	private static final Pattern LOOSE_BRACKET = Pattern.compile("^[^<>]{0,40}?<([A-Za-z0-9_]{3,16})>\\s*(.+)$");
	private static final Pattern LOOSE_COLON = Pattern.compile("^(?:\\[[^\\]]{1,24}\\]\\s*)*([A-Za-z0-9_]{3,16})[^:>»›\\n]{0,25}?\\s*[:»›>]+\\s+(.+)$");

	@WrapMethod(
		method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V"
	)
	private void vanillah$addCopyButton(Component message, @Nullable MessageSignature signature, @Nullable GuiMessageTag tag, Operation<Void> original) {
		String text = vanillah$findPlayerMessage(message, signature);
		if (text == null || text.isBlank()) {
			original.call(message, signature, tag);
			return;
		}

		Style style = Style.EMPTY
			.withColor(ChatFormatting.BLUE)
			.withClickEvent(new ClickEvent.CopyToClipboard(text))
			.withHoverEvent(new HoverEvent.ShowText(Component.literal("Copy Text")));

		Component withButton = Component.empty()
			.append(message)
			.append(Component.literal(" "))
			.append(Component.literal("(copy)").setStyle(style));

		original.call(withButton, signature, tag);
	}

	@Nullable
	private static String vanillah$findPlayerMessage(Component message, @Nullable MessageSignature signature) {
		if (message.getContents() instanceof TranslatableContents translatable
			&& PLAYER_KEYS.contains(translatable.getKey())) {
			Object[] args = translatable.getArgs();
			if (args.length >= 2) {
				Object last = args[args.length - 1];
				return vanillah$clean(last instanceof Component c ? c.getString() : String.valueOf(last));
			}
		}

		String plain = message.getString();

		if (signature != null) {
			return vanillah$clean(plain);
		}

		return vanillah$detectFromFormat(plain);
	}

	@Nullable
	private static String vanillah$detectFromFormat(String plain) {
		ClientPacketListener connection = Minecraft.getInstance().getConnection();
		if (connection != null) {
			for (PlayerInfo info : connection.getOnlinePlayers()) {
				String name = info.getProfile().name();
				if (name == null || name.isEmpty() || !plain.contains(name)) {
					continue;
				}
				Pattern pattern = Pattern.compile(
					"^[^>:»›]{0,40}?(?<![A-Za-z0-9_])" + Pattern.quote(name)
						+ "(?![A-Za-z0-9_])[^:>»›\\n]{0,25}?\\s*[:>»›]+\\s*(.+)$"
				);
				Matcher matcher = pattern.matcher(plain);
				if (matcher.find()) {
					return vanillah$clean(matcher.group(1));
				}
			}
		}

		if (LOOSE_DETECTION) {
			Matcher bracket = LOOSE_BRACKET.matcher(plain);
			if (bracket.find()) {
				return vanillah$clean(bracket.group(2));
			}
			Matcher colon = LOOSE_COLON.matcher(plain);
			if (colon.find()) {
				return vanillah$clean(colon.group(2));
			}
		}
		return null;
	}

	private static String vanillah$clean(String text) {
		String stripped = ChatFormatting.stripFormatting(text);
		return (stripped == null ? text : stripped).strip();
	}
}