package com.jike.shanglv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.jike.shanglv.Common.CommonFunc;
import com.jike.shanglv.Common.CustomProgressDialog;
import com.jike.shanglv.Enums.SPkeys;
import com.jike.shanglv.Models.TrainInfo;
import com.jike.shanglv.NetAndJson.HttpUtils;
import com.jike.shanglv.NetAndJson.JSONHelper;

public class ActivityTrainSearchlist extends Activity {

	private Context context;
	private ImageButton back_imgbtn;
	private TextView title_tv, total_train_count_tv,sort_type_tv,sort_time_tv;
	private LinearLayout bytraintype_LL, bytime_ll;
	private ImageView sort_type_iv,sort_time_iv;
	private ListView listview;
	private String startcity_code = "", arrivecity_code = "", startcity = "",
			arrivecity = "", startoff_date = "";// ������ҳ���ȡ������
	private SharedPreferences sp;
	private CustomProgressDialog progressdialog;
	private String trainsReturnJson;// ���صĲ�ѯ�б�json

	private ListAdapter adapter;
	private ArrayList<TrainInfo> train_List;
	private Boolean byTimeAsc=false,byTypeAsc=false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_train_searchlist);
		initView();

		startQuery();
	}

	private void initView() {
		context = this;
		sp = getSharedPreferences(SPkeys.SPNAME.getString(), 0);
		train_List = new ArrayList<TrainInfo>();
		back_imgbtn = (ImageButton) findViewById(R.id.back_imgbtn);
		listview = (ListView) findViewById(R.id.listview);
		title_tv = (TextView) findViewById(R.id.title_tv);
		back_imgbtn.setOnClickListener(btnClickListner);
		total_train_count_tv = (TextView) findViewById(R.id.total_train_count_tv);
		bytraintype_LL = (LinearLayout) findViewById(R.id.bytraintype_LL);
		bytime_ll = (LinearLayout) findViewById(R.id.bytime_ll);
		sort_type_tv=(TextView) findViewById(R.id.sort_type_tv);
		sort_time_tv=(TextView) findViewById(R.id.sort_time_tv);
		sort_type_iv=(ImageView) findViewById(R.id.sort_type_iv);
		sort_time_iv=(ImageView) findViewById(R.id.sort_time_iv);
		bytraintype_LL.setOnClickListener(btnClickListner);
		bytime_ll.setOnClickListener(btnClickListner);

		getIntentData();

		title_tv.setText(startcity + "-" + arrivecity);
	}

	// ��ȡIntent����,������ҳ�������������ʹ��
	private void getIntentData() {
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			if (bundle.containsKey("startcity_code"))
				startcity_code = bundle.getString("startcity_code");// ����������
			if (bundle.containsKey("arrivecity_code"))
				arrivecity_code = bundle.getString("arrivecity_code");
			if (bundle.containsKey("startcity"))
				startcity = bundle.getString("startcity");// ��������
			if (bundle.containsKey("arrivecity"))
				arrivecity = bundle.getString("arrivecity");
			if (bundle.containsKey("startoff_date"))
				startoff_date = bundle.getString("startoff_date");
		}
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				JSONTokener jsonParser;
				jsonParser = new JSONTokener(trainsReturnJson);
				try {
					if (trainsReturnJson.length()==0) {
						new AlertDialog.Builder(context).setTitle("δ�鵽�ó��ε��г���Ϣ")
						.setPositiveButton("ȷ��", null).show();
						progressdialog.dismiss();
						break;
					}
					JSONObject jsonObject = (JSONObject) jsonParser.nextValue();
					String state = jsonObject.getString("c");

					if (state.equals("0000")) {
						JSONArray jasonlist = jsonObject.getJSONArray("d");
						createList(jasonlist);
						total_train_count_tv.setText("��" + train_List.size()
								+ "��");
						adapter = new ListAdapter(context, train_List);
						listview.setAdapter(adapter);
						listview.setOnItemClickListener(new OnItemClickListener() {
							@Override
							public void onItemClick(AdapterView<?> parent,
									View view, int position, long id) {
								TrainInfo ti = train_List.get(position);
								Intent intents = new Intent(
										context,
										ActivityTrainBooking.class);
								intents.putExtra("TrainInfoString", JSONHelper.toJSON(ti));
								intents.putExtra("startcity", startcity);
								intents.putExtra("arrivecity", arrivecity);
								intents.putExtra("startdate",startoff_date);
								startActivity(intents);
							}
						});

					} else {
						String message = jsonObject.getString("msg");
						new AlertDialog.Builder(context).setTitle("��ѯʧ��")
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

	/**
	 * ����list����
	 * 
	 * @param flist_list
	 */
	private void createList(JSONArray flist_list) {
		train_List.clear();
		for (int i = 0; i < flist_list.length(); i++) {
			try {
				TrainInfo ti = JSONHelper.parseObject(
						flist_list.getJSONObject(i), TrainInfo.class);
				if (!ti.getWZ_Y().toString().equals("null")
						&& !ti.getWZ_Y().toString().equals("0")
						&& !ti.getWZ_Y().toString().equals("")) {
					TrainInfo ti1 = (TrainInfo) ti.clone();
					ti1.setRemain_Count(ti.getWZ_Y().toString());
					ti1.setPrice(ti.getWZ());
					ti1.setSeat_Type("����");
					train_List.add(ti1);
				}

				if ((!ti.getRW_Y().toString().equals("null")
						&& !ti.getRW_Y().toString().equals("0") && !ti
						.getRW_Y().toString().equals(""))) {
					TrainInfo ti2 = (TrainInfo) ti.clone();
					ti2.setRemain_Count(ti.getRW_Y().toString());
					ti2.setPrice(ti.getRW());
					ti2.setSeat_Type("����");
					train_List.add(ti2);
				}

				if ((!ti.getYW_Y().toString().equals("null")
						&& !ti.getYW_Y().toString().equals("0") && !ti
						.getYW_Y().toString().equals(""))) {
					TrainInfo ti3 = (TrainInfo) ti.clone();
					ti3.setRemain_Count(ti.getYW_Y().toString());
					ti3.setPrice(ti.getYW());
					ti3.setSeat_Type("Ӳ��");
					train_List.add(ti3);
				}

				if ((ti.getTrainType().equals("������") || ti.getTrainType()
						.equals("���ٶ���"))
						&& (!ti.getYZ_Y().toString().equals("null")
								&& !ti.getYZ_Y().toString().equals("0") && !ti
								.getYZ_Y().toString().equals(""))) {
					TrainInfo ti4 = (TrainInfo) ti.clone();
					ti4.setRemain_Count(ti.getYZ_Y().toString());
					ti4.setPrice(ti.getYZ());
					ti4.setSeat_Type("������");
					train_List.add(ti4);
				} else if ((!ti.getYZ_Y().toString().equals("null")
						&& !ti.getYZ_Y().toString().equals("0") && !ti
						.getYZ_Y().toString().equals(""))) {
					TrainInfo ti5 = (TrainInfo) ti.clone();
					ti5.setRemain_Count(ti.getYZ_Y().toString());
					ti5.setPrice(ti.getYZ());
					ti5.setSeat_Type("Ӳ��");
					train_List.add(ti5);
				}

				if ((ti.getTrainType().equals("������") || ti.getTrainType()
						.equals("���ٶ���"))
						&& (!ti.getRZ_Y().toString().equals("null")
								&& !ti.getRZ_Y().toString().equals("0") && !ti
								.getRZ_Y().toString().equals(""))) {
					TrainInfo ti6 = (TrainInfo) ti.clone();
					ti6.setRemain_Count(ti.getRZ_Y().toString());
					ti6.setPrice(ti.getRZ());
					ti6.setSeat_Type("һ����");
					train_List.add(ti6);
				} else if ((!ti.getRZ_Y().toString().equals("null")
						&& !ti.getRZ_Y().toString().equals("0") && !ti
						.getRZ_Y().toString().equals(""))) {
					TrainInfo ti7 = (TrainInfo) ti.clone();
					ti7.setRemain_Count(ti.getRZ_Y().toString());
					ti7.setPrice(ti.getRZ());
					ti7.setSeat_Type("����");
					train_List.add(ti7);
				}
			} catch (Exception e) {
			}
		}
	}

	private void startQuery() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				// url?action=trainlist&str={"s":"beijing","e":"shanghai","t":"2014-04-30"}&sign=1232432&userkey=2bfc0c48923cf89de19f6113c127ce81&sitekey=defage
				MyApp ma = new MyApp(context);
				// String siteid=sp.getString(SPkeys.siteid.getString(), "65");
				String str = "{\"s\":\"" + startcity_code + "\",\"e\":\""
						+ arrivecity_code + "\",\"t\":\"" + startoff_date
						+ "\"}";
				String param = "action=trainlist&str=" + str + "&userkey="
						+ MyApp.userkey + "&sign="
						+ CommonFunc.MD5(MyApp.userkey + "trainlist" + str)
						+ "&sitekey=" + MyApp.sitekey;
				trainsReturnJson = HttpUtils.getJsonContent(ma.getServeUrl(),
						param);
				Message msg = new Message();
				msg.what = 1;
				handler.sendMessage(msg);
			}
		}).start();
		progressdialog = CustomProgressDialog.createDialog(context);
		progressdialog.setMessage("���ڲ�ѯ�����Ժ�...");
		progressdialog.setCancelable(true);
		progressdialog.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
			}
		});
		progressdialog.show();
	}
	Comparator<TrainInfo> comparator_time_asc = new Comparator<TrainInfo>() {
		public int compare(TrainInfo s1, TrainInfo s2) {
			return s2.getGoTime().compareTo(s1.getGoTime());
		}
	};
	Comparator<TrainInfo> comparator_time_desc = new Comparator<TrainInfo>() {
		public int compare(TrainInfo s1, TrainInfo s2) {
			return s1.getGoTime().compareTo(s2.getGoTime());
		}
	};
	Comparator<TrainInfo> comparator_type_asc = new Comparator<TrainInfo>() {
		public int compare(TrainInfo s1, TrainInfo s2) {
			return s1.getTrainID().compareTo(s2.getTrainID());
		}
	};
	Comparator<TrainInfo> comparator_type_desc = new Comparator<TrainInfo>() {
		public int compare(TrainInfo s1, TrainInfo s2) {
			return s2.getTrainID().compareTo(s1.getTrainID());
		}
	};
	View.OnClickListener btnClickListner = new View.OnClickListener() {
		@SuppressLint("ResourceAsColor")
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.date_yesterday_ll:
				startQuery();

				break;
			case R.id.date_tomorrow_ll:
				startQuery();
				break;
				//bytime_ll
			case R.id.bytraintype_LL://sort_type_tv,sort_time_tv
				sort_type_tv.setSelected(true);
				sort_time_tv.setSelected(false);
				sort_type_iv.setSelected(true);
				sort_time_iv.setSelected(false);
				byTypeAsc = !byTypeAsc;
				if (byTypeAsc) {
					sort_type_iv.setBackground(getResources()
							.getDrawable(R.drawable.sort_arrow_up));
					Collections.sort(train_List, comparator_type_desc);
					adapter.notifyDataSetChanged();
				} else {
					sort_type_iv.setBackground(getResources()
							.getDrawable(R.drawable.sort_arrow_down));
					Collections.sort(train_List, comparator_type_asc);
					adapter.notifyDataSetChanged();
				}
				break;
			case R.id.bytime_ll://sort_type_tv,sort_time_tv
				sort_type_tv.setSelected(false);
				sort_time_tv.setSelected(true);
				sort_type_iv.setSelected(false);
				sort_time_iv.setSelected(true);
				byTimeAsc = !byTimeAsc;
				if (byTimeAsc) {
					sort_time_iv.setBackground(getResources()
							.getDrawable(R.drawable.sort_arrow_up));
					Collections.sort(train_List, comparator_time_desc);
					adapter.notifyDataSetChanged();
				} else {
					sort_time_iv.setBackground(getResources()
							.getDrawable(R.drawable.sort_arrow_down));
					Collections.sort(train_List, comparator_time_asc);
					adapter.notifyDataSetChanged();
				}
				break;
			case R.id.back_imgbtn:
				finish();
				break;
			case R.id.home_imgbtn:
				startActivity(new Intent(context, MainActivity.class));
				break;
			default:
				break;
			}
		}
	};

	private class ListAdapter extends BaseAdapter {
		private LayoutInflater inflater;
		private List<TrainInfo> str;

		public ListAdapter(Context context, List<TrainInfo> list1) {
			this.inflater = LayoutInflater.from(context);
			this.str = list1;
		}

		public void updateBitmap(List<TrainInfo> list1) {
			this.str = list1;
		}

		@Override
		public int getCount() {
			return str.size();
		}

		@Override
		public Object getItem(int position) {
			return str.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.item_train_searchlist,
						null);
			}
			TextView train_num_tv = (TextView) convertView
					.findViewById(R.id.train_num_tv);
			TextView train_type_tv = (TextView) convertView
					.findViewById(R.id.train_type_tv);
			TextView start_time_tv = (TextView) convertView
					.findViewById(R.id.start_time_tv);
			TextView arrive_time_tv = (TextView) convertView
					.findViewById(R.id.arrive_time_tv);
			TextView used_time_tv = (TextView) convertView
					.findViewById(R.id.used_time_tv);
			TextView start_station_tv = (TextView) convertView
					.findViewById(R.id.start_station_tv);
			TextView end_station_tv = (TextView) convertView
					.findViewById(R.id.end_station_tv);
			TextView seat_grad_tv = (TextView) convertView
					.findViewById(R.id.seat_grad_tv);
			TextView price_tv = (TextView) convertView
					.findViewById(R.id.price_tv);
			TextView remain_count_tv = (TextView) convertView
					.findViewById(R.id.remain_count_tv);
			ImageView start_station_icon_iv = (ImageView) convertView
					.findViewById(R.id.start_station_icon_iv);
			ImageView end_station_icon_iv = (ImageView) convertView
					.findViewById(R.id.end_station_icon_iv);

			train_num_tv.setText(str.get(position).getTrainID());
			train_type_tv.setText(str.get(position).getTrainType());
			start_time_tv.setText(str.get(position).getGoTime());
			arrive_time_tv.setText(str.get(position).getETime());
			used_time_tv.setText("��ʱ�� " + str.get(position).getRunTime());
			start_station_tv.setText(str.get(position).getStationS());
			end_station_tv.setText(str.get(position).getStationE());
			seat_grad_tv.setText(str.get(position).getSeat_Type());
			price_tv.setText("+" + str.get(position).getPrice());
			remain_count_tv.setText("��Ʊ " + str.get(position).getRemain_Count()
					+ "��");

			String SFType = str.get(position).getSFType();
			if (SFType.length() == 3) {
				String SType = SFType.substring(0, 1);
				String FType = SFType.substring(2, 3);
				if (SType.equals("ʼ")) {
					start_station_icon_iv.setBackground(getResources()
							.getDrawable(R.drawable.trains_start));
				} else if (SType.equals("��")) {
					start_station_icon_iv.setBackground(getResources()
							.getDrawable(R.drawable.train_over));
				}

				if (FType.equals("��")) {
					end_station_icon_iv.setBackground(getResources()
							.getDrawable(R.drawable.train_final));
				} else if (FType.equals("��")) {
					end_station_icon_iv.setBackground(getResources()
							.getDrawable(R.drawable.train_over));
				}
			}

			return convertView;
		}
	}
}