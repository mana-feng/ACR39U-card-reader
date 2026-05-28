

public class Apdu 
{
	private final int VALID_COMMAND_LENGTH = 5;
	
	 private byte _cla;
     private byte _ins;
     private byte _p1;
     private byte _p2;
     private byte _p3;
     private int _lengthExpected;

     private byte[] _sendData;
     private byte[] _receiveData;
     private byte[] _statusWord;
     
     int _counter;
     
     public Apdu()
     {
    	 _lengthExpected = 0;
     }
     
     /// <summary>
     /// The T=0 instruction class.
     /// </summary>
     public byte getCla() {return this._cla;}
     public void setCla(byte cla) {this._cla = cla;}
     
     /// <summary>
     /// An instruction code in the T=0 instruction class.
     /// </summary>
     public byte getIns() {return this._ins;}
     public void setIns(byte ins){this._ins = ins;}     
     
     /// <summary>
     /// Reference codes that complete the instruction code.
     /// </summary>
     public byte getP1() {return this._p1;}
     public void setP1(byte p1) {this._p1 = p1;}
     
     /// <summary>
     /// Reference codes that complete the instruction code.
     /// </summary>
     public byte getP2() {return this._p2;}
     public void setP2(byte p2) {this._p2 = p2;}
     
     /// <summary>
     /// The number of data bytes to be transmitted during the command, per ISO 7816-4, Section 8.2.1.
     /// </summary>
     public byte getP3() {return this._p3;}
     public void setP3(byte p3) {this._p3 = p3;}
     
     /// <summary>
     /// Length of data expected from the card
     /// </summary>
     public int getLengthExpected() {return this._lengthExpected;}
     public void setLengthExpected(int lengthExpected) {this._lengthExpected = lengthExpected;}
     
     public byte[] getSendData(){return this._sendData;}
     public void setSendData(byte[] sendData) {this._sendData = sendData;}
     
     public byte[] getReceiveData(){return this._receiveData;}
     public void setReceiveData(byte[] receiveData) {this._receiveData = receiveData;}
     
     public byte[] getStatusWord() {return this._statusWord;}
     public void setStatusWord(byte[] statusWord) {this._statusWord = statusWord;}
     
     public void setCommand(byte[] command) throws Exception
     {
    	 if(command.length != VALID_COMMAND_LENGTH)
    		 throw new Exception("无效指令");
    	 
    	 setCla(command[0]);
    	 setIns(command[1]);
    	 setP1(command[2]);
    	 setP2(command[3]);
    	 setP3(command[4]);
    	 
    	 return;
     }
     
     public boolean getStatusWord(byte[] data)
     {
    	 if(getStatusWord() == null)
    		 return false;
    	 
    	 for(_counter = 0; _counter < getStatusWord().length; _counter++)
    		 if(getStatusWord()[_counter] != data[_counter])
    			 return false;
    	 
    	 return true;
     }
}
