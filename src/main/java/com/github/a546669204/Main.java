package com.github.a546669204;


import com.github.a546669204.common.CommonProxy;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.common.config.Config.Type;
import org.apache.logging.log4j.Logger;

@Mod(modid=Main.MODID, name=Main.NAME, version=Main.VERSION, acceptedMinecraftVersions="[1.12.2]")
public class Main
{
    public static final String MODID = "powerfultool";
    public static final String NAME = "PowerfulTool";
    public static final String VERSION = "1.0.3";
    public static Logger logger;

    @SidedProxy(clientSide = "com.github.a546669204.client.ClientProxy",serverSide = "com.github.a546669204.common.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
        proxy.preInit(event);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        ConfigManager.sync(MODID, Type.INSTANCE);
        proxy.init(event);
    }
    @SubscribeEvent
    public void onConfigChangedEvent(OnConfigChangedEvent event)
    {
        if (event.getModID().equals(MODID))
        {
            ConfigManager.sync(MODID, Type.INSTANCE);
        }
    }
}
