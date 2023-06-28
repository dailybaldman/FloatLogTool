package FloatLogTool.Util;

import android.os.Environment;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
//工具类
public class SDcardUtil {
	static String Environment_getExternalStorageState;
	static File Environment_getExternalStorageDirectory;
    static Runtime Runtime_getRuntime;
	/**
     * 获取外置SD卡路径以及TF卡的路径
     * <p>
     * 返回的数据：paths.get(0)肯定是外置SD卡的位置，因为它是primary external storage.
     *
     * @return 所有可用于存储的不同的卡的位置，用一个List来保存
     */
    public static List<String> getExtSDCardPathList() throws Exception{
        List<String> paths = new ArrayList<String>();
        String extFileStatus = Environment.getExternalStorageState();
//		if(extFileStatus!=null){
//			throw new Exception(""+extFileStatus.toString());
//		}
//		
        File extFile = Environment.getExternalStorageDirectory();
        //首先判断一下外置SD卡的状态，处于挂载状态才能获取的到
        if (extFileStatus.equals(Environment.MEDIA_MOUNTED)
			&& extFile.exists() && extFile.isDirectory()
			&& extFile.canWrite()) {
            //外置SD卡的路径
            paths.add(extFile.getAbsolutePath());
        }
        try {
            // obtain executed result of command line code of 'mount', to judge
            // whether tfCard exists by the result
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec("mount");
            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            int mountPathIndex = 1;
            while ((line = br.readLine()) != null) {
                // format of sdcard file system: vfat/fuse
                if ((!line.contains("fat") && !line.contains("fuse") && !line
					.contains("storage"))
					|| line.contains("secure")
					|| line.contains("asec")
					|| line.contains("firmware")
					|| line.contains("shell")
					|| line.contains("obb")
					|| line.contains("legacy") || line.contains("data")) {
                    continue;
                }
                String[] parts = line.split(" ");
                int length = parts.length;
                if (mountPathIndex >= length) {
                    continue;
                }
                String mountPath = parts[mountPathIndex];
                if (!mountPath.contains("/") || mountPath.contains("data")
					|| mountPath.contains("Data")) {
                    continue;
                }
                File mountRoot = new File(mountPath);
                if (!mountRoot.exists() || !mountRoot.isDirectory()
					|| !mountRoot.canWrite()) {
                    continue;
                }
                boolean equalsToPrimarySD = mountPath.equals(extFile
															 .getAbsolutePath());
                if (equalsToPrimarySD) {
                    continue;
                }
                //扩展存储卡即TF卡或者SD卡路径
                paths.add(mountPath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return paths;
    }
    
}
