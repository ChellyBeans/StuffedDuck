package com.example.aleysha.ubicomp_group_7;

/**
 * Created by Kartik on 13/06/2017.
 */

import java.security.AccessController;
import java.security.Provider;

///
/// The following code was taken from:
/// https://stackoverflow.com/questions/2020088/sending-email-in-android-using-javamail-api-without-using-the-default-built-in-a
///
public final class JSSEProvider extends Provider {



    public JSSEProvider() {

        super("HarmonyJSSE", 1.0, "Harmony JSSE Provider");

        AccessController

                .doPrivileged(new java.security.PrivilegedAction<Void>() {
                    public Void run() {
                        put("SSLContext.TLS",
                                "org.apache.harmony.xnet.provider.jsse.SSLContextImpl");
                        put("Alg.Alias.SSLContext.TLSv1", "TLS");
                        put("KeyManagerFactory.X509",
                                "org.apache.harmony.xnet.provider.jsse.KeyManagerFactoryImpl");
                        put("TrustManagerFactory.X509",
                                "org.apache.harmony.xnet.provider.jsse.TrustManagerFactoryImpl");
                        return null;
                    }
                });
    }
}
