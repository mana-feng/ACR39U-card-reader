

@SuppressWarnings("serial")
public class PcscException extends Exception
{	
	protected PcscProvider.CODES _readerResponse;
	public PcscProvider.CODES getReaderResponse(){ return this._readerResponse;}
	
	protected String _message;
	public String getMessage(){ return getErrorMessage();}
	
	public PcscException(PcscProvider.CODES errorCode)
	{
		_readerResponse = errorCode;
	}
	
	private String getErrorMessage()
    {
        switch (getReaderResponse())
        {
        case SCARD_E_CANCELLED:
            return ("操作被取消。");
        case SCARD_E_CANT_DISPOSE:
            return ("系统无法释放资源。");
        case SCARD_E_CARD_UNSUPPORTED:
            return ("卡片不满足最低支持要求。");
        case SCARD_E_DUPLICATE_READER:
            return ("读卡器识别重复，请拔除多余读卡器。");
        case SCARD_E_INSUFFICIENT_BUFFER:
            return ("数据缓冲区太小。");
        case SCARD_E_INVALID_ATR:
            return ("ATR 字符串无效。");
        case SCARD_E_INVALID_HANDLE:
            return ("提供的句柄无效。");
        case SCARD_E_INVALID_PARAMETER:
            return ("提供的参数无法正确解析。");
        case SCARD_E_INVALID_TARGET:
            return ("注册表启动信息缺失或无效。");
        case SCARD_E_INVALID_VALUE:
            return ("提供的参数值无法正确解析。");
        case SCARD_E_NOT_READY:
            return ("读卡器或卡片未就绪，无法接受指令。");
        case SCARD_E_NOT_TRANSACTED:
            return ("尝试结束一个不存在的交易。");
        case SCARD_E_NO_MEMORY:
            return ("内存不足，无法完成此指令。");
        case SCARD_E_NO_SERVICE:
            return ("读卡器未正常运行。请重新插入卡片，并重新插拔读卡器。");
        case SCARD_E_NO_SMARTCARD:
            return ("操作需要卡片，但设备中未检测到卡片。");
        case SCARD_E_PCI_TOO_SMALL:
            return ("PCI 接收缓冲区太小。");
        case SCARD_E_PROTO_MISMATCH:
            return ("请求的协议与卡片当前使用的协议不兼容。");
        case SCARD_E_READER_UNAVAILABLE:
            return ("指定的读卡器当前不可用。");
        case SCARD_E_READER_UNSUPPORTED:
            return ("读卡器驱动不满足最低支持要求。");
        case SCARD_E_SERVICE_STOPPED:
            return ("卡片资源管理器已关闭。");
        case SCARD_E_SHARING_VIOLATION:
            return ("由于存在其他连接，无法访问卡片。");
        case SCARD_E_SYSTEM_CANCELLED:
            return ("操作被系统取消，可能是由于注销或关机。");
        case SCARD_E_TIMEOUT:
            return ("操作超时。");
        case SCARD_E_UNKNOWN_CARD:
            return ("无法识别的卡片名称。");
        case SCARD_E_UNKNOWN_READER:
            return ("无法识别的读卡器名称。");
        case SCARD_E_NO_READERS_AVAILABLE:
            return ("没有找到相关的读卡器。");
        case SCARD_F_COMM_ERROR:
            return ("检测到内部通信错误。");
        case SCARD_F_INTERNAL_ERROR:
            return ("内部一致性检查失败。");
        case SCARD_F_UNKNOWN_ERROR:
            return ("检测到内部错误，来源未知。");
        case SCARD_F_WAITED_TOO_LONG:
            return ("内部一致性计时器已超时。");
        case SCARD_S_SUCCESS:
            return ("没有发生任何错误。");
        case SCARD_E_DIR_NOT_FOUND:
            return ("卡片中不存在指定的目录。");
        case SCARD_W_RESET_CARD:
            return ("卡片已被重置，共享状态信息无效。");
        case SCARD_W_UNPOWERED_CARD:
            return ("卡片没有供电，请检查卡片触点是否损坏，或者是否接触不良。");
        case SCARD_W_UNRESPONSIVE_CARD:
            return ("卡片未回复任何信息，请检查卡片接触点是否损坏，或者是否接触不良。");
        case SCARD_W_UNSUPPORTED_CARD:
            return ("由于 ATR 配置冲突，读卡器无法与卡片通信。");
        case SCARD_W_REMOVED_CARD:
            return ("卡片已被移除，请重新初始化读卡器软件并连接卡片。");
        case ACS_NFC_ERR_IO:
            return ("设备错误");
        case ACS_NFC_ERR_INVALID:
            return ("参数错误");
        case ACS_NFC_ERR_DEV_NOT_SUPP:
            return ("设备模式不支持该功能");
        case ACS_NFC_ERR_NOT_SUCH_DEV:
            return ("未找到指定设备");
        case ACS_NFC_ERR_NOT_DATA:
            return ("无法收到卡片回复");
        case ACS_NFC_ERR_TIMEOUT:
            return ("操作超时");
        case ACS_NFC_ERR_FAIL:
            return ("发生 NFC 错误");
        case ACS_NFC_ERR_RF_TRANS:
            return ("收到的数据不符合 ISO18092 标准");
        case ACS_NFC_ERR_SOFT:
            return ("未知错误发生");
        default:
            return ("未知错误发生");
    
        }
    }
	
	public PcscException()
	{
		super();
	}
	
	public PcscException(String message)
	{
		super(message);
	}
	
	public PcscException(String message, byte[] readerResponse)
	{
		super(message + "\nResponse : " + Helper.byteAsString(readerResponse, true));
	}
 }
