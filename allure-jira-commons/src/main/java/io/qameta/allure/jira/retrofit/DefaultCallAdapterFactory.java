/*
 *  Copyright 2016-2023 Qameta Software OÜ
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package io.qameta.allure.jira.retrofit;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Retrofit;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * Default call adapter factory.
 *
 * @param <T> return type.
 */
public final class DefaultCallAdapterFactory<T> extends CallAdapter.Factory {

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

    private static String getErrorMessage(final retrofit2.Response<?> response) {
        try (ResponseBody errorBody = response.errorBody()) {
            return Objects.isNull(errorBody) ? response.message() : errorBody.string();
        } catch (IOException e) {
            throw new ServiceException("could not read error body", e);
        }
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
            final retrofit2.Response<T> response;
            try {
                response = call.execute();
            } catch (IOException e) {
                throw new ServiceException("Could not execute request", e);
            }

            if (!response.isSuccessful()) {
                throw new ServiceException(getErrorMessage(response));
            }
            return response;
        }
    }

    /**
     * Call adapter factory for instances.
     */
    @SuppressFBWarnings("NP_NONNULL_RETURN_VIOLATION")
    private class InstanceCallAdapter implements CallAdapter<T, Object> {

        private static final int NOT_FOUND = 404;

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
            final retrofit2.Response<T> response;
            try {
                response = call.execute();
            } catch (IOException e) {
                throw new ServiceException("Could not get request body", e);
            }
            if (!response.isSuccessful()) {
                if (response.code() == NOT_FOUND) {
                    return null;
                }
                throw new ServiceException(getErrorMessage(response));
            }
            return response.body();
        }
    }

}
