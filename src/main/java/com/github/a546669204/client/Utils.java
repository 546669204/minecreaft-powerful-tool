package com.github.a546669204.client;


import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;

import java.util.Random;

public class Utils
{
    public static Minecraft mc = Minecraft.getMinecraft();
    public static void sendMessage(String str){
        Minecraft.getMinecraft().player.sendMessage(new TextComponentString(String.format("%s", new Object[] { str })));
    }
    public static void playerTurn(double x,double y,double z){
        double d0 = x+0.5 - mc.player.posX;
        double d1 = y -(mc.player.posY +mc.player.getEyeHeight());//+  mc.player.getEyeHeight()
        double d2 = z+0.5 - mc.player.posZ;
        double d3 = (double) MathHelper.sqrt(d0 * d0 + d2 * d2);
        float f = (float)(MathHelper.atan2(d2, d0) * (180D / Math.PI)) - 90.0F;
        float f1 = (float)(-(MathHelper.atan2(d1, d3) * (180D / Math.PI)));
        mc.player.turn(new Float((-mc.player.rotationYaw + f)/0.15),new Float((mc.player.rotationPitch - f1)/0.15));
    }
    public static BlockPos randomPos(){
        Random ra =new Random();
        ra.setSeed(Minecraft.getSystemTime());
        return mc.player.getPosition().add(ra.nextInt(20)-10,0,ra.nextInt(20)-10);
    }
}
