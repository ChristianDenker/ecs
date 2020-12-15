package de.jade.ecs;

import java.io.FileInputStream;

import s57.S57dec;
import s57.S57map;

public class Application {
	public static void main(String[] args) {
		try {
			FileInputStream in = new FileInputStream("D:\\Shipping\\Alte ENC zum Üben\\DE421070.000");
			S57map s57map = new S57map(true);
			S57dec.decodeChart(in, s57map);
//			ArrayList<Feature> list = s57map.features.get(Obj.SOUNDG);
			ChartViewer chartViewer = new ChartViewer();
			chartViewer.addSeachart(s57map);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}