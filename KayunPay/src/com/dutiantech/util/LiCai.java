package com.dutiantech.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map; 

public class LiCai {

	private long amount ;
	private double rate ;
	private int limit ;
	private long interest = 0 ;
	@SuppressWarnings("unused")
	private long benxi = 0 ;
	
	/**
	 * 
	 * @param amount		单位分
	 * @param rate			如 24.01%，传入值则为 2401
	 * @param limit			正整数
	 */
	public LiCai( long amount , long rate , int limit ){
		this.amount = amount ;			//转为float
		this.rate = (rate/120000.00) ;		//转为月息
		this.limit = limit ;	
	}
	
	public long dengebenxi( ){
		return (long)(( amount * rate )*Math.pow( (1 + rate ) , limit ) /
				( Math.pow( ( 1 + rate ) , limit ) - 1 ));
	}
	
	public double dengxi(){
		return amount * rate ;
	}
	
	public long getInterest(){
		return interest ;
	}
	
	public List<Map<String , Long>> getDengEList(){
		List<Map<String, Long>> list = new ArrayList<Map<String , Long>>() ;
		long tmpAmount = amount ;
		long bx =  dengebenxi(); 
		benxi = (long)bx;
		int startIndex = 1 ;
		do{
			long xi = (long) (tmpAmount * rate) ;
			tmpAmount = tmpAmount - bx + xi ;
			list.add( makeMonthInfo(startIndex, bx-xi, xi, tmpAmount)) ;
			interest += xi ;
			startIndex ++ ;
		}while(startIndex <= limit );
		
		
		return list ;
	}
	
	/**
	 * 	拆分借款标时使用
	 * 	计算等额本息，抹去每一流水的小数点，将差额补到最后一期
	 * @return
	 */
	public List<Map<String , Long>> getDengEList4loan(){
		List<Map<String, Long>> list = new ArrayList<Map<String , Long>>() ;
		long tmpAmount = amount ;
		long bx = (long) dengebenxi(); 
		benxi = bx;
		int startIndex = 1 ;
		long tmpBen = 0 ;
		do{
			long xi = (long)(tmpAmount * rate) ;
			tmpAmount = tmpAmount - bx + xi ;
			long tmpBenValue = bx - xi ;
			//计算本金小数值
			if( startIndex < limit ){
				long lastValue = tmpBenValue - tmpBenValue%100 ;
				tmpBen += lastValue ;
				list.add( makeMonthInfo(startIndex, lastValue , xi, tmpAmount)) ;
			}else if(startIndex == limit ){
				long lastBen = this.amount - tmpBen ;
				list.add( makeMonthInfo(startIndex, lastBen  , xi, 0)) ;
				//System.out.println((bx-xi) + "    " + lastValue);
			}
			interest += xi ;
			startIndex ++ ;
		}while(startIndex <= limit );
		
		
		return list ;
	}
	
	/*	正常计算等额本息
	public List<Map<String , Long>> getDengEList(){
		List<Map<String, Long>> list = new ArrayList<Map<String , Long>>() ;
		double tmpAmount = amount ;
		double bx = dengebenxi(); 
		benxi = (long)bx;
		int startIndex = 1 ;
		do{
			double xi = tmpAmount * rate ;
			tmpAmount = tmpAmount - bx + xi ;
			list.add( makeMonthInfo(startIndex, bx, xi, tmpAmount)) ;
			interest += xi ;
			startIndex ++ ;
		}while(startIndex <= limit );
		
		
		return list ;
	}*/
	
	private Map<String , Long> makeMonthInfo( int month , long ben , long xi , long balance ){
		Map<String , Long > tmpMap = new HashMap<String , Long >() ;;
		tmpMap.put("month", (long) month ) ;
		tmpMap.put("benxi", ben+xi ) ;
		tmpMap.put("ben", ben ) ;
		tmpMap.put("xi", xi ) ;
		tmpMap.put("balance",  balance<0?0:balance ) ;
		return tmpMap ;
	}
	
	public List<Map<String , Long>> getDengXiList(){
		List<Map<String , Long >> list = new ArrayList<Map<String , Long>>() ;
		double xi = dengxi();
		for( int i = 1 ;i < limit ; i ++ ){
			list.add( makeMonthInfo(i, 0, (long)xi , amount ) ) ;
		}
		list.add(makeMonthInfo( limit , (long)(amount) , (long)xi , 0 ) ) ;
		return list ;
	}
	
	public Map<String , Long> getDengXi4month( int monthIndex ){
		if( monthIndex > limit ){
			return null ;
		}
		List<Map<String , Long >> list = getDengXiList() ;
		return list.get(monthIndex-1) ;
	}
	
	/**
	 * 	获取某一月的理财信息
	 * @param monthIndex
	 * @return
	 */
	public Map<String , Long> getDengE4month( int monthIndex ){
		if( monthIndex > limit ){
			return null ;
		}
		List<Map<String , Long >> list = getDengEList() ;
		return list.get(monthIndex-1) ;
	}
	
	/**
	 * 	获取某一月的理财信息
	 * @param monthIndex
	 * @return
	 */
	public Map<String , Long> getDengE4month4loan( int monthIndex ){
		if( monthIndex > limit ){
			return null ;
		}
		List<Map<String , Long >> list = getDengEList4loan() ;
		return list.get(monthIndex-1) ;
	}
	
	public void test(){

		//List<Map<String , Double >> list = getDengEList() ;
		List<Map<String , Long >> list = getDengEList() ;
		System.out.println( list.size());
		for( Map<String , Long > tmp : list ){
			System.out.println(String.format("第%d个月，本息：%d , 本金：%d , 利息：%d , 剩余本金：%d", 
						tmp.get("month") , tmp.get("benxi") , tmp.get("ben")
						 , tmp.get("xi") , (int)Math.rint(tmp.get("balance"))));
		}
		
//		System.out.println("等息：" + dengxi());	
//		System.out.println("等息：" + getDengXi4month(4));
//		System.out.println("等额：" + dengebenxi());
//		System.out.println("等额：" + getDengE4month(4));
	}
	
	
	public static void main(String[] args) {
		int limit = 3 ;
		LiCai licai = new LiCai(6000000 , 1700 , limit) ;
		licai.test(); 
		List<Map<String , Long>> list = licai.getDengXiList() ;
		long ben = 0 ;
		for(Map<String , Long > map : list){
			ben += map.get("ben");
			System.out.println(map);
		}
		System.out.println(ben);
		LiCai tmp = new LiCai(6000000, 1700, limit);
		Map<String,Long> x = tmp.getDengXi4month(1);
		System.out.println(x.get("ben"));
		System.out.println(x.get("xi"));
	}

}
