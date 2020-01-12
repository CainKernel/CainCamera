# Face++ 人脸SDK 注册流程
首先你得注册一个账号:

### 1、打开控制台 -> 应用管理 -> API Key，并点击创建API Key
![点击创建API Key](https://github.com/CainKernel/CainCamera/blob/master/document/resources/facepp/registration_zh_01.png)

### 2、填写应用信息并点击创建
![填写应用信息](https://github.com/CainKernel/CainCamera/blob/master/document/resources/facepp/registration_zh_02.png)

### 3、创建成功会显示应用名称和信息
![应用信息](https://github.com/CainKernel/CainCamera/blob/master/document/resources/facepp/registration_zh_03.png)

### 4、打开控制台 -> 应用管理 -> Bundle ID，点击绑定Bundle ID
![点击绑定Bundle ID](https://github.com/CainKernel/CainCamera/blob/master/document/resources/facepp/registration_zh_04.png)

### 5、填写绑定Bundle ID的应用名称和包名
![填写绑定应用信息](https://github.com/CainKernel/CainCamera/blob/master/document/resources/facepp/registration_zh_05.png)

### 6、Bundle ID配置成功显示如下：
![Bundle ID 配置成功](https://github.com/CainKernel/CainCamera/blob/master/document/resources/facepp/registration_zh_06.png)

### 7、API Key -> 复制API Key 和 API Secret
![复制API Key 和 API Secret](https://github.com/CainKernel/CainCamera/blob/master/document/resources/facepp/registration_zh_07.png)

### 8、替换项目中的 API_KEY 和 API_SECRET
![替换API_KEY 和 API_SECRET](https://github.com/CainKernel/CainCamera/blob/master/document/resources/facepp/registration_zh_08.png)

至此，Face++ 人脸SDK注册成功，编译打包运行即可。
如果联网注册失败，请删除的API Key 和 Bundle ID，重走一遍上面的流程。
另外Face++ 使用版是有每日安装次数和设备数限制的，重复卸载安装次数过多会注册失败，需要删除API Key 和Bundle ID重走上面的步骤才能正常使用。

验证：
道具中的猫耳朵动态贴纸可以验证人脸SDK是否成功运行
![猫耳朵](https://github.com/CainKernel/CainCamera/blob/master/filterlibrary/src/main/assets/thumbs/resource/cat.png)
