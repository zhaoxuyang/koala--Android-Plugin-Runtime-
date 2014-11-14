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
final class ActivityIntentResolver extends
		IntentResolver<PackageParser.ActivityIntentInfo, PackageParser.Activity> {
	
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
			int flags, ArrayList<PackageParser.Activity> packageActivities) {
		if (packageActivities == null) {
			return null;
		}
		mFlags = flags;
		final boolean defaultOnly = (flags & PackageManager.MATCH_DEFAULT_ONLY) != 0;
		int N = packageActivities.size();
		ArrayList<ArrayList<PackageParser.ActivityIntentInfo>> listCut = new ArrayList<ArrayList<PackageParser.ActivityIntentInfo>>(
				N);

		ArrayList<PackageParser.ActivityIntentInfo> intentFilters;
		for (int i = 0; i < N; ++i) {
			intentFilters = packageActivities.get(i).intents;
			if (intentFilters != null && intentFilters.size() > 0) {
				listCut.add(intentFilters);
			}
		}
		return super.queryIntentFromList(intent, resolvedType, defaultOnly,
				listCut);
	}

	public final void addActivity(PackageParser.Activity a) {
		mActivities.put(a.getComponentName(), a);
		int NI = a.intents.size();
		for (int j = 0; j < NI; j++) {
			PackageParser.ActivityIntentInfo intent = a.intents.get(j);
			addFilter(intent);
		}
	}

	public final void removeActivity(PackageParser.Activity a) {
		mActivities.remove(a.getComponentName());
		int NI = a.intents.size();
		for (int j = 0; j < NI; j++) {
			PackageParser.ActivityIntentInfo intent = a.intents.get(j);
			removeFilter(intent);
		}
	}

	@Override
	protected boolean allowFilterResult(
			PackageParser.ActivityIntentInfo filter, List<PackageParser.Activity> dest) {
		for (int i = dest.size() - 1; i >= 0; i--) {
			PackageParser.Activity destAi = dest.get(i);
			if (destAi == filter.activity) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected String packageForFilter(PackageParser.ActivityIntentInfo info) {
		return info.activity.owner.packageName;
	}

	@Override
	protected PackageParser.Activity newResult(PackageParser.ActivityIntentInfo info,
			int match) {
		return info.activity;
	}

	@Override
	protected void sortResults(List<PackageParser.Activity> results) {
	}

	@Override
	protected void dumpFilter(PrintWriter out, String prefix,
			PackageParser.ActivityIntentInfo filter) {
		out.print(prefix);
		out.print(Integer.toHexString(System.identityHashCode(filter.activity)));
		out.print(' ');
		out.print(filter.activity.getComponentName());
		out.print(" filter ");
		out.println(Integer.toHexString(System.identityHashCode(filter)));
	}

	// Keys are String (activity class name), values are Activity.
	public final HashMap<ComponentName, PackageParser.Activity> mActivities = new HashMap<ComponentName, PackageParser.Activity>();
	private int mFlags;
	
	
}