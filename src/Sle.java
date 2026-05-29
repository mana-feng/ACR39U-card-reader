

/*===========================================================================================
 * 
 *  Copyright (C)   : Advanced Card System Ltd
 * 
 *  File            : Sle.java
 * 
 *  Description     : Contain methods and properties related to SLE memory cards.
 * 
 *  Author          : Anthony Mark Tayabas
 *  
 *  Date            : October 21, 2013
 * 
 *  Revision Traile : [Author] / [Date if modification] / [Details of Modifications done] 
 * 
 * =========================================================================================*/

import javax.smartcardio.CardException;

import java.util.Arrays;

public class Sle {
	
	private final int STATUS_WORD_LENGTH = 2;
	private final int ADDRESS_LENGTH = 2;
	private final int MAXIMUM_ADDRESS = 0xff;
	private final int MAXIMUM_DATA_LENGTH_256 = 256;
	private final int MAXIMUM_DATA_LENGTH_1024 = 1024;
	
	private PcscReader _pcscConnection;

	public enum CARD_TYPE
    {
		SLE_5528((byte)0x05),
		SLE_5542((byte)0x06),
		SLE_5536((byte)0x07),
		SLE_4404((byte)0x08);
        
        private final int ID;
        CARD_TYPE(int id) {this.ID = id;}
        public int getCardType() {return ID;}
    }
	
	public enum IC_TYPE
    {
		SLE_5528((byte)0x13),
		SLE_5542((byte)0x15);
        
        private final int ID;
        IC_TYPE(int id) {this.ID = id;}
        public int getIcType() {return ID;}
    }
	
	public enum PROTOCOL_TYPE
    {
		RESERVED_MIN((byte)0x00),
		RESERVED_MAX((byte)0x70),
		NOT_DEFINED_MIN((byte)0xB0),
		NOT_DEFINED_MAX((byte)0xE0),
		SERIAL_DATA((byte)0x80),
		THREE_WIRE((byte)0x90),
		TWO_WIRE((byte)0xA0),
		RFU((byte)0xF0),
		UNKNOWN((byte)0xFF);
        
        private final int ID;
        PROTOCOL_TYPE(int id) {this.ID = id;}
        public int getProtocolType() {return ID;}
    }
	
	public enum DATA_UNITS
    {
		NO_INDICATION((byte)0x00),
		BYTES_128((byte)0x10),
		BYTES_256((byte)0x20),
		BYTES_512((byte)0x30),
		BYTES_1024((byte)0x40),
		BYTES_2048((byte)0x50),
		BYTES_4096((byte)0x60),
		UNKNOWN((byte)0xFF);
        
        private final int ID;
        DATA_UNITS(int id) {this.ID = id;}
        public int getDataUnits() {return ID;}
    }
	
	public Sle()
	{
		_pcscConnection = new PcscReader();
	}
	
	public Sle(PcscReader pcsc)
	{
		_pcscConnection = pcsc;
	}
	
	public PcscReader getPcscConnection() { return _pcscConnection; }
    public void setPcscConnection(PcscReader value) { this._pcscConnection = value; }

    public void selectCardType(CARD_TYPE selectedCardType) throws Exception
	{
		Apdu apdu;
		
		apdu = new Apdu();
		apdu.setCommand(new byte[] { (byte) 0xFF, (byte) 0xA4, 0x00, 0x00, 0x01 });
		apdu.setSendData(new byte[]{(byte) selectedCardType.getCardType()});
		apdu.setLengthExpected(0);
		
		getPcscConnection().sendApduCommand(apdu);
		
		if (apdu.getStatusWord()[0] != (byte)0x90)
            throw new CardException("选择卡片类型失败");
	}
	
	public byte[] readMemoryCard(byte address, byte length) throws Exception
	{
		Apdu apdu;
		
		if (length > MAXIMUM_ADDRESS)
			throw new Exception("长度超过允许的最大值.");
		
		apdu = new Apdu();
		apdu.setCommand(new byte[] { (byte) 0xFF, (byte) 0xB0, 0x00, address, length });
		apdu.setSendData(new byte[0]);
		apdu.setLengthExpected(length & 0xFF);
		
		getPcscConnection().sendApduCommand(apdu);
		
		if (apdu.getStatusWord()[0] != (byte)0x90)
            throw new CardException("读卡失败");
		
		return Arrays.copyOfRange(apdu.getReceiveData(), 0, length & 0xFF);
	}
	
	public byte[] readMemoryCard(byte[] address, byte length) throws Exception
	{
		Apdu apdu;
		
		if (length > MAXIMUM_ADDRESS)
			throw new Exception("长度超过允许的最大值.");
		
		if (address.length != ADDRESS_LENGTH)
			throw new Exception("地址输入错误");
		
		apdu = new Apdu();
		apdu.setCommand(new byte[] { (byte) 0xFF, (byte) 0xB0, address[0], address[1], length });
		apdu.setSendData(new byte[0]);
		apdu.setLengthExpected(length & 0xFF);
		
		getPcscConnection().sendApduCommand(apdu);
		
		if (apdu.getStatusWord()[0] != (byte)0x90)
            throw new CardException("读卡失败");
		
		return apdu.getReceiveData();
	}
	
	public byte readErrorCounter(byte lengthExpected) throws Exception
	{
		Apdu apdu = new Apdu();
		
		apdu.setLengthExpected(lengthExpected);
		
		apdu.setCommand(new byte[] {(byte) 0xFF, (byte) 0xB1, 0x00, 0x00, lengthExpected});
		apdu.setSendData(null);
		
		getPcscConnection().sendApduCommand(apdu);
		
		if (apdu.getStatusWord()[0] != (byte)0x90)
            throw new CardException("读错误计数失败");
		
		return apdu.getReceiveData()[0];
		
	}
	
	public byte[] readProtectionBits() throws Exception
	{
		Apdu apdu;
		
		apdu = new Apdu();
		apdu.setCommand(new byte[] { (byte) 0xFF, (byte) 0xB2, 0x00, 0x00, 0x04 });
		apdu.setSendData(new byte[0]);
		apdu.setLengthExpected(4);
		
		getPcscConnection().sendApduCommand(apdu);
		
		if (apdu.getStatusWord()[0] != (byte)0x90)
            throw new CardException("读保护位数据失败");
		
		return Arrays.copyOfRange(apdu.getReceiveData(), 0, 4);
	}
	
	public byte[] readProtectionBits(byte[] address, byte length) throws Exception
	{
		Apdu apdu;
		
		apdu = new Apdu();
		apdu.setCommand(new byte[] { (byte) 0xFF, (byte) 0xB2, address[0], address[1], length });
		apdu.setSendData(new byte[0]);
		apdu.setLengthExpected(length & 0xFF);
		
		getPcscConnection().sendApduCommand(apdu);
		
		if (apdu.getStatusWord()[0] != (byte)0x90)
            throw new CardException("读保护位数据失败");
		
		return Arrays.copyOfRange(apdu.getReceiveData(), 0, length & 0xFF);
	}
	
	public void writeMemoryCard(byte address, byte[] data, byte length) throws Exception
	{
		Apdu apdu;
		
		if (data.length > MAXIMUM_DATA_LENGTH_256)
			throw new Exception("数据长度异常");
		
		apdu = new Apdu();
		apdu.setCommand(new byte[] { (byte) 0xFF, (byte) 0xD0, 0x00, address, length });
		apdu.setSendData(Arrays.copyOf(data, length & 0xFF));
		
		getPcscConnection().sendApduCommand(apdu);
		
		if (apdu.getStatusWord()[0] != (byte)0x90)
            throw new CardException("写卡失败");
	}
	
	public void writeMemoryCard(byte[] address, byte[] data, byte length) throws Exception
	{
		Apdu apdu;
		
		if (address.length != ADDRESS_LENGTH)
			throw new Exception("地址输入错误");
		
		if (data.length > MAXIMUM_DATA_LENGTH_256)
			throw new Exception("数据长度异常");
		
		apdu = new Apdu();
		apdu.setCommand(new byte[] { (byte) 0xFF, (byte) 0xD0, address[0], address[1], length });
		apdu.setSendData(Arrays.copyOf(data, length & 0xFF));
		
		getPcscConnection().sendApduCommand(apdu);
		
		if (apdu.getStatusWord()[0] != (byte)0x90)
            throw new CardException("写卡失败");
	}
	
	public void writeProtectionBits(byte address, byte[] data, byte length) throws Exception
	{
		Apdu apdu;
		
		if (data.length > MAXIMUM_DATA_LENGTH_256)
			throw new Exception("数据长度异常");
		
		apdu = new Apdu();
		apdu.setCommand(new byte[] { (byte) 0xFF, (byte) 0xD1, 0x00, address, length });
		apdu.setSendData(Arrays.copyOf(data, length & 0xFF));
		
		getPcscConnection().sendApduCommand(apdu);
		
		if (apdu.getStatusWord()[0] != (byte)0x90)
            throw new CardException("写保护位失败");
	}
	
	public void writeProtectionBits(byte[] address, byte[] data, byte length) throws Exception
	{
		Apdu apdu;
		
		if (address.length != ADDRESS_LENGTH)
			throw new Exception("地址输入错误");
		
		if (data.length > MAXIMUM_DATA_LENGTH_1024)
			throw new Exception("数据长度异常");
		
		apdu = new Apdu();
		apdu.setCommand(new byte[] { (byte) 0xFF, (byte) 0xD1, address[0], address[1], length });
		apdu.setSendData(Arrays.copyOf(data, length & 0xFF));
		
		getPcscConnection().sendApduCommand(apdu);
		
		if (apdu.getStatusWord()[0] != (byte)0x90)
            throw new CardException("写保护位数据错误");
	}
	
	public byte presentCode(byte[] code) throws Exception
	{
		Apdu apdu = new Apdu();
		
		apdu.setCommand(new byte[] { (byte) 0xFF, (byte) 0x20, 0x00, 0x00, (byte)code.length });
		apdu.setSendData(code);
		
		getPcscConnection().sendApduCommand(apdu);
		
		if (apdu.getStatusWord()[0] != (byte)0x90)
            throw new CardException("验证密码失败");
		
		return apdu.getStatusWord()[1];
	}
	
	public void changeCode(byte[] code) throws Exception
	{
		Apdu apdu = new Apdu();
		
		apdu.setCommand(new byte[] { (byte) 0xFF, (byte) 0xD2, 0x00, 0x01, 0x03 });
		apdu.setSendData(code);
		
		getPcscConnection().sendApduCommand(apdu);
		
		if (apdu.getStatusWord()[0] != (byte)0x90)
            throw new CardException("修改密码失败");
	}
	
	public byte[] getAtr() throws Exception
	{
		return readMemoryCard((byte)0x00, (byte)0x04);
	}
	
	public byte[] getCardInformation() throws Exception
	{
		byte[] cardInformation = new byte[12];
		byte[] temporaryData = new byte[27];
		
		temporaryData = readMemoryCard((byte)0x00, (byte)0x1B);
		
		System.arraycopy(temporaryData, 0, cardInformation, 0, 4);
		System.arraycopy(temporaryData, 6, cardInformation, 4, 2);
		System.arraycopy(temporaryData, 21, cardInformation, 6, 6);
		
		return cardInformation;
	}
	
	public PROTOCOL_TYPE getProtocolType(byte cardInformation)
	{
		byte protocolType = (byte)(cardInformation & 0xF0);
		
		if (protocolType == (byte)PROTOCOL_TYPE.SERIAL_DATA.getProtocolType())
			return PROTOCOL_TYPE.SERIAL_DATA;
		else if (protocolType == (byte)PROTOCOL_TYPE.THREE_WIRE.getProtocolType())
			return PROTOCOL_TYPE.THREE_WIRE;
		else if (protocolType == (byte)PROTOCOL_TYPE.TWO_WIRE.getProtocolType())
			return PROTOCOL_TYPE.TWO_WIRE;
		else if (protocolType == (byte)PROTOCOL_TYPE.RFU.getProtocolType())
			return PROTOCOL_TYPE.RFU;
		else if (protocolType == (byte)PROTOCOL_TYPE.RESERVED_MIN.getProtocolType() ||
					protocolType == (byte)PROTOCOL_TYPE.RESERVED_MAX.getProtocolType() ||
					(protocolType > (byte)PROTOCOL_TYPE.RESERVED_MIN.getProtocolType() &&
							protocolType < (byte)PROTOCOL_TYPE.RESERVED_MAX.getProtocolType()))
			return PROTOCOL_TYPE.RESERVED_MAX;
		else if (protocolType == (byte)PROTOCOL_TYPE.NOT_DEFINED_MIN.getProtocolType() ||
				protocolType == (byte)PROTOCOL_TYPE.NOT_DEFINED_MAX.getProtocolType() ||
				(protocolType > (byte)PROTOCOL_TYPE.NOT_DEFINED_MIN.getProtocolType() &&
						protocolType < (byte)PROTOCOL_TYPE.NOT_DEFINED_MAX.getProtocolType()))
			return PROTOCOL_TYPE.NOT_DEFINED_MAX;
		else
			return PROTOCOL_TYPE.UNKNOWN;	
	}
	
	public DATA_UNITS getDataUnits(byte cardInformation)
	{
		byte dataUnits = (byte)(cardInformation << 1);
		dataUnits = (byte)(dataUnits & 0xF0);
		
		if (dataUnits == (byte)DATA_UNITS.BYTES_128.getDataUnits())
			return DATA_UNITS.BYTES_128;
		else if (dataUnits == (byte)DATA_UNITS.BYTES_256.getDataUnits())
			return DATA_UNITS.BYTES_256;
		else if (dataUnits == (byte)DATA_UNITS.BYTES_512.getDataUnits())
			return DATA_UNITS.BYTES_512;
		else if (dataUnits == (byte)DATA_UNITS.BYTES_1024.getDataUnits())
			return DATA_UNITS.BYTES_1024;
		else if (dataUnits == (byte)DATA_UNITS.BYTES_2048.getDataUnits())
			return DATA_UNITS.BYTES_2048;
		else if (dataUnits == (byte)DATA_UNITS.BYTES_4096.getDataUnits())
			return DATA_UNITS.BYTES_4096;
		else if (dataUnits == (byte)DATA_UNITS.NO_INDICATION.getDataUnits())
			return DATA_UNITS.NO_INDICATION;
		else
			return DATA_UNITS.UNKNOWN;
	}
}
