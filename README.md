# ImotoFantasyAndroidFramework2

## 可用于打包 RPGM 版的妹相随为安卓 APK

默认使用 sdk35 打包，最低支持 sdk29（安卓 10）
gradle、maven 已配置阿里云镜像源

### 使用方法：

将游戏资源存放到 **_app/src/main/assets/_** 目录下（'www'文件夹内的文件，不包含'www'文件夹）后打包即可（注意不要替换掉 js 文件夹中的文件）

### 与第一版的区别：

不再使用外置按钮来控制存档的控制，直接修改 rpg_managers.js 中的存档操作逻辑。

#### 优点：

- 点击游戏中存档的保存按钮后，存档将直接保存到 **_Android/data/game.imotofantasy/files/save/_** 目录下，并且存档可以直接给 **基于 RPG Maker 引擎的 PC 版** 使用。

- 不需要获取任何权限，非 ROOT 用户可以直接进入 **_Android/data/game.imotofantasy/files/save/_** 目录下对存档进行导入、导出操作。

#### 缺点：

- 游戏进入时加载存档缓慢，不如第一版快，
- 存档读取和保存执行缓慢

### 额外更新：

1. 点击手机返回健时弹出提示，避免误操作导致存档丢失。
2. 手机锁屏、切换应用时暂停游戏，防止 WebView 自动刷新导致存档丢失。
3. 全屏启动 WebView、解决通知栏、底部手势提示线对游戏界面的影响，避免游戏内容被遮挡。
