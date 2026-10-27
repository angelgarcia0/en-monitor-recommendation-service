/**
 * Recommendation service: the Recommendation bounded context of EnergyMonitor, deployed on its own.
 *
 * <p>Owns the consumption advice of a home. Every hour it reviews the hourly energy of the homes
 * whose devices reported in the last week and stores a recommendation when a rule matches: most
 * of the energy used in the evening peak, a device drawing power at night, a week well above the
 * one before, a pace that exceeds the limit of the home, or a device using more than it used to.
 * The members of the home list, read and delete them, and every new one is announced to the
 * notification service, which delivers it.
 *
 * <p>Internal layout: {@code domain}, {@code application}, {@code adapter},
 * {@code infrastructure}.
 */
package com.energymonitor.recommendation;
