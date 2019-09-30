package com.github.a546669204.client;

import com.github.a546669204.Main;
import com.github.a546669204.client.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class AutoLogin {
    public static Minecraft mc = Minecraft.getMinecraft();
    public boolean isRun = false;
    public AutoLogin(){
        MinecraftForge.EVENT_BUS.register(this);
    }
    @SubscribeEvent
    public void ActionPerformed(GuiScreenEvent.ActionPerformedEvent event)
    {
        isRun = true;
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if(mc.getConnection() != null && mc.getConnection().getNetworkManager() != null){
                    Main.logger.info("isChannelOpen"+mc.getConnection().getNetworkManager().isChannelOpen());
                    Main.logger.info("currentScreen"+mc.currentScreen.toString());

                    if(!mc.getConnection().getNetworkManager().isChannelOpen()){
                        mc.displayGuiScreen(new GuiConnecting(mc.currentScreen,mc, ModConfig.autoLogin.ip,ModConfig.autoLogin.port));
                    }
                }
            }
        }, 0*60*1000, ModConfig.autoLogin.time);
        Main.logger.info("定时器启动" + ModConfig.autoLogin.time);
    }

}
