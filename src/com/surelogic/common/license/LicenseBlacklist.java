package com.surelogic.common.license;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jobs.NullSLProgressMonitor;
import com.surelogic.common.jobs.SLJob;
import com.surelogic.common.jobs.SLProgressMonitor;
import com.surelogic.common.jobs.SLStatus;
import com.surelogic.common.logging.SLLogger;

public final class LicenseBlacklist {

	private final List<UUID> f_blacklist = new ArrayList<UUID>();

	public synchronized List<UUID> getList() {
		return new ArrayList<UUID>(f_blacklist);
	}

	private boolean f_fromNet = false;

	public synchronized boolean fromNet() {
		return f_fromNet;
	}

	public SLJob getUpdateFromNetJob() {
		return new UpdateFromNetJob();
	}

	public void updateFromNet() {
		final SLJob job = getUpdateFromNetJob();
		job.run(new NullSLProgressMonitor());
	}

	private synchronized void updateList(final List<UUID> blackList) {
		f_blacklist.clear();
		f_blacklist.addAll(blackList);
		f_fromNet = true;
	}

	/**
	 * A job to read the SureLogic license blacklist.
	 */
	private class UpdateFromNetJob implements SLJob {

		private final String f_blacklistLocation = I18N
				.msg("common.manage.licenses.blacklist.url");

		private final List<UUID> f_netBlacklist = new ArrayList<UUID>();

		public String getName() {
			return "Download SureLogic license blacklist";
		}

		public SLStatus run(SLProgressMonitor monitor) {
			monitor.begin();
			boolean performUpdate = true;
			try {
				final URL url = new URL(f_blacklistLocation);
				BufferedReader dis = new BufferedReader(new InputStreamReader(
						url.openStream()));
				String line;
				while ((line = dis.readLine()) != null) {
					line = line.trim();
					if (!line.startsWith("#") && !"".equals(line)) {
						f_netBlacklist.add(UUID.fromString(line));
					}
				}
				dis.close();
			} catch (Exception e) {
				performUpdate = false;
				SLLogger.getLogger().log(Level.WARNING,
						I18N.err(144, f_blacklistLocation), e);
			}
			if (performUpdate)
				updateList(f_netBlacklist);
			monitor.done();
			return SLStatus.OK_STATUS;
		}
	}
}
