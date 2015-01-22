package android.app;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser;

/**
 * 基于源码做的更改
 * @author zhaoxuyang
 * @since 2014-11-14
 */
final class ProviderIntentResolver extends
		IntentResolver<PackageParser.ProviderIntentInfo, PackageParser.Provider> {
	
	public List queryIntent(Intent intent, String resolvedType,
			boolean defaultOnly) {
		mFlags = defaultOnly ? PackageManager.MATCH_DEFAULT_ONLY : 0;
		return super.queryIntent(intent, resolvedType, defaultOnly);
	}

	public List queryIntent(Intent intent, String resolvedType, int flags) {
		mFlags = flags;
		return super.queryIntent(intent, resolvedType,
				(flags & PackageManager.MATCH_DEFAULT_ONLY) != 0);
	}

	public List queryIntentForPackage(Intent intent, String resolvedType,
			int flags, ArrayList<PackageParser.Provider> packageProviders) {
		if (packageProviders == null) {
			return null;
		}
		mFlags = flags;
		final boolean defaultOnly = (flags & PackageManager.MATCH_DEFAULT_ONLY) != 0;
		int N = packageProviders.size();
		ArrayList<ArrayList<PackageParser.ProviderIntentInfo>> listCut = new ArrayList<ArrayList<PackageParser.ProviderIntentInfo>>(
				N);

		ArrayList<PackageParser.ProviderIntentInfo> intentFilters;
		for (int i = 0; i < N; ++i) {
			intentFilters = packageProviders.get(i).intents;
			if (intentFilters != null && intentFilters.size() > 0) {
				listCut.add(intentFilters);
			}
		}
		return super.queryIntentFromList(intent, resolvedType, defaultOnly,
				listCut);
	}

	public final void addProvider(PackageParser.Provider p) {
	    mProviders.put(p.getComponentName(), p);
		int NI = p.intents.size();
		for (int j = 0; j < NI; j++) {
			PackageParser.ProviderIntentInfo intent = p.intents.get(j);
			addFilter(intent);
		}
	}

	public final void removeProvider(PackageParser.Provider p) {
	    mProviders.remove(p.getComponentName());
		int NI = p.intents.size();
		for (int j = 0; j < NI; j++) {
			PackageParser.ProviderIntentInfo intent = p.intents.get(j);
			removeFilter(intent);
		}
	}

	@Override
	protected boolean allowFilterResult(
			PackageParser.ProviderIntentInfo filter, List<PackageParser.Provider> dest) {
		for (int i = dest.size() - 1; i >= 0; i--) {
			PackageParser.Provider destAi = dest.get(i);
			if (destAi == filter.provider) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected String packageForFilter(PackageParser.ProviderIntentInfo info) {
		return info.provider.owner.packageName;
	}

	@Override
	protected PackageParser.Provider newResult(PackageParser.ProviderIntentInfo info,
			int match) {
		return info.provider;
	}

	@Override
	protected void sortResults(List<PackageParser.Provider> results) {
	}

	@Override
	protected void dumpFilter(PrintWriter out, String prefix,
			PackageParser.ProviderIntentInfo filter) {
		out.print(prefix);
		out.print(Integer.toHexString(System.identityHashCode(filter.provider)));
		out.print(' ');
		out.print(filter.provider.getComponentName());
		out.print(" filter ");
		out.println(Integer.toHexString(System.identityHashCode(filter)));
	}

	// Keys are String (Service class name), values are Service.
	public final HashMap<ComponentName, PackageParser.Provider> mProviders = new HashMap<ComponentName, PackageParser.Provider>();
	private int mFlags;
	
	
}