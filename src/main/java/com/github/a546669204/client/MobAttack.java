package com.github.a546669204.client;


import java.util.*;

import com.github.a546669204.client.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemShield;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import net.minecraftforge.fml.common.gameevent.TickEvent;


public class MobAttack
{
    public static Map<EntityLivingBase, Integer> mHurtResistantTimes = new HashMap();
    public static Minecraft mc = Minecraft.getMinecraft();
    private Queue<Entity> mEntityQueue = new ArrayDeque();
    private long mTicks;

    public static boolean isRun = false;

    public MobAttack(){
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static List<Entity> getPlayerEntity(){
        EntityPlayerSP  player = mc.player;
        AxisAlignedBB aabb = new AxisAlignedBB(player.posX-8*5,player.posY-4*5,player.posZ-8*5,
                                               player.posX+8*5,player.posY+4*5,player.posZ+8*5);
        return mc.world.getEntitiesWithinAABBExcludingEntity(player,aabb);
    }

    public void attackMob(Entity entity)
    {
        //检测自动防御是否开启 && 副手为盾牌
        if(ModConfig.autoAttack.useShiled && mc.player.getHeldItem(EnumHand.OFF_HAND).getItem() instanceof ItemShield){
            KeyLoader.pressUpRight();
            this.mEntityQueue.offer(entity);
        }else{
            mc.playerController.attackEntity(mc.player, entity);
            mc.player.swingArm(EnumHand.MAIN_HAND);
        }

        if ((entity instanceof EntityLivingBase)) {
            mHurtResistantTimes.put((EntityLivingBase)entity,Integer.valueOf(((EntityLivingBase)entity).maxHurtResistantTime));
        }
    }

    public boolean canAttackEntity(Entity entity)
    {

        //怪物或者角色死亡
        if (entity.isDead || mc.player.isDead) {
            return false;
        }
        //最大伤害时间
        if (mHurtResistantTimes.containsKey(entity) && (((Integer)mHurtResistantTimes.get(entity)).intValue() > ((EntityLivingBase)entity).maxHurtResistantTime / 2.0F)) {
            return false;
        }


        double distanceSq = ModConfig.autoAttack.range;


        //计算距离
        if (mc.player.getDistanceSq(entity) >= distanceSq) {
            return false;
        }else if(mc.player.getDistanceSq(entity) <= 3){
            return true;
        }

        //能否看见
        if (!mc.player.canEntityBeSeen(entity)) {
            return false;
        }

        return true;
    }

    @SubscribeEvent
    public void clientTick(TickEvent.ClientTickEvent event)
    {
        if (!event.phase.equals(TickEvent.Phase.END)) {
            return;
        }
        if(!isRun){
            return ;
        }
        //判断是否进入世界 未进入世界 清除缓存 队列
        if (mc.world == null)
        {
            mHurtResistantTimes.clear();
            this.mEntityQueue.clear();
            return;
        }
        Iterator<Map.Entry<EntityLivingBase, Integer>> iterator = mHurtResistantTimes.entrySet().iterator();
        while (iterator.hasNext())
        {
            Map.Entry<EntityLivingBase, Integer> e = (Map.Entry)iterator.next();
            EntityLivingBase living = (EntityLivingBase)e.getKey();
            //怪物死亡判断
            if ((living.isDead) || (living.getHealth() <= 0.0F) || (((Integer)e.getValue()).intValue() <= 0)) {
                iterator.remove();
            } else {
                e.setValue(Integer.valueOf(((Integer)e.getValue()).intValue() - 1));
            }
        }

        //处理持盾逻辑
        ;
        if (ModConfig.autoAttack.useShiled && !mc.player.isHandActive() && !this.mEntityQueue.isEmpty()){
            Entity e = (Entity)this.mEntityQueue.poll();
            mc.playerController.attackEntity(mc.player, e);
            mc.player.swingArm(EnumHand.MAIN_HAND);
            KeyLoader.pressDownRight();
        }


        this.mTicks += 1L;
        if (this.mTicks % ModConfig.autoAttack.time != 0L) {
            return;
        }

        List<Entity> entities = this.getPlayerEntity();
        Double minDistance =  100D;
        Entity targeEntity = null;
        for (int i = 0; i < entities.size(); i++)
        {
            Entity e = (Entity)entities.get(i);
            if (ModConfig.autoAttack.target.containsKey(e.getName()) && canAttackEntity(e)) {
                double ed = mc.player.getDistanceSq(e);
                if(minDistance>ed){
                    minDistance = ed;
                    targeEntity = e;
                }
            }
        }

        if (targeEntity != null ){
            attackMob(targeEntity);
        }
    }
}
