package android.app;

import java.util.ArrayList;

/**
 * 插件的描述
 * @author zhaoxuyang
 * @since 2014-11-14
 */
public class ApkFile {

    public String name;
    
    public String apkName;
    
    public ArrayList<String> nativeLibs = new ArrayList<String>();
    
    public float version;
    
}
