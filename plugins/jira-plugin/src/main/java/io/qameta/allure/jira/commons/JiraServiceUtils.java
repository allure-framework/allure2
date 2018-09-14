package io.qameta.allure.jira.commons;

import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static io.qameta.allure.util.PropertyUtils.requireProperty;

/**
 * Jira service util.
 */
public final class JiraServiceUtils {

    private static final String JIRA_ENDPOINT = "ALLURE_JIRA_ENDPOINT";
    private static final String JIRA_USERNAME = "ALLURE_JIRA_USERNAME";
    private static final String JIRA_PASSWORD = "ALLURE_JIRA_PASSWORD";

    private JiraServiceUtils() {
    }

    public static <T> T newInstance(final Class<T> jiraService) {
        final String endpoint = addSlashIfMissing(requireProperty(JIRA_ENDPOINT));
        final String username = requireProperty(JIRA_USERNAME);
        final String password = requireProperty(JIRA_PASSWORD);

        final OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new BasicAuthInterceptor(username, password))
                .build();

        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(JacksonConverterFactory.create())
                .addCallAdapterFactory(new DefaultCallAdapterFactory<>())
                .client(client)
                .build();
        return retrofit.create(jiraService);
    }

    /**
     * Basic authenticator for okhttp client.
     */
    public static final class BasicAuthInterceptor implements Interceptor {

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

    /**
     * Default call adapter factory.
     * @param <T> return type.
     */
    public static final class DefaultCallAdapterFactory<T> extends CallAdapter.Factory {

        @Override
        public CallAdapter<T, ?> get(final Type returnType, final Annotation[] annotations, final Retrofit retrofit) {
            if (returnType.getTypeName().startsWith(Call.class.getName())) {
                return null;
            }
            if (returnType.getTypeName().startsWith(retrofit2.Response.class.getName())) {
                return new ResponseCallAdapter(((ParameterizedType) returnType).getActualTypeArguments()[0]);
            }
            return new InstanceCallAdapter(returnType);
        }

        /**
         * Call adapter factory for Response<...>.
         */
        private class ResponseCallAdapter implements CallAdapter<T, retrofit2.Response<T>> {

            private final Type returnType;

            ResponseCallAdapter(final Type returnType) {
                this.returnType = returnType;
            }

            @Override
            public Type responseType() {
                return returnType;
            }

            @Override
            public retrofit2.Response<T> adapt(final Call<T> call) {
                try {
                    return call.execute();
                } catch (IOException e) {
                    throw new ServiceException("Could not execute request", e);
                }
            }
        }

        /**
         * Call adapter factory for instances.
         */
        private class InstanceCallAdapter implements CallAdapter<T, Object> {

            private final Type returnType;

            InstanceCallAdapter(final Type returnType) {
                this.returnType = returnType;
            }

            @Override
            public Type responseType() {
                return returnType;
            }

            @Override
            public Object adapt(final Call<T> call) {
                try {
                    return call.execute().body();
                } catch (IOException e) {
                    throw new ServiceException("Could not get request body", e);
                }
            }
        }

    }

    /**
     * Service exception when something wrong when execution call.
     */
    public static final class ServiceException extends RuntimeException {

        public ServiceException(final String message, final Throwable e) {
            super(message, e);
        }

    }

    private static String addSlashIfMissing(String endpoint) {
        return endpoint.endsWith("/") ? endpoint : endpoint + "/";
    }

}