package com.stock.model.tb;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class tb {
	
	public static void main(String[] args) {
		//��ȡ��������******************��ʼ*************************
		TbUtil tu = new TbUtil();
		ArrayList row=new ArrayList();
		try {
	        BufferedReader reader = new BufferedReader(new FileReader("d:/data.csv"));//��������ļ���
	        reader.readLine();//��һ����Ϣ��Ϊ������Ϣ������,�����Ҫ��ע�͵�
	        String line = null;
	        while((line=reader.readLine())!=null){
	            String item[] = line.split(",");//CSV��ʽ�ļ�Ϊ���ŷָ����ļ���������ݶ����з�
	           
	            String last = item[item.length-1];//�������Ҫ��������
	            //int value = Integer.parseInt(last);//�������ֵ������ת��Ϊ��ֵ
	            row.add(item);
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
		//��ȡ��������******************����*************************
		
		for(int i=0;i<row.size();i++){
			String[] temp = (String[]) row.get(i);
			System.out.print(temp[0]);System.out.print(",");
			System.out.print(temp[1]);System.out.print(",");
			System.out.print(temp[2]);System.out.print(",");
			System.out.print(temp[3]);System.out.print(",");
			System.out.println(temp[4]);System.out.print(",");
			System.out.println(temp[5]);
		}
		System.out.println("--------------------------�����ָ���---------------------------");
		//����K������һ�δ���
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
		System.out.println("--------------------------�����ָ���---------------------------");
		/*
		 * ��ȡ��ֵ����ֵ�Ķ��������ֵ���е�˵��
		 * ���䵱ǰ���̼�Ϊ����һ����ֵ�������ǰһ���Ǹߣ������Ϊ�ͣ���֮�෴��
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
		System.out.println("--------------------------�����ָ���---------------------------");
		
		//�ڶ�������������Ҫ�����Peak������
		peakList=tu.secondClean(peakList);
		for(int i=0;i<peakList.size();i++){
			Peak temp = (Peak)peakList.get(i);
			System.out.print(temp.getDate());System.out.print(",");
			System.out.print(temp.getPrice());System.out.print(",");
			System.out.println(temp.getIsHigh());
		}
		System.out.println("--------------------------�����ָ���---------------------------");
		/*
		 * ����Peak��Ȼ�γ�һ������
		 * ȡ��ÿ�����ƣ����������б�
		 * ��¼ÿ�����Ƶ�macd���ܣ������Ժ��жϱ�����
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
		System.out.println("--------------------------�����ָ���---------------------------");
		/*
		 * ��ȡÿ�������Ƶ��ص����֣����У�����һ��С����
		 * ������ÿÿ��������ȥ�Ƚ�
		 * 
		 */
		ArrayList zsList = new ArrayList();
		ArrayList<Zs> bigZsList = new ArrayList<Zs>();
		Zs bigZs = new Zs();
		boolean hasQs=false;//�ϸ������Ƿ��Ѿ��ж�Ӧ�������ˡ�
		boolean hasZs = false;//�Ƿ�֮ǰ��һ��������
		for(int i = trendList.size()-1;i>0;i--){
			if((i-2)>=0){
				Trend thisTrend = (Trend) trendList.get(i-2);
				Trend nextTrend = (Trend) trendList.get(i-1);
				Trend nNextTrend = (Trend) trendList.get(i);
				Zs smallZs  = tu.hasZs(thisTrend,nextTrend,nNextTrend);
				//���zs���ǿգ���������С�����
				//С����֮��������ص���Ҫ��comZs����չ
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
				//������������Ʋ��γ����࣬�ڶ������ƿ϶���һ�����������ǻ����µ�����
				//һ���������������֮ǰһ����������γ��ˣ���Ȼ�������һ��ѭ���Ͳ��γ����֮࣬ǰ�Ͳ����γɴ������ˣ�
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
		System.out.println("--------------------------�����ָ���---------------------------");
		/*
		 * ����õ���bigZsList�ǰ�������β���Ƶ����ࡣ������Ҫɾ����β����
		 * ɾ����β���㷨���Zs��
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
		 * �����ж�����
		 * 1����һ�����ࣨǰ�������������ǵ�
		 * 2����������̼۸�����ϸ�����ĸߵ�
		 * 3����������̼۵����ϸ�Peak(�ϸ�PeakΪ�ߵ㣩
		 * 4���������û�г��ֱ���
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
			System.out.println("������������γ�����");
		}else{
			System.out.println("�ݲ�����");
		}
	}
}
