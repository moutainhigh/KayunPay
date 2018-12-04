package com.dutiantech.vo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.dutiantech.util.FileOperate;

public class Award {
	// 编号
	public String id;
	// 概率（0.1代表10%，最多3位小数，即千分之一级）
	public float probability;
	// 数量（该奖品剩余数量）
	public int count;

	public Award(String id, float probability, int count) {
		super();
		this.id = id;
		this.probability = probability;
		this.count = count;
	}

	public Award() {
	}
	
	public static Award lottery(List<Award> awards) {
		// 总概念区间
		float totalPro = 0f;
		// 存储每个奖品新概率区间
		List<Float> proSection = new ArrayList<Float>();
		proSection.add(0f);
		// 遍历每个奖品，设置概率区间，总的概率区间为每个概率区间的总和
		for (Award award : awards) {
			// 每个概率区间为奖品概率乘以1000（把三位小数转换为整）再乘以剩余奖品数量
			totalPro += award.probability * 10 * award.count;
			proSection.add(totalPro);
		}
		// 获取总的概率区间中的随机数
		System.out.println(proSection.toString());
		Random random = new Random();
		float randomPro = (float) random.nextInt((int)totalPro);
		// 判断取到的随机数在哪个奖品的概率区间中
		for(int i = 0, size = proSection.size(); i< size; i++) {
			 if (randomPro >= proSection.get(i) && randomPro < proSection.get(i + 1)) {
				 return awards.get(i);
			 }
		}
		return null;
	}

	public static void main(String[] args) {
		List<Award> awards = new ArrayList<Award>();
		awards.add(new Award("1元现金抵用券	", 0.16f, 0));
		awards.add(new Award("5元现金抵用券	", 0.16f, 150));
		awards.add(new Award("10元现金抵用券	", 0.16f, 100));
		awards.add(new Award("20元现金抵用券	", 0.16f, 50));
		awards.add(new Award("50元现金抵用券	", 0.16f, 20));
		awards.add(new Award("100元现金抵用券", 0.16f, 10));
		FileOperate fo = new FileOperate();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 330; i++) {
			Award award = lottery(awards);
//			System.out.println("恭喜您，抽到了：" + award.id);
			sb.append("恭喜您，抽到了：" + award.id).append("\r\n");
			award.count = award.count -1;
		}
		fo.createFile("E://DeryptMobile.txt", sb.toString());
	}
}
