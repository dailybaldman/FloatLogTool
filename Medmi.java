package FloatLogTool;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import java.util.ArrayList;
import android.widget.Toast;

public class Medmi implements ServiceConnection {

	

	static ArrayList<MessageObject> message= new ArrayList<MessageObject>();
	static int instanceCount=0;
	static DataTube datatube;
	static FloatWindowService.MyBind bind;
	static Medmi log ;
	static Context myContext;
	static StackTraceElement[] stackTraceElement;
	//必须先判断服务是否绑定，都会就是bug，血(大量时间才修复这个bug)的教训
	static boolean is_service_binded=false;
	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		bind = (FloatWindowService.MyBind) service;
		datatube = bind.LoadWindow((Activity)myContext);
		is_service_binded=true;
	    o(0,"服务已开启");
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
	}
	//实例化一个工具类，用来绑定服务
  private  void bindFloatWindowService (Context context){
	  	this.myContext = context;
		 Intent i =new Intent(myContext,FloatWindowService.class);
		 myContext.bindService(i,Medmi.this,myContext.BIND_AUTO_CREATE);
	}
    //必须要初始化，储存上下文参数，不然不能启动intent
	public static void go(Context context){
		if(instanceCount<1){
			log = new Medmi();
			log.bindFloatWindowService(context);
			check_data_arraylist();
			instanceCount++;
		}
	}

	private static void check_data_arraylist() {
		new Thread(){
			public void  run() {
				while (true) {
					if (message.size() != 0 & is_service_binded) {
						message.trimToSize();
						datatube.out(message.get(0).getTag(), message.get(0).getData());
						message.remove(0);
					}
				}
			}
		}.start();
	}
	public 	static void o(int tag,Object data){
		o(tag,data.toString());
	}
	
	public	static void o(Object o){
		o(o.toString());
	}
	
	public	static void o(boolean b){
		o("Boolean:"+b);
	}
	
	public	static void o(Void v){
		o("Void:"+v);
	}
	
	public	static void o(byte v){
		o("byte:"+v);
	}
	
	public	static void o(Byte v){
		o("Byte:"+v);
	}
	
	public	static void o(short v){
		o("short:"+v);
	}
	
	public	static void o(Short v){
		o("Short:"+v);
	}
	
	public	static void o(char v){
		o("char:"+v);
	}
	
	public	static void o(CharSequence v){
		o("CharSequence:"+v);
	}
	
	public	static void o(int v){
		o("int:"+v);
	}
	
	public	static void o(long v){
		o("long:"+v);
	}
	
	public	static void o(float v){
		o("float:"+v);
	}
	
	public	static void o(double v){
		o("double:"+v);
	}
	
	public	static void o (String data){
			o(0,data);
	}
	public	static void o(final int tag,final String data){
		stackTraceElement = Thread.currentThread().getStackTrace();
		StackTraceElement se = stackTraceElement[4];
		String classname = se.getClassName();
		String method= se.getMethodName();
		String fdata = "来源："+classname+";->"+method+"\n"+  data;
		if (is_service_binded) {
			datatube.out(tag,fdata);
		} else {
			message.add(new MessageObject(tag,fdata));
		}
	}
}
