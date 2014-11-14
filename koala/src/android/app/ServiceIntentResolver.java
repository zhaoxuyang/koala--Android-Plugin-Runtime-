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
final class ServiceIntentResolver extends
		IntentResolver<PackageParser.ServiceIntentInfo, PackageParser.Service> {
	
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
			int flags, ArrayList<PackageParser.Service> packageServices) {
		if (packageServices == null) {
			return null;
		}
		mFlags = flags;
		final boolean defaultOnly = (flags & PackageManager.MATCH_DEFAULT_ONLY) != 0;
		int N = packageServices.size();
		ArrayList<ArrayList<PackageParser.ServiceIntentInfo>> listCut = new ArrayList<ArrayList<PackageParser.ServiceIntentInfo>>(
				N);

		ArrayList<PackageParser.ServiceIntentInfo> intentFilters;
		for (int i = 0; i < N; ++i) {
			intentFilters = packageServices.get(i).intents;
			if (intentFilters != null && intentFilters.size() > 0) {
				listCut.add(intentFilters);
			}
		}
		return super.queryIntentFromList(intent, resolvedType, defaultOnly,
				listCut);
	}

	public final void addService(PackageParser.Service s) {
		mServices.put(s.getComponentName(), s);
		int NI = s.intents.size();
		for (int j = 0; j < NI; j++) {
			PackageParser.ServiceIntentInfo intent = s.intents.get(j);
			addFilter(intent);
		}
	}

	public final void removeService(PackageParser.Service s) {
		mServices.remove(s.getComponentName());
		int NI = s.intents.size();
		for (int j = 0; j < NI; j++) {
			PackageParser.ServiceIntentInfo intent = s.intents.get(j);
			removeFilter(intent);
		}
	}

	@Override
	protected boolean allowFilterResult(
			PackageParser.ServiceIntentInfo filter, List<PackageParser.Service> dest) {
		for (int i = dest.size() - 1; i >= 0; i--) {
			PackageParser.Service destAi = dest.get(i);
			if (destAi == filter.service) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected String packageForFilter(PackageParser.ServiceIntentInfo info) {
		return info.service.owner.packageName;
	}

	@Override
	protected PackageParser.Service newResult(PackageParser.ServiceIntentInfo info,
			int match) {
		return info.service;
	}

	@Override
	protected void sortResults(List<PackageParser.Service> results) {
	}

	@Override
	protected void dumpFilter(PrintWriter out, String prefix,
			PackageParser.ServiceIntentInfo filter) {
		out.print(prefix);
		out.print(Integer.toHexString(System.identityHashCode(filter.service)));
		out.print(' ');
		out.print(filter.service.getComponentName());
		out.print(" filter ");
		out.println(Integer.toHexString(System.identityHashCode(filter)));
	}

	// Keys are String (Service class name), values are Service.
	public final HashMap<ComponentName, PackageParser.Service> mServices = new HashMap<ComponentName, PackageParser.Service>();
	private int mFlags;
	
	
}