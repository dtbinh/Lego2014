import lejos.nxt.*;
/**
 * The locomotions of a  LEGO 9797 car is controlled by
 * sound detected through a microphone on port 1. 
 * 
 * @author  Ole Caprani
 * @version 23.08.07
 */
public class e4_SoundCtrCar 
{
    private static int soundThreshold = 90;
    private static SoundSensor sound = new SoundSensor(SensorPort.S1);
	
    private static  void waitForLoudSound() throws Exception
    {
        int soundLevel;

        Thread.sleep(500);
        do
	    {
		soundLevel = sound.readValue();
		LCD.drawInt(soundLevel,4,10,0); 
	    }
        while ( soundLevel < soundThreshold );
    }

    public static void main(String [] args) throws Exception
    {
	Button.ESCAPE.addButtonListener(new ButtonListener() {
		public void buttonPressed(Button b) {
		    LCD.drawString("ENTER pressed", 0, 0);
		    Car.stop();
		    System.exit(1);
		}

		public void buttonReleased(Button b) {
		    LCD.clear();
		}
	    });
        LCD.drawString("dB level: ",0,0);
        LCD.refresh();
	   	   
        while (! Button.ESCAPE.isDown()) {
		waitForLoudSound();		    			   
		LCD.drawString("Forward ",0,1);
		Car.forward(100, 100);
		    
		waitForLoudSound();		    			   
		LCD.drawString("Right   ",0,1);
		Car.forward(100, 0);
		    
		waitForLoudSound();		    			   
		LCD.drawString("Left    ",0,1);
		Car.forward(0, 100);
		    
		waitForLoudSound();		    			   
		LCD.drawString("Stop    ",0,1); 
		Car.stop();
	    }
	Car.stop();
	LCD.clear();
	LCD.drawString("Program stopped", 0, 0);
	Thread.sleep(2000); 
    }
}
