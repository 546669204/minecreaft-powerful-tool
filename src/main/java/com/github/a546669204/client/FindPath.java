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
import net.minecraftforge.fml.common.eventhandler.EventPriority;
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
    public static boolean turnType = true;
    public static Path path = null;

    public FindPath(){
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void renderWorldLastEvent(RenderWorldLastEvent event)
    {
        drawPath();
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void clientTick(TickEvent.ClientTickEvent event)
    {
        if(!event.phase.equals(TickEvent.Phase.START)){
            return ;
        }
        if (mc.world == null){
            return  ;
        }

        if (this.path == null) {
            return ;
        }

        if(turnType){
            trun();
            travel();
        }else{
            ignoreTravel();
        }
    }
    // 转向
    private void trun(){
        double d0 = this.path.getCurrentPos().x+0.5 - mc.player.posX;
        double d1 = this.path.getCurrentPos().y -(mc.player.posY +mc.player.getEyeHeight());//+  mc.player.getEyeHeight()
        double d2 = this.path.getCurrentPos().z+0.5 - mc.player.posZ;
        double d3 = (double) MathHelper.sqrt(d0 * d0 + d2 * d2);
        float f = (float)(MathHelper.atan2(d2, d0) * (180D / Math.PI)) - 90.0F;
        float f1 = (float)(-(MathHelper.atan2(d1, d3) * (180D / Math.PI)));
        mc.player.turn(new Float((-mc.player.rotationYaw + f)/0.15),new Float((mc.player.rotationPitch - f1)/0.15));
    }
    //行走
    private void travel(){
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
    //无视当前转向行走
    private void ignoreTravel(){
//        double diffX = this.path.getCurrentPos().x+0.5 - mc.player.posX;
//        double diffZ = this.path.getCurrentPos().z+0.5 - mc.player.posZ;
//        double f3 =  (double)MathHelper.sqrt(diffX * diffX + diffZ * diffZ);
        if(mc.player.getDistanceSq(new BlockPos(this.path.getCurrentPos())) <= 1){
            if(this.path.getCurrentPathIndex() < this.path.getCurrentPathLength() -1){
                this.path.incrementPathIndex();
            }else {
                this.path = null;
            }
        }else{

            BlockPos targetBP = new BlockPos(this.path.getCurrentPos());
            double d0 = targetBP.getX() - mc.player.posX;
            double d2 = targetBP.getZ() - mc.player.posZ;

            float f = (float)(MathHelper.atan2(d2, d0) * (180D / Math.PI)) - 90.0F;

            float angle = -f+mc.player.rotationYaw;


            float strafeFlag = 1;
            float forwardFlag = 1;

            if( angle >= 180){
                angle = angle - 360;
            }
            if( angle <= -180){
                angle = angle + 360;
            }

            mc.player.travel(MathHelper.sin(angle*  0.017453292F)*strafeFlag,0F,MathHelper.cos(angle * 0.017453292F)*forwardFlag);
        }
    }
    //创建路径
    public static Boolean createPath(BlockPos target,int targetType,boolean skip,boolean turn){
        turnType = turn;
        if (skip){
            if(targetType == 1){
                PathPoint[] pathpoints = {new PathPoint(target.up().getX(),target.up().getY(),target.up().getZ())};
                path = new Path(pathpoints);
            }else{
                PathPoint[] pathpoints = {new PathPoint(target.getX(),target.getY(),target.getZ())};
                path = new Path(pathpoints);
            }
        }else{
            NodeProcessor nodeProcessor = new WalkNodeProcessor();
            PathFinder pf = new PathFinder(nodeProcessor);
            float f = 128;
            BlockPos blockpos = new BlockPos(mc.player);
            int i = (int) (f + 8.0F);
            ChunkCache chunkcache = new ChunkCache(mc.world, blockpos.add(-i, -i, -i), blockpos.add(i, i, i), 0);
            EntityRabbit cow = new EntityRabbit(mc.world);
            cow.setPosition(mc.player.posX,mc.player.posY,mc.player.posZ);
            if(targetType == 1){
                path = pf.findPath(chunkcache, cow, target.up(), f);
            }else{
                path = pf.findPath(chunkcache, cow, target, f);
            }
        }
        if(path == null){
            return false;
        }
        return true;
    }
    public static void removePath(){
        path = null;
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
}
