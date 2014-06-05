import lejos.nxt.*;
import java.io.*;
import lejos.robotics.Color;

public class FirstDriver {
    private final LightSensor sensorF = new LightSensor(SensorPort.S1,true);
    private final LightSensor sensorB = new LightSensor(SensorPort.S2,true);
    private final ColorSensor cs = new ColorSensor(SensorPort.S4);

    private final NXTRegulatedMotor M3 = Motor.C;
    private final MotorPort MP1 = MotorPort.A, MP2 = MotorPort.B;
    private final int LEFT = -1,RIGHT=1,FRONT=0,BACK=1;
    private int green = -1, black1F = 1024,black1B = 1024, white1F = -1, white1B = -1;
    private int power = 90, black2F = 1024,black2B = 1024, white2F = -1, white2B = -1;


    public int light(int end) {
	return ((end == FRONT)?sensorF:sensorB).readNormalizedValue();
    }

    public void grap() {
	M3.rotateTo(-270);
    }
    public void release() {
	M3.rotateTo(0);
    }
    public static void controlMotor(MotorPort m, int value) {
	if      (value > 0 ) m.controlMotor(value,1);
	else if (value < 0 ) m.controlMotor(-1*value,2);
	else if (value == 0) m.controlMotor(value,3); //3 stop
	else if (value == -1) m.controlMotor(value,4); //4 float
    }

    public void calibrate() throws Exception {
	controlMotor(MP1,power);
	controlMotor(MP2,power);
	green = light(FRONT);
	LCD.drawInt(green, 2, 7, 0);
	while (light(FRONT) < green+40);
	Thread.sleep(100);
	white1F = light(FRONT); // 1 FRONT WHITE
	LCD.drawInt(white1F, 2, 7, 2);
	while (light(FRONT) > white1F-40);
	while (light(FRONT) < white1F-20) // 1 FRONT BLACK
	    black1F = (light(FRONT) < black1F)?light(FRONT):black1F;
	LCD.drawInt(black1F, 2, 11, 1);
	white2F = light(BACK); // 2 FRONT WHITE
	LCD.drawInt(white2F, 2, 10, 2);
	while (light(BACK) > white2F-40);
	while (light(BACK) < white2F-20) // 2 FRONT BLACK
	    black2F = (light(BACK) < black2F)?light(BACK):black2F;
	LCD.drawInt(black2F, 2, 11, 1);
	turn(LEFT);
	turn(LEFT);
	white1B = light(FRONT); // 1 WHITE BACK
	white2B = light(BACK); // 2 WHITE BACK
	controlMotor(MP1,-power);
	controlMotor(MP2,-power);
	while (light(FRONT) > white1B-40);
	while (light(FRONT) < white1B-20) // 1 BLACK BACK
	    black1B = (light(FRONT) < black1B)?light(FRONT):black1B;
	controlMotor(MP1,power);
	controlMotor(MP2,power);
	while (light(BACK) > white2B-40);
	while (light(BACK) < white2B-20) // 2 BLACK BACK
	    black2B = (light(BACK) < black2B)?light(BACK):black2B;
    }
    public void turn(int direction) {
	MP1.resetTachoCount();
	controlMotor(MP1,power*direction);
	controlMotor(MP2,-power*direction);
	while (MP1.getTachoCount()*direction<297);
	controlMotor(MP1,0);
	controlMotor(MP2,0);
    }
    public void move(int i) {
	MP1.resetTachoCount();
	int d = (i<0)?-1:1;
	controlMotor(MP1,power*d);
	controlMotor(MP2,power*d);
	while (d*MP1.getTachoCount()<i*d);
	controlMotor(MP1,0);
	controlMotor(MP2,0);
    }
    public void followP_BACK() {
	float Kp = 1f;
	int Tp = 50;
	int offset = (int) (white2B/5+black2B/5*4);
	while (!Button.ESCAPE.isDown()) {
	    int error = light(BACK) - offset;
	    int Turn = (int) (Kp*error);
	    controlMotor(MP1,-((Tp-Turn)));
	    controlMotor(MP2,-((Tp+Turn)));
	}
    }
    int ru = 0;
    public int determineColor() {
	ColorSensor.Color raw = cs.getRawColor();
	int red = raw.getRed();
	int green = raw.getGreen();
	int blue = raw.getBlue();
	if (valval-green > 70)
	    return 1;
	else 
	    return -1;
    }

    public void followP(int direction) throws Exception {
	float Kp = 1f;
	int Tp = 80;
	int offset = (int) (white1F/8+black1F/8*7);
	if (direction == BACK)
	    offset = (int) (white1B/4+black1B/4*3);
	LCD.clear();
	LCD.drawInt(offset,0,0);

	while (!Button.ESCAPE.isDown()) {
	    int error = light(FRONT) - offset;
	    int Turn = (int) (Kp*error);
	    controlMotor(MP1,(Tp+Turn));
	    controlMotor(MP2,(Tp-Turn));
	    if (determineColor() > 0) {
		controlMotor(MP1,0);
		controlMotor(MP2,0);
		Thread.sleep(100);
		break;

	    }

	    if (light(FRONT) < offset) {
		if (ru == 0 && light(FRONT) < offset - 30) 
		    break;
		else if (ru == 1 && light(FRONT) < offset - 15) 
		    break;
	    }

	}
	ru++;
    }

    public int abs(int i){return (i<0)?-i:i;}
    public int sign(int i){return (i<0)?-1:1;}
    public void allign(int direction) throws Exception {
	int offset = (int) (white1F/8+black1F/8*7);
	int maxPower = 50, minPower = 40;
	while (!Button.ESCAPE.isDown()) {
	    int error = light(FRONT) - offset;
	    if (abs(error) > maxPower) 
		error = maxPower*sign(error);
	    else if (abs(error) < minPower) 
		error = minPower*sign(error);
	    
	    controlMotor(MP1,error);
	    controlMotor(MP2,-error);
	    LCD.drawInt(error, 0, 1);
	    if (light(FRONT)== offset)
		break;
	}
    }
    public void turnSolar() {
	grap();
	move(200);
	turn(LEFT);
	turn(LEFT);
	move(-50);
	release();
	move(-200);
	grap();
	move(2*800);
	release();
    }
    private Datalog dl;
    int valval = -2;
    public FirstDriver() throws Exception {
	cs.setFloodlight(Color.WHITE);
	ColorSensor.Color raw = cs.getRawColor();
	LCD.drawString("green: ", 0, 0);
	LCD.drawString("black: ", 0, 1);
	LCD.drawString("white: ", 0, 2);
	calibrate();
	move(-80);
	turn(LEFT);
	followP(FRONT);
	valval = cs.getRawLightValue();
	move(240);
	turn(RIGHT);
	followP(BACK);
	move(240);
	turn(LEFT);
	followP(FRONT);
	//	allign(FRONT);
	LCD.drawString("FUUUUUUUUUUUUUUUCK: ", 0, 2);
	//	power=40;
	//move(165);
	//power = 80;
	//turnSolar();
    }
    public static void main (String[] aArg) throws Exception {
	new FirstDriver();
    }
}
