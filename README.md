# 我的世界万能工具箱

万能工具箱专注于解放双手增加娱乐性的工具箱
支持版本12.2.2

## 主要功能
 - 自动攻击
 - 自动防御
 - 自动采集
 - 自动种植
 - 自动售卖

## 使用说明 
1. 使用该插件需先安装forge  
2. 下载最新的文件 复制到mod文件夹下  
```
默认设置 
H 打开自动攻击
V 打开自动采集
C 打开设置界面
```
设置界面可调
 - 攻速 (单位为tick  1tick为1/20s)
 - 攻击范围 (距离计算采用 x^2+y^2+z^2)
 - 攻击目标 (根据Entity名称攻击 拒绝误伤)
 - 是否使用盾牌 (非攻击阶段全程使用盾牌保护自己)

## 二次开发

安装gradlew  
进入到目录下执行
```
Windows: "gradlew.bat setupDecompWorkspace"
Linux/Mac OS: "./gradlew setupDecompWorkspace"
```

以下代码全部在liunx环境下执行 如windows请自行修改"gradlew"为"gradlew.bat"
```
使用 eclipse
gradlew eclipse
使用 idea
gradlew idea
gradlew genIntellijRuns
```

enjoy




## TODO
 - 拾取控制
 - 更多的设置修改