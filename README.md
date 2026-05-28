# ACR39U Card Reader

基于 Java 的 ACS ACR39U 智能卡读写器工具。
新增了一键读卡写文件、读文件写卡功能，修改读数据区域的文本显示，文本框增加输入规范限制，完全汉化界面。

经过多次测试，确认在 SLE 存储卡上正常工作，但at24c此类IIC卡片上无法正常工作。按照官方手册来说建立了api连接后直接可以通过指令读写数据，但是我的at24c卡片在读取数据时会返回错误码，提示复位未响应。不确定是我手里的这个读卡器的问题，还是底层驱动的问题，因为连官方的quickview也在我的设备上无法识别。

如果有人有解决方法，请在评论区分享。

## 功能 / Features

- **读卡器管理** -- 枚举并选择连接的 PC/SC 读卡器
- **卡片类型识别** -- 通过 ATR 解析自动识别 Mifare、SLE、AT88 等卡片类型
- **SLE 存储卡编程** -- 支持 SLE 4418/4428/5528、SLE 4432/4442/5542 存储卡的读写、验证、保护位操作
- **APDU 指令交互** -- 构造并发送 APDU 指令，查看指令日志
- **数据导入导出** -- 支持将卡片数据导出到文件或从文件导入

## 环境要求 / Requirements

- JDK 8 或其他支持smartcard API 的更高版本
- ACS ACR39U 读卡器驱动 (PC/SC)
- Windows / Linux / macOS

## 项目结构 / Project Structure

```
ACR39U-card-reader/
├── README.md
├── META-INF/
│   └── MANIFEST.MF
└── src/
    ├── Acr39.java                   # ACR39 设备功能扩展
    ├── ACS_logo.png                 # 应用图标
    ├── Apdu.java                    # APDU 指令结构与解析
    ├── CardSelector.java            # 卡片类型识别 (ATR 解析)
    ├── Helper.java                  # 字节/十六进制转换工具
    ├── MainApplet.java              # 入口 (JApplet)
    ├── MemoryCardProgramming.java   # SLE 存储卡编程主界面
    ├── PcscException.java           # PC/SC 异常及中文错误信息
    ├── PcscProvider.java            # PC/SC 常量与错误码定义
    ├── PcscReader.java              # PC/SC 读写器通信封装
    ├── ReaderEvents.java            # APDU 收发事件机制
    └── Sle.java                     # SLE 存储卡操作封装
```

## 编译与运行 / Build & Run

```bash
# 编译
javac -encoding UTF-8 src/*.java

# 运行 (GUI)
java -cp src MemoryCardProgramming

# 或打包为 JAR
jar cfm ACR39U-card-reader.jar META-INF/MANIFEST.MF -C src .
java -jar ACR39U-card-reader.jar
```

## 使用说明 / Usage

1. 插入 ACR39U 读卡器并安装驱动
2. 插入 SLE 存储卡
3. 运行程序，从下拉列表中选择读卡器和卡片类型
4. 点击 **连接 (Connect)** 建立与卡片的通信
5. 使用 **读取 (Read)** / **写入 (Write)** 按钮操作卡片数据
6. 可通过 **验证 (Verify Code)** 进行密码校验，**保护位 (Protection Bits)** 设置写保护

## 支持的卡片类型 / Supported Card Types

- SLE 4418 / SLE 4428 / SLE 5528
- SLE 4432 / SLE 4442 / SLE 5542

## 许可 / License

Advanced Card Systems Ltd.

---

[ACS 官方网站](https://www.acs.com.hk/)