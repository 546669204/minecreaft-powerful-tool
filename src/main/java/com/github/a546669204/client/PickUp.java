package com.github.a546669204.client;

import com.github.a546669204.Main;
import com.github.a546669204.client.config.ModConfig;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static net.minecraftforge.fml.common.eventhandler.Event.Result.ALLOW;
import static net.minecraftforge.fml.common.eventhandler.Event.Result.DENY;

public class PickUp {


    public PickUp()
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onItemPickup (EntityItemPickupEvent event)
    {
        Item item = event.getItem().getItem().getItem();
        String itemName = event.getItem().getItem().getDisplayName();
        Main.logger.info("您拾起了   " + itemName);
        //判断拾起的是否是 装备
        if(item instanceof ItemShield ||item instanceof ItemSword || item instanceof ItemBow || item instanceof ItemAxe){
            if (ModConfig.pickUp.notWhite){
                if(itemName.indexOf("§f") == 0  || itemName.indexOf("§") == -1){
                    event.setResult(DENY);
                    event.setCanceled(true);
                    return ;
                }
            }
        }
        event.setResult(ALLOW);
    }



}
