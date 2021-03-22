package com.github.monulo.zoom;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Objects;

@Mod(modid = "zoommod", name = "Zoom-Mod", version = "1.0")
public class ZoomMod {
    private long systemTime = getSystemTime();
    private Minecraft mc;
    private float origSens;
    private boolean inZoom = false;

    private float sens = 0;
    private float fov = 0;
    private float fovPrev = 0;

    public int speed = 5;
    public static boolean stab = true;

    public static Configuration config;
    public static KeyBinding[] keyBindings = new KeyBinding[5];
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        String category = "Zoom Mod";
        keyBindings[0] = new KeyBinding("Zoom +", Keyboard.KEY_G, category);
        keyBindings[1] = new KeyBinding("Zoom -", Keyboard.KEY_R, category);
        keyBindings[2] = new KeyBinding("Reset Zoom", Keyboard.KEY_V, category);
        keyBindings[3] = new KeyBinding("Zoom Speed +", Keyboard.KEY_UP, category);
        keyBindings[4] = new KeyBinding("Zoom Speed -", Keyboard.KEY_DOWN, category);
        for(int i = 0; i < keyBindings.length; ++i) {
            ClientRegistry.registerKeyBinding(keyBindings[i]);
        }
    }
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        resetOpt();
    }
    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        mc = Minecraft.getMinecraft();
        origSens = mc.gameSettings.mouseSensitivity;
    }
    @SubscribeEvent
    public void onFOVModifier(EntityViewRenderEvent.FOVModifier e) {
        if(!inZoom) {
            return;
        }
        if(e.getEntity().equals(mc.player)) {
            e.setFOV((float) (fovPrev + ((fov - fovPrev) * e.getRenderPartialTicks())));
        }
    }
    @SubscribeEvent
    public void tick(TickEvent.ClientTickEvent e) throws IOException {
        if(e.phase == TickEvent.Phase.START) {
            fovPrev = fov;
            if(mc.currentScreen == null && mc.player != null) {
                if(keyBindings[2].isPressed()) {
                    resetOpt();
                } else if(keyBindings[1].isKeyDown()) {
                    zoom(1);
                } else if(keyBindings[0].isKeyDown()) {
                    zoom(-1);
                } else if(keyBindings[3].isPressed()) {
                    if(speed < 20) {
                        speed++;
                        Minecraft.getMinecraft().player.sendStatusMessage(Objects.requireNonNull(ITextComponent.Serializer.jsonToComponent(String.valueOf(speed))), true);
                    }
                } else if(keyBindings[4].isPressed()) {
                    if(speed > 1) {
                        speed--;
                        Minecraft.getMinecraft().player.sendStatusMessage(Objects.requireNonNull(ITextComponent.Serializer.jsonToComponent(String.valueOf(speed))), true);
                    }
                }
            } else if(e.phase == TickEvent.Phase.END) {
                systemTime = getSystemTime();
            }
        }
    }
    private void zoom(int sig) {
        if(mc.currentScreen != null) {
            return;
        }
        float mod = speed;
        float fov = this.fov;
        float newFov = fov - mod * sig;
        if(newFov < 2.0) {
            newFov = 2.0F;
        }
        if(newFov != fov) {
            if(newFov >= mc.gameSettings.fovSetting) {
                resetOpt();
            } else {
                inZoom = true;
                this.fov = newFov;
                if(stab) {
                    float newSens = origSens * (newFov / mc.gameSettings.fovSetting);
                    mc.gameSettings.mouseSensitivity = newSens;
                    sens = newSens;
                }
            }
        }
    }
    private void resetOpt() {
        mc.gameSettings.mouseSensitivity = origSens;
        sens = mc.gameSettings.mouseSensitivity;
        fov = mc.gameSettings.fovSetting;
        inZoom = false;
    }
    private static long getSystemTime() {
        return Sys.getTime() * 1000L / Sys.getTimerResolution();
    }
}
