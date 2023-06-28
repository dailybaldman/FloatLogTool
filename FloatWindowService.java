package FloatLogTool;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import android.text.format.DateFormat;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Adapter;
import android.widget.RelativeLayout;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.ScrollView;
import android.view.View.OnDragListener;
import android.view.DragEvent;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.view.Gravity;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.HorizontalScrollView;
import android.graphics.Rect;
import android.widget.EditText;
import android.text.TextWatcher;
import android.text.Editable;
import android.widget.Button;
import android.widget.ToggleButton;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.InputType;
import FloatLogTool.Util.StringUtils;
import android.widget.AbsListView.LayoutParams;
import android.transition.Visibility;


public class FloatWindowService extends Service implements DataTube ,OnTouchListener {
    static Context context;
	static RelativeLayout mRelativeLayout;
	static int count =0;
	static int messagecount=0;
	static int maxMessage=1000;
	static ArrayList<String> finaldata= new  ArrayList<String>();
	static ArrayList<IndexData> index_data_list= new  ArrayList<IndexData>();
    static mClickListener MyClickListenner;
	static ClipboardManager mClipboardManager;
	static ClipData mClipData;
	static FloatWindowService.MyListAdapter mySourceListAdapter;
	static File stored_location;
	static FileOutputStream myFileOutputStream;
	static FileInputStream myFileInputStream;
	static String rootDir;
	static boolean WantToStoreData=false;
	static boolean WantToShowWindow=true;
	static int PhoneWidth;
	static int PhoneHeight;
	static LinearLayout mLinearLayout;
	static HorizontalScrollView mScrollview;
	static Switch mSwitcher_BigSmall;
	static ListView mylistview;
    static EditText meditText_searchBox;
	static SeekBar mseekfontContr;
	static SeekBar mseekBackgroundTransContr;
    static boolean bRecording = true;
//    static ToggleButton  mButtonstopOrClearDatalist;
    static TextView mtv_switch;
	static int textSize  =12;
	static int  currentAlpha = 160;
    static int current_list;
    static int List_source=1;
    static int List_filtered=2;
    static boolean bDidUserReallyInputSth = false;
    MyBind mybind = new MyBind();
    String Totaldata="";
    String strcurrentversion = "FloatLogTool v4";
    WindowManager mWindowManager;
    WindowManager.LayoutParams mWindowManagerLayoutParams;
    MyListAdapter currentAdapter =null;
    TextView textview;
    int downx;
    int downy;
    long downtimeDown;
    long downtimenow;
    long currentime;
    TextView mtv_CurrentVersion ;
    boolean STATUS_VIEW_MOVEABLE = false;
    boolean bScaleShape = true;
    int CURRENT_STATUS;
    int STATUS_VIEW_CANNOT_MOVE = 0;
    int STATUS_VIEW_CAN_MOVE_BUT_NOT_MOVING =1;
    int STATUS_VIEW_IS_MOVING=2;
	GestureDetector mGestureDetector;
	Handler  mhandler = new Handler(){
		public void  handleMessage(Message m) {
            bRecording = true;
			messagecount++;
			String s = m.obj.toString();
			if (WantToStoreData) {
				try {
					String toStore = DateFormat.format("yyyy_MM_dd_kk:mm:ss", System.currentTimeMillis()) + "\n" + s + "\n" + "\n";
					myFileOutputStream.write(toStore.getBytes(StandardCharsets.UTF_8));
				} catch (IOException e) {
					Toast.makeText(getApplication(), "写入错误❌：" + e.toString(), Toast.LENGTH_SHORT).show();
				}
			}
			IndexData mIndexData = new IndexData(messagecount, s);
			index_data_list.add(mIndexData);
			MessageLimit(false);
			triggleSearching();
            updateListView();
		}

        

		private void MessageLimit(boolean b) {
			if (b) {
				if (messagecount > maxMessage) {
					index_data_list.remove(0);
					index_data_list.trimToSize();
				}
			}	
		}
	};



	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //    Toast.makeText(getApplication(), "触发了onTouch", Toast.LENGTH_SHORT).show();
				//设置长按才触发移动，一次点击事件内
                downx = (int) event.getRawX();
                downy = (int) event.getRawY();
                downtimeDown = event.getDownTime();
                break;
            case MotionEvent.ACTION_MOVE:
				downtimenow = event.getDownTime();
				currentime = event.getEventTime();
                int currentx=(int) event.getRawX();
                int currenty = (int) event.getRawY();
                int movedx= currentx - downx;
                int movedy = currenty - downy;
				downx = currentx;
				downy = currenty;
				//处于同一事件中
				if (downtimenow == downtimeDown) {
					//在200ms内偏移判断长按类型
					if (0 < (currentime - downtimeDown) & (currentime - downtimeDown) < 200) {
						if (movedx < 2 & movedy < 2) {
							//判定为短长按事件
							STATUS_VIEW_MOVEABLE = true;
						} else {

							STATUS_VIEW_MOVEABLE = false;
						}
						//如果大于200ms，继续判断
					} else {

						if (STATUS_VIEW_MOVEABLE) {
							mWindowManagerLayoutParams.x = mWindowManagerLayoutParams.x + movedx ;
							mWindowManagerLayoutParams.y = mWindowManagerLayoutParams.y + movedy;
							mWindowManager.updateViewLayout(mRelativeLayout, mWindowManagerLayoutParams);

						}
					}                    
				}


                break;
            case MotionEvent.ACTION_UP:

                break;
            default:
                break;
        }
		return false;
	}
	@Override
	public void out(int tag, String data) {
		switch (tag) {
			case 0:
                if (bRecording) {
                    push(data);
                }
				break;
			case 1:
				break;
			default:
				break;
		}
	}


	public  void push(String data) {
		Message m = new Message();
		m.obj = data;
		mhandler.sendMessage(m);
	}

	public class MyListAdapter extends ArrayAdapter<IndexData> {
		Context context;
		int childItemLayout;



		public class ViewHolder {
			TextView tv1;
			TextView tv2;
			boolean copied = false;
        }
		public MyListAdapter(Context context, int childItemLayout, ArrayList<IndexData> list) {
			super(context, childItemLayout, list);
			this.context = context;
			this.childItemLayout = childItemLayout;

		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = null;
			final IndexData index_data_item = getItem(position);
			ViewHolder vh =null;
			if (convertView != null) {
				vh = (FloatWindowService.MyListAdapter.ViewHolder) convertView.getTag();
				view = convertView;
			} else {
				view = LayoutInflater.from(context).inflate(childItemLayout, parent, false);		
				vh = new ViewHolder();
				vh.tv1 = view.findViewById(android.R.id.text1);
				vh.tv2 = view.findViewById(android.R.id.text2);
				view.setTag(vh);
			}
			view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
			vh.tv1.setTextColor(Color.BLACK);
			vh.tv2.setTextColor(Color.BLACK);
			vh.tv1.setTextSize(12);
			vh.tv2.setTextSize(textSize);
			vh.tv1.setText(index_data_item.getIndex() + ":");
			vh.tv2.setText(index_data_item.getData());
			return view;
		}

	}

	public class MyItemClickListener implements AdapterView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			TextView tv2 = view.findViewById(android.R.id.text2);
			ClipData mClipData = ClipData.newPlainText("0", tv2.getText());
			mClipboardManager.setPrimaryClip(mClipData);

		}
	}

	public class MyTtemLongclickListener implements AdapterView.OnItemLongClickListener {

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {


			return true;
		}


	}

	private void toNormalWindow() {
        meditText_searchBox.setVisibility(View.VISIBLE);
		mWindowManagerLayoutParams.width = 1000;
		mWindowManagerLayoutParams.height = 800;
        mWindowManagerLayoutParams.flags =   WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
		mWindowManager.updateViewLayout(mRelativeLayout, mWindowManagerLayoutParams);
		bScaleShape = true;

	}



	private void tominiWindow() {
        meditText_searchBox.setVisibility(View.INVISIBLE);
        mWindowManagerLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        //    mSwitcher_BigSmall.requestFocus();
		mWindowManagerLayoutParams.width = 80;
		mWindowManagerLayoutParams.height = 80;
		mWindowManager.updateViewLayout(mRelativeLayout, mWindowManagerLayoutParams);
		bScaleShape = false;
		ScrollToLeft();
	}



    public void ScrollToLeft() {
        mScrollview.fullScroll(ScrollView.FOCUS_LEFT);

    }
	public class MyBind extends Binder {




		public  DataTube LoadWindow(Activity c) {
            FloatWindowService.context = c;
			initial(c);
			startFloatingWindow(c);
			return (DataTube)FloatWindowService.this;
		}
		private void initial(Activity c) {

			mClipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			if (WantToStoreData) {
				request_external_storage_permission(c);

				rootDir = Environment.getExternalStorageDirectory().getAbsolutePath();
				stored_location = new File(rootDir + "/Medmi");
				if (!stored_location.exists()) {
					stored_location.mkdir();
				}
				CharSequence df = DateFormat.format("yyyy_MM_dd_kk:mm:ss", System.currentTimeMillis());
				File current_log_file = new File(stored_location + "/Medmi_log_" + getPackageName() + "_" + df + ".txt");
				if (!current_log_file.exists()) {
					try {
						current_log_file.createNewFile();
					} catch (IOException e) {
						//			Toast.makeText(getApplication(), "创建文件错误❌："+e.toString(), Toast.LENGTH_SHORT).show();
					}
				}
				try {
					myFileOutputStream = new FileOutputStream(current_log_file);
				} catch (FileNotFoundException e) {
					Toast.makeText(getApplication(), "输出流错误❌：️" + e.toString(), Toast.LENGTH_LONG).show();
				}
			}

		}



		private void request_external_storage_permission(Activity c) {
			String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE};
			c.requestPermissions(permissions, 0);
		}
		private  void startFloatingWindow(Activity c) {
			if (Settings.canDrawOverlays(c)) {
				mWindowManager = (WindowManager) c.getSystemService(WINDOW_SERVICE);
				PhoneWidth = mWindowManager.getDefaultDisplay().getWidth();
				PhoneHeight =  mWindowManager.getDefaultDisplay().getHeight();
				mWindowManagerLayoutParams = new WindowManager.LayoutParams();
				MyClickListenner = new mClickListener();
				initListView();
				initRelativeLayout();

				if (WantToShowWindow) {
					mWindowManagerLayoutParams.width = 1000;
					mWindowManagerLayoutParams.height = 800;
					bScaleShape = true;
				} else {
					mWindowManagerLayoutParams.width = 1;
					mWindowManagerLayoutParams.height = 1;
					bScaleShape = false;
				}

				mWindowManagerLayoutParams.x = 0;
				mWindowManagerLayoutParams.y = 0;
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
					mWindowManagerLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
				} else {
					mWindowManagerLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
				}
				mWindowManagerLayoutParams.format = PixelFormat.TRANSLUCENT;
				mWindowManagerLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
				mWindowManager.addView(mRelativeLayout, mWindowManagerLayoutParams);
				count++;

			} else {
				getFloatWindowService(c);
			}
		}

		private void initRelativeLayout() {
			mRelativeLayout = new RelativeLayout(FloatWindowService.this);
            mtv_CurrentVersion = new TextView(FloatWindowService.this);
			ViewGroup.LayoutParams mylistviewlayoutparams =new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
			mLinearLayout = new LinearLayout(FloatWindowService.this);
			mScrollview = new HorizontalScrollView(FloatWindowService.this);
			mSwitcher_BigSmall = new Switch(FloatWindowService.this);
			mseekBackgroundTransContr = new SeekBar(FloatWindowService.this);
           
			mseekfontContr = new SeekBar(FloatWindowService.this);
            meditText_searchBox = new EditText(FloatWindowService.this);
            meditText_searchBox.setVisibility(View.INVISIBLE);
          //  meditText_searchBox.setFocusable(false);
            mtv_switch = new TextView(FloatWindowService.this);
			mseekBackgroundTransContr.setProgress((int)(currentAlpha * 0.392));
			mseekfontContr.setProgress(50);
			LinearLayout mtempLinearLayout_vertical = new LinearLayout(FloatWindowService.this);
			LinearLayout mtempLinearLayout_horizon_surround_with_scrollview = new LinearLayout(FloatWindowService.this);
			mtempLinearLayout_vertical.setOrientation(LinearLayout.VERTICAL);
            //	mtempLinearLayout_vertical.addView(mseekBackgroundTransContr,ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            //	mtempLinearLayout_vertical.addView(mseekfontContr,ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
			mLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
			mSwitcher_BigSmall.setChecked(true);
			mSwitcher_BigSmall.setId(0001);
			mseekBackgroundTransContr.setId(0002);
			mseekfontContr.setId(0003);
            mtv_switch.setId(0004);
            meditText_searchBox.setId(0005);
            mtv_CurrentVersion.setId(0006);
            if (bRecording) {
                mtv_switch.setText("⏸");
            } else {
                mtv_switch.setText("▶️");
            }
            mtv_switch.setOnClickListener(MyClickListenner);
			mseekBackgroundTransContr.setOnSeekBarChangeListener(new mseekbarchangelistenner());
			mseekfontContr.setOnSeekBarChangeListener(new mseekbarchangelistenner());
			mSwitcher_BigSmall.setOnCheckedChangeListener(new mOnCheckedButtonListenner());
            //    meditText_searchBox.
            meditText_searchBox.setOnFocusChangeListener(new mViewFocusChangeListener());
            meditText_searchBox.addTextChangedListener(new mTextWatcher());
            meditText_searchBox.setMaxLines(1);
            meditText_searchBox.setLines(1);
            meditText_searchBox.setInputType(InputType.TYPE_CLASS_TEXT);
            meditText_searchBox.setHint("在此输入搜索内容");
            meditText_searchBox.setTextColor(Color.GREEN);
            mtv_CurrentVersion.setText(strcurrentversion);
            mtv_CurrentVersion.setOnClickListener(MyClickListenner);
            setEditTextInputSpace(meditText_searchBox);
			mtempLinearLayout_horizon_surround_with_scrollview.setGravity(Gravity.CENTER | Gravity.LEFT);
			mtempLinearLayout_horizon_surround_with_scrollview.addView(mSwitcher_BigSmall, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		    mtempLinearLayout_horizon_surround_with_scrollview.addView(mtv_switch, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            mtempLinearLayout_horizon_surround_with_scrollview.addView(mtv_CurrentVersion,ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            mtempLinearLayout_horizon_surround_with_scrollview.addView(meditText_searchBox,ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);           
            mtempLinearLayout_horizon_surround_with_scrollview.addView(mseekBackgroundTransContr, 500, ViewGroup.LayoutParams.WRAP_CONTENT);           
			mtempLinearLayout_horizon_surround_with_scrollview.addView(mseekfontContr, 500, ViewGroup.LayoutParams.WRAP_CONTENT);
			mScrollview.addView(mtempLinearLayout_horizon_surround_with_scrollview, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			mScrollview.setBackgroundColor(Color.GRAY);
			mScrollview.getBackground().setAlpha(50);
            mScrollview.setOnTouchListener(FloatWindowService.this);
			mRelativeLayout.addView(mylistview, mylistviewlayoutparams);
			mRelativeLayout.addView(mScrollview, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		}
		private void initListView() {
			mylistview = new ListView(FloatWindowService.this);
			mylistview.setOnTouchListener(FloatWindowService.this);
			mylistview.setBackgroundColor(0xffDBDBDB);
			mylistview.getBackground().setAlpha(currentAlpha);
			mylistview.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
    //        mylistview.setStackFromBottom(false);
       
			mySourceListAdapter = new MyListAdapter(FloatWindowService.this, android.R.layout.simple_list_item_2, index_data_list);
			mylistview.setDivider(new ColorDrawable(0x55DBDBDB));
			mylistview.setAdapter(mySourceListAdapter);
            mySourceListAdapter.notifyDataSetChanged();
			mylistview.setOnItemLongClickListener(new MyTtemLongclickListener());
			mylistview.setOnItemClickListener(new MyItemClickListener());           
		}
		private  void getFloatWindowService(Activity c) {
			if (!Settings.canDrawOverlays(c)) {
				Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
				c.startActivity(intent);
			}

		}
	}

    public class mViewFocusChangeListener implements View.OnFocusChangeListener {

        @Override
        public void onFocusChange(View p1, boolean p2) {
            if (p2) {
                meditText_searchBox.setWidth(600);
            } else {
                meditText_searchBox.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
            }

        }
    }

    public class mClickListener implements View.OnClickListener {

        @Override
        public void onClick(View p1) {
            switch (p1.getId()) {
                case 0004:
                    if (bRecording) {
                        stopRecorddata();
                    } else {
                        startRecorddata();
                    }
                    break;
                    
                    case 0006:
                        if(meditText_searchBox.getVisibility()==View.INVISIBLE){
                            meditText_searchBox.setWidth(300);
                            mtv_CurrentVersion.setTextColor(Color.GREEN);
                            meditText_searchBox.setVisibility(View.VISIBLE);
                            mseekBackgroundTransContr.setVisibility(View.VISIBLE);
                            mseekfontContr.setVisibility(View.VISIBLE);
                        }else{
                            mtv_CurrentVersion.setTextColor(Color.RED);
                            meditText_searchBox.setVisibility(View.INVISIBLE);
                            mseekBackgroundTransContr.setVisibility(View.INVISIBLE);
                            mseekfontContr.setVisibility(View.INVISIBLE);
                        }
                        break;
                default:
                    Toast.makeText(FloatWindowService.this, "没有添加功能", Toast.LENGTH_SHORT).show();
                    break;
            }

        }
    }



    public void startRecorddata() {
        bRecording = true;
        mtv_switch.setText("⏸️");
    }

    public void stopRecorddata() {
        bRecording = false;
        mtv_switch.setText("▶️");
    }
    
    public void triggleSearching(){
        
        String target = meditText_searchBox.getText().toString();
        target =  StringUtils.replaceAll(target);
        if(target.length()>0&&meditText_searchBox.getVisibility()==View.VISIBLE){
        triggleSearching(target);
        }else{
            currentAdapter = mySourceListAdapter;
        }
        
    }

    public void updateListView() {
        mylistview.setAdapter(currentAdapter);
        currentAdapter.notifyDataSetChanged();
        mylistview.setSelection(mylistview.getBottom());
    }
    
    public void triggleSearching(String target) {
        ArrayList<IndexData> tempIndexData = new ArrayList<IndexData>();
        MyListAdapter m = new MyListAdapter(FloatWindowService.this,android.R.layout.simple_expandable_list_item_2,tempIndexData);
        //触发搜索，获取到输入框的值
        if(target!=""){          
            //  获取到当前列表中所有的值
            for(int i =0;i<index_data_list.size();i++){
                String singledata = index_data_list.get(i).getData();
                if(singledata.contains(target)){
                    tempIndexData.add(index_data_list.get(i));
                }
            }
            //查询结束，让列表显示，就是设置新的适配器？
           
                currentAdapter = m;           
                
            
        }
        
    }
    /**
     * 禁止EditText输入空格和换行符
     *
     * @param editText EditText输入框
     */
    public  void setEditTextInputSpace(EditText editText) {
          InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (source.toString().contentEquals("\n")) {                    
                    return "";
                } else {
                    return null;
                }
            }

            
        };
        editText.setFilters(new InputFilter[]{filter});
    }
  
    
    
    public class mTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence p1, int p2, int p3, int p4) {
            
        }

        @Override
        public void onTextChanged(CharSequence p1, int p2, int p3, int p4) {
            bDidUserReallyInputSth = true;
            if(p1.length()!=0){
                triggleSearching(p1+"");
                updateListView();
            }else{
                turnbackToSourceData();
            }
            //判断是否为空
            
                  
            
            
        }

        public void turnbackToSourceData() {
            mylistview.setAdapter(mySourceListAdapter);
            mySourceListAdapter.notifyDataSetChanged();
            current_list = List_source;
        }

        @Override
        public void afterTextChanged(Editable p1) {
            
        }


    }

	public class mseekbarchangelistenner implements SeekBar.OnSeekBarChangeListener {

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			switch (seekBar.getId()) {
				case 0002:
					mylistview.getBackground().setAlpha((int)(progress * 2.55));
					mySourceListAdapter.notifyDataSetChanged();
					break;

				case 0003:
					textSize = 12 + (progress - 50) / 10;
					mySourceListAdapter.notifyDataSetChanged();
					break;
				default:
					break;
			}
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
		}
	}

	public class mOnCheckedButtonListenner implements CompoundButton.OnCheckedChangeListener {

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			switch (buttonView.getId()) {
				case 0001:
					if (!isChecked) {
						tominiWindow();
					} else {
						toNormalWindow();
					}
					break;
				default:
                    break;
			}
		}
	}


    @Override
    public IBinder onBind(Intent intent) {
        return mybind;
    }

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		return super.onStartCommand(intent, flags, startId);
	}


}



