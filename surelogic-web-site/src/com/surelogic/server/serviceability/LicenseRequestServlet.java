package com.surelogic.server.serviceability;

import java.io.IOException;
import java.security.PrivateKey;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

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

public class LicenseRequestServlet extends HttpServlet {
  private static final long serialVersionUID = 6357187305188901382L;

  static final Logger LOG = SLLogger.getLoggerFor(LicenseRequestServlet.class);

  private static final String PARAM_LIC = "common.serviceability.licenserequest.license";
  private static final String PARAM_MAC = "common.serviceability.licenserequest.macAddresses";

  private static final String ACK = "common.serviceability.licenserequest.resp.failure.success";
  private static final String LOGEMAIL = "common.serviceability.licenserequest.logEmail";
  private static final String REQ = "common.serviceability.licenserequest.req";
  private static final String ACTREW = "common.serviceability.licenserequest.req.actrew";
  private static final String REMOVE = "common.serviceability.licenserequest.req.remove";
  private static final String BLACKLISTED = "common.serviceability.licenserequest.resp.failure.blacklisted";
  private static final String EXPIRED = "common.serviceability.licenserequest.resp.failure.expired";
  private static final String INSTALLLIMIT = "common.serviceability.licenserequest.resp.failure.installLimit";
  private static final String SERVERERROR = "common.serviceability.licenserequest.resp.failure.serverError";
  private static final String LICENSEURL = "common.serviceability.licenseadmin.url";

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

  void handleRequest(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
    final String typeStr = req.getParameter(I18N.msg(REQ));
    if (I18N.msg(ACTREW).equals(typeStr)) {
      install(req, resp);
    } else if (I18N.msg(REMOVE).equals(typeStr)) {
      remove(req, resp);
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
      if (installBeforeDate != null)
        q.prepared("WebServices.insertLicenseInfo").call(uuid, license.getProduct().toString(), license.getHolder(),
            license.getDurationInDays(), new Timestamp(installBeforeDate.getTime()), license.getType().toString(),
            license.getMaxActive());
      else
        q.prepared("WebServices.insertLicenseInfoNoInstallDeadline").call(uuid, license.getProduct().toString(),
            license.getHolder(), license.getDurationInDays(), license.getType().toString(), license.getMaxActive());
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
        LOG.log(Level.SEVERE, String.format("The server did not find a row in LICENSE_NETCHECK_COUNTS for %s.", uuid));
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
  static void logCheck(final Query q, final Timestamp time, final String ip, final SLLicense license, final NetCheckEvent event) {
    final String uuid = license.getUuid().toString();
    q.prepared("WebServices.logNetCheck").call(time, ip, uuid.toString(), event.value);
    q.statement("WebServices.updateCheckCount").call(event.getColumn(), uuid.toString());
    Email.sendSupportEmail(event.toString(), I18N.msg(LOGEMAIL, license.getHolder(), license.getProduct().toString(),
        time.toString(), ip, uuid, I18N.msg(LICENSEURL, SLUtility.SERVICEABILITY_URL, uuid), event.toString()));
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

    Install(@NonNull PossiblyActivatedSLLicense sl, @NonNull Timestamp now, @NonNull String ip,
        @NonNull ImmutableSet<String> clientMacAddresses) {
      this.sl = sl;
      this.now = now;
      this.ip = ip;
      this.clientMacAddresses = clientMacAddresses;
    }

    @Override
    public String perform(final Query q) {
      final SignedSLLicense signedLicense = sl.getSignedSLLicense();
      final SLLicense license = signedLicense.getLicense();
      final UUID uuid = license.getUuid();
      if (checkBlacklist(q, signedLicense)) {
        // This license has been blacklisted
        logCheck(q, now, ip, license, NetCheckEvent.INSTALL_BLACKLISTED);
        return fail(I18N.msg(BLACKLISTED, license.getProduct(), uuid));
      }
      if (sl.isPastInstallBeforeDate()) {
        return fail(I18N.msg(EXPIRED, license.getProduct(), uuid, SLUtility.toStringHumanDay(license.getInstallBeforeDate())));
      }
      final SLLicenseNetCheck check = new SLLicenseNetCheck(uuid, calculateNetcheckDate(license), clientMacAddresses);
      final LicenseInfo info = getAndInitInfo(q, license);
      if (license.getType() == SLLicenseType.PERPETUAL && sl.isActivated()) {
        // This is a renewal
        if (info.getInstalls() == 0) {
          // We are probably missing some data, or have a
          // badly behaving client. We'll go ahead and tick the
          // install count as well.
          logCheck(q, now, ip, license, NetCheckEvent.INSTALL_SUCCESS);
        }
        logCheck(q, now, ip, license, NetCheckEvent.RENEW_SUCCESS);
      } else {
        // This is an install
        if (info.isInstallAvailable()) {
          logCheck(q, now, ip, license, NetCheckEvent.INSTALL_SUCCESS);
        } else {
          logCheck(q, now, ip, license, NetCheckEvent.INSTALL_LIMIT_EXCEEDED);
          return fail(I18N.msg(INSTALLLIMIT, license.getProduct(), uuid, license.getMaxActive()));
        }
      }
      return performNetCheck(check);
    }

    private String performNetCheck(final SLLicenseNetCheck check) {
      final PrivateKey key = SiteUtil.getKey();
      final SignedSLLicenseNetCheck signed = SignedSLLicenseNetCheck.getInstance(check, key);
      return signed.getSignedHexString();
    }
  }

  void install(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
    @Nullable
    final String licStr = req.getParameter(I18N.msg(PARAM_LIC));
    @Nullable
    final String macAddressesParam = req.getParameter(I18N.msg(PARAM_MAC));
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
          result = conn.withTransaction(new Install(sl, now, ip, clientMacAddresses));
        } catch (TransactionException e) {
          result = fail(I18N.msg(SERVERERROR));
        }
        resp.getWriter().println(result);
      }
    } else {
      LOG.info("No license token provided to an install request");
    }
  }

  /**
   * Tries to remove a {@link PossiblyActivatedSLLicense} in the server
   * database.
   */
  static class Remove implements DBQuery<String> {

    @NonNull
    private final String ip;
    @NonNull
    private final Timestamp now;
    @NonNull
    private final PossiblyActivatedSLLicense sl;

    public Remove(@NonNull PossiblyActivatedSLLicense sl, @NonNull Timestamp now, @NonNull String ip) {
      this.sl = sl;
      this.now = now;
      this.ip = ip;
    }

    @Override
    public String perform(final Query q) {
      logCheck(q, now, ip, sl.getSignedSLLicense().getLicense(), NetCheckEvent.REMOVAL_SUCCESS);
      return I18N.msg(ACK);
    }

  }

  static String fail(final String reason) {
    return I18N.msg("common.serviceability.licenserequest.resp.failure.prefix") + ' ' + reason;
  }

  static Date calculateNetcheckDate(final SLLicense license) {
    final Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.DAY_OF_YEAR, license.getDurationInDays());
    return calendar.getTime();
  }

  void remove(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
    final String licStr = req.getParameter(I18N.msg("common.serviceability.licenserequest.license"));
    final Timestamp now = new Timestamp(System.currentTimeMillis());
    final String ip = req.getRemoteAddr();
    if (licStr != null) {
      final List<PossiblyActivatedSLLicense> licenses = SLLicensePersistence.readPossiblyActivatedLicensesFromString(licStr);
      final ServicesDBConnection conn = ServicesDBConnection.getInstance();
      for (final PossiblyActivatedSLLicense sl : licenses) {
        final String result = conn.withTransaction(new Remove(sl, now, ip));
        resp.getWriter().println(result);
      }
    } else {
      LOG.info("No license token provided to an install request");
    }
  }
}
