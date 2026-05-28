
import javax.smartcardio.CardException;

/*=========================================================================================
'   Company         :   Advanced Card Systems Ltd.
'
'   Author          :   Arturo B. Salvamante Jr.
'
'   Module          :   ACSModule.java
'
'   Date            :   March 19, 2008
'
'   Revision Trail  :   Date / Author / Description
						09-10-2008 / M.J.E.C.Castillo / added IOCTL_SMARTCARD_ENABLE_PIN_VERIFICATION,
						IOCTL_SMARTCARD_ENABLE_PIN_MODIFICATION,
						IOCTL_SMARTCARD_DISABLE_SECURE_PIN_ENTRY,
 						IOCTL_SMARTCARD_GET_FIRMWARE_VERSION,
						IOCTL_SMARTCARD_DISPLAY_LCD_MESSAGE, and
						IOCTL_SMARTCARD_READ_KEY for ACR83
'						04-19-2010 / M.J.E.Castillo / Added Error codes
'
'=========================================================================================*/

public class PcscProvider {
	
        public static final int SCARD_S_SUCCESS = 0;
        public static final int SCARD_ATR_LENGTH = 33;      
        
        ///Memory Card Type
        public static final int CT_MCU = 0x00;                   // MCU
        public static final int CT_IIC_Auto = 0x01;               // IIC (Auto Detect Memory Size)
        public static final int CT_IIC_1K = 0x02;                 // IIC (1K)
        public static final int CT_IIC_2K = 0x03;                 // IIC (2K)
        public static final int CT_IIC_4K = 0x04;                 // IIC (4K)
        public static final int CT_IIC_8K = 0x05;                 // IIC (8K)
        public static final int CT_IIC_16K = 0x06;                // IIC (16K)
        public static final int CT_IIC_32K = 0x07;                // IIC (32K)
        public static final int CT_IIC_64K = 0x08;                // IIC (64K)
        public static final int CT_IIC_128K = 0x09;               // IIC (128K)
        public static final int CT_IIC_256K = 0x0A;               // IIC (256K)
        public static final int CT_IIC_512K = 0x0B;               // IIC (512K)
        public static final int CT_IIC_1024K = 0x0C;              // IIC (1024K)
        public static final int CT_AT88SC153 = 0x0D;              // AT88SC153
        public static final int CT_AT88SC1608 = 0x0E;             // AT88SC1608
        public static final int CT_SLE4418 = 0x0F;                // SLE4418
        public static final int CT_SLE4428 = 0x10;                // SLE4428
        public static final int CT_SLE4432 = 0x11;                // SLE4432
        public static final int CT_SLE4442 = 0x12;                // SLE4442
        public static final int CT_SLE4406 = 0x13;                // SLE4406
        public static final int CT_SLE4436 = 0x14;                // SLE4436
        public static final int CT_SLE5536 = 0x15;                // SLE5536
        public static final int CT_MCUT0 = 0x16;                  // MCU T=0
        public static final int CT_MCUT1 = 0x17;                  // MCU T=1
        public static final int CT_MCU_Auto = 0x18;               // MCU Autodetect
        
        
        //==========================CONTEXT SCOPE================================================

        /// <summary>
        /// The context is a user context, and any database operations 
        /// are performed within the domain of the user.
        /// </summary>
        public static final int SCARD_SCOPE_USER = 0;
        
        /// <summary>
        /// The context is that of the current terminal, and any database 
        /// operations are performed within the domain of that terminal.  
        /// (The calling application must have appropriate access permissions 
        /// for any database actions.)
        /// </summary>
        public static final int SCARD_SCOPE_TERMINAL = 1;

        /// <summary>
        /// The context is the system context, and any database operations 
        /// are performed within the domain of the system.  (The calling
        /// application must have appropriate access permissions for any 
        /// database actions.)
        /// </summary>
        public static final int SCARD_SCOPE_SYSTEM = 2;

        /// <summary>
        /// The application is unaware of the current state, and would like 
        /// to know. The use of this value results in an immediate return
        /// from state transition monitoring services. This is represented
        /// by all bits set to zero.
        /// </summary>
        public static final int SCARD_STATE_UNAWARE = 0x00;        

        /// <summary>
        /// The application requested that this reader be ignored. No other
        /// bits will be set.
        /// </summary>
        public static final int SCARD_STATE_IGNORE = 0x01;
        
        /// <summary>
        /// This implies that there is a difference between the state 
        /// believed by the application, and the state known by the Service
        /// Manager.When this bit is set, the application may assume a
        /// significant state change has occurred on this reader.
        /// </summary>
        public static final int SCARD_STATE_CHANGED = 0x02;        

        /// <summary>
        /// This implies that the given reader name is not recognized by
        /// the Service Manager. If this bit is set, then SCARD_STATE_CHANGED
        /// and SCARD_STATE_IGNORE will also be set.
        /// </summary>
        public static final int SCARD_STATE_UNKNOWN = 0x04;
        
        /// <summary>
        /// This implies that the actual state of this reader is not
        /// available. If this bit is set, then all the following bits are
        /// clear.
        /// </summary>
        public static final int SCARD_STATE_UNAVAILABLE = 0x08;
        
        /// <summary>
        /// This implies that there is not card in the reader.  If this bit
        /// is set, all the following bits will be clear.
        /// </summary>
        public static final int SCARD_STATE_EMPTY = 0x10;
        
        /// <summary>
        /// This implies that there is a card in the reader. 
        /// </summary>
        public static final int SCARD_STATE_PRESENT = 0x20;
        
        /// <summary>
        /// This implies that there is a card in the reader with an ATR
        /// matching one of the target cards. If this bit is set,
        /// SCARD_STATE_PRESENT will also be set.  This bit is only returned
        /// on the SCardLocateCard() service.
        /// </summary>
        public static final int SCARD_STATE_ATRMATCH = 0x40;
        
        /// <summary>
        /// This implies that the card in the reader is allocated for 
        /// exclusive use by another application. If this bit is set,
        /// SCARD_STATE_PRESENT will also be set.
        /// </summary>
        public static final int SCARD_STATE_EXCLUSIVE = 0x80;
        
        /// <summary>
        /// This implies that the card in the reader is in use by one or 
        /// more other applications, but may be connected to in shared mode. 
        /// If this bit is set, SCARD_STATE_PRESENT will also be set.
        /// </summary>
        public static final int SCARD_STATE_INUSE = 0x100;
        
        /// <summary>
        /// This implies that the card in the reader is unresponsive or not
        /// supported by the reader or software.
        /// </summary>
        public static final int SCARD_STATE_MUTE = 0x200;
        
        /// <summary>
        /// This implies that the card in the reader has not been powered up. 
        /// </summary>
        public static final int SCARD_STATE_UNPOWERED = 0x400;
        
        /// <summary>
        /// This application is not willing to share this card with other 
        /// applications.
        /// </summary>
        public static final int SCARD_SHARE_EXCLUSIVE = 1;
        
        /// <summary>
        /// This application is willing to share this card with other 
        /// applications.
        /// </summary>
        public static final int SCARD_SHARE_SHARED = 2;
        
        /// <summary>
        /// This application demands direct control of the reader, so it 
        /// is not available to other applications.
        /// </summary>
        public static final int SCARD_SHARE_DIRECT = 3;

        
        //================================Disposition=============================================        

        /// <summary>
        /// Don't do anything special on close
        /// </summary>
        public static final int SCARD_LEAVE_CARD = 0;

        /// <summary>
        /// Reset the card on close
        /// </summary>
        public static final int SCARD_RESET_CARD = 1;

        /// <summary>
        /// Power down the card on close
        /// </summary>
        public static final int SCARD_UNPOWER_CARD = 2;

        /// <summary>
        /// Eject the card on close
        /// </summary>
        public static final int SCARD_EJECT_CARD = 3;
        
        
        //=============================ACS IOCTL Class===========================================   
        public static final int FILE_DEVICE_SMARTCARD = 0x310000; // Reader action IOCTLs
        public static final int IOCTL_SMARTCARD_DIRECT = FILE_DEVICE_SMARTCARD + 2050 * 4;
        public static final int IOCTL_SMARTCARD_SELECT_SLOT = FILE_DEVICE_SMARTCARD + 2051 * 4;
        public static final int IOCTL_SMARTCARD_DRAW_LCDBMP = FILE_DEVICE_SMARTCARD + 2052 * 4;
        public static final int IOCTL_SMARTCARD_DISPLAY_LCD = FILE_DEVICE_SMARTCARD + 2053 * 4;
        public static final int IOCTL_SMARTCARD_CLR_LCD = FILE_DEVICE_SMARTCARD + 2054 * 4;
        public static final int IOCTL_SMARTCARD_READ_KEYPAD = FILE_DEVICE_SMARTCARD + 2055 * 4;
        public static final int IOCTL_SMARTCARD_READ_RTC = FILE_DEVICE_SMARTCARD + 2057 * 4;
        public static final int IOCTL_SMARTCARD_SET_RTC = FILE_DEVICE_SMARTCARD + 2058 * 4;
        public static final int IOCTL_SMARTCARD_SET_OPTION = FILE_DEVICE_SMARTCARD + 2059 * 4;
        public static final int IOCTL_SMARTCARD_SET_LED = FILE_DEVICE_SMARTCARD + 2060 * 4;
        public static final int IOCTL_SMARTCARD_LOAD_KEY = FILE_DEVICE_SMARTCARD + 2062 * 4;
        public static final int IOCTL_SMARTCARD_READ_EEPROM = FILE_DEVICE_SMARTCARD + 2065 * 4;
        public static final int IOCTL_SMARTCARD_WRITE_EEPROM = FILE_DEVICE_SMARTCARD + 2066 * 4;
        public static final int IOCTL_SMARTCARD_GET_VERSION = FILE_DEVICE_SMARTCARD + 2067 * 4;
        public static final int IOCTL_SMARTCARD_GET_READER_INFO = FILE_DEVICE_SMARTCARD + 2051 * 4;
        public static final int IOCTL_SMARTCARD_SET_CARD_TYPE = FILE_DEVICE_SMARTCARD + 2060 * 4;
        public static final int IOCTL_SMARTCARD_ACR128_ESCAPE_COMMAND = FILE_DEVICE_SMARTCARD + 2079 * 4;
        
        public static final int IOCTL_SMARTCARD_ENABLE_PIN_VERIFICATION = FILE_DEVICE_SMARTCARD + 2075 * 4;
        public static final int IOCTL_SMARTCARD_ENABLE_PIN_MODIFICATION = FILE_DEVICE_SMARTCARD + 2076 * 4;
        public static final int IOCTL_SMARTCARD_DISABLE_SECURE_PIN_ENTRY = FILE_DEVICE_SMARTCARD + 2077 * 4;
        public static final int IOCTL_SMARTCARD_GET_FIRMWARE_VERSION = FILE_DEVICE_SMARTCARD + 2078 * 4;
        public static final int IOCTL_SMARTCARD_DISPLAY_LCD_MESSAGE = FILE_DEVICE_SMARTCARD + 2079 * 4;
        public static final int IOCTL_SMARTCARD_READ_KEY = FILE_DEVICE_SMARTCARD + 2080 * 4;
        public static final int CM_IOCTL_GET_FEATURE_REQUEST = FILE_DEVICE_SMARTCARD + 3400 * 4;
        
        //===================================Error Codes=========================================        
        public static final int SCARD_F_INTERNAL_ERROR = -2146435071;
        public static final int SCARD_E_CANCELLED = -2146435070;
        public static final int SCARD_E_INVALID_HANDLE = -2146435069;
        public static final int SCARD_E_INVALID_PARAMETER = -2146435068;
        public static final int SCARD_E_INVALID_TARGET = -2146435067;
        public static final int SCARD_E_NO_MEMORY = -2146435066;
        public static final int SCARD_F_WAITED_TOO_LONG = -2146435065;
        public static final int SCARD_E_INSUFFICIENT_BUFFER = -2146435064;
        public static final int SCARD_E_UNKNOWN_READER = -2146435063;
        public static final int SCARD_E_NO_READERS_AVAILABLE = -2146435026;


        public static final int SCARD_E_TIMEOUT = -2146435062;
        public static final int SCARD_E_SHARING_VIOLATION = -2146435061;
        public static final int SCARD_E_NO_SMARTCARD = -2146435060;
        public static final int SCARD_E_UNKNOWN_CARD = -2146435059;
        public static final int SCARD_E_CANT_DISPOSE = -2146435058;
        public static final int SCARD_E_PROTO_MISMATCH = -2146435057;


        public static final int SCARD_E_NOT_READY = -2146435056;
        public static final int SCARD_E_INVALID_VALUE = -2146435055;
        public static final int SCARD_E_SYSTEM_CANCELLED = -2146435054;
        public static final int SCARD_F_COMM_ERROR = -2146435053;
        public static final int SCARD_F_UNKNOWN_ERROR = -2146435052;
        public static final int SCARD_E_INVALID_ATR = -2146435051;
        public static final int SCARD_E_NOT_TRANSACTED = -2146435050;
        public static final int SCARD_E_READER_UNAVAILABLE = -2146435049;
        public static final int SCARD_P_SHUTDOWN = -2146435048;
        public static final int SCARD_E_PCI_TOO_SMALL = -2146435047;

        public static final int SCARD_E_READER_UNSUPPORTED = -2146435046;
        public static final int SCARD_E_DUPLICATE_READER = -2146435045;
        public static final int SCARD_E_CARD_UNSUPPORTED = -2146435044;
        public static final int SCARD_E_NO_SERVICE = -2146435043;
        public static final int SCARD_E_SERVICE_STOPPED = -2146435042;

        public static final int SCARD_W_UNSUPPORTED_CARD = -2146435041;
        public static final int SCARD_W_UNRESPONSIVE_CARD = -2146435040;
        public static final int SCARD_W_UNPOWERED_CARD = -2146435039;
        public static final int SCARD_W_RESET_CARD = -2146435038;

        //From SCARD_W_REMOVED_CARD to SCARD_E_DIR_NOT_FOUND
        public static final int SCARD_E_DIR_NOT_FOUND = -2146435037;

        public static final int SCARD_W_REMOVED_CARD = -2146434967;
        
        public static final int SCARD_E_UNEXPECTED		= -2146435041; //  An unexpected card error has occurred.
        public static final int SCARD_E_ICC_INSTALLATION		= -2146435040; //  No Primary Provider can be found for the smart card.
        public static final int SCARD_E_ICC_CREATEORDER		= -2146435039; //  The requested order of object creation is not supported.
        public static final int SCARD_E_UNSUPPORTED_FEATURE	= -2146435038; //  This smart card does not support the requested feature.
        public static final int SCARD_E_FILE_NOT_FOUND		= -2146435036; //  The identified file does not exist in the smart card.
        public static final int SCARD_E_NO_DIR			= -2146435035; //  The supplied path does not represent a smart card directory.
        public static final int SCARD_E_NO_FILE			= -2146435034; //  The supplied path does not represent a smart card file.
        public static final int SCARD_E_NO_ACCESS			= -2146435033; //  Access is denied to this file.
        public static final int SCARD_E_WRITE_TOO_MANY		= -2146435032; //  The smartcard does not have enough memory to store the information.
        public static final int SCARD_E_BAD_SEEK			= -2146435031; //  There was an error trying to set the smart card file object pointer.
        public static final int SCARD_E_INVALID_CHV		= -2146435030; //  The supplied PIN is incorrect.
        public static final int SCARD_E_UNKNOWN_RES_MNG		= -2146435029; //  An unrecognized error code was returned from a layered component.
        public static final int SCARD_E_NO_SUCH_CERTIFICATE	= -2146435028; //  The requested certificate does not exist.
        public static final int SCARD_E_CERTIFICATE_UNAVAILABLE	= -2146435027; //  The requested certificate could not be obtained.
        public static final int SCARD_E_COMM_DATA_LOST		= -2146435025; //  A communications error with the smart card has been detected.  Retry the operation.
        public static final int SCARD_E_NO_KEY_CONTAINER		= -2146435024; //  The requested key container does not exist on the smart card.
        public static final int SCARD_E_SERVER_TOO_BUSY		= -2146435023; //  The Smart card resource manager is too busy to complete this operation.
        // These are warning codes.
        public static final int SCARD_W_SECURITY_VIOLATION	= -2146434966; //  Access was denied because of a security violation.
        public static final int SCARD_W_WRONG_CHV			= -2146434965; //  The card cannot be accessed because the wrong PIN was presented.
        public static final int SCARD_W_CHV_BLOCKED		= -2146434964; //  The card cannot be accessed because the maximum number of PIN entry attempts has been reached.
        public static final int SCARD_W_EOF			= -2146434963; //  The end of the smart card file has been reached.
        public static final int SCARD_W_CANCELLED_BY_USER		= -2146434962; //  The action was cancelled by the user.
        public static final int SCARD_W_CARD_NOT_AUTHENTICATED	= -2146434961; //  No PIN was presented to the smart card.
        public static final int SCARD_W_CACHE_ITEM_NOT_FOUND	= -2146434960; //  The requested item could not be found in the cache.
        public static final int SCARD_W_CACHE_ITEM_STALE		= -2146434959; //  The requested cache item is too old and was deleted from the cache.
        public static final int SCARD_W_CACHE_ITEM_TOO_BIG	= -2146434958; //  The new cache item exceeds the maximum per-item size defined for the cache.
        
        
        //==============================Protocol=============================================        
        /// <summary>
        /// There is no active protocol.
        /// </summary>
        public static final int SCARD_PROTOCOL_UNDEFINED = 0x00;

        /// <summary>
        /// T=0 is the active protocol.
        /// </summary>
        public static final int SCARD_PROTOCOL_T0 = 0x01;                

        /// <summary>
        /// T=1 is the active protocol.
        /// </summary>
        public static final int SCARD_PROTOCOL_T1 = 0x02;                

        /// <summary>
        /// Raw is the active protocol.
        /// </summary>
        public static final int SCARD_PROTOCOL_RAW = 0x10000;
        
        
        //================================Reader State=========================================
        /// <summary>
        /// This value implies the driver is unaware of the current 
        /// state of the reader.
        /// </summary>
        public static final int SCARD_UNKNOWN = 0;
        
        /// <summary>
        /// This value implies there is no card in the reader.
        /// </summary>
        public static final int SCARD_ABSENT = 1;
        
        /// <summary>
        /// This value implies there is a card is present in the reader, 
        /// but that it has not been moved into position for use.        
        /// </summary>
        public static final int SCARD_PRESENT = 2;
        
        /// <summary>
        /// This value implies there is a card in the reader in position 
        /// for use.  The card is not powered.
        /// </summary>
        public static final int SCARD_SWALLOWED = 3;
        
        /// <summary>
        /// This value implies there is power is being provided to the card, 
        /// but the Reader Driver is unaware of the mode of the card.
        /// </summary>
        public static final int SCARD_POWERED = 4;
        
        /// <summary>
        /// This value implies the card has been reset and is awaiting 
        /// PTS negotiation.
        /// </summary>
        public static final int SCARD_NEGOTIABLE = 5;
        
        /// <summary>
        /// This value implies the card has been reset and specific 
        /// communication protocols have been established.
        /// </summary>
        public static final int SCARD_SPECIFIC = 6;        
        
        
        //===========================================Miscellaneous========================================
        public enum CODES
    	{
    		// Error Codes
    		SCARD_F_INTERNAL_ERROR("SCARD_F_INTERNAL_ERROR"),
    	    SCARD_E_CANCELLED("SCARD_E_CANCELLED"),
    	    SCARD_E_INVALID_HANDLE("SCARD_E_INVALID_HANDLE"),
    	    SCARD_E_INVALID_PARAMETER("SCARD_E_INVALID_PARAMETER"),
    	    SCARD_E_INVALID_TARGET("SCARD_E_INVALID_TARGET7"),
    	    SCARD_E_NO_MEMORY("SCARD_E_NO_MEMORY"),
    	    SCARD_F_WAITED_TOO_LONG("SCARD_F_WAITED_TOO_String"),
    	    SCARD_E_INSUFFICIENT_BUFFER("SCARD_E_INSUFFICIENT_BUFFER"),
    	    SCARD_E_UNKNOWN_READER("SCARD_E_UNKNOWN_READER"),
    	    SCARD_E_NO_READERS_AVAILABLE("SCARD_E_NO_READERS_AVAILABLE"),

    	    SCARD_E_TIMEOUT("SCARD_E_TIMEOUT"),
    	    SCARD_E_SHARING_VIOLATION("SCARD_E_SHARING_VIOLATION"),
    	    SCARD_E_NO_SMARTCARD("SCARD_E_NO_SMARTCARD"),
    	    SCARD_E_UNKNOWN_CARD("SCARD_E_UNKNOWN_CARD"),
    	    SCARD_E_CANT_DISPOSE("SCARD_E_CANT_DISPOSE"),
    	    SCARD_E_PROTO_MISMATCH("SCARD_E_PROTO_MISMATCH"),


    	    SCARD_E_NOT_READY("SCARD_E_NOT_READY"),
    	    SCARD_E_INVALID_VALUE("SCARD_E_INVALID_VALUE"),
    	    SCARD_E_SYSTEM_CANCELLED("SCARD_E_SYSTEM_CANCELLED"),
    	    SCARD_F_COMM_ERROR("SCARD_F_COMM_ERROR"),
    	    SCARD_F_UNKNOWN_ERROR("SCARD_F_UNKNOWN_ERROR"),
    	    SCARD_E_INVALID_ATR("SCARD_E_INVALID_ATR"),
    	    SCARD_E_NOT_TRANSACTED("SCARD_E_NOT_TRANSACTED"),
    	    SCARD_E_READER_UNAVAILABLE("SCARD_E_READER_UNAVAILABLE"),
    	    SCARD_P_SHUTDOWN("SCARD_P_SHUTDOWN8"),
    	    SCARD_E_PCI_TOO_SMALL("SCARD_E_PCI_TOO_SMALL"),

    	    SCARD_E_READER_UNSUPPORTED("SCARD_E_READER_UNSUPPORTED"),
    	    SCARD_E_DUPLICATE_READER("SCARD_E_DUPLICATE_READER"),
    	    SCARD_E_CARD_UNSUPPORTED("SCARD_E_CARD_UNSUPPORTED"),
    	    SCARD_E_NO_SERVICE("SCARD_E_NO_SERVICE"),
    	    SCARD_E_SERVICE_STOPPED("SCARD_E_SERVICE_STOPPED"),

    	    SCARD_W_UNSUPPORTED_CARD("SCARD_W_UNSUPPORTED_CARD"),
    	    SCARD_W_UNRESPONSIVE_CARD("SCARD_W_UNRESPONSIVE_CARD"),
    	    SCARD_W_UNPOWERED_CARD("SCARD_W_UNPOWERED_CARD"),
    	    SCARD_W_RESET_CARD("SCARD_W_RESET_CARD"),
    	    
    		// From SCARD_W_REMOVED_CARD to SCARD_E_DIR_NOT_FOUND
    	    SCARD_E_DIR_NOT_FOUND("SCARD_E_DIR_NOT_FOUND"),
    	    SCARD_W_REMOVED_CARD("SCARD_W_REMOVED_CARD"),

    	    // NFC Library
    	    ACS_NFC_ERR_IO("ACS_NFC_ERR_IO"),
    	    ACS_NFC_ERR_INVALID("ACS_NFC_ERR_INVALID"),
    	    ACS_NFC_ERR_DEV_NOT_SUPP("ACS_NFC_ERR_DEV_NOT_SUPP"),
    	    ACS_NFC_ERR_NOT_SUCH_DEV("ACS_NFC_ERR_NOT_SUCH_DEV"),
    	    ACS_NFC_ERR_NOT_DATA("ACS_NFC_ERR_NOT_DATA"),
    	    ACS_NFC_ERR_TIMEOUT("ACS_NFC_ERR_TIMEOUT"),
    	    ACS_NFC_ERR_FAIL("ACS_NFC_ERR_FAIL"),
    	    ACS_NFC_ERR_RF_TRANS("ACS_NFC_ERR_RF_TRANS"),
    	    ACS_NFC_ERR_SOFT("ACS_NFC_ERR_SOFT"),
    	    
    	    SCARD_S_SUCCESS("SCARD_S_SUCCESS");
    		
    	    private String _id;
    	    CODES(String id) {this._id = id;}
    	    public String getErrorCode() {return _id;}
    	    public static CODES getKeyNameErrorCode(String iErrorCode)
    	    {
    	    	for (CODES ErrorCode : CODES.values()) {
    	            if (ErrorCode._id == iErrorCode) return ErrorCode;
    	        }
    	    	return SCARD_S_SUCCESS;
    	    }
    	}
        
        public static String getScardErrorMessage(CardException ex)
        {
        	switch (CODES.getKeyNameErrorCode(ex.getCause().getMessage()))
            {
                case SCARD_E_CANCELLED:
                    return ("操作被取消。");
                case SCARD_E_CANT_DISPOSE:
                    return ("系统无法释放资源。");
                case SCARD_E_CARD_UNSUPPORTED:
                    return ("卡片不满足最低支持要求。");
                case SCARD_E_DUPLICATE_READER:
                    return ("读卡器驱动未能产生唯一的读卡器名称。");
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
                    return ("读卡器资源管理器未运行。");
                case SCARD_E_NO_SMARTCARD:
                    return ("操作需要卡片，但设备中未检测到卡片。");
                case SCARD_E_PCI_TOO_SMALL:
                    return ("PCI 接收缓冲区太小。");
                case SCARD_E_PROTO_MISMATCH:
                    return ("请求的协议与卡片当前使用的协议不兼容。");
                case SCARD_E_READER_UNAVAILABLE:
                    return ("未找到读卡器。");
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
                    return ("未找到读卡器。");
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
                    return ("卡片未供电，无法继续通信。");
                case SCARD_W_UNRESPONSIVE_CARD:
                    return ("卡片未响应复位指令。");
                case SCARD_W_UNSUPPORTED_CARD:
                    return ("由于 ATR 配置冲突，读卡器无法与卡片通信。");
                case SCARD_W_REMOVED_CARD:
                    return ("卡片已被移除，无法继续通信。");
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
                    return ("未知错误");
                default:
                    return ("(" + ex.getCause().getMessage() + ") 未记录的错误");
            }
        }
        
}
