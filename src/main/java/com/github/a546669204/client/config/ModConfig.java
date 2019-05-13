package com.github.a546669204.client.config;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;

public class ModConfig {

    public ModConfig(){
        load();
        Global = this;
    }
    public static final AutoAttack autoAttack = new AutoAttack();
    public static final PickUp pickUp = new PickUp();
    public static final AutoCollection autoCollection = new AutoCollection();
    public static final AutoLogin autoLogin = new AutoLogin();
    public static Object Global = null;


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

    public static class AutoLogin{
        public double time = 30*60*1000L;//检测间隔
        public String loginCmd = "/l 123456";//登陆命令
        public String ip = "127.0.0.1";//服务器ip
        public int port = 65525;//服务器端口
    }


    public void load(){
        try{
            Properties pro = new Properties();
            File file  = new File("powerTool.properties");
            if(!file.exists()){
                file.createNewFile();
            }
            FileInputStream in = new FileInputStream(file);
            pro.load(in);

            Set<String>  proset = pro.stringPropertyNames();
            for(String proName:proset){
                System.out.println(proName);
                String proValue = pro.getProperty(proName);
                if(proValue != null){
                    Object that = this;
                    Field field = null;
                    for(String v:proName.split("\\.")){
                        field = that.getClass().getField(v);
                        if(!StringUtils.endsWith(proName,v)){
                            that = field.get(that);
                        }

                    }
                    Class type = field.getType();
                    if(type.isAssignableFrom(String.class)){
                        field.set(that,proValue);
                    }else if(type.isAssignableFrom(Number.class)){
                        if(type == Integer.class){
                            field.set(that,new Integer(proValue) );
                        }
                        if(type == Double.class){
                            field.set(that,new Double(proValue) );
                        }
                        if(type == Long.class){
                            field.set(that,new Long(proValue) );
                        }
                        if(type == Float.class){
                            field.set(that,new Float(proValue) );
                        }
                    }else if(type.isAssignableFrom(Boolean.class)){
                        field.set(that,new Boolean(proValue) );
                    }else if(type.isAssignableFrom(Map.class)){
                        String[] arr = proValue.split("\\|\\|\\|");
                        Map hashmap = new HashMap();
                        for (int i = 0; i < arr.length; i++) {
                            hashmap.put(arr[i],true);
                        }
                        field.set(that,hashmap);
                    }
                }
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

            diguiWrite(ModConfig.class,"",Global,pro);

            pro.store(oFile,"xiaoming");
            oFile.close();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public static void diguiWrite(Class c,String parent,Object that,Properties pro) {
        try {
            Field[] fields = c.getFields();
            for (Field field : fields) {
                Class type = field.getType();
                if(type.isPrimitive()){
                    pro.setProperty(parent + field.getName(),""+ field.get(that));
                }else if(type.isAssignableFrom(String.class)){
                    pro.setProperty(parent + field.getName(),""+ field.get(that));
                }else if(type.isAssignableFrom(Number.class)){
                    pro.setProperty(parent + field.getName(),""+ field.get(that));
                }else if(type.isAssignableFrom(Boolean.class)){
                    pro.setProperty(parent + field.getName(),field.get(that).toString());
                }else if(type.isAssignableFrom(Map.class)){
                    HashMap hashmap = (HashMap)field.get(that);
                    pro.setProperty(parent + field.getName(),StringUtils.join((String[])hashmap.keySet().toArray(new String[0]),"|||"));
                }else{
                    diguiWrite(type,field.getName()+".",field.get(that),pro);
                }
            }
        }catch (Exception e){

        }
    };


}