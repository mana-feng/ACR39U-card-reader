
import java.util.ArrayList;
import java.util.EventObject;
import java.util.Iterator;
import java.util.List;

public class ReaderEvents 
{
	@SuppressWarnings("serial")
	public class TransmitApduEventArg extends EventObject
	{	
		private byte[] _data;
		
		public byte[] getData()
		{
			return this._data;
		}
		
		public void setData(byte[] data)
		{
			this._data = data;
		}
		
	    public TransmitApduEventArg(Object sender, byte[] data)
	    {
	    	super(sender);
	    	this._data = data;
	    }
	    
	    public String getAsString(boolean spaceInBetween)
		{
			if(this.getData() == null)
				return "";
			
			return Helper.byteAsString(this.getData(), spaceInBetween);
		}
	}       
	        
	interface ITransmitApduHandler
	{
		public void onSendCommand(TransmitApduEventArg event);
		public void onReceiveCommand(TransmitApduEventArg event);
	}
	
	private List<ITransmitApduHandler> _listeners = new ArrayList<ITransmitApduHandler>();

	public List<ITransmitApduHandler> getListeners()
	{
		return this._listeners;
	}
	
	public void setListeners(List<ITransmitApduHandler> listeners)
	{
		this._listeners = listeners;
	}
	
	public synchronized void addEventListener(ITransmitApduHandler listener) 
	{
		this.getListeners().add(listener);
	}
    
    public synchronized void removeEventListener(ITransmitApduHandler listener) 
    {
    	this.getListeners().remove(listener);
    }
    
    public synchronized void sendCommandData(byte[] data) 
    {
        TransmitApduEventArg event = new TransmitApduEventArg(this, data);
        Iterator<ITransmitApduHandler> listeners = this.getListeners().iterator();
        
        while (listeners.hasNext()) 
        {
            ((ITransmitApduHandler) listeners.next()).onSendCommand(event);
        }
    }
    
    public synchronized void receiveCommandData(byte[] data) 
    {
        TransmitApduEventArg event = new TransmitApduEventArg( this, data);
        Iterator<ITransmitApduHandler> listeners = this.getListeners().iterator();
        
        while (listeners.hasNext()) 
        {
            ((ITransmitApduHandler) listeners.next()).onReceiveCommand(event);
        }
    }
}
