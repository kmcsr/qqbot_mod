
# 中文

## 用途
- 本mod用于同步*Minecraft服务器*与*QQ群*之间的消息
- 本mod**暂不支持**同步*图片消息*与*@消息*

## 适用性
- 本mod适用于**fabric服务端**, 需装有**fabric-api**且支持**Mixin**
- 需要能够访问*腾讯QQ服务器*
- 建议服务端配置**至少2核** *(由于mirai收/发QQ消息会另起一个协程/线程, 单核可能会与服务端主线程**互相干扰**)*

## 本mod优势
- 直接集成mirai, 避免启动多个服务端, 方便游戏内配置和管理
- 提供了QQ号断线重连功能, 避免手机上手滑**切换到BotQQ号**后导致服务端QQ**断线**带来麻烦 *(mirai只提供了**网络断线**重连, 未提供QQ**下线**后重连)*

## 使用方法

### 配置
1. 获取一个bot**专用**QQ号
2. 将本mod放入**服务端文件夹**下的`mods`文件夹
3. 通过*各种途径*获取`mirai-core-all.jar`文件, 并放入`mods/libs`文件夹
4. 在**服务端文件夹**下创建`qqbot_mod`文件夹, 文件夹中创建`botconfig.json`文件, 配置内容详见[下文](#botconfigjson)
5. 配置完成后请重启服务端

### 使用
#### 游戏内
- 玩家使用`/bindqq <qq号>`绑定QQ号, 重复执行将覆盖之前的QQ号
- `/qqbot reload`重新加载配置文件, `/qqbot save`保存配置文件

#### QQ群
- 发送`!#[消息内容]`将\[*消息内容*\]同步至Minecraft服务器
- 发送`!/[指令]`在Minecraft服务器中执行`[指令]`并返回执行结果
- 发送`!:[指令]`配置QQ群Bot *(开发中)*
- 注: 以`!!`开头的消息将被Bot**忽略**

## 配置文件
> **注意:** 本mod使用**json**作为配置文件, 使用者请提前了解**json语法**
> 使用`/qqbot reload` **(推荐)**或`/realod`命令重新加载配置文件, `/qqbot save`保存配置文件

### botconfig.json
- "qqid" *Number类型*: bot的QQ号
- "qqpassword" *HexString类型*: botQQ号对应的密码, **需经过一次*md5加密***
- "groupid" *NumberList类型*: bot管理的群号, bot**需在该群中** *(不需要成为群管理, 但请**不要禁言**bot)*

### qqinfo_[qq号].json
该文件由mirai**首次登录**时**自动生成**, 记录该qq号的对应设备信息*(请勿随意泄露给他人)*. **勿动/勿删**, 否则可能需要重新登录qq

### userconfig.json
该文件保存玩家与其绑定的QQ号, **请不要随意修改本文件**  
数据模板:`"[玩家UUID]": {"qq": [玩家绑定QQ号 *Number类型*], "uuid": "[玩家UUID]"}`


# English
None
