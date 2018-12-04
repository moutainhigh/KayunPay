package com.dutiantech.util;

public class LocationUtil {

	public static final double EARTH_RADIUS = 6378.137; // 地球半径，平均半径为6371km

	private static double rad(double d) {
		return d * Math.PI / 180.0;
	}

	/**
	 * 1. Lat1 Lung1 表示A点经纬度，Lat2 Lung2 表示B点经纬度； 2. a=Lat1 – Lat2 为两点纬度之差
	 * b=Lung1 -Lung2 为两点经度之差； 3. 6378.137为地球半径，单位为千米； 计算出来的结果单位为千米。
	 * 
	 * @param lat1
	 * @param lng1
	 * @param lat2
	 * @param lng2
	 * @return
	 */
	public static double getDistance(double lat1, double lng1, double lat2,
			double lng2) {
		double radLat1 = rad(lat1);
		double radLat2 = rad(lat2);
		double a = radLat1 - radLat2;
		double b = rad(lng1) - rad(lng2);
		double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
				+ Math.cos(radLat1) * Math.cos(radLat2)
				* Math.pow(Math.sin(b / 2), 2)));
		s = s * EARTH_RADIUS;
		s = Math.round(s * 10000) / 10000;
		return s;
	}

	/**
	 * 计算方位角pab， 其中lat_a, lng_a是A的纬度和经度； lat_b, lng_b是B的纬度和经度
	 * 
	 * @param lat_a
	 * @param lng_a
	 * @param lat_b
	 * @param lng_b
	 * @return
	 */
	@SuppressWarnings("unused")
	private double gps2d(double lat_a, double lng_a, double lat_b, double lng_b) {
		double d = 0;
		lat_a = lat_a * Math.PI / 180;
		lng_a = lng_a * Math.PI / 180;
		lat_b = lat_b * Math.PI / 180;
		lng_b = lng_b * Math.PI / 180;

		d = Math.sin(lat_a) * Math.sin(lat_b) + Math.cos(lat_a)
				* Math.cos(lat_b) * Math.cos(lng_b - lng_a);
		d = Math.sqrt(1 - d * d);
		d = Math.cos(lat_b) * Math.sin(lng_b - lng_a) / d;
		d = Math.asin(d) * 180 / Math.PI;
		// d = Math.round(d*10000);
		return d;
	}

	// 将角度转换为弧度
	static double deg2rad(double degree) {
		return degree / 180 * Math.PI;
	}

	// 将弧度转换为角度
	static double rad2deg(double radian) {
		return radian * 180 / Math.PI;
	}

	/**
	 * 计算某个经纬度的周围某段距离的正方形的四个点
	 *
	 * @param lng
	 *            float 经度
	 * @param lat
	 *            float 纬度
	 * @param distance
	 *            float 该点所在圆的半径，该圆与此正方形内切，默认值为0.5千米
	 * @return array 正方形的四个点的经纬度坐标
	 */
	public static double[][] returnSquarePoint(double lat, double lng,
			double distance) {
		double dlng = 2 * Math.asin(Math.sin(distance / (2 * EARTH_RADIUS))
				/ Math.cos(deg2rad(lat)));
		dlng = rad2deg(dlng);
		double dlat = distance / EARTH_RADIUS;
		dlat = rad2deg(dlat);
		double[] leftTop = { lat + dlat, lng - dlng };
		double[] rightTop = { lat + dlat, lng + dlng };
		double[] leftBottom = { lat - dlat, lng - dlng };
		double[] rightBottom = { lat - dlat, lng + dlng };
		double[][] squarePoint = { leftTop, rightTop, leftBottom, rightBottom };
		return squarePoint;
	}

	@SuppressWarnings("unused")
	public void test(double lat, double lng) {
		// 使用此函数计算得到结果后，带入sql查询。
		double[][] $squares = returnSquarePoint(lng, lat, 0.5);
		String infoSql = "select id,locateinfo,lat,lng from `lbs_info` where lat<>0 and lat>{$squares['right-bottom']['lat']} and lat<{$squares['left-top']['lat']} and lng>{$squares['left-top']['lng']} and lng<{$squares['right-bottom']['lng']} ";
	}

}
