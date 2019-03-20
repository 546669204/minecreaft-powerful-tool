package com.github.a546669204.client;

import com.github.a546669204.Main;
import com.github.a546669204.client.config.ModConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.BlockSoulSand;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityRabbit;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketHeldItemChange;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.pathfinding.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Color;
import scala.Array;

import java.util.*;

public class FindPath {
    public static  Minecraft mc = Minecraft.getMinecraft();
    public static boolean isRun = false;
    public BlockPos target ;
    public int targetType = 0;
    public Entity targetEntity = null;
    public boolean finding = false;
    public Path path = null;
    public List<BlockPos> blackList = new ArrayList<BlockPos>();

    private int lastLeft = 0;
    private int lastRight = 0;

    private long lastSellTime = Minecraft.getSystemTime();

    public FindPath(){
        MinecraftForge.EVENT_BUS.register(this);
    }
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void renderWorldLastEvent(RenderWorldLastEvent event)
    {
        drawPath();
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
            return ;
        }
//        System.out.println(String.format("当前玩家位置 x:%f,y:%f,z:%f",mc.player.posX,mc.player.posY,mc.player.posZ));
        //自动拾取
//        AxisAlignedBB axisalignedbb = mc.player.getEntityBoundingBox().grow(2.0D, 0.5D, 2.0D);
//        List<Entity> list = mc.world.getEntitiesWithinAABBExcludingEntity(mc.player, axisalignedbb);
//
//        for (int i = 0; i < list.size(); ++i)
//        {
//            Entity entity = list.get(i);
//            if (!entity.isDead)
//            {
//                entity.onCollideWithPlayer(mc.player);
//            }
//        }

        // 自动装载
        if (ModConfig.autoCollection.autoSupplement) {
            ItemStack is = mc.player.getHeldItem(EnumHand.MAIN_HAND);
            if(is.getCount() <= 1){
                NonNullList<ItemStack> mainInventory = mc.player.inventory.mainInventory;
                for (int i = 9; i < mainInventory.size(); i++) {
                    if(mainInventory.get(i).getItem().equals(is.getItem()) && mainInventory.get(i).getCount() > 0){
//                        mc.player.addItemStackToInventory(mainInventory.get(i));
                        try
                        {
                            int j1 = i;
                            mc.player.inventory.pickItem(j1);
                            mc.player.connection.sendPacket(new SPacketSetSlot(-2, mc.player.inventory.currentItem, mc.player.inventory.getStackInSlot(mc.player.inventory.currentItem)));
                            mc.player.connection.sendPacket(new SPacketSetSlot(-2, j1, mc.player.inventory.getStackInSlot(j1)));
                            mc.player.connection.sendPacket(new SPacketHeldItemChange(mc.player.inventory.currentItem));
                        }
                        catch (Exception exception)
                        {
                            Main.logger.error("Couldn't pick item", (Throwable)exception);
                        }
                        break;
                    };
                }
            }
        }

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
                if(this.targetEntity.isDead){
                    this.target = null;
                    this.path = null;
                    this.targetEntity = null;
                }
            }else{
                //            Main.logger.info("BlockgetDistanceSq  " + mc.player.getDistanceSq(this.target));
                if(block instanceof BlockAir){
                    this.target =  null;
                }else if(mc.player.getDistanceSq(this.target)<=2){
//                KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(),false);
                    if(block instanceof BlockSoulSand ||block instanceof  BlockFarmland) {
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
                                this.path = null;
                            }
                        }
                    }
                }
            }




        }

        if(this.target == null){
            return;
        }

//        if(this.path != null && this.path.isFinished()){
//            this.path = null;
//        }

        if (this.path == null) {
            return ;
        }
        if(false){
            return;
        }



        // 转向
        double d0 = this.path.getCurrentPos().x+0.5 - mc.player.posX;
        double d1 = this.path.getCurrentPos().y -(mc.player.posY +mc.player.getEyeHeight());//+  mc.player.getEyeHeight()
        double d2 = this.path.getCurrentPos().z+0.5 - mc.player.posZ;
        double d3 = (double) MathHelper.sqrt(d0 * d0 + d2 * d2);
        float f = (float)(MathHelper.atan2(d2, d0) * (180D / Math.PI)) - 90.0F;
        float f1 = (float)(-(MathHelper.atan2(d1, d3) * (180D / Math.PI)));
        mc.player.turn(new Float((-mc.player.rotationYaw + f)/0.15),new Float((mc.player.rotationPitch - f1)/0.15));//


        double diffX = this.path.getCurrentPos().x+0.5 - mc.player.posX;
        double diffZ = this.path.getCurrentPos().z+0.5 - mc.player.posZ;
        double f3 =  (double)MathHelper.sqrt(diffX * diffX + diffZ * diffZ);


        if(mc.player.getDistanceSq(new BlockPos(this.path.getCurrentPos())) <= 1){
            if(this.path.getCurrentPathIndex() < this.path.getCurrentPathLength() -1){
                this.path.incrementPathIndex();
            }
        }else{
            mc.player.travel(0F,0F,1);
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
//            System.out.println(String.format("发现目标 坐标：" + minP.toString()));
//            System.out.println(String.format("当前 坐标：" + mc.player.getPosition().toString()));
            createPath();
            if (this.path == null){
//                blackList.add(this.target);
                this.target = null;
            }
        }

        //搜索完毕
        this.finding = false;
    }

    public void createPath(){
        if(this.targetType == 1){
            PathPoint[] pathpoints = {new PathPoint(this.target.up().getX(),this.target.up().getY(),this.target.up().getZ())};
            this.path = new Path(pathpoints);
        }else{
            PathPoint[] pathpoints = {new PathPoint(this.target.getX(),this.target.getY(),this.target.getZ())};
            this.path = new Path(pathpoints);
        }

        return;
//        NodeProcessor nodeProcessor = new WalkNodeProcessor();
//        PathFinder pf = new PathFinder(nodeProcessor);
//        float f = 128;
//        BlockPos blockpos = new BlockPos(mc.player);
//        int i = (int) (f + 8.0F);
//        ChunkCache chunkcache = new ChunkCache(mc.world, blockpos.add(-i, -i, -i), blockpos.add(i, i, i), 0);
//        EntityRabbit cow = new EntityRabbit(mc.world);
//        cow.setPosition(mc.player.posX,mc.player.posY,mc.player.posZ);
//        if(this.targetType == 1){
//            this.path = pf.findPath(chunkcache, cow, this.target.up(), f);
//        }else{
//            this.path = pf.findPath(chunkcache, cow, this.target, f);
//        }

    }
    private void drawPath() {
        if (this.path == null){
            return ;
        }
        Vec3d player_pos = mc.player.getPositionVector();
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glTranslated(-player_pos.x, -player_pos.y, -player_pos.z);

        GL11.glColor4d(255, 0,0, 150);
        GL11.glLineWidth(30F);
        GL11.glDepthMask(false);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

        for (int i = 0; i < this.path.getCurrentPathLength(); i++) {
            Color c = new Color(0, 255, 0, 150);
            PathPoint pp = this.path.getPathPointFromIndex(i);
            if(i == this.path.getCurrentPathIndex()){
                c = new Color(255,0,0,150);
            }

            if(i != 0&& i != this.path.getCurrentPathLength()-1){
                bufferBuilder.pos(pp.x+0.5, pp.y+mc.player.getEyeHeight(), pp.z+0.5).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();
            }
            bufferBuilder.pos(pp.x+0.5, pp.y+mc.player.getEyeHeight(), pp.z+0.5).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();
        }
        tessellator.draw();


        GL11.glDepthMask(true);
        GL11.glPopAttrib();
    }

    private static void drawLine(BlockPos start, BlockPos end) {
        GlStateManager.pushMatrix();
        GlStateManager.glLineWidth(2F);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        BufferBuilder bb = Tessellator.getInstance().getBuffer();
        bb.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        bb.pos(start.getX(), start.getY(), start.getZ()).color(0, 1, 0, 1F).endVertex();
        bb.pos(end.getX(), end.getY(), end.getZ()).color(0, 1, 0, 1F).endVertex();
        Tessellator.getInstance().draw();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }

    public static void drawBoundingBox(Vec3d player_pos, Vec3d posA, Vec3d posB, boolean smooth, float width) {

        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glTranslated(-player_pos.x, -player_pos.y, -player_pos.z);

        Color c = new Color(255, 0, 0, 150);
        GL11.glColor4d(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
        GL11.glLineWidth(width);
        GL11.glDepthMask(false);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

        double dx = Math.abs(posA.x - posB.x);
        double dy = Math.abs(posA.y - posB.y);
        double dz = Math.abs(posA.z - posB.z);

        //AB
        bufferBuilder.pos(posA.x, posA.y, posA.z).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();          //A
        bufferBuilder.pos(posA.x, posA.y, posA.z+dz).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();       //B
        //BC
        bufferBuilder.pos(posA.x, posA.y, posA.z+dz).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();       //B
        bufferBuilder.pos(posA.x+dx, posA.y, posA.z+dz).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();    //C
        //CD
        bufferBuilder.pos(posA.x+dx, posA.y, posA.z+dz).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();    //C
        bufferBuilder.pos(posA.x+dx, posA.y, posA.z).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();       //D
        //DA
        bufferBuilder.pos(posA.x+dx, posA.y, posA.z).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();       //D
        bufferBuilder.pos(posA.x, posA.y, posA.z).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();          //A
        //EF
        bufferBuilder.pos(posA.x, posA.y+dy, posA.z).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();       //E
        bufferBuilder.pos(posA.x, posA.y+dy, posA.z+dz).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();    //F
        //FG
        bufferBuilder.pos(posA.x, posA.y+dy, posA.z+dz).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();    //F
        bufferBuilder.pos(posA.x+dx, posA.y+dy, posA.z+dz).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex(); //G
        //GH
        bufferBuilder.pos(posA.x+dx, posA.y+dy, posA.z+dz).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex(); //G
        bufferBuilder.pos(posA.x+dx, posA.y+dy, posA.z).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();    //H
        //HE
        bufferBuilder.pos(posA.x+dx, posA.y+dy, posA.z).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();    //H
        bufferBuilder.pos(posA.x, posA.y+dy, posA.z).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();       //E
        //AE
        bufferBuilder.pos(posA.x, posA.y, posA.z).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();          //A
        bufferBuilder.pos(posA.x, posA.y+dy, posA.z).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();       //E
        //BF
        bufferBuilder.pos(posA.x, posA.y, posA.z+dz).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();       //B
        bufferBuilder.pos(posA.x, posA.y+dy, posA.z+dz).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();    //F
        //CG
        bufferBuilder.pos(posA.x+dx, posA.y, posA.z+dz).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();    //C
        bufferBuilder.pos(posA.x+dx, posA.y+dy, posA.z+dz).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex(); //G
        //DH
        bufferBuilder.pos(posA.x+dx, posA.y, posA.z).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();       //D
        bufferBuilder.pos(posA.x+dx, posA.y+dy, posA.z).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();    //H

        tessellator.draw();


        GL11.glDepthMask(true);
        GL11.glPopAttrib();
    }

    public int getDistanceSq(BlockPos p){
        NodeProcessor nodeProcessor = new WalkNodeProcessor();
        nodeProcessor.setCanEnterDoors(true);
        PathFinder pf = new PathFinder(nodeProcessor);
        float f = 128;
        BlockPos blockpos = new BlockPos(mc.player);
        int i = (int) (f + 8.0F);
        ChunkCache chunkcache = new ChunkCache(mc.world, blockpos.add(-i, -i, -i), blockpos.add(i, i, i), 0);
        EntityCow cow = new EntityCow(mc.world);
        cow.setPosition(mc.player.posX,mc.player.posY,mc.player.posZ);
        Path path = pf.findPath(chunkcache, cow, p, f);
        if (path == null){
            return Integer.MAX_VALUE;
        }
        return path.getCurrentPathLength();
    }

}
