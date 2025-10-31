

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;

import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

@SuppressWarnings("serial")
public class MainApplet extends JApplet implements ActionListener
{
	public MainApplet() {}
	
	// Variables
	boolean _isMemoryCardProgrammingOpen = false;

	//GUI Variables
    private JButton _ButtonMemoryCardProgramming;
	
	static MemoryCardProgramming _memoryCardProgramming;

	public void init() 
   	{
		setSize(275,120);
		_ButtonMemoryCardProgramming = new JButton();
		_ButtonMemoryCardProgramming.setFont(new Font("Verdana", Font.PLAIN, 10));
		_ButtonMemoryCardProgramming.setBounds(10, 44, 250, 23);

		_ButtonMemoryCardProgramming.setText("SLE Memory Card Programming");
        
        JLabel LabelMemoryCardProgramming = new JLabel("SLE Memory Card Programming");
        LabelMemoryCardProgramming.setHorizontalAlignment(SwingConstants.CENTER);
        LabelMemoryCardProgramming.setFont(new Font("Verdana", Font.PLAIN, 10));
        LabelMemoryCardProgramming.setBounds(10, 19, 250, 14);
        
        getContentPane().setLayout(null);
        getContentPane().add(LabelMemoryCardProgramming);
        getContentPane().add(_ButtonMemoryCardProgramming);
        
        _ButtonMemoryCardProgramming.addActionListener(this);
   		
   	}
	
	public void actionPerformed(ActionEvent e) 
	{		
		if(_ButtonMemoryCardProgramming == e.getSource())
		{
			closeFrames();
			
			if(_isMemoryCardProgrammingOpen == false)
			{
				try {
					_memoryCardProgramming = new MemoryCardProgramming();
				} catch (ParseException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				_memoryCardProgramming.setVisible(true);
				_isMemoryCardProgrammingOpen = true;
			}
			else
			{			
				_memoryCardProgramming.dispose();
				
				try {
					_memoryCardProgramming = new MemoryCardProgramming();
				} catch (ParseException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				_memoryCardProgramming.setVisible(true);
				_isMemoryCardProgrammingOpen = true;
			}			
		}
	}
	
	public void closeFrames()
	{		
		if(_isMemoryCardProgrammingOpen == true)
		{
			_memoryCardProgramming.dispose();
			_isMemoryCardProgrammingOpen = false;
		}		
	}
	
	public void start()
	{
	
	}
}