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
    public static Minecraft mc = Minecraft.getMinecraft();
    public static boolean isRun = false;

    private Queue<Entity> mEntityQueue = new ArrayDeque();
    public static Map<EntityLivingBase, Integer> mHurtResistantTimes = new HashMap();
    private long mTicks;
    private static BlockPos lastBlockPos = null;


    public MobAttack(){
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static void changeStatus(){
        isRun = !isRun;
        if(isRun && ModConfig.autoAttack.type == 1){
            lastBlockPos = mc.player.getPosition();
            FindPath.createPath(lastBlockPos,1,false,false);
        }
        if(!isRun){
            lastBlockPos = null;
            FindPath.removePath();
        }
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
        }
        this.mEntityQueue.offer(entity);

        if ((entity instanceof EntityLivingBase)) {
            mHurtResistantTimes.put((EntityLivingBase)entity,Integer.valueOf(((EntityLivingBase)entity).maxHurtResistantTime));
        }
    }

    public boolean canAttackEntity(Entity entity)
    {
        //怪物或者角色死亡
        if (!mc.player.isEntityAlive() || !entity.isEntityAlive()) {
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
        if (!this.mEntityQueue.isEmpty()){
            if(ModConfig.autoAttack.useShiled && !mc.player.isHandActive()){
                KeyLoader.pressDownRight();
            }
            Entity e = (Entity)this.mEntityQueue.poll();
            mc.playerController.attackEntity(mc.player, e);
            mc.player.swingArm(EnumHand.MAIN_HAND);
        }


        this.mTicks += 1L;
        if (this.mTicks % ModConfig.autoAttack.time != 0L) {
            return;
        }

        List<Entity> entities = this.getPlayerEntity();
        Double minDistance =  Double.MAX_VALUE;
        Entity targeEntity = null;
        Double minDistance2 =  Double.MAX_VALUE;
        Entity targeEntity2 = null;
        for (int i = 0; i < entities.size(); i++)
        {
            Entity e = (Entity)entities.get(i);
            if (ModConfig.autoAttack.target.containsKey(e.getName())) {
                double ed = mc.player.getDistanceSq(e);
                if(canAttackEntity(e)){
                    if(minDistance>ed){
                        minDistance = ed;
                        targeEntity = e;
                    }
                }
                if(minDistance2>ed){
                    minDistance2 = ed;
                    targeEntity2 = e;
                }


            }
        }

        if(ModConfig.autoAttack.type == 2){
            if(minDistance2>=ModConfig.autoAttack.range && targeEntity2 != null){
                FindPath.createPath(targeEntity2.getPosition(),1,false,false);
            }
        }

        if (targeEntity != null ){
            if(ModConfig.autoAttack.turnTarget){
                double mobY = targeEntity.posY+targeEntity.getEyeHeight();
                if(mobY > mc.player.posY+mc.player.getEyeHeight()){
                    mobY = mc.player.posY+mc.player.getEyeHeight();
                }
                Utils.playerTurn(targeEntity.posX,mobY,targeEntity.posZ);
            }
            attackMob(targeEntity);
        }
    }
}
