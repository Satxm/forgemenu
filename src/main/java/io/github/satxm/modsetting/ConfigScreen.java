package io.github.satxm.modsetting;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TranslatableComponent;

public class ConfigScreen extends Screen {
	private final ModSetting.Config cfg;
	private final Screen lastScreen;

	public ConfigScreen(Screen screen) {
		super(new TranslatableComponent("options.title"));
		this.lastScreen = screen;
		Minecraft client = Minecraft.getInstance();
		this.cfg = ModSetting.getConfig(client);
	}

	protected void init() {
		this.addRenderableWidget(
				new Button(this.width / 2 - 102, this.height - 40, 204, 20, CommonComponents.GUI_DONE, (button) -> {
					ModSetting.saveConfig(cfg);
					this.minecraft.updateTitle();
					this.minecraft.setScreen(this.lastScreen);
				}));

		this.addRenderableWidget(
				CycleButton.onOffBuilder(cfg.EnableModButton).create(this.width / 2 - 102, this.height / 2 - 12, 204,
						20, new TranslatableComponent("gui.EnableModButton"), (button, EnableModButton) -> {
							cfg.EnableModButton = EnableModButton;
						}));

		this.addRenderableWidget(
				CycleButton.onOffBuilder(cfg.SingleLineButton).create(this.width / 2 - 102, this.height / 2 + 12, 204,
						20, new TranslatableComponent("gui.SingleLineButton"), (button, SingleLineButton) -> {
							cfg.SingleLineButton = SingleLineButton;
						}));
	}

	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		drawCenteredString(poseStack, this.font, this.title, this.width / 2, 20, 16777215);
		super.render(poseStack, i, j, f);
	}
}
