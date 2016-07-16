package com.stock.model.tb;
import java.util.ArrayList;
import java.util.Arrays;


public class TbUtil {
	/*
	 * ���ڼ�����������������Ǹ�
	 */
	public double tbMax(double a,double b,double c){
			double result =a>=b?a>=c?a:c:b>=c?b:c;
			return result;
	}
	/*
	 * ���ڼ��������������С���Ǹ�
	 */
	public double tbMin(double a,double b,double c){
		double result =a<=b?a<=c?a:c:b<=c?b:c;
		return result;
	}
	/*
	 * �ϲ��������࣬������ص��򷵻���չ������࣬���û�ص�������Null
	 * zsHighPriceΪ���Ըߵ�����͵ģ�zsLowPriceΪ���Ե͵�����ߵ�
	 * 
	 */
	public Zs comZs(Zs zs1,Zs zs2){
		String beginDate = zs1.beginDate;
		String endDate = zs2.endDate;
		double lowPrice = tbMin(zs1.lowPrice, zs2.lowPrice, 9999);
		double highPrice = tbMax(zs1.highPrice,zs2.highPrice,0);
		double zsLowPrice = tbMax(zs1.zsLowPrice, zs2.zsLowPrice, 0);
		double zsHighPrice = tbMin(zs1.zsHighPrice, zs2.zsHighPrice, 9999);
		double[] arrHigh ={zs1.highPrice,zs1.sHighPrice,zs2.highPrice,zs2.sHighPrice};
		double[] arrLow ={zs1.lowPrice,zs1.sLowPrice,zs2.lowPrice,zs2.sLowPrice};
		//���ڻ�ȡ�θߵ㣬��Ϊ����ɾ�������Ƶ�ʱ��Ҫ�õ���
		double sHigh = getNum(arrHigh,3);
		double sLow = getNum(arrLow,3);
		if (zsHighPrice>=zsLowPrice){
			Zs zs =new Zs(beginDate, endDate, lowPrice, highPrice, zsLowPrice, zsHighPrice);
			zs.setsHighPrice(sHigh);
			zs.setsLowPrice(sLow);
			return zs;
		}
		return null;
	}
	//���ڻ�ȡallNum��������num�����֣��������������ġ�
	public double getNum(double[] allNum,int num){
		Arrays.sort(allNum);
		return allNum[num-1];
	}
	/*
	 * ��һ���������ݣ���Ҫ����K�߽�������
	 * �������ݣ�ȥ��������ϵ����K������ǰһ��ߵ�͵͵㶼�ں�һ���м䣬��ǰһ������ݿ����ų�
	 */
	public ArrayList firstClean(ArrayList al){
		for(int i=1;i<al.size()-1;i++){
			String[] lastRow = (String[]) al.get(i-1);
			String[] thisRow = (String[]) al.get(i);
			String[] nextRow = (String[]) al.get(i+1);
			if(isContain(thisRow, lastRow, 2, 4)){
				al.remove(i);
				i--;
			}
			if(!isContain(thisRow, lastRow, 2, 4)&&isContain(thisRow, nextRow, 2, 4)){
				al.remove(i);
				i--;
			}
		}
		return al;
	}
	
/*
 * �ڶ�����ϴ�����Peak����ϴ 
 * ��ϴ��ʽ���������ָߵ��ʱ��ȡ��ߵ㣻�������ֵ͵��ʱ��ȡ��͵㡣
 */
	public ArrayList<Peak> secondClean(ArrayList<Peak> al){
		for(int i=1;i<al.size()-1;i++){
			Peak thisPeak = (Peak) al.get(i);
			Peak nextPeak = (Peak) al.get(i+1);
			if(thisPeak.getIsHigh()==nextPeak.getIsHigh()){
				if(thisPeak.getIsHigh()){
					if(thisPeak.getPrice()<=nextPeak.getPrice()){
						al.remove(i);
						i--;
					}
					else{
						al.remove(i+1);
						i--;
					}
				}
				if(!thisPeak.getIsHigh()){
					if(thisPeak.getPrice()>=nextPeak.getPrice()){
						al.remove(i);
						i--;
					}
					else{
						al.remove(i+1);
						i--;
					}
				}
			}
		}
		return al;
	}
	
	//�ж�comp1��comp2�Ƿ��ǰ�����ϵ��highpos��lowpos�����������������ߵ�͵͵����ĸ�λ��
	//�������ʹ�ûز��ܵ�ʱ����Ҫ����
	public Boolean isContain(String[] comp1,String[] comp2,int highPos,int lowPos){
		Boolean result = false;
		double comp1High = Double.parseDouble(comp1[highPos]);
		double comp1Low = Double.parseDouble(comp1[lowPos]);
		double comp2High = Double.parseDouble(comp2[highPos]);
		double comp2Low = Double.parseDouble(comp2[lowPos]);
		if(comp1High<=comp2High && comp1Low>=comp2Low){
			result = true;
		}
		return result;
	}
	
	//list�е�����Ϊ���飬
	//��ȡlist�е�Peak�ߵ��Peak�͵㣬highPos��lowPos��������������У��ߵ�͵͵��λ��
	//ʹ�ûز��ܵ�ʱ����Ҫ����
	public ArrayList<Peak> getPeak(ArrayList al,int highPos,int lowPos){
		ArrayList<Peak> result = new ArrayList<Peak>();
		for(int i=1;i<al.size()-1;i++){
			String[] lastRow = (String[]) al.get(i-1);
			String[] thisRow = (String[]) al.get(i);
			String[] nextRow = (String[]) al.get(i+1);
			double comp1High = Double.parseDouble(lastRow[highPos]);
			double comp1Low = Double.parseDouble(lastRow[lowPos]);
			double comp2High = Double.parseDouble(thisRow[highPos]);
			double comp2Low = Double.parseDouble(thisRow[lowPos]);
			double comp3High = Double.parseDouble(nextRow[highPos]);
			double comp3Low = Double.parseDouble(nextRow[lowPos]);
			if(comp1High<=comp2High&&comp2High>=comp3High){
				Peak peak = new Peak(thisRow[0], comp2High, true);
				result.add(peak);
			}
			if(comp1Low>=comp2Low&&comp2Low<=comp3Low){
				Peak peak = new Peak(thisRow[0], comp2Low, false);
				result.add(peak);
			}
		}
		return result;
	}
	/*
	 * ȡ����Trend���ص�����
	 * �ж��Ƿ��н�����min(high1,high2,high3),max(low1,low2,low3)
	 * �θߵ�sHigh��sLow���������ƶ��ڸߵ���˵��ʵֻ��2����min�ʹθ���ʵ����ȵ�
	 */
	public Zs hasZs(Trend td1,Trend td2,Trend td3){
		TbUtil tu = new TbUtil();
		double zsLow=tu.tbMax(td1.lowPrice, td2.lowPrice, td3.lowPrice);
		double zsHigh=tu.tbMin(td1.HighPrice, td2.HighPrice, td3.HighPrice);
		double high=tu.tbMax(td1.HighPrice, td2.HighPrice, td3.HighPrice);
		double low=tu.tbMin(td1.lowPrice, td2.lowPrice, td3.lowPrice);
		double sHigh=tu.tbMin(td1.HighPrice, td2.HighPrice, td3.HighPrice);
		double sLow=tu.tbMax(td1.lowPrice, td2.lowPrice, td3.lowPrice);
		if(zsHigh>=zsLow){
			Zs zs = new Zs(td1.beginDate, td3.endDate, low, high, zsLow, zsHigh);
			zs.setsHighPrice(sHigh);
			zs.setsLowPrice(sLow);
			return zs;
		}
		return null;
	}
	
	public Trend getTrendByEndDay(String endDay,ArrayList<Trend> list){
		Trend result = new Trend();
		for (int i = list.size()-1;i>=0;i--){
			if (list.get(i).getEndDate().equals(endDay)){
				result = list.get(i);
			}
		}
		return result;
	}
}
