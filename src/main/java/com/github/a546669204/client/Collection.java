package com.github.a546669204.client;

import com.github.a546669204.Main;
import com.github.a546669204.client.config.ModConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.BlockSoulSand;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketHeldItemChange;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Collection {
    public static Minecraft mc = Minecraft.getMinecraft();
    public static boolean isRun = false;

    public BlockPos target ;
    public int targetType = 0;
    public Entity targetEntity = null;
    public boolean finding = false;
    public List<BlockPos> blackList = new ArrayList<BlockPos>();

    private int lastLeft = 0;
    private int lastRight = 0;
    private long lastSellTime = Minecraft.getSystemTime();


    public Collection()
    {
        MinecraftForge.EVENT_BUS.register(this);
    }
    @SubscribeEvent
    public void clientTick(TickEvent.ClientTickEvent event)
    {
        if(!event.phase.equals(TickEvent.Phase.START)){
            return ;
        }
        if (mc.world == null){
            return  ;
        }
        if(!isRun){
            if(this.target != null){
                this.target = null;
                FindPath.removePath();
            }
            return ;
        }
//        // 自动装载
//        if (ModConfig.autoCollection.autoSupplement) {
//            ItemStack is = mc.player.getHeldItem(EnumHand.MAIN_HAND);
//            if(is.getCount() <= 1){
//                NonNullList<ItemStack> mainInventory = mc.player.inventory.mainInventory;
//                for (int i = 9; i < mainInventory.size(); i++) {
//                    if(mainInventory.get(i).getItem().equals(is.getItem()) && mainInventory.get(i).getCount() > 0){
//                        try
//                        {
//                            int j1 = i;
//                            mc.player.inventory.pickItem(j1);
//                            mc.player.connection.sendPacket(new SPacketSetSlot(-2, mc.player.inventory.currentItem, mc.player.inventory.getStackInSlot(mc.player.inventory.currentItem)));
//                            mc.player.connection.sendPacket(new SPacketSetSlot(-2, j1, mc.player.inventory.getStackInSlot(j1)));
//                            mc.player.connection.sendPacket(new SPacketHeldItemChange(mc.player.inventory.currentItem));
//                        }
//                        catch (Exception exception)
//                        {
//                            Main.logger.error("Couldn't pick item", (Throwable)exception);
//                        }
//                        break;
//                    };
//                }
//            }
//        }

        if (ModConfig.autoCollection.autoSell) {
            if(Minecraft.getSystemTime() - lastSellTime > 5*60*1000){
                lastSellTime = Minecraft.getSystemTime();
                mc.player.sendChatMessage("/sellall");
                mc.player.sendChatMessage("/sellall");
            }
        }


        if(this.target == null && !this.finding){
            findTarget();
        }
        lastLeft++;
        lastRight++;

        if (this.target != null) {
            IBlockState sb = mc.world.getBlockState(this.target);
            Block block = sb.getBlock();
            if(this.targetEntity != null){
                if(this.targetEntity.isEntityAlive()){
                    this.target = null;
                    FindPath.removePath();
                    this.targetEntity = null;
                }
            }else{
                //            Main.logger.info("BlockgetDistanceSq  " + mc.player.getDistanceSq(this.target));
                if(block instanceof BlockAir){
                    this.target =  null;
                }else if(mc.player.getDistanceSq(this.target)<=2){
//                KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(),false);
                    if(block instanceof BlockSoulSand ||block instanceof BlockFarmland) {
                        if(lastRight > ModConfig.autoCollection.collectionTime){
                            mc.playerController.processRightClickBlock(mc.player, mc.world, this.target, EnumFacing.UP, new Vec3d(this.target.getX(),this.target.getY(),this.target.getZ()), EnumHand.MAIN_HAND);
                            mc.player.swingArm(EnumHand.MAIN_HAND);
                            this.target = null;
                            lastRight = 0;
                        }

                    }else if(block  == Blocks.WHEAT || block == Blocks.CARROTS || block == Blocks.POTATOES || block == Blocks.BEETROOTS || block == Blocks.NETHER_WART){
                        if(lastLeft > ModConfig.autoCollection.plantingTime){
                            mc.playerController.clickBlock( this.target, EnumFacing.getDirectionFromEntityLiving(this.target, mc.player));
                            mc.player.swingArm(EnumHand.MAIN_HAND);
                            this.target = null;
                            lastLeft = 0;
                        }

                    }
                }else{
                    for (Map.Entry<IProperty<?>, Comparable<?>> entry:sb.getProperties().entrySet()) {
//                        Main.logger.info("BlockState "+ entry.getKey().getName() + "==>" +entry.getValue().toString());
                        Object[] arr = entry.getKey().getAllowedValues().toArray();
                        if(entry.getKey().getName().equals("age") ){
                            if(!entry.getValue().equals(arr[arr.length - 1])){
                                this.target =  null;
                                FindPath.removePath();
                            }
                        }
                    }
                }
            }




        }

    }
    public void findTarget(){
        //搜索中
        this.finding = true;

        BlockPos playerPos = new BlockPos(mc.player);

        int cx = playerPos.getX();
        int cy = playerPos.getY();
        int cz = playerPos.getZ();

        double minD = Double.MAX_VALUE;
        BlockPos minP = null;
        int type = 0;
        Entity minE = null;

//        Main.logger.info("查找目标中");

        for (int x = cx-48; x < cx+48; x++) {
            for (int y = cy-12; y < cy+12; y++) {
                for (int z = cz-48; z < cz+48; z++) {
                    BlockPos p = new BlockPos(x,y,z);
                    if(blackList.contains(p)){
                        continue;
                    }
                    IBlockState sb = mc.world.getBlockState(p);
                    Block b = sb.getBlock();
                    if(b == Blocks.AIR || b ==  Blocks.BEDROCK){
                        continue;
                    }

                    if(b == Blocks.SOUL_SAND || b == Blocks.FARMLAND){
                        IBlockState sbup = mc.world.getBlockState(p.up());
                        if(sbup.getBlock() instanceof BlockAir){
                            //找到未种植方块
                            double distance  = mc.player.getDistanceSq(p) + Math.pow(p.getY()-mc.player.posY,2)*50;
                            if(distance < minD){
                                minD = distance;
                                minP = p;
                                type = 1;
                            }
                        }
                    }else if (b  == Blocks.WHEAT || b == Blocks.CARROTS || b == Blocks.POTATOES || b == Blocks.BEETROOTS || b == Blocks.NETHER_WART){
                        //小麦 胡萝卜 马铃薯 甜菜根
                        for (Map.Entry<IProperty<?>, Comparable<?>> entry:sb.getProperties().entrySet()) {
                            Object[] arr = entry.getKey().getAllowedValues().toArray();
                            if(entry.getKey().getName().equals("age") && entry.getValue().equals(arr[arr.length - 1])){
                                double distance  = mc.player.getDistanceSq(p)*10+ Math.pow(p.getY()-mc.player.posY,2)*50;
                                if(distance < minD){
                                    minD = distance;
                                    minP = p;
                                    type = 2;
                                }
                            }
                        }
                    }
                }
            }
        }

//        List<Entity> entities = MobAttack.getPlayerEntity();
//        for (int i = 0; i < entities.size(); i++)
//        {
//            Entity e = (Entity)entities.get(i);
//
//            if (e instanceof EntityItem && (e.posY-mc.player.posY)<3) {
//                double distance  = mc.player.getDistanceSq(e.getPosition())+ Math.pow(e.posY-mc.player.posY,2)*50;
//                if(distance < minD){
//                    minD = distance;
//                    minP = e.getPosition();
//                    type = 3;
//                }
//            }
//        }

        if (minP != null){
            this.target = minP;
            this.targetType = type; // 0 未知 1 田地 2 农作物 3 掉落物
            this.targetEntity = minE;
//            Main.logger.info("查找目标中"+this.target);
            if (!FindPath.createPath(this.target,this.targetType,Math.abs(mc.player.getPosition().getY() -this.target.getY()) <=3,true)){
                this.target = null;
            }
        }

        //搜索完毕
        this.finding = false;
    }
    public static void changeStatus(){
//        FindPath.createPath(Utils.randomPos(),1,false,false);
//        return;
        isRun = !isRun;
        if(!isRun){
            FindPath.removePath();
        }
    }
}
