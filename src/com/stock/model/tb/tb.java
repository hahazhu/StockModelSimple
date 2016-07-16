package com.stock.model.tb;
import java.awt.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

import javax.swing.RowFilter;

public class tb {
	
	public static void main(String[] args) {
		//获取测试数据******************开始*************************
		TbUtil tu = new TbUtil();
		ArrayList row=new ArrayList();
		try {
	        BufferedReader reader = new BufferedReader(new FileReader("d:/data.csv"));//换成你的文件名
	        reader.readLine();//第一行信息，为标题信息，不用,如果需要，注释掉
	        String line = null;
	        while((line=reader.readLine())!=null){
	            String item[] = line.split(",");//CSV格式文件为逗号分隔符文件，这里根据逗号切分
	           
	            String last = item[item.length-1];//这就是你要的数据了
	            //int value = Integer.parseInt(last);//如果是数值，可以转化为数值
	            row.add(item);
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
		//获取测试数据******************结束*************************
		
		for(int i=0;i<row.size();i++){
			String[] temp = (String[]) row.get(i);
			System.out.print(temp[0]);System.out.print(",");
			System.out.print(temp[1]);System.out.print(",");
			System.out.print(temp[2]);System.out.print(",");
			System.out.print(temp[3]);System.out.print(",");
			System.out.println(temp[4]);System.out.print(",");
			System.out.println(temp[5]);
		}
		System.out.println("--------------------------完美分割线---------------------------");
		//对于K线做第一次处理
		tu.firstClean(row);
		for(int i=0;i<row.size();i++){
			String[] temp = (String[]) row.get(i);
			System.out.print(temp[0]);System.out.print(",");
			System.out.print(temp[1]);System.out.print(",");
			System.out.print(temp[2]);System.out.print(",");
			System.out.print(temp[3]);System.out.print(",");
			System.out.println(temp[4]);System.out.print(",");
			System.out.println(temp[5]);
		}
		System.out.println("--------------------------完美分割线---------------------------");
		/*
		 * 获取峰值，峰值的定义详见峰值类中的说明
		 * 补充当前收盘价为最新一个峰值。（如果前一个是高，这个就为低，反之相反）
		 */
		ArrayList<Peak> peakList=tu.getPeak(row, 2, 4);
		Peak lastPeak = (Peak)peakList.get(peakList.size()-1);
		if(lastPeak.isHigh){
			String[] temp = (String[]) row.get(row.size()-1);
			Peak virtualPeak = new Peak(temp[0],Double.parseDouble( temp[3]), false);
			peakList.add(virtualPeak);
		}else{
			String[] temp = (String[]) row.get(row.size()-1);
			Peak virtualPeak = new Peak(temp[0],Double.parseDouble( temp[3]), true);
			peakList.add(virtualPeak);
		}
		for(int i=0;i<peakList.size();i++){
			Peak temp = (Peak)peakList.get(i);
			System.out.print(temp.getDate());System.out.print(",");
			System.out.print(temp.getPrice());System.out.print(",");
			System.out.println(temp.getIsHigh());
		}
		System.out.println("--------------------------完美分割线---------------------------");
		
		//第二次清理：本次主要针对于Peak做清理
		peakList=tu.secondClean(peakList);
		for(int i=0;i<peakList.size();i++){
			Peak temp = (Peak)peakList.get(i);
			System.out.print(temp.getDate());System.out.print(",");
			System.out.print(temp.getPrice());System.out.print(",");
			System.out.println(temp.getIsHigh());
		}
		System.out.println("--------------------------完美分割线---------------------------");
		/*
		 * 两个Peak必然形成一个趋势
		 * 取到每个趋势，加入趋势列表
		 * 记录每个趋势的macd汇总，用于以后判断背离用
		 */
		ArrayList<Trend> trendList = new ArrayList<Trend>();
		for(int i=0;i<peakList.size()-1;i++){
			Peak peak = (Peak)peakList.get(i);
			Peak nextPeak=(Peak)peakList.get(i+1);
				double macdVol=0;
				Trend trend = new Trend(peak.date, nextPeak.date, peak.price<=nextPeak.price?peak.price:nextPeak.price, peak.price>=nextPeak.price?peak.price:nextPeak.price, peak.price>nextPeak.price?false:true, 0);
				for(int j=0;j<row.size();j++){
					String[] temp = (String[]) row.get(j);
					if ((temp[0].compareTo(trend.beginDate)>=0)&&(temp[0].compareTo(trend.endDate)<=0)){
						if((trend.isRise)&&(Double.parseDouble(temp[5])>0)){
							macdVol +=Double.parseDouble(temp[5]);
						}
						if((!trend.isRise)&&(Double.parseDouble(temp[5])<0)){
							macdVol +=Double.parseDouble(temp[5]);
						}
						
					}
				}
				trend.setMacdVol(macdVol);
				trendList.add(trend);
		}
		for(int i=0;i<trendList.size();i++){
			Trend trend = (Trend) trendList.get(i);
			System.out.println(trend.toString());
		}
		System.out.println("--------------------------完美分割线---------------------------");
		/*
		 * 获取每三个趋势的重叠部分，如有，就是一个小中枢
		 * 倒序处理，每每三个趋势去比较
		 * 
		 */
		ArrayList zsList = new ArrayList();
		ArrayList<Zs> bigZsList = new ArrayList<Zs>();
		Zs bigZs = new Zs();
		boolean hasQs=false;//上个中枢是否已经有对应的趋势了。
		boolean hasZs = false;//是否之前有一个中枢了
		for(int i = trendList.size()-1;i>0;i--){
			if((i-2)>=0){
				Trend thisTrend = (Trend) trendList.get(i-2);
				Trend nextTrend = (Trend) trendList.get(i-1);
				Trend nNextTrend = (Trend) trendList.get(i);
				Zs smallZs  = tu.hasZs(thisTrend,nextTrend,nNextTrend);
				//如果zs不是空，代表是有小中枢的
				//小中枢之间如果有重叠需要用comZs来扩展
				if(smallZs!=null){
					smallZs.setZsTrend(null);
					smallZs.setFirstTrend(thisTrend);
					smallZs.setSecondTrend(nextTrend);
					smallZs.setThirdTrend(nNextTrend);
					zsList.add(smallZs);
					if(hasZs){
						bigZs=tu.comZs(smallZs,bigZs);
					}else{
						bigZs= smallZs;
					}
					hasZs = true;
				}
				//如果这三个趋势不形成中枢，第二个趋势肯定是一个真正的上涨或者下跌趋势
				//一旦出现这种情况，之前一个大中枢就形成了（当然，如果第一个循环就不形成中枢，之前就不能形成大中枢了）
				else{
					if(i != trendList.size()-1){
						bigZs.setZsTrend(nextTrend);
						bigZsList.add(bigZs);
						bigZs= new Zs();
						hasZs=false;
					}
				}
			}
		}
		for(int i=0;i<bigZsList.size();i++){
			Zs zs = (Zs) bigZsList.get(i);
			System.out.println(zs.toString());
		}
		System.out.println("--------------------------完美分割线---------------------------");
		/*
		 * 上面得到的bigZsList是包含了首尾趋势的中枢。现在需要删除首尾趋势
		 * 删除首尾的算法详见Zs类
		 */
		ArrayList zsTrendList = new ArrayList();
		for (int i=0;i<bigZsList.size();i++){
			bigZsList.get(i).delTrend(bigZsList.get(i).getZsTrend());
			if((i>=0)&&(i<bigZsList.size()-1)){
				bigZsList.get(i+1).delTrend(bigZsList.get(i).getZsTrend());
			}
		}
		for(int i=0;i<bigZsList.size();i++){
			Zs zs = (Zs) bigZsList.get(i);
			System.out.println(zs.toString());
		}
		
		/*
		 * 最终判断三买：
		 * 1、上一个中枢（前）的趋势是上涨的
		 * 2、今天的收盘价格高于上个中枢的高点
		 * 3、今天的收盘价低于上个Peak(上个Peak为高点）
		 * 4、这个趋势没有出现背离
		 * 
		 */
		Zs lastZs = bigZsList.get(0);
		String[] lastRow = (String[]) row.get(row.size()-1); 
		Double lastClose = Double.parseDouble(lastRow[3]);
		Trend lastSecTrend = trendList.get(trendList.size()-2);
		if(	lastZs.zsTrend.getIsRise()&&
				(lastZs.getZsHighPrice()<lastClose)&&
				(lastPeak.getPrice()>lastClose)&&
				(lastSecTrend.getMacdVol()>lastZs.getZsTrend().getMacdVol())
			){
			System.out.println("最近可能正在形成三买");
		}else{
			System.out.println("暂不买入");
		}
	}
}
