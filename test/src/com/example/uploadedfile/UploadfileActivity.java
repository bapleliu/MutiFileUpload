package com.example.uploadedfile;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.test.R;

public class UploadfileActivity extends Activity {
	private static final String TAG = "uploadFile";
	private static final int TIME_OUT = 10 * 1000; // 超时时间
	private static final String CHARSET = "utf-8"; // 设置编码
	// 要上传的文件路径，理论上可以传输任何文件，实际使用时根据需要处理
	private static final String PATH = Environment
			.getExternalStorageDirectory().getPath();
	// private String uploadFile = PATH + "/changba_log740.txt";
	private String uploadFile = PATH + "/DCIM/Camera/1476237990329.jpg";
	private String uploadFile2 = PATH + "/DCIM/Camera/share.jpg";
	private String uploadFile3 = PATH + "/DCIM/Camera/1476163996685.jpg";
	private String uploadFile4 = PATH + "/ADownLoadTest/abc.jpg";

	private List<String> urlList;
	private String srcPath = PATH + "/DCIM/TRANS/test.txt";
	// 服务器上接收文件的处理页面，这里根据需要换成自己的
	private String actionUrl = "http://192.168.92.2:88/test.php";
	// private String actionUrl =
	// "http://190.168.1.17/mobileapp/android/getAppLog.php";
	private TextView mText1;
	private TextView mText2;
	private Button mButton;
	File file, file2, file3, file4;
	private List<File> fileList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mText1 = (TextView) findViewById(R.id.myText2);
		mText1.setText("文件路径：\n" + uploadFile);
		mText2 = (TextView) findViewById(R.id.myText3);
		mText2.setText("上传网址：\n" + actionUrl);

		urlList = new ArrayList<String>();
		urlList.add(uploadFile);
		urlList.add(uploadFile4);
		urlList.add(uploadFile2);
		urlList.add(uploadFile3);
		fileList = new ArrayList<File>();
		file = new File(uploadFile);
		file2 = new File(uploadFile2);
		file3 = new File(uploadFile3);
		file4 = new File(uploadFile4);
		fileList.add(file);
		fileList.add(file2);
		fileList.add(file3);
		fileList.add(file4);
		/* 设置mButton的onClick事件处理 */
		mButton = (Button) findViewById(R.id.myButton);
		mButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// uploadFile(actionUrl);
				System.out.println("------http://192.168.92.2:88-----"
						+ uploadFile);

				if (file != null) {
					new Thread(networkTask).start();

				}
				Toast.makeText(UploadfileActivity.this, "00000",
						Toast.LENGTH_LONG).show();
			}
		});
	}

	/**
	 * 网络操作相关的子线程
	 */
	Runnable networkTask = new Runnable() {

		@Override
		public void run() {
			// TODO
			// String request = UploadUtil.uploadFile(file, actionUrl);
			/**********************/
			Map<String, String> map = new HashMap<String, String>();
			map.put("data", "11223");
			map.put("da", "hhjjhh");
			String request = mutiUploadFile(actionUrl, fileList, map);
			/********************/

			// String request = MyUploadMultiFileSync(actionUrl, urlList, null);

			// uploadFile(actionUrl);
			// 在这里进行 http request.网络请求相关操作
			Message msg = new Message();
			Bundle data = new Bundle();
			data.putString("value", request);
			msg.setData(data);
			handler.sendMessage(msg);
		}

	};
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Bundle data = msg.getData();
			String val = data.getString("value");
			Log.i("mylog", "请求结果为-->" + val);
			// TODO
			// UI界面的更新等相关操作
		}
	};

	/* 上传文件至Server，uploadUrl：接收文件的处理页面 */
	private void uploadFile(String uploadUrl) {
		String end = "\r\n";
		String twoHyphens = "--";
		String boundary = "******";
		try {
			URL url = new URL(uploadUrl);
			HttpURLConnection httpURLConnection = (HttpURLConnection) url
					.openConnection();
			// 设置每次传输的流大小，可以有效防止手机因为内存不足崩溃
			// 此方法用于在预先不知道内容长度时启用没有进行内部缓冲的 HTTP 请求正文的流。
			httpURLConnection.setChunkedStreamingMode(128 * 1024);// 128K
			// 允许输入输出流
			httpURLConnection.setDoInput(true);
			httpURLConnection.setDoOutput(true);
			httpURLConnection.setUseCaches(false);
			// 使用POST方法
			httpURLConnection.setRequestMethod("POST");
			httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
			httpURLConnection.setRequestProperty("Charset", "UTF-8");
			httpURLConnection.setRequestProperty("Content-Type",
					"multipart/form-data;boundary=" + boundary);

			DataOutputStream dos = new DataOutputStream(
					httpURLConnection.getOutputStream());
			dos.writeBytes(twoHyphens + boundary + end);
			dos.writeBytes("Content-Disposition: form-data; name=\"attach\"; filename=\""
					+ srcPath.substring(srcPath.lastIndexOf("/") + 1)
					+ "\""
					+ end);
			dos.writeBytes(end);

			FileInputStream fis = new FileInputStream(srcPath);
			byte[] buffer = new byte[8192]; // 8k
			int count = 0;
			// 读取文件
			while ((count = fis.read(buffer)) != -1) {
				dos.write(buffer, 0, count);
			}
			fis.close();

			dos.writeBytes(end);
			dos.writeBytes(twoHyphens + boundary + twoHyphens + end);
			dos.flush();

			InputStream is = httpURLConnection.getInputStream();
			InputStreamReader isr = new InputStreamReader(is, "utf-8");
			BufferedReader br = new BufferedReader(isr);
			String result = br.readLine();

			Toast.makeText(this, result, Toast.LENGTH_LONG).show();
			dos.close();
			is.close();

		} catch (Exception e) {
			e.printStackTrace();
			setTitle(e.getMessage());
			System.out.println("==========" + e.getMessage());
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * 多文件上传
	 * 
	 * @param requestUrl
	 * @param file
	 */
	public String mutiUploadFile(String RequestURL, List<File> listFile,
			Map<String, String> params) {
		String result = null;
		Log.e(TAG, "result : " + result);
		String BOUNDARY = UUID.randomUUID().toString(); // 边界标识 随机生成
		String PREFIX = "--", LINE_END = "\r\n";
		String CONTENT_TYPE = "multipart/form-data"; // 内容类型

		try {
			URL url = new URL(RequestURL);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(TIME_OUT);
			conn.setConnectTimeout(TIME_OUT);
			conn.setDoInput(true); // 允许输入流
			conn.setDoOutput(true); // 允许输出流
			conn.setUseCaches(false); // 不允许使用缓存
			conn.setRequestMethod("POST"); // 请求方式
			conn.setRequestProperty("Charset", CHARSET); // 设置编码
			conn.setRequestProperty("connection", "keep-alive");
			conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary="
					+ BOUNDARY);

			/**
			 * 当文件不为空，把文件包装并且上传
			 */
			DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
			for (int i = 0; i < listFile.size(); i++) {

				if (listFile.get(i) != null) {
					StringBuffer sb = new StringBuffer();
					sb.append(PREFIX);
					sb.append(BOUNDARY);
					sb.append(LINE_END);
					/**
					 * 这里重点注意： name里面的值为服务器端需要key 只有这个key 才可以得到对应的文件
					 * filename是文件的名字，包含后缀名的 比如:abc.png
					 */

					sb.append("Content-Disposition: form-data; name=\"attach"
							+ i + "\"; filename=\"" + listFile.get(i).getName()
							+ "\"" + LINE_END);
					sb.append("Content-Type: application/octet-stream; charset="
							+ CHARSET + LINE_END);
					sb.append(LINE_END);
					dos.write(sb.toString().getBytes());
					InputStream is = new FileInputStream(listFile.get(i));
					byte[] bytes = new byte[1024];
					int len = 0;
					while ((len = is.read(bytes)) != -1) {
						dos.write(bytes, 0, len);
					}
					is.close();
					dos.write(LINE_END.getBytes());
					byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END)
							.getBytes();
					dos.write(end_data);
					dos.flush();
				}
			}

			// set 尾部
			StringBuilder sb2 = new StringBuilder();

			if (params != null && !params.isEmpty()) {
				for (String key : params.keySet()) {
					String value = params.get(key);
					Log.i("fff", "ff-->" + value);
					sb2.append(PREFIX);
					sb2.append(BOUNDARY);
					sb2.append(LINE_END);
					sb2.append("Content-Disposition: form-data; ");
					sb2.append("name=" + "\"");
					sb2.append(key + "\"");
					sb2.append(LINE_END);
					sb2.append(LINE_END);
					sb2.append(value);
					sb2.append(LINE_END);
				}
			}
			sb2.append(PREFIX + BOUNDARY + LINE_END);
			dos.writeBytes(sb2.toString());

			dos.flush();
			Log.i("hfdhsddfj", "sb2:" + sb2.toString());
			/**
			 * 获取响应码 200=成功 当响应成功，获取响应的流
			 */
			int res = conn.getResponseCode();
			Log.i("mylog", "code-->" + res);
			Log.e(TAG, "response code:" + res);
			// if(res==200)
			// {
			Log.e(TAG, "request success");
			InputStream input = conn.getInputStream();
			StringBuffer sb1 = new StringBuffer();
			int ss;
			while ((ss = input.read()) != -1) {
				sb1.append((char) ss);
			}
			result = sb1.toString();
			Log.e(TAG, "result : " + result);
			// }
			// else{
			// Log.e(TAG, "request error");
			// }

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;
	}

}