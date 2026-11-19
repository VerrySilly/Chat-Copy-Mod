package com.vanillah.client.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.ChatFormatting;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ChatComponent.class)
public class ChatComponentMixin {

	@WrapMethod(
		method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V"
	)
	private void vanillah$addCopyButton(Component message, @Nullable MessageSignature signature, @Nullable GuiMessageTag tag, Operation<Void> original) {
		if (signature == null) {
			original.call(message, signature, tag);
			return;
		}

		String text = vanillah$extractContent(message);
		if (text.isBlank()) {
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

	private static String vanillah$extractContent(Component message) {
		if (message.getContents() instanceof TranslatableContents translatable) {
			Object[] args = translatable.getArgs();
			if (args.length >= 2) {
				Object last = args[args.length - 1];
				return last instanceof Component c ? c.getString() : String.valueOf(last);
			}
		}
		return message.getString();
	}
}