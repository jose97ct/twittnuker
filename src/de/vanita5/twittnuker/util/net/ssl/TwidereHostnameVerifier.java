/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2014 vanita5 <mail@vanita5.de>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.vanita5.twittnuker.util.net.ssl;

import android.content.Context;

import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLException;

public class TwidereHostnameVerifier extends AbstractCheckSignatureVerifier {

	private final Context context;
	private final boolean ignoreSSLErrors;

	public TwidereHostnameVerifier(final Context context, final boolean ignoreSSLErrors)
			throws NoSuchAlgorithmException, KeyStoreException {
		this.context = context;
		this.ignoreSSLErrors = ignoreSSLErrors;
	}

	@Override
	public void verify(final String host, final String[] cns, final String[] subjectAlts, final X509Certificate cert)
			throws SSLException {
		if (ignoreSSLErrors) return;
		if (!checkCert(cert)) throw new SSLException(String.format("Untrusted cert %s", cert));
		if (!verify(host, cns, subjectAlts, false)) throw new SSLException(String.format("Unable to verify %s", host));
	}

	private boolean checkCert(final X509Certificate cert) {
		return true;
	}

}