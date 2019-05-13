package com.github.a546669204.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.GuiConnecting;

import java.util.Timer;
import java.util.TimerTask;

public class autoLogin {
    public static Minecraft mc = Minecraft.getMinecraft();
    public autoLogin(){
        new Timer().schedule(new TimerTask() {
            public void run() {
                if(!mc.getConnection().getNetworkManager().isChannelOpen()){
                    mc.displayGuiScreen(new GuiConnecting(mc.currentScreen,mc,"mc.rongyaomc.com",25565));
                }
            }
        }, 0, 10000);
    }
}
