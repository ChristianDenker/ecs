package de.jade.ecs.util;

import org.jxmapviewer.viewer.util.MercatorUtils;
import org.jxmapviewer.viewer.wms.WMSService;

public class WMSServiceImagePNG extends WMSService {

	/**
	 * @param baseUrl the base URL
	 * @param layer   the layer
	 */
	public WMSServiceImagePNG(String baseUrl, String layer) {
		super(baseUrl, layer);
	}

	@Override
	public String toWMSURL(int x, int y, int zoom, int tileSize) {
		String format = "image/png";
		String styles = "";
		String srs = "EPSG:4326";
		int ts = tileSize;
		int circumference = widthOfWorldInPixels(zoom, tileSize);
		double radius = circumference / (2 * Math.PI);
		double ulx = MercatorUtils.xToLong(x * ts, radius);
		double uly = MercatorUtils.yToLat(y * ts, radius);
		double lrx = MercatorUtils.xToLong((x + 1) * ts, radius);
		double lry = MercatorUtils.yToLat((y + 1) * ts, radius);
		String bbox = ulx + "," + uly + "," + lrx + "," + lry;
		
        String url = getBaseUrl() + "version=1.3.0&request=" + "GetMap&Layers=" + getLayer() + "&format=" + format
                + "&BBOX=" + bbox + "&width=" + ts + "&height=" + ts +  "&CRS=CRS:84" + "&Styles=" + styles +
                // "&transparent=TRUE"+
                "";

		return url;
	}

	private int widthOfWorldInPixels(int zoom, int TILE_SIZE) {
		// int TILE_SIZE = 256;
		int tiles = (int) Math.pow(2, zoom);
		int circumference = TILE_SIZE * tiles;
		return circumference;
	}

}
