package com.surelogic.server.serviceability;

import java.io.IOException;
import java.security.PrivateKey;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.surelogic.NonNull;
import com.surelogic.Nullable;
import com.surelogic.common.SLUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jdbc.DBQuery;
import com.surelogic.common.jdbc.Nulls;
import com.surelogic.common.jdbc.Query;
import com.surelogic.common.jdbc.Result;
import com.surelogic.common.jdbc.ResultHandler;
import com.surelogic.common.jdbc.Row;
import com.surelogic.common.jdbc.StringResultHandler;
import com.surelogic.common.jdbc.TransactionException;
import com.surelogic.common.license.PossiblyActivatedSLLicense;
import com.surelogic.common.license.SLLicense;
import com.surelogic.common.license.SLLicenseNetCheck;
import com.surelogic.common.license.SLLicensePersistence;
import com.surelogic.common.license.SLLicenseType;
import com.surelogic.common.license.SignedSLLicense;
import com.surelogic.common.license.SignedSLLicenseNetCheck;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.server.SiteUtil;
import com.surelogic.server.jdbc.ServicesDBConnection;

public class LicenseNetcheckServlet extends HttpServlet {

  private static final long serialVersionUID = 6357187305188901382L;

  enum NetCheckEvent {
    INSTALL_SUCCESS("Successful install", "INSTALL_COUNT"), RENEW_SUCCESS("Successful renewal",
        "RENEWAL_COUNT"), INSTALL_BLACKLISTED("Failed install - on blacklist", "BLACKLIST_COUNT"), INSTALL_LIMIT_EXCEEDED(
            "Failed install - too many installs", "TOO_MANY_COUNT"), REMOVAL_SUCCESS("Successful removal", "REMOVAL_COUNT");

    final String value;
    final String column;

    NetCheckEvent(final String val, final String column) {
      this.value = val;
      this.column = column;
    }

    public Object getColumn() {
      return column;
    }
  }

  @Override
  protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
    handleRequest(req, resp);
  }

  @Override
  protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
    handleRequest(req, resp);
  }

  private void handleRequest(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
    final String typeStr = req.getParameter(I18N.msg("web.check.param.req"));
    final String os = req.getParameter(I18N.msg("web.check.param.os"));
    final String javaVersion = req.getParameter(I18N.msg("web.check.param.java"));
    final String eclipseVersion = req.getParameter(I18N.msg("web.check.param.eclipse"));
    final String counts = req.getParameter(I18N.msg("web.check.param.counts"));
    if (I18N.msg("web.check.param.req.value.actrew").equals(typeStr)) {
      install(req, resp, os, javaVersion, eclipseVersion, counts);
    } else if (I18N.msg("web.check.param.req.value.remove").equals(typeStr)) {
      remove(req, resp, os, javaVersion, eclipseVersion, counts);
    } else {
      resp.getWriter().println(String.format("Unrecognized Request: %s", typeStr));
    }
  }

  /**
   * Check to see if a license has been blacklisted.
   * 
   * @param license
   * @return true if the license has been blacklisted
   */
  static boolean checkBlacklist(final Query q, final SignedSLLicense license) {
    return q.prepared("WebServices.checkBlacklist", new StringResultHandler())
        .call(license.getLicense().getUuid().toString()) != null;
  }

  static class LicenseInfo {
    public LicenseInfo(final int maxInstallCount, final int installCount, final int renewCount, final int removeCount,
        final int blacklistcount, final int tooManyCount) {
      this.maxInstalls = maxInstallCount;
      this.installs = installCount;
      this.renewals = renewCount;
      this.removals = removeCount;
      this.blacklisted = blacklistcount;
      this.tooManyInstalls = tooManyCount;
    }

    final int maxInstalls;
    final int installs;
    final int renewals;
    final int removals;
    final int blacklisted;
    final int tooManyInstalls;

    public int getInstalls() {
      return installs;
    }

    public int getMaxInstalls() {
      return maxInstalls;
    }

    public int getRenewals() {
      return renewals;
    }

    public int getRemovals() {
      return removals;
    }

    public int getBlacklisted() {
      return blacklisted;
    }

    public int getTooManyInstalls() {
      return tooManyInstalls;
    }

    boolean isInstallAvailable() {
      return installs - removals < maxInstalls;
    }
  }

  /**
   * Return the install count information for a particular license. If the
   * information is not already in the database, then a new entry is created.
   * 
   * @param q
   *          a query.
   * @param license
   *          the license.
   * @return the install count information for a particular license. If the
   *         information is not already in the database, then a new entry is
   *         created.
   */
  static LicenseInfo getAndInitInfo(@NonNull final Query q, @NonNull final SLLicense license) {
    final String uuid = license.getUuid().toString();
    Integer maxInstalls = q.prepared("WebServices.selectLicenseInfoById", new ResultHandler<Integer>() {
      @Override
      public Integer handle(final Result result) {
        for (Row r : result) {
          // We don't care about anything but max active here
          r.nextString();
          r.nextString();
          r.nextString();
          r.nextString();
          r.nextInt();
          r.nextDate();
          r.nextString();
          return r.nextInt();
        }
        return null;
      }
    }).call(uuid);
    if (maxInstalls == null) {
      // No license yet
      q.prepared("WebServices.insertCheckCount").call(uuid);
      @Nullable
      final Date installBeforeDate = license.getInstallBeforeDate();
      final Object installBeforeDateDb = installBeforeDate == null ? Nulls.DATE : new Timestamp(installBeforeDate.getTime());
      final Object emailDb = license.getEmail() == null ? Nulls.STRING : license.getEmail();
      final Object companyDb = license.getCompany() == null ? Nulls.STRING : license.getCompany();
      q.prepared("WebServices.insertLicenseInfo").call(uuid, license.getProduct().toString(), license.getHolder(), emailDb,
          companyDb, license.getDurationInDays(), installBeforeDateDb, license.getType().toString(), license.getMaxActive());
      maxInstalls = license.getMaxActive();
      return new LicenseInfo(maxInstalls, 0, 0, 0, 0, 0);
    }
    final int max = maxInstalls;
    return q.prepared("WebServices.selectCheckCount", new ResultHandler<LicenseInfo>() {

      @Override
      public LicenseInfo handle(final Result result) {
        for (final Row r : result) {
          final int installCount = r.nextInt();
          final int renewCount = r.nextInt();
          final int removeCount = r.nextInt();
          final int blacklistcount = r.nextInt();
          final int tooManyCount = r.nextInt();
          return new LicenseInfo(max, installCount, renewCount, removeCount, blacklistcount, tooManyCount);
        }
        // No row was found
        SLLogger.getLogger().log(Level.SEVERE, I18N.err(354, uuid));
        throw new IllegalStateException();
      }
    }).call(uuid);
  }

  /**
   * Logs out that a check event of some sort occurred.
   * 
   * @param q
   *          a {@link Query} object, may not be read-only
   * @param time
   *          the time that the event occurred
   * @param ip
   *          the requestor's ip address
   * @param id
   *          the {@link UUID} of the {@link SLLicense}
   * @param event
   *          the type of check event that occurred
   */
  static void logCheck(final Query q, final Timestamp time, final String ip, final SLLicense license, final NetCheckEvent event,
      @Nullable String os, @Nullable String javaVersion, @Nullable String eclipseVersion, @Nullable String counts) {
    final String uuid = license.getUuid().toString();
    final Object osDb = os == null ? Nulls.STRING : os;
    final Object javaVersionDb = javaVersion == null ? Nulls.STRING : javaVersion;
    final Object eclipseVersionDb = eclipseVersion == null ? Nulls.STRING : eclipseVersion;
    final Object countsDb = counts == null ? Nulls.STRING : counts;
    q.prepared("WebServices.logNetCheck").call(time, ip, uuid.toString(), event.value, osDb, javaVersionDb, eclipseVersionDb,
        countsDb);
    q.statement("WebServices.updateCheckCount").call(event.getColumn(), uuid.toString());
//    Email.sendSupportEmail(event.toString(), I18N.msg("web.check.logEmail", license.getHolder(), license.getProduct().toString(),
//        time.toString(), ip, uuid, I18N.msg("web.admin.license.url", SLUtility.SERVICEABILITY_SERVER, uuid), event.toString()));
  }

  /**
   * Tries to install/renew a {@link PossiblyActivatedSLLicense}. Returns the
   * signed hex string representation of the {@link SLLicenseNetCheck}, or null
   * if the install failed for some reason. The date encoded in the
   * {@link SLLicenseNetCheck} is the expiration/renewal date for the license.
   * 
   * Several checks may be performed during an install/renew. All licenses are
   * checked against the blacklist, and denied if their uuid shows up. For a
   * support/use license, we also check the install date to make sure that the
   * license is not being installed past the deadline.
   */
  static class Install implements DBQuery<String> {

    @NonNull
    private final PossiblyActivatedSLLicense sl;
    @NonNull
    private final Timestamp now;
    @NonNull
    private final String ip;
    @NonNull
    private final ImmutableSet<String> clientMacAddresses;
    @Nullable
    private final String os;
    @Nullable
    private final String javaVersion;
    @Nullable
    private final String eclipseVersion;
    @Nullable
    private final String counts;

    Install(@NonNull PossiblyActivatedSLLicense sl, @NonNull Timestamp now, @NonNull String ip,
        @NonNull ImmutableSet<String> clientMacAddresses, @Nullable String os, @Nullable String javaVersion,
        @Nullable String eclipseVersion, @Nullable String counts) {
      this.sl = sl;
      this.now = now;
      this.ip = ip;
      this.clientMacAddresses = clientMacAddresses;
      this.os = os;
      this.javaVersion = javaVersion;
      this.eclipseVersion = eclipseVersion;
      this.counts = counts;
    }

    @Override
    public String perform(final Query q) {
      final SignedSLLicense signedLicense = sl.getSignedSLLicense();
      final SLLicense license = signedLicense.getLicense();
      final UUID uuid = license.getUuid();
      if (checkBlacklist(q, signedLicense)) {
        // This license has been blacklisted
        logCheck(q, now, ip, license, NetCheckEvent.INSTALL_BLACKLISTED, os, javaVersion, eclipseVersion, counts);
        return fail(I18N.msg("web.check.response.failure.blacklisted", license.getProduct(), uuid));
      }
      if (sl.isPastInstallBeforeDate()) {
        return fail(I18N.msg("web.check.response.failure.expired", license.getProduct(), uuid,
            SLUtility.toStringHumanDay(license.getInstallBeforeDate())));
      }
      final SLLicenseNetCheck check = new SLLicenseNetCheck(uuid, calculateNetcheckDate(license, sl.isActivated()),
          clientMacAddresses);
      final LicenseInfo info = getAndInitInfo(q, license);
      if (license.getType() == SLLicenseType.PERPETUAL && sl.isActivated()) {
        // This is a renewal
        if (info.getInstalls() == 0) {
          // We are probably missing some data, or have a
          // badly behaving client. We'll go ahead and tick the
          // install count as well.
          logCheck(q, now, ip, license, NetCheckEvent.INSTALL_SUCCESS, os, javaVersion, eclipseVersion, counts);
        }
        logCheck(q, now, ip, license, NetCheckEvent.RENEW_SUCCESS, os, javaVersion, eclipseVersion, counts);
      } else {
        // This is an install
        if (info.isInstallAvailable()) {
          logCheck(q, now, ip, license, NetCheckEvent.INSTALL_SUCCESS, os, javaVersion, eclipseVersion, counts);
        } else {
          logCheck(q, now, ip, license, NetCheckEvent.INSTALL_LIMIT_EXCEEDED, os, javaVersion, eclipseVersion, counts);
          return fail(I18N.msg("web.check.response.failure.installLimit", license.getProduct(), uuid, license.getMaxActive()));
        }
      }
      return toStringNetCheckAfterEncoding(check);
    }

    private String toStringNetCheckAfterEncoding(final SLLicenseNetCheck check) {
      final PrivateKey key = SiteUtil.getKey();
      final SignedSLLicenseNetCheck signed = SignedSLLicenseNetCheck.getInstance(check, key);
      return signed.getSignedHexString();
    }
  }

  void install(final HttpServletRequest req, final HttpServletResponse resp, @Nullable String os, @Nullable String javaVersion,
      @Nullable String eclipseVersion, @Nullable String counts) throws IOException {
    @Nullable
    final String licStr = req.getParameter(I18N.msg("web.check.param.license"));
    @Nullable
    final String macAddressesParam = req.getParameter(I18N.msg("web.check.param.macAddresses"));
    final ImmutableSet<String> clientMacAddresses;
    if (macAddressesParam != null) {
      clientMacAddresses = ImmutableSet.copyOf(Splitter.on(',').trimResults().omitEmptyStrings().split(macAddressesParam));
    } else
      clientMacAddresses = ImmutableSet.of();
    @NonNull
    final Timestamp now = new Timestamp(System.currentTimeMillis());
    @NonNull
    final String ip = req.getRemoteAddr();
    if (licStr != null) {
      final List<PossiblyActivatedSLLicense> licenses = SLLicensePersistence.readPossiblyActivatedLicensesFromString(licStr);
      final ServicesDBConnection conn = ServicesDBConnection.getInstance();
      for (final PossiblyActivatedSLLicense sl : licenses) {
        String result;
        try {
          result = conn.withTransaction(new Install(sl, now, ip, clientMacAddresses, os, javaVersion, eclipseVersion, counts));
        } catch (TransactionException e) {
          final String out = I18N.msg("web.check.response.failure.serverError", SLUtility.toString(e));
          result = fail(out);
          SLLogger.getLogger().log(Level.SEVERE, "Failure during net check license install", e);
        }
        resp.getWriter().println(result);
      }
    } else
      SLLogger.getLogger().info(I18N.err(353, "install/renew", req.getRequestURI()));
  }

  /**
   * Tries to remove a {@link PossiblyActivatedSLLicense} in the server
   * database.
   */
  static class Remove implements DBQuery<String> {
    @NonNull
    final String ip;
    @NonNull
    final Timestamp now;
    @NonNull
    final PossiblyActivatedSLLicense sl;
    @Nullable
    private final String os;
    @Nullable
    private final String javaVersion;
    @Nullable
    private final String eclipseVersion;
    @Nullable
    private final String counts;

    public Remove(@NonNull PossiblyActivatedSLLicense sl, @NonNull Timestamp now, @NonNull String ip, @Nullable String os,
        @Nullable String javaVersion, @Nullable String eclipseVersion, @Nullable String counts) {
      this.sl = sl;
      this.now = now;
      this.ip = ip;
      this.os = os;
      this.javaVersion = javaVersion;
      this.eclipseVersion = eclipseVersion;
      this.counts = counts;
    }

    @Override
    public String perform(final Query q) {
      logCheck(q, now, ip, sl.getSignedSLLicense().getLicense(), NetCheckEvent.REMOVAL_SUCCESS, os, javaVersion, eclipseVersion,
          counts);
      return I18N.msg("web.check.response.success");
    }

  }

  static String fail(final String reason) {
    return I18N.msg("web.check.response.failure.prefix") + ' ' + reason;
  }

  static Date calculateNetcheckDate(final SLLicense license, final boolean isActivated) {
    final int durationInDays;
    if (!isActivated && license.getType() == SLLicenseType.PERPETUAL)
      durationInDays = Math.min(license.getDurationInDays(), SLUtility.DURATION_IN_DAYS_OF_PERPETUAL_LICENSE_FIRST_INSTALL);
    else
      durationInDays = license.getDurationInDays();
    final Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.DAY_OF_YEAR, durationInDays);
    return calendar.getTime();
  }

  static void remove(final HttpServletRequest req, final HttpServletResponse resp, @Nullable String os,
      @Nullable String javaVersion, @Nullable String eclipseVersion, @Nullable String counts) throws IOException {
    final String licStr = req.getParameter(I18N.msg("web.check.param.license"));
    final Timestamp now = new Timestamp(System.currentTimeMillis());
    final String ip = req.getRemoteAddr();
    if (licStr != null) {
      final List<PossiblyActivatedSLLicense> licenses = SLLicensePersistence.readPossiblyActivatedLicensesFromString(licStr);
      final ServicesDBConnection conn = ServicesDBConnection.getInstance();
      for (final PossiblyActivatedSLLicense sl : licenses) {
        String result;
        try {
          result = conn.withTransaction(new Remove(sl, now, ip, os, javaVersion, eclipseVersion, counts));
        } catch (TransactionException e) {
          final String out = I18N.msg("web.check.response.failure.serverError", SLUtility.toString(e));
          result = fail(out);
          SLLogger.getLogger().log(Level.SEVERE, "Failure during net check license removal", e);
        }
        resp.getWriter().println(result);
      }
    } else
      SLLogger.getLogger().info(I18N.err(353, "remove", req.getRequestURI()));
  }
}
