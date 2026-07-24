// Whole screen is DrawContext-based (Minecraft 1.20+). Excluded on older versions.
//? if >=1.20 {
package online.slavok.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class FriendlyPhantomsScreen extends Screen {
	private final Screen parent;

	public FriendlyPhantomsScreen(Screen parent) {
		super(Text.literal("Friendly Phantoms"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		this.addDrawableChild(
			ButtonWidget.builder(ScreenTexts.DONE, button -> this.close())
				.dimensions(this.width / 2 - 100, this.height - 40, 200, 20)
				.build()
		);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, this.height / 4, 0xFFFFFF);
		context.drawCenteredTextWithShadow(
			this.textRenderer,
			Text.literal("Phantoms will no longer attack you.").formatted(Formatting.GRAY),
			this.width / 2, this.height / 4 + 24, 0xFFFFFF
		);
	}

	@Override
	public void close() {
		if (this.client != null) {
			this.client.setScreen(this.parent);
		}
	}
}
//?}
