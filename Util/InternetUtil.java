package FloatLogTool.Util;

public class InternetUtil {
  //  将整型IP地址转化为常见的
    public static String intToIp(int ipInt) {  
		StringBuilder sb = new StringBuilder();  
		sb.append(ipInt & 0xFF).append(".");  
		sb.append((ipInt >> 8) & 0xFF).append(".");  
		sb.append((ipInt >> 16) & 0xFF).append(".");  
		sb.append((ipInt >> 24) & 0xFF);  
		return sb.toString();  
	}  
	// 返回byte数组 的每一个值
	private static String getByte(byte[] temp) {
		String data ="";
		for(int i=0;i<temp.length; i++){
			data+=temp[i];
		}
		return data;
	}
    
}
