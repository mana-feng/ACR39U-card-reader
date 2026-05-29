

import java.util.Arrays;


public class Helper 
{
	public static String byteAsString(byte data)
	{
		String temporaryString = "";

		temporaryString += String.format("%02X", data);

		return temporaryString;
	}
	
	public static String byteAsString(byte[] data, boolean spaceInBetween)
	{
		String temporaryString = "";
		int counter;

		if(data == null)
			return "";

		for (counter = 0; counter < data.length; counter++)
			temporaryString += String.format((spaceInBetween ? "%02X " : "%02X"), data[counter]);

		return temporaryString;
	}
	
	public static String byteAsString(byte[] b, int startIndx, int len, boolean spaceinbetween)
	{
		int index;
		byte[] temporaryArray;
		
		if(b.length < startIndx + len)
		{
			temporaryArray = new byte[len];
			// resize the array
			for(index = 0; index < len; index++)
				temporaryArray[index] = b[index];
			
			b = new byte[len];
			
			for(index = 0; index < len; index++)
				b[index] = temporaryArray[index];
		}
		
		byte[] newByte = new byte[len];
		for(index = 0; index < len; index++)
			newByte[index] = b[startIndx + index];
		
		return byteAsString(newByte, spaceinbetween);
	}

	public static byte[] getBytes(String stringBytes, String delimeter)
	{
		String[] arrayString = stringBytes.split(delimeter);
		byte[] bytesResult = new byte[arrayString.length];
		int counter;

		for (counter = 0; counter < arrayString.length; counter++)
			bytesResult[counter] = (byte)Integer.parseInt(arrayString[counter],16);

		return bytesResult;
	}

	public static byte[] getBytes(String stringBytes)
	{
		String formattedString = "";
		int temporaryCounter = 0;
		int counter;

		if(stringBytes.trim() == "")
			return null;

		for(counter = 0; counter < stringBytes.length(); counter++)
		{
			if(stringBytes.charAt(counter) == ' ')
				continue;

			if(temporaryCounter > 0 && temporaryCounter % 2 == 0)
				formattedString += " ";

			formattedString += stringBytes.charAt(counter);

			temporaryCounter++;
		}

		return getBytes(formattedString, " ");
	}

	public static byte[] stringToByteArray(String string)
	{
		byte[] buffer = new byte[string.length()/2];
		String temporaryString;
		int counter;

		for (counter = 0; counter < buffer.length; counter++ )
		{
			temporaryString = string.substring(counter*2, counter*2 + 2);
			buffer[counter] = (byte)((Integer)Integer.parseInt(temporaryString, 16)).byteValue();
		}

		return buffer; 
	}

	public static String byteArrayToString (byte[] data)
	{

		String str = "";
		
		for(int i = 0;i < data.length;i ++) {
			if(i < data.length - 1) {
				str += String.format("%02X", data[i]);
				str += " ";
			}else {
				str += String.format("%02X", data[i]);
			}
		}
		
		return str;
	}

	public static int byteToInt(byte[] data, boolean isLittleEndian)
	{
		byte[] holder = new byte[4];
		byte[] reverseArray = new byte[4];
		int counter;

		if (isLittleEndian)
		{
			// Make sure that the array size is 4
			System.arraycopy(data, 0, holder, 0, data.length);

			for (counter = 0; counter < holder.length; counter++)
				reverseArray[counter] = holder[3 - counter];

			return byteToInt(reverseArray);
		}
		else
		{
			return byteToInt(data);
		}		
	}

	public static int byteToInt(byte[] data)
	{
		byte[] holder = new byte[4];

		if (data == null)
			return -1;

		// Make sure that the array size is 4
		System.arraycopy(data, 0, holder, 4 - data.length, data.length);

		return (((holder[0] & 0xFF) << 24) + ((holder[1] & 0xFF) << 16) + ((holder[2] & 0xFF) << 8) + (holder[3] & 0xFF)); 
	}
	
	public static byte[] intToByte(int number)
	{
		byte[] data = new byte[4];

		data[0] = (byte)((number >> 24) & 0xFF);
		data[1] = (byte)((number >> 16) & 0xFF);
		data[2] = (byte)((number >> 8) & 0xFF);
		data[3] = (byte)(number & 0xFF);

		return data;
	}

	public static String removeWhiteSpaces(String string) 
	{
		return string.replaceAll("\\s", "");		
	}
	
	// Convert Hex String to Byte Array
	public static byte[] hex2Byte(String string)
	{
		byte[] bytes = new byte[string.length() / 2];
		int counter;
		
	    for (counter = 0; counter < bytes.length; counter++)
	    {
	    	bytes[counter] = (byte) Integer.parseInt(string.substring(2 * counter, 2 * counter + 2), 16);
	    }
	    return bytes;
	}
	
	public static byte[] appendArrays(byte[] array1, byte array2)
    {
        byte[] combinedArray = new byte[1 + array1.length];
        System.arraycopy(array1, 0, combinedArray, 0, array1.length);
        combinedArray[array1.length] = array2;
        return combinedArray;
    }
	
	public static byte[] appendArrays(byte[] array1, byte[] array2)
    {
        byte[] combinedArray = new byte[array1.length + array2.length];
        System.arraycopy(array1, 0, combinedArray, 0, array1.length);
        System.arraycopy(array2, 0, combinedArray, array1.length, array2.length);
        return combinedArray;
    }
	
	public static byte[] appendArrays(byte array1, byte[] array2)
    {        
        byte[] combinedArray = new byte[1 + array2.length];
        System.arraycopy(array2, 0, combinedArray, 1, array2.length);
        combinedArray[0] = array1;
        return combinedArray;
    }
	
	public static Object resizeArray(Object oldArray, int newSize)
	{
	   int oldSize = java.lang.reflect.Array.getLength(oldArray);
	   @SuppressWarnings("rawtypes")
	   Class elementType = oldArray.getClass().getComponentType();
	   Object newArray = java.lang.reflect.Array.newInstance(
	         elementType, newSize);
	   int preserveLength = Math.min(oldSize, newSize);
	   if (preserveLength > 0)
	      System.arraycopy(oldArray, 0, newArray, 0, preserveLength);
	   return newArray;
	}
	
	public static String byteArrayToString(byte[] value, int startIndex, int length, boolean spaceInBetween)
    {
    	byte[] newByte;

        if (value.length < startIndex + length)
        	Arrays.copyOf(value, startIndex + length);
        
        newByte = new byte[length];
        
        System.arraycopy(value, startIndex, newByte, 0, length);
        
        return byteArrayToString(newByte, spaceInBetween);
    }
    
    public static String byteArrayToString(byte[] temporaryByte, boolean spaceInBetween)
    {
    	String temporaryString = "";
    	int counter;
    	
    	if (temporaryByte == null)
    		return "";
    	
    	for (counter = 0; counter < temporaryByte.length; counter++)
    	{
    		temporaryString += String.format("%02X", temporaryByte[counter]);
    		
    		if (spaceInBetween)
    			temporaryString += " ";
    	}
    	
    	return temporaryString;
    }
    
    public static boolean byteArrayIsEqual(byte[] array1, byte[] array2, int length)
	{
		if(array1.length < length)
			return false;
		
		if(array2.length < length)
			return false;
		
		for(int i = 0; i < length; i++)
		{
			if(array1[i] != array2[i])
				return false;
		}
		
		return true;
	}
    
    public static boolean byteArrayIsEqual(byte[] array1, byte[] array2)
	{
		return byteArrayIsEqual(array1, array2, array2.length);
	}
    
    public static String byteArrayToString (byte[] data, int length)
	{
		String string = "";
		int index = 0;

		while((data[index] & 0xFF) != 0x00)
		{	
			string  += (char)(data[index] & 0xFF);				
			index++;
			if (index == length)
				break;
		}
		
		return string;
	}

	public static byte[] subBytes(byte[] data, int begin, int count)
	{
		byte[] result = new byte[count];
		System.arraycopy(data, begin, result, 0, count);
		return result;
	}

}
