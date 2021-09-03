package com.sencorsta.utils.geometry;


/**
 * @author ICe
 *
 */
public class FixedVector2 {
	
	public int x, y;
	
	public static final double FIXED_POINT=1000.0;
	
	public double xD,yD;
	
	public FixedVector2() {
		this.x = 0;
		this.y = 0;
		updateDouble();
	}
	
	public FixedVector2(int x, int y) {
		this.x = x;
		this.y = y;
		updateDouble();
	}
	
	public FixedVector2(double xD, double yD) {
		this.xD = xD;
		this.yD = yD;
		updateLong();
	}
	
	public void updateDouble() {
		xD=x/FIXED_POINT;
		yD=y/FIXED_POINT;
	}
	public void updateLong() {
		
		x=(int) (xD*FIXED_POINT);
		y=(int) (yD*FIXED_POINT);
	} 
	
	public int square() {
		updateDouble();
		return (int) ((xD * xD + yD * yD)*FIXED_POINT);
	}
	
	public int magnitude() {
		updateDouble();
		double res=Math.sqrt((xD * xD + yD * yD));
		return (int)(res*FIXED_POINT);
	}
	
	public FixedVector2 normalize() {
		FixedVector2 temp=clone();
		temp.updateDouble();
		int m = temp.magnitude();
		double mD=m/FIXED_POINT;
		if (m!=0) {
			temp.xD /= mD;
			temp.yD /= mD;
		}
		temp.updateLong();
		return temp;
	}
	public int dot(FixedVector2 vec) {
		vec.updateDouble();
		this.updateDouble();
		return (int) ((xD * vec.xD + yD * vec.yD)*FIXED_POINT);
	}
	public int dot(int x2, int y2) {
		FixedVector2 vec=new FixedVector2(x2,y2);
		return dot(vec);
	}
	public int distance(int x, int y) {
		FixedVector2 vec=new FixedVector2(x, y);
		return distance(vec);
	}
	
	public int distance (FixedVector2 vec) {
		vec.updateDouble();
		this.updateDouble();
		double modX=vec.xD-xD;
		double modY=vec.yD-yD;
		return (int)(Math.sqrt(modX * modX + modY * modY)*FIXED_POINT);
	}
	
	public FixedVector2 clone() {
		return new FixedVector2(x, y);
	}
	
	
	public FixedVector2 multip(FixedVector2 vec) {
		vec.updateDouble();
		FixedVector2 temp=clone();
		temp.updateDouble();
		temp.xD=vec.xD*temp.xD;
		temp.yD=vec.yD*temp.yD;
		temp.updateLong();
		return temp;
	}
	
	public FixedVector2 multip(int num) {
		FixedVector2 vec=new FixedVector2(num, num);
		return multip(vec);
	}
	
	public FixedVector2 plus(FixedVector2 vec) {
		vec.updateDouble();
		FixedVector2 temp=clone();
		temp.updateDouble();
		temp.xD=vec.xD+temp.xD;
		temp.yD=vec.yD+temp.yD;
		temp.updateLong();
		return temp;
	}
	
	public FixedVector2 plus(int num) {
		FixedVector2 vec=new FixedVector2(num, num);
		return plus(vec);
	}
	
	public FixedVector2 minus(FixedVector2 vec) {
		vec.updateDouble();
		FixedVector2 temp=clone();
		temp.updateDouble();
		temp.xD=temp.xD-vec.xD;
		temp.yD=temp.yD-vec.yD;
		temp.updateLong();
		return temp;
	}
	
	public FixedVector2 minus(int num) {
		FixedVector2 vec=new FixedVector2(num, num);
		return minus(vec);
	}
	
	public FixedVector2 divide(FixedVector2 vec) {
		vec.updateDouble();
		FixedVector2 temp=clone();
		temp.updateDouble();
		temp.xD=temp.xD/vec.xD;
		temp.yD=temp.yD/vec.yD;
		temp.updateLong();
		return temp;
	}
	
	public FixedVector2 divide(int num) {
		FixedVector2 vec=new FixedVector2(num, num);
		return divide(vec);
	}
	
	public String toString() {
		return String.format("[x:%8d,y:%8d]",x,y);
	}

	public FixedVector2 toFixed() {
		FixedVector2 temp=clone();
		temp.x*=FIXED_POINT;
		temp.y*=FIXED_POINT;
		temp.updateDouble();
		return temp;
	}
}
