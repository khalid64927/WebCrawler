package com.khalid.crawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import android.graphics.drawable.Drawable;
import android.util.Log;

public class HTTPRequest {
	private ArrayList<NameValuePair> mParams;
	private ArrayList<NameValuePair> mHeaders;
	private String mURL;
	private int mResponseCode;
	private String mMessage;
	private String mResponseString;
	private static final String TAG = "HTTPRequest";
	private InputStream mMediaStream;
	private int mBuffferSize;

	
	public enum RequestMethod {
		GET, POST
	}

	public HTTPRequest(String url) {
		this.mURL = url;
		mParams = new ArrayList<NameValuePair>();
		mHeaders = new ArrayList<NameValuePair>();
	}

	public void addParam(String name, String value) {
		mParams.add(new BasicNameValuePair(name, value));
	}

	public void addHeader(String name, String value) {
		mHeaders.add(new BasicNameValuePair(name, value));
	}
	
	public boolean checkContent(String urlString){
		boolean isText = true;
		URL url;
		try {
			url = new URL(urlString);
			URLConnection connection = url.openConnection();
			if ((connection.getContentType() != null)
					&& !connection.getContentType().toLowerCase()
							.startsWith("text/")) {
				isText = false;
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return isText;
	}

	public void execute(RequestMethod method) throws Exception {
		BasicHttpParams httpParams = new BasicHttpParams();
		switch (method) {
		case GET: {
			// Add parameters
			String combinedParams = "";
			if (!mParams.isEmpty()) {
				httpParams = setParams(mParams);
			}

			HttpGet request = new HttpGet(mURL);

			// Add headers
			for (NameValuePair h : mHeaders)
				request.addHeader(h.getName(), h.getValue());

			executeRequest(request, mURL, httpParams);
			break;
		}
		case POST: {
			// ignoring post connection
			break;
		}
		}
	}

	private BasicHttpParams setParams(ArrayList<NameValuePair> params) {
		BasicHttpParams httpParams = new BasicHttpParams();
		for (NameValuePair p : params) {
			httpParams.setParameter(p.getName(), p.getValue());
		}
		HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(httpParams, HTTP.UTF_8);
		return httpParams;
	}

	private void executeRequest(HttpUriRequest request, String url,
			BasicHttpParams params) {
		HttpClient client = new DefaultHttpClient();
		try {
			KeyStore trustStore = KeyStore.getInstance(KeyStore
					.getDefaultType());
			trustStore.load(null, null);
			SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
			sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory
					.getSocketFactory(), 80));
			registry.register(new Scheme("https", sf, 443));
			// removed

			ClientConnectionManager ccm = new ThreadSafeClientConnManager(
					params, registry);
			HttpResponse httpResponse;
			httpResponse = client.execute(request);
			
			
			mResponseCode = httpResponse.getStatusLine().getStatusCode();
			
			mMessage = httpResponse.getStatusLine().getReasonPhrase();
			HttpEntity entity = httpResponse.getEntity();
			InputStream instream;
			instream = entity.getContent();
			if (entity != null) {
				
				if (entity.getContentType().getValue().contains("text")) {
					Header contentEncoding = httpResponse.getFirstHeader("Content-Encoding");
					
					if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
					    instream = new GZIPInputStream(instream);
					}
					mResponseString = convertStreamToString(instream);
					// Closing the input stream will trigger connection release
					instream.close();
				}else{
					mMediaStream = instream;
					
				}	
			}

		}catch (ClientProtocolException e) {
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
		} catch (KeyStoreException e1) {
			e1.printStackTrace();
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		} catch (CertificateException e1) {
			e1.printStackTrace();
		} catch (KeyManagementException e1) {
			e1.printStackTrace();
		} catch (UnrecoverableKeyException e1) {
			e1.printStackTrace();
		}catch (Exception e1) {
			e1.printStackTrace();
		}
	}
	
	public InputStream getMediaStream(){
		return mMediaStream;
	}

	private static String convertStreamToString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null)
				sb.append(line + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	private static Drawable convertStreamToDrawable(InputStream is) {
		return Drawable.createFromStream(is, null);
	}

	public String getResponseString() {
		return mResponseString;
	}

	public String getErrorMessage() {
		return mMessage;
	}

	public int getResponseCode() {
		return mResponseCode;
	}

	public int getmBuffferSize() {
		return mBuffferSize;
	}
}

class MySSLSocketFactory extends SSLSocketFactory {
	private SSLContext sslContext = SSLContext.getInstance("TLS");

	public MySSLSocketFactory(final KeyStore truststore)
			throws NoSuchAlgorithmException, KeyManagementException,
			KeyStoreException, UnrecoverableKeyException {
		super(truststore);

		TrustManager tm = new X509TrustManager() {

			@Override
			public X509Certificate[] getAcceptedIssuers() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void checkServerTrusted(X509Certificate[] chain,
					String authType) throws CertificateException {
				// TODO Auto-generated method stub

			}

			@Override
			public void checkClientTrusted(X509Certificate[] chain,
					String authType) throws CertificateException {
				// TODO Auto-generated method stub

			}
		};
		sslContext.init(null, new TrustManager[] { tm }, null);
		// sslContext.init(null, new TrustManager[] { tm }, null);

	}

	@Override
	public Socket createSocket(final Socket socket, final String host,
			final int port, final boolean autoClose) throws IOException {
		return sslContext.getSocketFactory().createSocket(socket, host, port,
				autoClose);
	}

	@Override
	public Socket createSocket() throws IOException {
		return sslContext.getSocketFactory().createSocket();
	}

}
