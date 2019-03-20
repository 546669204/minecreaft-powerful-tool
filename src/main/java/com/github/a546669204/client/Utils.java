package com.github.a546669204.client;


import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.text.TextComponentString;

public class Utils
{
    public static void sendMessage(String str){
        Minecraft.getMinecraft().player.sendMessage(new TextComponentString(String.format("%s", new Object[] { str })));
    }
}
