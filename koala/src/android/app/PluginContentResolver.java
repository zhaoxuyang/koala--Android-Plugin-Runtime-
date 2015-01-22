package android.app;

import android.content.ContentResolver;
import android.content.Context;
import android.content.IContentProvider;

public class PluginContentResolver extends ContentResolver{

    public PluginContentResolver(Context context) {
        super(context);
    }

    @Override
    protected IContentProvider acquireProvider(Context context, String name) {
        return PluginManagerImpl.getInstance().getContentProvider(name);
    }

    @Override
    protected IContentProvider acquireUnstableProvider(Context context, String name) {
        return null;
    }

    @Override
    public boolean releaseProvider(IContentProvider provider) {
        return false;
    }

    @Override
    public boolean releaseUnstableProvider(IContentProvider provider) {
        return false;
    }

    @Override
    public void unstableProviderDied(IContentProvider provider) {
        
    }

}
