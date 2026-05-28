
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.TerminalFactory;

public class PcscReader 
{
	private final int DATA_START_INDEX = 5;
	
	protected TerminalFactory _terminalFactory;
	protected List<CardTerminal> _cardTerminalList;
	protected CardTerminal _activeTerminal;
	protected Card _card;
	protected CardChannel _cardChannel;
	protected CommandAPDU _commandApdu;
	protected ResponseAPDU _responseApdu;
	protected byte[] _controlCommand, _controlResponse;
	protected String _preferredProtocol;

	protected int _controlCode;
	protected boolean _connectionActive;
	private int _returnCode;

	protected ReaderEvents _eventHandler;

	// Default constructor
	public PcscReader()
	{
		setTerminalFactory(TerminalFactory.getDefault());
		setPreferredProtocol("*");
		setConnectionActive(false);
		
		// Set a system property for the protocol of the card so that "GET RESPONSE" should be called manually
		System.setProperty("sun.security.smartcardio.t0GetResponse", "false");
		System.setProperty("sun.security.smartcardio.t1GetResponse", "false");
	}

	public TerminalFactory getTerminalFactory() { return this._terminalFactory;	}
	public void setTerminalFactory(TerminalFactory terminalFactory) { this._terminalFactory = terminalFactory; }

	public List<CardTerminal> getCardTerminalList() { return this._cardTerminalList; }
	public void setCardTerminalList(List<CardTerminal> cardTerminalList) { this._cardTerminalList = cardTerminalList; }

	public CardTerminal getCardTerminal(int index) { return this._cardTerminalList.get(index); }

	public Card getCard() { return this._card; }
	public void setCard(Card card) { this._card = card; }

	public CardChannel getCardChannel() { return this._cardChannel; }
	public void setCardChannel(CardChannel cardChannel) { this._cardChannel = cardChannel; }

	public CommandAPDU getCommandApdu() { return this._commandApdu; }
	public void setCommandApdu(CommandAPDU commandApdu) { this._commandApdu = commandApdu; }

	public ResponseAPDU getResponseApdu() { return this._responseApdu; }
	public void setResponseApdu(ResponseAPDU responseApdu) { this._responseApdu = responseApdu; }

	public void setControlCommand(byte[] controlCommand) { this._controlCommand = controlCommand; }
	public byte[] getControlCommand() { return this._controlCommand; }

	public void setControlResponse(byte[] controlResponse) { this._controlResponse = controlResponse; }
	public byte[] getControlResponse() { return this._controlResponse; }

	public void setControlCode(int controlCode)	{ this._controlCode = controlCode; }
	public int getControlCode() { return this._controlCode; }

	public void setActiveTerminal(CardTerminal activeTerminal) { this._activeTerminal = activeTerminal; }
	public CardTerminal getActiveTerminal() { return this._activeTerminal; }

	public void setPreferredProtocol(String preferredProtocol) { this._preferredProtocol = preferredProtocol; }
	public String getPreferredProtocol() { return this._preferredProtocol; }

	public ReaderEvents getEventHandler() { return this._eventHandler; }
	public void setEventHandler(ReaderEvents eventHandler) { this._eventHandler = eventHandler; }

	public boolean getConnectionActive() { return this._connectionActive; }
	public void setConnectionActive(boolean connectionActive) { this._connectionActive = connectionActive; }

	// List the available smart card readers
	public String[] listTerminals() throws Exception
	{
		String[] terminals;
		int counter;
		
		try
		{
			setCardTerminalList(getTerminalFactory().terminals().list());
		}
		catch(CardException ex)
		{
			if(ex.getCause().getMessage().equals(PcscProvider.CODES.SCARD_E_SERVICE_STOPPED.toString()))
			{
				establishContext();
				setCardTerminalList(getTerminalFactory().terminals().list());
			}
			else
			{
				throw ex;
			}
		}
		
		terminals = new String[getCardTerminalList().size()]; 

		for (counter = 0; counter < getCardTerminalList().size(); counter++)
			terminals[counter] = getCardTerminalList().get(counter).getName(); 

		return terminals;
	}

	// Connect to the smart card through the specified smart card reader (overloaded function)
	public int connect(int terminalNumber, String preferredProtocol) throws Exception
	{
		setActiveTerminal(getCardTerminalList().get(terminalNumber));
		setPreferredProtocol(preferredProtocol);

		return connect();
	}
	
	// Connect to the smart card through the specified smart card reader (overloaded function)
	public int connect(String readerName, String preferredProtocol) throws Exception
	{
		int counter;
		
		for (counter = 0; counter < getCardTerminalList().size(); counter++)
		{
			if (getCardTerminalList().get(counter).getName().indexOf(readerName) > -1)
			{
				setActiveTerminal(getCardTerminalList().get(counter));
				setPreferredProtocol(preferredProtocol);
				break;
			}
		}

		return connect();
	}

	// Connect to the smart card through the specified smart card reader (overloaded function)
	public int connect(int terminalNumber) throws Exception
	{
		setActiveTerminal(getCardTerminalList().get(terminalNumber));

		return connect();
	}

	// Connect to the smart card through the specified smart card reader
	public int connect() throws Exception
	{
		int terminalIndex;
		int counter;
		
		try
		{
			setCard(getActiveTerminal().connect(getPreferredProtocol()));
			setCardChannel(getCard().getBasicChannel());
		}
		catch(CardException ex)
		{
			if(ex.getCause().getMessage().equals(PcscProvider.CODES.SCARD_E_SERVICE_STOPPED.toString()))
			{
				establishContext();
				
				String previousSelectedTerminal = getActiveTerminal().getName();
				List<CardTerminal> previousCardTerminals = getCardTerminalList();

				String[] newTerminals = listTerminals();
				
				terminalIndex = -1;
				
				for(counter = 0; counter < newTerminals.length; counter++)
				{
					if(newTerminals[counter].equals(previousSelectedTerminal))
					{
						terminalIndex = counter;							
						break;
					}
				}
				
				if(terminalIndex == -1)
				{
					setCardTerminalList(previousCardTerminals);
					throw new PcscException(PcscProvider.CODES.SCARD_E_UNKNOWN_READER);
				}
				
				setActiveTerminal(getCardTerminalList().get(terminalIndex));
				setCard(getActiveTerminal().connect(getPreferredProtocol()));
				setCardChannel(getCard().getBasicChannel());
			}
			else
			{
				throw ex;
			}
		}
		
		setConnectionActive(true);
		
		return 0;
	}

	// Connect directly to the smart card reader	
	public int connectDirect(int terminalNumber, boolean isSetTerminalNumber) throws Exception
	{
		String previousSelectedTerminal;
		List<CardTerminal> previousCardTerminals;
		String[] newTerminals;
		int terminalIndex;
		int counter;
		
		try
		{
			if(isSetTerminalNumber)
				setActiveTerminal(getCardTerminalList().get(terminalNumber));
			
			setCard(getActiveTerminal().connect("direct"));
			setConnectionActive(true);
		}
		catch(CardException ex)
		{			
			if(ex.getCause().getMessage().equals(PcscProvider.CODES.SCARD_E_SERVICE_STOPPED.toString()))
			{
				establishContext();
				
				previousSelectedTerminal = getActiveTerminal().getName();
				previousCardTerminals = getCardTerminalList();
				newTerminals = listTerminals();
				terminalIndex = -1;
				
				if(isSetTerminalNumber)
				{					
					for(counter = 0; counter < newTerminals.length; counter++)
					{
						if(newTerminals[counter].equals(previousSelectedTerminal))
						{
							terminalIndex = counter;							
							break;
						}
					}
				}
				if(terminalIndex == -1)
				{
					setCardTerminalList(previousCardTerminals);
					throw new PcscException(PcscProvider.CODES.SCARD_E_UNKNOWN_READER);
				}
					
				setActiveTerminal(getCardTerminalList().get(terminalIndex));
				setCard(getActiveTerminal().connect("direct"));
				setConnectionActive(true);
			}
			else
			{
				throw ex;
			}
		}
		
		return 0;		
	}

	// Disconnect from the smart card
	public int disconnect() throws Exception
	{
		//The disconnect method of Card.java has a reset parameter which is used as follows:		
		//SCardDisconnect(cardId, (reset ? SCARD_LEAVE_CARD : SCARD_RESET_CARD));
		//So if reset is true, the card is not being reset, if false, the card is being reset.
		getCard().disconnect(false);
		
		setConnectionActive(false);

		return _returnCode;
	}
	
	// Send APDU commands to the smart card (overloaded function)
	public int sendApduCommand(Apdu apdu) throws Exception
	{
		int sendApduCommand = 0;
		int sendDataLength = 0;
		byte[] commandApdu;
		
		if (apdu.getSendData() != null)
			sendDataLength = apdu.getSendData().length; 
		
		commandApdu = new byte[5 + sendDataLength];
		
		System.arraycopy(new byte[] { apdu.getCla(), apdu.getIns(), apdu.getP1(), apdu.getP2(), apdu.getP3() }, 0, commandApdu, 0, 5);
		
		if (sendDataLength > 0)
			System.arraycopy(apdu.getSendData(), 0, commandApdu, DATA_START_INDEX, apdu.getSendData().length);

		try
		{
			sendApduCommand = sendApduCommand(commandApdu);
			
			apdu.setStatusWord(new byte[] { (byte) getResponseApdu().getSW1(), (byte) getResponseApdu().getSW2() });
			if (getResponseApdu().getData().length > 0)
				apdu.setReceiveData(getResponseApdu().getData());
		} 
		catch(CardException ex)
		{
			if(ex.getCause().getMessage().equals(PcscProvider.CODES.SCARD_E_SERVICE_STOPPED.toString()))
			{
				establishContext();	
				
				sendApduCommand = sendApduCommand(commandApdu);
				
				apdu.setStatusWord(new byte[] { (byte) getResponseApdu().getSW1(), (byte) getResponseApdu().getSW2() });
				
				if (getResponseApdu().getData().length > 0)
					apdu.setReceiveData(getResponseApdu().getData());
			}
			else
			{
				throw ex;				
			}
		}

		return sendApduCommand;
	}
	

	// Send APDU commands to the smart card (overloaded function)
	public int sendApduCommand(byte[] apdu) throws Exception
	{
		setCommandApdu(new CommandAPDU(apdu));

		return sendApduCommand();
	}

	// Send APDU commands to the smart card
	public int sendApduCommand() throws Exception
	{
		try
		{
			getEventHandler().sendCommandData(getCommandApdu().getBytes());
			setResponseApdu(getCardChannel().transmit(getCommandApdu()));
			getEventHandler().receiveCommandData(getResponseApdu().getBytes());
		}
		catch(CardException ex)
		{
			if(ex.getCause().getMessage().equals(PcscProvider.CODES.SCARD_E_SERVICE_STOPPED.toString()))
			{
				establishContext();

				getEventHandler().sendCommandData(getCommandApdu().getBytes());
				setResponseApdu(getCardChannel().transmit(getCommandApdu()));
				getEventHandler().receiveCommandData(getResponseApdu().getBytes());
			}
			else
			{
				throw ex;
			}			
		}
		return _returnCode;
	}

	// Send direct control commands to the smart card reader (overloaded function)
	public int sendControlCommand(int controlCode, byte[] controlCommand) throws Exception
	{
		setControlCode(controlCode);
		setControlCommand(controlCommand);

		return sendControlCommand();
	}

	// Send direct control commands to the smart card reader (overloaded function)
	public int sendControlCommand(byte[] controlCommand) throws Exception
	{
		setControlCommand(controlCommand);

		return sendControlCommand();
	}

	// Send direct control commands to the smart card reader 
	public int sendControlCommand() throws Exception
	{
		try
		{	
			getEventHandler().sendCommandData(getControlCommand());
			setControlResponse(getCard().transmitControlCommand(getControlCode(), getControlCommand()));
			getEventHandler().receiveCommandData(getControlResponse());
		}
		catch(CardException ex)
		{
			if(ex.getCause().getMessage().equals(PcscProvider.CODES.SCARD_E_SERVICE_STOPPED.toString()))
			{
				establishContext();

				getEventHandler().sendCommandData(getControlCommand());
				setControlResponse(getCard().transmitControlCommand(getControlCode(), getControlCommand()));
				getEventHandler().receiveCommandData(getControlResponse());
			}
			else
			{
				throw ex;
			}		
			
		}
		return _returnCode;
	}
	
	// Get the ATR of the smart card	
	public byte[] getAtr() throws Exception
	{		
		return getCard().getATR().getBytes();
	}
	
	// Get the protocol in use of the card
	public String getCardProtocol() throws Exception
	{
		return getCard().getProtocol();
	}
	
	// Re-establish resource manager context
	public void establishContext() throws Exception
	{
		Class<?> pcscTerminal;
		Field contextId;
		Class<?> pcsc;
		Method SCardEstablishContext;
		Field SCARD_SCOPE_USER;
		long newId;
		TerminalFactory factory;
		CardTerminals terminals;
		Field fieldTerminals;
		Class<?> classMap;
		Method clearMap;
		
		if (getConnectionActive())
			disconnect();
		
		pcscTerminal = Class.forName("sun.security.smartcardio.PCSCTerminals");
        contextId = pcscTerminal.getDeclaredField("contextId");
        
        contextId.setAccessible(true);

        if(contextId.getLong(pcscTerminal) != 0L)
        {
            // First get a new context value
            pcsc = Class.forName("sun.security.smartcardio.PCSC");
            SCardEstablishContext = pcsc.getDeclaredMethod("SCardEstablishContext", new Class[] { Integer.TYPE });
            
            SCardEstablishContext.setAccessible(true);

            SCARD_SCOPE_USER = pcsc.getDeclaredField("SCARD_SCOPE_USER");
            
            SCARD_SCOPE_USER.setAccessible(true);

            newId = ((Long)SCardEstablishContext.invoke(pcsc, 
                    new Object[] { SCARD_SCOPE_USER.getInt(pcsc) }
            ));
            contextId.setLong(pcscTerminal, newId);

            // Then clear the terminals in cache
            factory = TerminalFactory.getDefault();
            terminals = factory.terminals();
            fieldTerminals = pcscTerminal.getDeclaredField("terminals");
            
            fieldTerminals.setAccessible(true);
            
            classMap = Class.forName("java.util.Map");
            clearMap = classMap.getDeclaredMethod("clear");

            clearMap.invoke(fieldTerminals.get(terminals));
        }       

		setConnectionActive(false);
	}
}
