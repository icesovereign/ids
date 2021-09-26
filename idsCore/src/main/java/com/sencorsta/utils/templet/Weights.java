package com.sencorsta.utils.templet;

import com.sencorsta.utils.random.ICeRandom;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ICe
 *
 */
public class Weights {
	private long[] cellArray;
	private long total;
	  
	public long getByIndex(int index) {
		return 	cellArray[index];	
	}
	  
	public long getTotal() {
		return total;
	}
	
	public Weights(int lenth) {
		super();
		cellArray=new long[lenth];
		total=0;
		for (int i = 0; i < cellArray.length; i++) {
			cellArray[i]=1;
			total+=cellArray[i];
		}
	}
	
	public Weights(long[] arr) {
		super();
		int lenth=arr.length;
		cellArray=new long[lenth];
		total=0;
		for (int i = 0; i < cellArray.length; i++) {
			cellArray[i]=arr[i];
			total+=cellArray[i];
		}
	}
	public Weights(int[] arr) {
		super();
		int lenth=arr.length;
		cellArray=new long[lenth];
		total=0;
		for (int i = 0; i < cellArray.length; i++) {
			cellArray[i]=arr[i];
			total+=cellArray[i];
		}
	}
	
	public Weights(List list) {
		super();
		int lenth=list.size();
		cellArray=new long[lenth];
		total=0;
		for (int i = 0; i < cellArray.length; i++) {
			cellArray[i]=Long.parseLong(list.get(i)+"");
			total+=cellArray[i];
		}
	}

	public void setWeightsByIndex(int index,long newValue) {
		total-=this.cellArray[index];
		this.cellArray[index]=newValue;
		total+=cellArray[index];
	}

	public void setWeightsByIndex(int index,int newValue) {
		setWeightsByIndex(index,(long)newValue);
	}
	
	public int getRandom() {
		long lenth=this.cellArray.length;
		Weights weights=this;
		long i= ICeRandom.getDynamic().nextLong(weights.getTotal());
		for (int j = 0; j < lenth; j++) {
			if (j>0) {
				i-=weights.getByIndex(j-1);
			}
			if (i<weights.getByIndex(j)) {
				return j;
			}
		}
		return -1;
	}

	public static void main(String[] args) {

		long money=20000;
		long size=6;

		if (size>money){
			System.err.println("红包数不能大于总钱数");
			return;
		}

		List<Long> list=new ArrayList<>();
		for (long m = 0; m <size ; m++) {
			list.add(ICeRandom.getDynamic().nextLong((long) money)+1);
		}
		Weights ww=new Weights(list);

		long count=0;

		List<Long> listMoney=new ArrayList<>();
		for (int i = 0; i <ww.cellArray.length-1 ; i++) {
			long myMoney=(money-size)*ww.getByIndex(i)/ww.getTotal()+1;
			count+=myMoney;
			listMoney.add(myMoney);

		}
		long myMoneyLast=money-count;
		listMoney.add(myMoneyLast);

		long total=0;
		for (int i = 0; i < listMoney.size(); i++) {
			total+=listMoney.get(i);
		}
		for (int i = 0; i < listMoney.size(); i++) {
			System.out.println("第"+(i+1)+"人"+listMoney.get(i));
		}
		System.out.println("total:"+total);




//		long minMoney=Long.MAX_VALUE;
//
//		long round=1000000;
//		while (round>0){
//			round--;
//			long money=20000;
//			long breakMoney=0;
//
//			long size=5;
//			List<longeger> list=new ArrayList<>();
//			for (long m = 0; m <size ; m++) {
//				list.add(ICeRandom.getDynamic().nextlong((long) money)+1);
//			}
//			Weights ww=new Weights(list);
//
//
//			boolean isbreak=false;
//
//			long count=0;
//
//
//			List<Long> listMoney=new ArrayList<>();
//			for (long i = 0; i <ww.cellArray.length-1 ; i++) {
//				long myMoney=(money-size)*ww.getByIndex(i)/ww.getTotal()+1;
//				count+=myMoney;
//				//System.out.prlongln("第"+(i+1)+"人"+myMoney);
//				listMoney.add(myMoney);
//				if (myMoney <=breakMoney){
//					isbreak=true;
//				}
//
//				if (minMoney>myMoney){
//					minMoney=myMoney;
//				}
//
//			}
//			long myMoneyLast=money-count;
//			listMoney.add(myMoneyLast);
//
//			//System.out.prlongln("第"+size+"人"+myMoneyLast);
//
//			if (myMoneyLast <=breakMoney){
//				isbreak=true;
//			}
//			if (minMoney>myMoneyLast){
//				minMoney=myMoneyLast;
//			}
//
//			long total=0;
//			for (long i = 0; i < listMoney.size(); i++) {
//				total+=listMoney.get(i);
//			}
//			if (total!=money||isbreak){
//				for (long i = 0; i < listMoney.size(); i++) {
//					System.out.prlongln("第"+(i+1)+"人"+listMoney.get(i));
//				}
//				System.out.prlongln("total:"+total);
//				break;
//			}
//
//		}
//		System.out.prlongln("minMoney:"+minMoney);

	}
	
}
