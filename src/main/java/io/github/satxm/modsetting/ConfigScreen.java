package io.github.satxm.modsetting;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.TranslationTextComponent;

public class ConfigScreen extends Screen {
	private final ModSetting.Config cfg;
	private final Screen lastScreen;
	private Button EnableModButton;
	private Button SingleLineButton;

	public ConfigScreen(Screen screen) {
		super(new TranslationTextComponent("options.title"));
		this.lastScreen = screen;
		Minecraft client = Minecraft.getInstance();
		this.cfg = ModSetting.getConfig(client);
	}

	protected void init() {
		this.addButton(new Button(this.width / 2 - 102, this.height - 40, 204, 20, DialogTexts.GUI_DONE, (button) -> {
			ModSetting.saveConfig(cfg);
			this.minecraft.updateTitle();
			this.minecraft.setScreen(this.lastScreen);
		}));

		this.EnableModButton = (Button) this.addButton(new Button(this.width / 2 - 102, this.height / 2 - 12, 204, 20,
				new TranslationTextComponent("gui.EnableModButton"), (button) -> {
					cfg.EnableModButton = !cfg.EnableModButton;
					this.updateSelectionStrings();
				}));

		this.SingleLineButton = (Button) this.addButton(new Button(this.width / 2 - 102, this.height / 2 + 12, 204, 20,
				new TranslationTextComponent("gui.SingleLineButton"), (button) -> {
					cfg.SingleLineButton = !cfg.SingleLineButton;
					this.updateSelectionStrings();
				}));

		this.updateSelectionStrings();
	}

	private void updateSelectionStrings() {
		this.EnableModButton.setMessage(
				DialogTexts.optionStatus(new TranslationTextComponent("gui.EnableModButton"), cfg.EnableModButton));
		this.SingleLineButton.setMessage(
				DialogTexts.optionStatus(new TranslationTextComponent("gui.SingleLineButton"), cfg.SingleLineButton));
	}

	public void render(MatrixStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		drawCenteredString(poseStack, this.font, this.title, this.width / 2, 20, 16777215);
		super.render(poseStack, i, j, f);
	}
}
