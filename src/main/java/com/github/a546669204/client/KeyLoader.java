package com.github.a546669204.client;

import com.github.a546669204.client.config.config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import javax.swing.*;


public class KeyLoader
{
    public static KeyBinding autoAttack;
    public static KeyBinding openSetting;
    public static KeyBinding test;

    public static JFrame  frame;

    public KeyLoader()
    {

        KeyLoader.autoAttack = new KeyBinding("自动攻击", Keyboard.KEY_H, "万能工具箱");
        KeyLoader.openSetting = new KeyBinding("设置修改", Keyboard.KEY_C, "万能工具箱");
        KeyLoader.test = new KeyBinding("自动采集", Keyboard.KEY_V, "万能工具箱");

        ClientRegistry.registerKeyBinding(KeyLoader.autoAttack);
        ClientRegistry.registerKeyBinding(KeyLoader.openSetting);
        ClientRegistry.registerKeyBinding(KeyLoader.test);

        MinecraftForge.EVENT_BUS.register(this);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event)
    {
        if(MobAttack.mc.world == null){
            return ;
        }
        if (KeyLoader.autoAttack.isPressed())
        {
            MobAttack.changeStatus();
            Utils.sendMessage(MobAttack.isRun?"自动攻击：启动":"自动攻击：关闭");
            return ;
        }
        if (KeyLoader.openSetting.isPressed()){
            if(frame == null){
                frame = new JFrame("config");
                frame.setContentPane(new config().config);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.pack();
                frame.setVisible(true);
            }
        }
        if (KeyLoader.test.isPressed()){
            Collection.changeStatus();
            Utils.sendMessage(Collection.isRun?"自动采集：启动":"自动采集：关闭");
        }
    }

    public static void closeWindow(){
        if (frame instanceof  JFrame){
            frame.dispose();
            frame = null;
        }

    }

    //鼠标右键模拟按下
    public static void pressDownRight(){
        KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindUseItem.getKeyCode(),true);
    }
    //鼠标右键模拟弹起
    public static void pressUpRight(){
        KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindUseItem.getKeyCode(),false);
    }
    //鼠标左键模拟按下
    public static void pressDownLeft(){
        KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindAttack.getKeyCode(),true);
    }
    //鼠标左键模拟弹起
    public static void pressUpLeft(){
        KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindAttack.getKeyCode(),false);
    }
    //W模拟按下
    public static void pressDownForward(){
        KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindForward.getKeyCode(),true);
    }
    //W模拟弹起
    public static void pressUpForward(){
        KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindForward.getKeyCode(),false);
    }
}