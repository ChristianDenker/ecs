package de.jade.ecs.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.batik.bridge.URIResolver;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;

public class SVGUtils {

	public static void main(String[] args) {
		try {
			BufferedImage RTEWPT03image = SVGUtils.rasterize(new File("src/main/resources/s421/portrayal/Symbols/","RTEWPT03.svg"));
			RTEWPT03image.getWidth();
			System.out.println(new File("src/main/resources/s421/portrayal/Symbols/svgStyle.css").toURI().toString());
			URIResolver rui;
		} catch (IOException e) {
			e.printStackTrace();
		}
	System.out.println();
	}

	/** rasterize given .svg-file into BufferedImage
	 * https://stackoverflow.com/questions/11435671/how-to-get-a-bufferedimage-from-a-svg
	 * 
	 * @param svgFile
	 * @return
	 * @throws IOException
	 */
	public static BufferedImage rasterize(File svgFile) throws IOException {

	    final BufferedImage[] imagePointer = new BufferedImage[1];

	    try {

	        TranscoderInput input = new TranscoderInput(new FileInputStream(svgFile));
	        input.setURI(svgFile.toURI().toString());
	        ImageTranscoder t = new ImageTranscoder() {
 
	            @Override
	            public BufferedImage createImage(int w, int h) {
	                return new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
	            }

	            @Override
	            public void writeImage(BufferedImage image, TranscoderOutput out)
	                    throws TranscoderException {
	                imagePointer[0] = image;
	            }
	        };
//	        t.addTranscodingHint(ImageTranscoder.KEY_ALTERNATE_STYLESHEET, new File("src/main/resources/s421/portrayal/Symbols/svgStyle.css").toURI().toString());
//	        t.addTranscodingHint(SVGAbstractTranscoder.KEY_EXECUTE_ONLOAD, true);
	        t.transcode(input, null);
	        
	    }
	    catch (TranscoderException ex) {
	        // Requires Java 6
	        ex.printStackTrace();
	        throw new IOException("Couldn't convert " + svgFile);
	    }

	    return imagePointer[0];
	}
}
