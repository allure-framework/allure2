package io.qameta.allure.jira.retrofit;

import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

/**
 * Basic authenticator for okhttp client.
 */
public final class BasicAuthInterceptor implements Interceptor {

    private final String credentials;

    public BasicAuthInterceptor(final String user, final String password) {
        this.credentials = Credentials.basic(user, password);
    }

    @Override
    public Response intercept(final Interceptor.Chain chain) throws IOException {
        final Request request = chain.request();
        final Request authenticatedRequest = request.newBuilder()
                .header("Authorization", credentials).build();
        return chain.proceed(authenticatedRequest);
    }

}
