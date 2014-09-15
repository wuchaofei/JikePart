//�û���¼
package com.jike.shanglv;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.jike.shanglv.Common.CommonFunc;
import com.jike.shanglv.Common.CustomProgressDialog;
import com.jike.shanglv.Enums.Platform;
import com.jike.shanglv.Enums.SPkeys;
import com.jike.shanglv.NetAndJson.HttpUtils;
import com.jike.shanglv.NetAndJson.JSONHelper;
import com.jike.shanglv.NetAndJson.UserInfo;

public class Activity_Login extends Activity {

	private ImageView back_imgbtn;
	private EditText uername_input_et, password_input_et;
	private ImageView autologin_checkbox_iv;
	private Button login_btn;
	private RelativeLayout autologin_rl;
	private TextView registernew_tv, forgetpassword_tv;
	private Context context;

	private Boolean auto = true;
	private Drawable checkedDrawable, uncheckedDrawable;
	private SharedPreferences sp;
	private String loginReturnJson;// ��¼��֤�󷵻صĽ������
	private CustomProgressDialog progressdialog;
	private boolean dialog_cancel = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_login);

		init();
	}

	private void init() {
		context = this;
		sp = getSharedPreferences(SPkeys.SPNAME.getString(), 0);
		auto = sp.getBoolean(SPkeys.autoLogin.getString(), true);

		checkedDrawable = context.getResources().getDrawable(
				R.drawable.fuxuankuang_yes);
		uncheckedDrawable = context.getResources().getDrawable(
				R.drawable.fuxuankuang_no);

		uername_input_et = (EditText) findViewById(R.id.uername_input_et);
		uername_input_et.setFocusable(false);
		uername_input_et.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				uername_input_et.setFocusableInTouchMode(true);
				return false;
			}
		});

		password_input_et = (EditText) findViewById(R.id.password_input_et);
		password_input_et.setFocusable(false);
		password_input_et.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				password_input_et.setFocusableInTouchMode(true);
				return false;
			}
		});

		if (auto) {
			uername_input_et.setText(sp.getString(
					SPkeys.lastUsername.getString(), ""));
			password_input_et.setText(sp.getString(
					SPkeys.lastPassword.getString(), ""));
			sp.edit()
					.putString(SPkeys.lastUsername.getString(),
							uername_input_et.getText().toString()).commit();
			sp.edit()
					.putString(SPkeys.lastPassword.getString(),
							password_input_et.getText().toString()).commit();
		}

		autologin_checkbox_iv = (ImageView) findViewById(R.id.autologin_checkbox_iv);
		login_btn = (Button) findViewById(R.id.login_btn);
		back_imgbtn = (ImageView) findViewById(R.id.back_imgbtn);
		autologin_rl = (RelativeLayout) findViewById(R.id.autologin_rl);
		registernew_tv = (TextView) findViewById(R.id.registernew_tv);
		forgetpassword_tv = (TextView) findViewById(R.id.forgetpassword_tv);

		login_btn.setOnClickListener(myListener);
		back_imgbtn.setOnClickListener(myListener);
		autologin_rl.setOnClickListener(myListener);
		registernew_tv.setOnClickListener(myListener);
		forgetpassword_tv.setOnClickListener(myListener);
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:// ��ȡ��¼���ص�����

				JSONTokener jsonParser;
				jsonParser = new JSONTokener(loginReturnJson);
				try {
					JSONObject jsonObject = (JSONObject) jsonParser.nextValue();
					String state = jsonObject.getString("c");

					if (state.equals("0000")) {
						String content = jsonObject.getString("d");
						sp.edit()
								.putString(SPkeys.UserInfoJson.getString(),
										content).commit();
						sp.edit()
								.putString(SPkeys.lastUsername.getString(),
										uername_input_et.getText().toString())
								.commit();
						sp.edit()
								.putString(SPkeys.lastPassword.getString(),
										password_input_et.getText().toString())
								.commit();
						sp.edit()
								.putBoolean(SPkeys.autoLogin.getString(), auto)
								.commit();

						// ���´��뽫�û���Ϣ�����л���SharedPreferences��
						UserInfo user = JSONHelper.parseObject(content,
								UserInfo.class);
						sp.edit().putString(SPkeys.userid.getString(),user.getUserid()).commit();
						sp.edit().putString(SPkeys.username.getString(),user.getUsername()).commit();
						sp.edit().putString(SPkeys.amount.getString(),user.getAmmount()).commit();
						sp.edit().putString(SPkeys.siteid.getString(),user.getSiteid()).commit();
						//������Ϣ�Ժ���ʱ������
						
						finish();
					} else {
						String message = jsonObject.getString("msg");
						// CustomProgressDialog cpd = new CustomProgressDialog(
						// context);
						// cpd.setMessage(message);
						// cpd.show();
						new AlertDialog.Builder(context).setTitle("��¼ʧ��")
								.setMessage(message)
								.setPositiveButton("ȷ��", null).show();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				progressdialog.dismiss();
				break;
			}
		}
	};

	OnClickListener myListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.back_imgbtn:
				finish();
				break;
			case R.id.autologin_rl:
				auto = !auto;
				if (auto) {
					autologin_checkbox_iv.setBackground(checkedDrawable);
				} else {
					autologin_checkbox_iv.setBackground(uncheckedDrawable);
				}
				break;
			case R.id.registernew_tv:
				startActivity(new Intent(context, Activity_Register.class));
				break;
			case R.id.forgetpassword_tv:// �һ�����
				startActivity(new Intent(context,
						Activity_RetrievePassword.class));
				break;
			case R.id.login_btn:
				if (uername_input_et.getText().toString().trim().length() == 0) {
					new AlertDialog.Builder(context).setTitle("�û�������Ϊ��")
							.setMessage("�������û���").setPositiveButton("ȷ��", null)
							.show();
					break;
				}
				if (password_input_et.getText().toString().trim().length() == 0) {
					new AlertDialog.Builder(context).setTitle("���벻��Ϊ��")
							.setMessage("����������").setPositiveButton("ȷ��", null)
							.show();
					break;
				}
				// ��¼��֤

				if (HttpUtils.showNetCannotUse(context)) {
					break;
				}
				
				new Thread(new Runnable() {
					@Override
					public void run() {
						int utype = 0;
						MyApp ma = new MyApp(context);
						Platform pf = ma.getPlatform();
						if (pf == Platform.B2B)
							utype = 1;
						else if (pf == Platform.B2C)
							utype = 2;
						String str = "{uname:'"
								+ uername_input_et.getText().toString().trim()
								+ "',upwd:'"
								+ password_input_et.getText().toString().trim()
								+ "',utype:" + utype + "}";
						String param = "action=userlogin&sitekey=&userkey="
								+ MyApp.userkey
								+ "&str="
								+ str
								+ "&sign="
								+ CommonFunc.MD5(MyApp.userkey + "userlogin"
										+ str);
						loginReturnJson = HttpUtils.getJsonContent(
								ma.getServeUrl(), param);
						Log.v("loginReturnJson", loginReturnJson);
						Message msg = new Message();
						msg.what = 1;
						handler.sendMessage(msg);
					}
				}).start();
				progressdialog = CustomProgressDialog.createDialog(context);
				progressdialog.setMessage("���ڵ�¼�����Ժ�...");
				progressdialog.setCancelable(true);
				progressdialog.setOnCancelListener(new OnCancelListener() {

					@Override
					public void onCancel(DialogInterface dialog) {
						dialog_cancel = true;
					}
				});
				progressdialog.show();

				// ��¼�󽫵�¼״̬��Ϊtrue
				sp.edit().putBoolean(SPkeys.loginState.getString(), true)
						.commit();
				break;
			default:
				break;
			}
		}
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}
}