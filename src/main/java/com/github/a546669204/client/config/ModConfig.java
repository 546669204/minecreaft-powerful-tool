package com.github.a546669204.client.config;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ModConfig {

    public ModConfig(){
        load();
    }
    public static final AutoAttack autoAttack = new AutoAttack();
    public static final PickUp pickUp = new PickUp();
    public static final AutoCollection autoCollection = new AutoCollection();

    public static class AutoAttack {

        //攻击范围
        public double range = 15.0D;
        //攻击间隔
        public double time = 3D;
        //是否自动使用盾牌
        public Boolean useShiled = false;
        //目标Map
        public Map<String,Boolean> target =new HashMap<String, Boolean>();
        //指向目标
        public Boolean turnTarget = false;
        //攻击模式 0 默认 1 不动如山 2 自动追怪
        public int type = 0;

    }

    public  static class PickUp{
        public Boolean notWhite = false;

    }

    public static class AutoCollection{
        public double collectionTime = 5D;//采集间隔
        public double plantingTime = 5D;//种植间隔
        public boolean autoSupplement = false;//自动补充
        public boolean autoSell = true;//自动出售

    }


    public static void load(){
        try{
            Properties pro = new Properties();
            File file  = new File("powerTool.properties");
            if(!file.exists()){
                file.createNewFile();
            }
            FileInputStream in = new FileInputStream(file);
            pro.load(in);

            autoAttack.time = new Double(pro.getProperty("time","3"));
            autoAttack.range = new Double(pro.getProperty("range","15"));

            autoAttack.useShiled = new Boolean(pro.getProperty("useShiled","false"));

            pickUp.notWhite = new Boolean(pro.getProperty("pickUp.notWhite","false"));
            autoAttack.turnTarget = new Boolean(pro.getProperty("autoAttack.turnTarget","false"));

            autoAttack.type = new Integer(pro.getProperty("autoAttack.type","0"));

            System.out.println(pro.getProperty("target",""));
            String[] arr = pro.getProperty("target","").split("\\|\\|\\|");
            for (int i = 0; i < arr.length; i++) {
                autoAttack.target.put(arr[i],true);
            }
            in.close();
        }catch(Exception e){
            e.printStackTrace();
        }

    }
    public  static  void store(){
        try{
            File file  = new File("powerTool.properties");
            if(!file.exists()){
                file.createNewFile();
            }
            FileOutputStream oFile = new FileOutputStream(file);
            Properties pro = new Properties();

            pro.setProperty("time",""+ autoAttack.time);
            pro.setProperty("range",""+ autoAttack.range);

            pro.setProperty("useShiled",autoAttack.useShiled.toString());
            pro.setProperty("pickUp.notWhite",pickUp.notWhite.toString());
            pro.setProperty("autoAttack.turnTarget",autoAttack.turnTarget.toString());
            pro.setProperty("autoAttack.type","" + autoAttack.type);


            pro.setProperty("target", StringUtils.join((String[])autoAttack.target.keySet().toArray(new String[0]),"|||"));

            pro.store(oFile,"xiaoming");
            oFile.close();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

}