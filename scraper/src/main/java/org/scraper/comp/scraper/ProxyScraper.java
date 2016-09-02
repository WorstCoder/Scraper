package org.scraper.comp.scraper;

import com.sun.org.apache.xml.internal.serializer.utils.Utils;
//import com.sun.org.apache.xpath.internal.operations.String;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.lept;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.opencv.core.Core;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.scraper.comp.web.Browser;
import org.scraper.comp.web.BrowserVersion;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


import org.opencv.core.Mat;

import static org.opencv.imgproc.Imgproc.*;


public class ProxyScraper {


	public static void main(String... args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		ProxyScraper ps = new ProxyScraper();
		//ProxyChceker pc = new ProxyChceker();
		//ps.ocrScraper("https://www.torvpn.com/en/proxy-list");
		//ps.normalScrape("http://proxylist.hidemyass.com/");
		ProxyChceker.checkProxy("66.182.125.8:16318", 10000);
		WebDriver driver = Browser.getBrowser(null, BrowserVersion.random(), "p");
		ps.cssScrape("http://proxylist.hidemyass.com/", driver);
	}

	public List<String> normalScrape(String url) {
		List<String> proxy = new ArrayList<>();

		try {
			Document doc = Jsoup.connect(url).timeout(10000).userAgent(BrowserVersion.random().ua()).get();

			String txt = doc.tagName("body").text();

			proxy = regexMatcher(txt);

			proxy.stream()
					.map(e -> ProxyChceker.checkProxy(e, 3000))
					.forEach(System.out::println);

		} catch (IOException e) {
			e.printStackTrace();
		}
		return proxy;
	}

	public List<String> cssScrape(String url, WebDriver driver) {
		List<String> proxy;

		driver.get(url);
		String copyableBeforeAfter = "var all = document.getElementsByTagName(\"*\");\n" +
				"for(var i=0;i<all.length;i++){\n" +
				"\tvar e = all.item(i);\n" +
				"if(e.tagName==\"BODY\") continue;\n" +
				"\ttry{\n" +
				"\t\tvar before = getComputedStyle(e,\":before\").content.replace(/\\\"/g,\"\");\n" +
				"\t\tvar after = getComputedStyle(e,\":after\").content.replace(/\\\"/g,\"\");\n" +
				"\t\t\tif(before != \"\" || after != \"\") e.innerHTML = before + e.innerHTML + after;\n" +
				"\t}catch(ex){}\n" +
				"}";
		((JavascriptExecutor) driver).executeScript(copyableBeforeAfter);

		String text = driver.findElement(By.tagName("body")).getText();

		proxy = regexMatcher(text);

		try {
			new ForkJoinPool(10).submit(() ->
					proxy.stream()
							.parallel()
							.map(e -> ProxyChceker.checkProxy(e, 10000))
							.forEach(System.out::println)).get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}

		return proxy;
	}

	public List<String> ocrScraper(String url) {
		List<String> proxy = null;
		try {
			Document doc = Jsoup.connect(url).timeout(10000).userAgent(BrowserVersion.random().ua()).get();


			String mainUrl = doc.baseUri().substring(0, doc.baseUri().indexOf("/", 8));
			List<String> imgsUrls = new ArrayList<>();

			Elements imgs = doc.getElementsByTag("img");

			long l = System.currentTimeMillis();

			for (Element e : imgs) {
				if (e.attr("src").charAt(0) == '/') {
					imgsUrls.add(mainUrl + e.attr("src"));
				} else {
					imgsUrls.add(e.attr("src"));
				}
			}

			System.out.println(System.currentTimeMillis() - l);
			l = System.currentTimeMillis();

			/*imgsUrls.parallelStream()
					.map(e -> {
						try {
							return Jsoup.connect(e).ignoreContentType(true).execute().bodyAsBytes();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						return null;
					})
					.forEach(e -> {
						Mat mat = Imgcodecs.imdecode(new MatOfByte(e), Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
						ocrFilter(mat);
						Imgcodecs.imwrite("D:/mat.tiff", mat);
						String ret = Tess.read(mat);
						System.out.println(":   " + ret);
					});
			System.out.println("PS"+(System.currentTimeMillis() - l));*/


			ExecutorService executor = Executors.newFixedThreadPool(30);

			l = System.currentTimeMillis();
			for (String iurl : imgsUrls) {
				long finalL = l;
				executor.execute(() -> {
					try {
						byte[] imgBytes = Jsoup.connect(iurl).ignoreContentType(true).execute().bodyAsBytes();

						Mat mat = Imgcodecs.imdecode(new MatOfByte(imgBytes), Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
						ocrFilter(mat);
						Imgcodecs.imwrite("D:/mat.tiff", mat);
						String ret = Tess.read(mat);
						System.out.println(":   " + ret + (System.currentTimeMillis() - finalL));
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				});
			}

			executor.awaitTermination(1, TimeUnit.DAYS);
			executor.shutdown();
			System.out.println("ES" + (System.currentTimeMillis() - l));
			l = System.currentTimeMillis();

			String imgUrl = imgsUrls.get(4);
			byte[] imgBytes = Jsoup.connect(imgUrl).ignoreContentType(true).execute().bodyAsBytes();
			l = System.currentTimeMillis();

			Mat mat = Imgcodecs.imdecode(new MatOfByte(imgBytes), Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
			ocrFilter(mat);
			Imgcodecs.imwrite("D:/mat.tiff", mat);
			String ret = Tess.read(mat);
			System.out.println((System.currentTimeMillis() - l) + ":   " + ret);

			imgs.get(0);
		} catch (IOException e) {
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return proxy;
	}

	private List<String> regexMatcher(String text) {
		Pattern ipRegex = Pattern.compile("[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}");
		Pattern portRegex = Pattern.compile("[0-9]{1,4}");
		Pattern ipPort = Pattern.compile(ipRegex + ":" + portRegex);

		List<String> proxy = new ArrayList<>();
		Matcher ipMatcher = ipRegex.matcher(text);
		Matcher portMatcher = portRegex.matcher(text);

		String tempProxy;
		while (ipMatcher.find()) {
			tempProxy = "";
			tempProxy += ipMatcher.group() + ":";

			if (portMatcher.find(ipMatcher.end())) {
				tempProxy += portMatcher.group();
				if (tempProxy.matches(ipPort.toString())) {

					proxy.add(tempProxy);
				}
			}
		}
		return proxy;
	}

	private void ocrFilter(Mat image) {
		Integer sizeMult = 8;
		resize(image, image, new Size((int) (image.size().width * sizeMult), (int) (image.size().height * sizeMult)));
		image.convertTo(image, 0, 2, -255);
		adaptiveThreshold(image, image, 255, ADAPTIVE_THRESH_GAUSSIAN_C, THRESH_BINARY, 255, 1);
	}


}
