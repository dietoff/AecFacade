package com.gtranslate;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

public class Audio {
	private static Audio audio;

	private Audio() {
	}

	public synchronized static Audio getInstance() {

		if (audio == null) {
			audio = new Audio();
		}
		return audio;
	}

	public InputStream getAudio(String text, String languageOutput)
			throws IOException {

			String encode = URLEncoder.encode(text.substring(0,25));
			URL url = new URL(URLCONSTANTS.GOOGLE_TRANSLATE_AUDIO + encode.replace(" ", "%20") + "&tl=" + languageOutput);
			System.out.println(url.toString());
			URLConnection urlConn = url.openConnection();
			urlConn.addRequestProperty("User-Agent",
					"Mozilla/5.0 (Macintosh; Intel Mac OS X 10.9; rv:36.0) Gecko/20100101 Firefox/36.0");
			InputStream audioSrc = urlConn.getInputStream();
			return new BufferedInputStream(audioSrc);
		
	}

	public void play(InputStream sound) throws JavaLayerException {
		new Player(sound).play();
	}

}
