package io.github.satxm.modsetting;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.gui.ModListScreen;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;

@Mod(ModSetting.MODID)
public class ModSetting {
	private static final Gson gson = new GsonBuilder().create();
	private static final Logger LOGGER = LogManager.getLogger(ModSetting.class);
	private static final Map<Minecraft, Config> configMap = Collections.synchronizedMap(new WeakHashMap<>());
	public static final String MODID = "modsetting";

	public ModSetting() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
		MinecraftForge.EVENT_BUS.addListener(ModSetting::ChangeButton);
		if (FMLEnvironment.dist.isClient()) {
			FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientInit);
		}
	}

	@OnlyIn(Dist.CLIENT)
	private void clientInit(FMLClientSetupEvent event) {
		ModLoadingContext.get().registerExtensionPoint(ConfigScreenFactory.class,
				() -> new ConfigScreenFactory((minecraft, screen) -> new ConfigScreen(screen)));
	}

	@SubscribeEvent
	public static void ChangeButton(final ScreenEvent.Init.Post event) {
		Screen screen = event.getScreen();
		Minecraft client = Minecraft.getInstance();
		Config cfg = configMap.get(client);
		if (cfg.EnableModButton) {
			if (screen instanceof PauseScreen && event.getListenersList().size() != 0) {
				if (cfg.SingleLineButton) {
					for (int k = 0; k < event.getListenersList().size(); k++) {
						Button OldButton = (Button) event.getListenersList().get(k);
						if (OldButton.y >= screen.height / 4 + 96 + -16) {
							OldButton.y += 24;
						}
					}
					Button ModScreenButton = new Button(screen.width / 2 - 102, screen.height / 4 + 96 + -16, 204, 20,
							Component.translatable("fml.menu.mods"),
							(button) -> client.setScreen(new ModListScreen(screen)));
					event.addListener(ModScreenButton);
				} else {
					for (int k = 0; k < event.getListenersList().size(); k++) {
						Button OldButton = (Button) event.getListenersList().get(k);
						if (OldButton.getMessage().getString()
								.equals(Component.translatable("menu.reportBugs").getString())) {
							int x = OldButton.x;
							int y = OldButton.y;
							int w = OldButton.getWidth();
							int h = OldButton.getHeight();
							Button ModScreenButton = new Button(x, y, w, h, Component.translatable("fml.menu.mods"),
									(button) -> client.setScreen(new ModListScreen(screen)));
							event.removeListener(OldButton);
							event.addListener(ModScreenButton);
						}
					}
				}
			}
		}
	}

	public static void saveConfig(Config cfg) {
		if (!cfg.needsDefaults) {
			try {
				Files.write(cfg.location, toPrettyFormat(cfg).getBytes(), StandardOpenOption.TRUNCATE_EXISTING,
						StandardOpenOption.CREATE);
			} catch (IOException e) {
				LOGGER.warn("Unable to write config file!", e);
			}
		}
	}

	public static Config getConfig(Minecraft client) {
		return Objects.requireNonNull(configMap.get(client), "no config ???");
	}

	private void setup(final FMLCommonSetupEvent event) {
		Minecraft client = Minecraft.getInstance();
		Path location = FMLPaths.CONFIGDIR.get().resolve("ForgeModButton.json");
		ModSetting.Config cfg;
		try {
			cfg = gson.fromJson(new String(Files.readAllBytes(location)), ModSetting.Config.class);
			cfg.location = location;
		} catch (IOException | JsonParseException e) {
			try {
				Files.deleteIfExists(location);
			} catch (IOException ioException) {
				//
			}
			cfg = new ModSetting.Config();
			cfg.location = location;
			saveConfig(cfg);
			cfg.needsDefaults = true;
		}
		configMap.put(client, cfg);
	}

	public static class Config {
		public boolean EnableModButton = true;
		public boolean SingleLineButton = false;
		public transient Path location;
		public transient boolean needsDefaults = false;
	}

	private static String toPrettyFormat(Object src) {
		String json = gson.toJson(src);
		JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		return gson.toJson(jsonObject);
	}
}
